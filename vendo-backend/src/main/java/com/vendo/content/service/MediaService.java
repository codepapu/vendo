package com.vendo.content.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.common.storage.FileStorageService;
import com.vendo.content.dto.response.MediaFileResponse;
import com.vendo.content.entity.MediaFile;
import com.vendo.content.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {
    
    private final MediaFileRepository mediaFileRepository;
    
    @Autowired(required = false)
    private FileStorageService fileStorageService;
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.storage.type:local}")
    private String storageType; // local, s3, minio
    
    @Value("${app.cdn.enabled:false}")
    private boolean cdnEnabled;
    
    @Value("${app.cdn.base-url:}")
    private String cdnBaseUrl;
    
    @Value("${app.media.image.max-width:2048}")
    private int maxImageWidth;
    
    @Value("${app.media.image.max-height:2048}")
    private int maxImageHeight;
    
    @Value("${app.media.image.quality:0.85}")
    private double imageQuality;
    
    @Transactional(readOnly = true)
    public Page<MediaFileResponse> getAllFiles(Long userId, String type, Pageable pageable) {
        if (userId != null && type != null) {
            return mediaFileRepository.findAll(pageable).map(this::mapToResponse);
        } else if (userId != null) {
            return mediaFileRepository.findByUserId(userId, pageable).map(this::mapToResponse);
        } else if (type != null) {
            return mediaFileRepository.findByType(type, pageable).map(this::mapToResponse);
        } else {
            return mediaFileRepository.findAll(pageable).map(this::mapToResponse);
        }
    }
    
    @Transactional(readOnly = true)
    public MediaFileResponse getFileById(Long id) {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file", "id", id));
        return mapToResponse(file);
    }
    
    @Transactional
    public MediaFileResponse uploadFile(MultipartFile multipartFile, Long userId) throws IOException {
        // Generate unique filename
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Determine file type
        String mimeType = multipartFile.getContentType();
        String type = determineFileType(mimeType);
        
        String fileKey;
        String fileUrl;
        String thumbnailKey = null;
        String thumbnailUrl = null;
        Integer width = null;
        Integer height = null;
        Long fileSize = multipartFile.getSize();
        
        // Process image if it's an image
        if ("image".equals(type)) {
            try {
                BufferedImage originalImage = ImageIO.read(multipartFile.getInputStream());
                if (originalImage != null) {
                    width = originalImage.getWidth();
                    height = originalImage.getHeight();
                    
                    // Optimize and resize image if needed
                    BufferedImage optimizedImage = optimizeImage(originalImage);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    String format = getImageFormat(mimeType);
                    ImageIO.write(optimizedImage, format, baos);
                    fileSize = (long) baos.size();
                    
                    // Upload optimized image
                    if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
                        // Upload to S3/MinIO
                        fileKey = fileStorageService.uploadFile(
                            new ByteArrayInputStream(baos.toByteArray()),
                            filename,
                            mimeType,
                            "images"
                        );
                        fileUrl = fileStorageService.getFileUrl(fileKey);
                        
                        // Generate and upload thumbnail
                        BufferedImage thumbnail = resizeImage(originalImage, 300, 300);
                        ByteArrayOutputStream thumbBaos = new ByteArrayOutputStream();
                        ImageIO.write(thumbnail, format, thumbBaos);
                        String thumbFilename = "thumb_" + filename;
                        thumbnailKey = fileStorageService.uploadFile(
                            new ByteArrayInputStream(thumbBaos.toByteArray()),
                            thumbFilename,
                            mimeType,
                            "images/thumbnails"
                        );
                        thumbnailUrl = fileStorageService.getFileUrl(thumbnailKey);
                    } else {
                        // Local storage fallback
                        Path uploadPath = Paths.get(uploadDir);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        Path filePath = uploadPath.resolve(filename);
                        Files.copy(new ByteArrayInputStream(baos.toByteArray()), filePath, StandardCopyOption.REPLACE_EXISTING);
                        fileKey = filePath.toString();
                        fileUrl = "/api/v1/media/files/" + filename;
                        
                        // Generate thumbnail
                        String thumbnailFilename = "thumb_" + filename;
                        Path thumbnailFilePath = uploadPath.resolve(thumbnailFilename);
                        Thumbnails.of(new ByteArrayInputStream(baos.toByteArray()))
                                .size(300, 300)
                                .keepAspectRatio(true)
                                .toFile(thumbnailFilePath.toFile());
                        thumbnailKey = thumbnailFilePath.toString();
                        thumbnailUrl = "/api/v1/media/files/" + thumbnailFilename;
                    }
                } else {
                    // Not a valid image, upload as-is
                    if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
                        fileKey = fileStorageService.uploadFile(multipartFile, "images");
                        fileUrl = fileStorageService.getFileUrl(fileKey);
                    } else {
                        Path uploadPath = Paths.get(uploadDir);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        Path filePath = uploadPath.resolve(filename);
                        Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        fileKey = filePath.toString();
                        fileUrl = "/api/v1/media/files/" + filename;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to process image: {}", filename, e);
                // Fallback to direct upload
                if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
                    fileKey = fileStorageService.uploadFile(multipartFile, "images");
                    fileUrl = fileStorageService.getFileUrl(fileKey);
                } else {
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    Path filePath = uploadPath.resolve(filename);
                    Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    fileKey = filePath.toString();
                    fileUrl = "/api/v1/media/files/" + filename;
                }
            }
        } else {
            // Non-image file
            String folder = type + "s"; // videos, documents, audios
            if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
                fileKey = fileStorageService.uploadFile(multipartFile, folder);
                fileUrl = fileStorageService.getFileUrl(fileKey);
            } else {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                fileKey = filePath.toString();
                fileUrl = "/api/v1/media/files/" + filename;
            }
        }
        
        // Generate CDN URL if enabled
        String cdnUrl = null;
        if (cdnEnabled && !cdnBaseUrl.isEmpty()) {
            cdnUrl = cdnBaseUrl + "/" + filename;
        }
        
        // Create media file record
        MediaFile mediaFile = MediaFile.builder()
                .filename(filename)
                .originalName(originalFilename)
                .path(fileKey)
                .url(fileUrl)
                .mimeType(mimeType)
                .fileSize(fileSize)
                .type(type)
                .userId(userId)
                .width(width)
                .height(height)
                .cdnUrl(cdnUrl)
                .build();
        
        MediaFile savedFile = mediaFileRepository.save(mediaFile);
        log.info("Media file uploaded: ID={}, Filename={}, Size={}x{}, Storage={}", 
                savedFile.getId(), filename, width, height, storageType);
        
        return mapToResponse(savedFile);
    }
    
    @Transactional
    public void deleteFile(Long id) throws IOException {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file", "id", id));
        
        // Delete physical file
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            fileStorageService.deleteFile(file.getPath());
        } else {
            Path filePath = Paths.get(file.getPath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
        
        mediaFileRepository.delete(file);
        log.info("Media file deleted: ID={}", id);
    }
    
    /**
     * Get file URL (presigned URL for S3/MinIO)
     */
    @Transactional(readOnly = true)
    public String getFileUrl(Long id) {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file", "id", id));
        
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            return fileStorageService.getFileUrl(file.getPath());
        }
        
        return file.getUrl();
    }
    
    /**
     * Resize image to specified dimensions
     */
    @Transactional
    public MediaFileResponse resizeImage(Long id, int width, int height) throws IOException {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file", "id", id));
        
        if (!"image".equals(file.getType())) {
            throw new IllegalArgumentException("File is not an image");
        }
        
        // Load original image
        BufferedImage originalImage;
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            // Download from S3/MinIO and process
            try (InputStream inputStream = fileStorageService.downloadFile(file.getPath())) {
                originalImage = ImageIO.read(inputStream);
            }
        } else {
            Path filePath = Paths.get(file.getPath());
            originalImage = ImageIO.read(filePath.toFile());
        }
        
        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }
        
        // Resize image
        BufferedImage resizedImage = resizeImage(originalImage, width, height);
        
        // Save resized image
        String resizedFilename = "resized_" + width + "x" + height + "_" + file.getFilename();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = getImageFormat(file.getMimeType());
        ImageIO.write(resizedImage, format, baos);
        
        String resizedKey;
        String resizedUrl;
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            resizedKey = fileStorageService.uploadFile(
                new ByteArrayInputStream(baos.toByteArray()),
                resizedFilename,
                file.getMimeType(),
                "images/resized"
            );
            resizedUrl = fileStorageService.getFileUrl(resizedKey);
        } else {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path resizedPath = uploadPath.resolve(resizedFilename);
            Files.copy(new ByteArrayInputStream(baos.toByteArray()), resizedPath, StandardCopyOption.REPLACE_EXISTING);
            resizedKey = resizedPath.toString();
            resizedUrl = "/api/v1/media/files/" + resizedFilename;
        }
        
        // Create new media file record for resized image
        MediaFile resizedFile = MediaFile.builder()
                .filename(resizedFilename)
                .originalName("resized_" + file.getOriginalName())
                .path(resizedKey)
                .url(resizedUrl)
                .mimeType(file.getMimeType())
                .fileSize((long) baos.size())
                .type("image")
                .userId(file.getUserId())
                .width(width)
                .height(height)
                .build();
        
        MediaFile savedFile = mediaFileRepository.save(resizedFile);
        log.info("Image resized: ID={}, Original={}, Size={}x{}", 
                savedFile.getId(), id, width, height);
        
        return mapToResponse(savedFile);
    }
    
    /**
     * Optimize image (compress and resize if needed)
     */
    @Transactional
    public MediaFileResponse optimizeImage(Long id) throws IOException {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file", "id", id));
        
        if (!"image".equals(file.getType())) {
            throw new IllegalArgumentException("File is not an image");
        }
        
        // Load original image
        BufferedImage originalImage;
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            // Download from S3/MinIO and process
            try (InputStream inputStream = fileStorageService.downloadFile(file.getPath())) {
                originalImage = ImageIO.read(inputStream);
            }
        } else {
            Path filePath = Paths.get(file.getPath());
            originalImage = ImageIO.read(filePath.toFile());
        }
        
        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }
        
        // Optimize image
        BufferedImage optimizedImage = optimizeImage(originalImage);
        
        // Save optimized image
        String optimizedFilename = "optimized_" + file.getFilename();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = getImageFormat(file.getMimeType());
        ImageIO.write(optimizedImage, format, baos);
        
        String optimizedKey;
        String optimizedUrl;
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            optimizedKey = fileStorageService.uploadFile(
                new ByteArrayInputStream(baos.toByteArray()),
                optimizedFilename,
                file.getMimeType(),
                "images/optimized"
            );
            optimizedUrl = fileStorageService.getFileUrl(optimizedKey);
        } else {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path optimizedPath = uploadPath.resolve(optimizedFilename);
            Files.copy(new ByteArrayInputStream(baos.toByteArray()), optimizedPath, StandardCopyOption.REPLACE_EXISTING);
            optimizedKey = optimizedPath.toString();
            optimizedUrl = "/api/v1/media/files/" + optimizedFilename;
        }
        
        // Update file record
        file.setPath(optimizedKey);
        file.setUrl(optimizedUrl);
        file.setFileSize((long) baos.size());
        file.setWidth(optimizedImage.getWidth());
        file.setHeight(optimizedImage.getHeight());
        
        MediaFile savedFile = mediaFileRepository.save(file);
        log.info("Image optimized: ID={}, Original Size={}, Optimized Size={}", 
                savedFile.getId(), file.getFileSize(), baos.size());
        
        return mapToResponse(savedFile);
    }
    
    /**
     * Optimize image - resize if too large and compress
     */
    private BufferedImage optimizeImage(BufferedImage original) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Resize if too large
        if (width > maxImageWidth || height > maxImageHeight) {
            double scale = Math.min((double) maxImageWidth / width, (double) maxImageHeight / height);
            width = (int) (width * scale);
            height = (int) (height * scale);
            return resizeImage(original, width, height);
        }
        
        return original;
    }
    
    /**
     * Resize image maintaining aspect ratio
     */
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) throws IOException {
        return Thumbnails.of(original)
                .size(targetWidth, targetHeight)
                .keepAspectRatio(true)
                .outputQuality(imageQuality)
                .asBufferedImage();
    }
    
    /**
     * Get image format from MIME type
     */
    private String getImageFormat(String mimeType) {
        if (mimeType == null) {
            return "jpg";
        }
        if (mimeType.contains("png")) {
            return "png";
        } else if (mimeType.contains("gif")) {
            return "gif";
        } else if (mimeType.contains("webp")) {
            return "webp";
        }
        return "jpg";
    }
    
    private String determineFileType(String mimeType) {
        if (mimeType == null) {
            return "document";
        }
        
        if (mimeType.startsWith("image/")) {
            return "image";
        } else if (mimeType.startsWith("video/")) {
            return "video";
        } else if (mimeType.startsWith("audio/")) {
            return "audio";
        } else {
            return "document";
        }
    }
    
    private MediaFileResponse mapToResponse(MediaFile file) {
        return MediaFileResponse.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .originalName(file.getOriginalName())
                .path(file.getPath())
                .url(file.getUrl())
                .mimeType(file.getMimeType())
                .fileSize(file.getFileSize())
                .type(file.getType())
                .userId(file.getUserId())
                .alt(file.getAlt())
                .description(file.getDescription())
                .width(file.getWidth())
                .height(file.getHeight())
                .duration(file.getDuration())
                .cdnUrl(file.getCdnUrl())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}
