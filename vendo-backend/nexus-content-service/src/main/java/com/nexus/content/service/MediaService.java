package com.nexus.content.service;

import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.content.dto.response.MediaFileResponse;
import com.nexus.content.entity.MediaFile;
import com.nexus.content.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {
    
    private final MediaFileRepository mediaFileRepository;
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.cdn.enabled:false}")
    private boolean cdnEnabled;
    
    @Value("${app.cdn.base-url:}")
    private String cdnBaseUrl;
    
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
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);
        
        // Save file
        Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Determine file type
        String mimeType = multipartFile.getContentType();
        String type = determineFileType(mimeType);
        
        // Process image if it's an image
        Integer width = null;
        Integer height = null;
        String thumbnailPath = null;
        
        if ("image".equals(type)) {
            try {
                BufferedImage originalImage = ImageIO.read(filePath.toFile());
                if (originalImage != null) {
                    width = originalImage.getWidth();
                    height = originalImage.getHeight();
                    
                    // Generate thumbnail
                    String thumbnailFilename = "thumb_" + filename;
                    Path thumbnailFilePath = uploadPath.resolve(thumbnailFilename);
                    Thumbnails.of(filePath.toFile())
                            .size(300, 300)
                            .keepAspectRatio(true)
                            .toFile(thumbnailFilePath.toFile());
                    thumbnailPath = thumbnailFilePath.toString();
                    log.info("Thumbnail generated: {}", thumbnailFilename);
                }
            } catch (Exception e) {
                log.warn("Failed to process image: {}", filename, e);
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
                .path(filePath.toString())
                .url("/api/v1/media/files/" + filename)
                .mimeType(mimeType)
                .fileSize(multipartFile.getSize())
                .type(type)
                .userId(userId)
                .width(width)
                .height(height)
                .cdnUrl(cdnUrl)
                .build();
        
        MediaFile savedFile = mediaFileRepository.save(mediaFile);
        log.info("Media file uploaded: ID={}, Filename={}, Size={}x{}", 
                savedFile.getId(), filename, width, height);
        
        return mapToResponse(savedFile);
    }
    
    @Transactional
    public void deleteFile(Long id) throws IOException {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file", "id", id));
        
        // Delete physical file
        Path filePath = Paths.get(file.getPath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        mediaFileRepository.delete(file);
        log.info("Media file deleted: ID={}", id);
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
