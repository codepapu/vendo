package com.vendo.content.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.common.storage.IFileStorageService;
import com.vendo.content.dto.response.ImageOptimizationResponse;
import com.vendo.content.entity.MediaFile;
import com.vendo.content.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageOptimizationService {
    
    private final MediaFileRepository mediaFileRepository;
    
    @Autowired(required = false)
    private IFileStorageService fileStorageService;
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.storage.type:local}")
    private String storageType;
    
    @Value("${app.image.optimization.enabled:true}")
    private boolean optimizationEnabled;
    
    @Value("${app.image.optimization.thumbnail.width:300}")
    private int thumbnailWidth;
    
    @Value("${app.image.optimization.thumbnail.height:300}")
    private int thumbnailHeight;
    
    @Value("${app.image.optimization.medium.width:800}")
    private int mediumWidth;
    
    @Value("${app.image.optimization.medium.height:800}")
    private int mediumHeight;
    
    @Value("${app.image.optimization.large.width:1920}")
    private int largeWidth;
    
    @Value("${app.image.optimization.large.height:1920}")
    private int largeHeight;
    
    @Value("${app.image.optimization.webp.enabled:true}")
    private boolean webpEnabled;
    
    @Value("${app.image.optimization.quality:0.85}")
    private double imageQuality;
    
    /**
     * Optimize an uploaded image and generate multiple sizes
     */
    @Transactional
    public ImageOptimizationResponse optimizeImage(MultipartFile file, Long userId) throws IOException {
        if (!optimizationEnabled) {
            throw new IllegalStateException("Image optimization is disabled");
        }
        
        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("File is not a valid image");
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        long originalSize = file.getSize();
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String baseFilename = UUID.randomUUID().toString();
        
        // Generate optimized images
        Map<String, ImageOptimizationResponse.OptimizedImage> optimizedImages = new HashMap<>();
        
        // Thumbnail
        ImageOptimizationResponse.OptimizedImage thumbnail = generateOptimizedImage(
                originalImage, baseFilename, "thumbnail", thumbnailWidth, thumbnailHeight, userId
        );
        optimizedImages.put("thumbnail", thumbnail);
        
        // Medium
        ImageOptimizationResponse.OptimizedImage medium = generateOptimizedImage(
                originalImage, baseFilename, "medium", mediumWidth, mediumHeight, userId
        );
        optimizedImages.put("medium", medium);
        
        // Large
        ImageOptimizationResponse.OptimizedImage large = generateOptimizedImage(
                originalImage, baseFilename, "large", largeWidth, largeHeight, userId
        );
        optimizedImages.put("large", large);
        
        // WebP versions if enabled
        if (webpEnabled) {
            ImageOptimizationResponse.OptimizedImage thumbnailWebp = generateWebPImage(
                    originalImage, baseFilename, "thumbnail", thumbnailWidth, thumbnailHeight, userId
            );
            optimizedImages.put("thumbnailWebp", thumbnailWebp);
            
            ImageOptimizationResponse.OptimizedImage mediumWebp = generateWebPImage(
                    originalImage, baseFilename, "medium", mediumWidth, mediumHeight, userId
            );
            optimizedImages.put("mediumWebp", mediumWebp);
            
            ImageOptimizationResponse.OptimizedImage largeWebp = generateWebPImage(
                    originalImage, baseFilename, "large", largeWidth, largeHeight, userId
            );
            optimizedImages.put("largeWebp", largeWebp);
        }
        
        // Save original image
        String originalKey = saveImage(originalImage, baseFilename + extension, "original", file.getContentType(), userId);
        String originalUrl = getFileUrl(originalKey);
        
        log.info("Image optimized: Original={}x{}, Generated {} variants", 
                originalWidth, originalHeight, optimizedImages.size());
        
        return ImageOptimizationResponse.builder()
                .originalUrl(originalUrl)
                .originalSize(originalSize)
                .originalWidth(originalWidth)
                .originalHeight(originalHeight)
                .optimizedImages(optimizedImages)
                .build();
    }
    
    /**
     * Optimize an existing image by ID
     */
    @Transactional
    public ImageOptimizationResponse optimizeExistingImage(Long imageId) throws IOException {
        MediaFile mediaFile = mediaFileRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Media file", "id", imageId));
        
        if (!"image".equals(mediaFile.getType())) {
            throw new IllegalArgumentException("File is not an image");
        }
        
        // Load original image
        BufferedImage originalImage;
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            try (InputStream inputStream = fileStorageService.downloadFile(mediaFile.getPath())) {
                originalImage = ImageIO.read(inputStream);
            }
        } else {
            Path filePath = Paths.get(mediaFile.getPath());
            originalImage = ImageIO.read(filePath.toFile());
        }
        
        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }
        
        String baseFilename = UUID.randomUUID().toString();
        Map<String, ImageOptimizationResponse.OptimizedImage> optimizedImages = new HashMap<>();
        
        // Generate optimized images
        ImageOptimizationResponse.OptimizedImage thumbnail = generateOptimizedImage(
                originalImage, baseFilename, "thumbnail", thumbnailWidth, thumbnailHeight, mediaFile.getUserId()
        );
        optimizedImages.put("thumbnail", thumbnail);
        
        ImageOptimizationResponse.OptimizedImage medium = generateOptimizedImage(
                originalImage, baseFilename, "medium", mediumWidth, mediumHeight, mediaFile.getUserId()
        );
        optimizedImages.put("medium", medium);
        
        ImageOptimizationResponse.OptimizedImage large = generateOptimizedImage(
                originalImage, baseFilename, "large", largeWidth, largeHeight, mediaFile.getUserId()
        );
        optimizedImages.put("large", large);
        
        if (webpEnabled) {
            ImageOptimizationResponse.OptimizedImage thumbnailWebp = generateWebPImage(
                    originalImage, baseFilename, "thumbnail", thumbnailWidth, thumbnailHeight, mediaFile.getUserId()
            );
            optimizedImages.put("thumbnailWebp", thumbnailWebp);
            
            ImageOptimizationResponse.OptimizedImage mediumWebp = generateWebPImage(
                    originalImage, baseFilename, "medium", mediumWidth, mediumHeight, mediaFile.getUserId()
            );
            optimizedImages.put("mediumWebp", mediumWebp);
            
            ImageOptimizationResponse.OptimizedImage largeWebp = generateWebPImage(
                    originalImage, baseFilename, "large", largeWidth, largeHeight, mediaFile.getUserId()
            );
            optimizedImages.put("largeWebp", largeWebp);
        }
        
        return ImageOptimizationResponse.builder()
                .originalId(imageId)
                .originalUrl(mediaFile.getUrl())
                .originalSize(mediaFile.getFileSize())
                .originalWidth(mediaFile.getWidth())
                .originalHeight(mediaFile.getHeight())
                .optimizedImages(optimizedImages)
                .build();
    }
    
    // Private helper methods
    
    private ImageOptimizationResponse.OptimizedImage generateOptimizedImage(
            BufferedImage original,
            String baseFilename,
            String sizeLabel,
            int targetWidth,
            int targetHeight,
            Long userId
    ) throws IOException {
        BufferedImage resized = resizeImage(original, targetWidth, targetHeight);
        
        String format = "jpg";
        String mimeType = "image/jpeg";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, format, baos);
        byte[] imageBytes = baos.toByteArray();
        
        String filename = baseFilename + "_" + sizeLabel + ".jpg";
        String key = saveImageBytes(imageBytes, filename, sizeLabel, mimeType, userId);
        String url = getFileUrl(key);
        
        return ImageOptimizationResponse.OptimizedImage.builder()
                .url(url)
                .format(format)
                .width(resized.getWidth())
                .height(resized.getHeight())
                .size((long) imageBytes.length)
                .sizeLabel(sizeLabel)
                .build();
    }
    
    private ImageOptimizationResponse.OptimizedImage generateWebPImage(
            BufferedImage original,
            String baseFilename,
            String sizeLabel,
            int targetWidth,
            int targetHeight,
            Long userId
    ) throws IOException {
        BufferedImage resized = resizeImage(original, targetWidth, targetHeight);
        
        // Note: WebP support requires additional library
        // For now, we'll use JPEG as fallback
        // In production, use a library like imageio-webp or convert via ImageIO
        String format = "jpg"; // Change to "webp" when WebP support is available
        String mimeType = "image/jpeg"; // Change to "image/webp" when available
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, format, baos);
        byte[] imageBytes = baos.toByteArray();
        
        String filename = baseFilename + "_" + sizeLabel + ".webp";
        String key = saveImageBytes(imageBytes, filename, sizeLabel + "/webp", mimeType, userId);
        String url = getFileUrl(key);
        
        return ImageOptimizationResponse.OptimizedImage.builder()
                .url(url)
                .format("webp")
                .width(resized.getWidth())
                .height(resized.getHeight())
                .size((long) imageBytes.length)
                .sizeLabel(sizeLabel)
                .build();
    }
    
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) throws IOException {
        return Thumbnails.of(original)
                .size(targetWidth, targetHeight)
                .keepAspectRatio(true)
                .outputQuality(imageQuality)
                .asBufferedImage();
    }
    
    private String saveImage(BufferedImage image, String filename, String folder, String contentType, Long userId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = contentType != null && contentType.contains("png") ? "png" : "jpg";
        ImageIO.write(image, format, baos);
        return saveImageBytes(baos.toByteArray(), filename, folder, contentType, userId);
    }
    
    private String saveImageBytes(byte[] imageBytes, String filename, String folder, String contentType, Long userId) throws IOException {
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            return fileStorageService.uploadFile(
                    new ByteArrayInputStream(imageBytes),
                    filename,
                    contentType,
                    "images/" + folder
            );
        } else {
            Path uploadPath = Paths.get(uploadDir, "images", folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.copy(new ByteArrayInputStream(imageBytes), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        }
    }
    
    private String getFileUrl(String key) {
        if (fileStorageService != null && ("s3".equals(storageType) || "minio".equals(storageType))) {
            return fileStorageService.getFileUrl(key);
        } else {
            // Extract relative path from absolute path
            String relativePath = key.replace(uploadDir, "").replace("\\", "/");
            return "/api/v1/media/files" + relativePath;
        }
    }
}

