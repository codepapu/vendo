package com.vendo.content.entity;

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
@Table(name = "pages", indexes = {
    @Index(name = "idx_pages_slug", columnList = "slug"),
    @Index(name = "idx_pages_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Page {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String title;
    
    @Column(nullable = false, unique = true, length = 191)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 191)
    private String template;
    
    @Column(length = 60)
    @Builder.Default
    private String status = "draft"; // draft, published
    
    @Column(name = "meta_title", length = 191)
    private String metaTitle;
    
    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;
    
    @Column(name = "meta_keywords", length = 191)
    private String metaKeywords;
    
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
