package com.nexus.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_files", indexes = {
    @Index(name = "idx_media_files_type", columnList = "type"),
    @Index(name = "idx_media_files_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MediaFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String filename;
    
    @Column(nullable = false, length = 191)
    private String originalName;
    
    @Column(nullable = false, length = 191)
    private String path;
    
    @Column(length = 191)
    private String url;
    
    @Column(length = 100)
    private String mimeType;
    
    @Column(name = "file_size")
    private Long fileSize; // in bytes
    
    @Column(length = 60)
    private String type; // image, video, document, audio
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(length = 191)
    private String alt;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "width")
    private Integer width; // For images
    
    @Column(name = "height")
    private Integer height; // For images
    
    @Column(name = "duration")
    private Integer duration; // For videos/audio in seconds
    
    @Column(name = "cdn_url", length = 500)
    private String cdnUrl;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
