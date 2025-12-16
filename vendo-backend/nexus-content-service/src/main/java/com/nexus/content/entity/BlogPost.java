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
import java.util.List;

@Entity
@Table(name = "blog_posts", indexes = {
    @Index(name = "idx_blog_posts_slug", columnList = "slug"),
    @Index(name = "idx_blog_posts_status", columnList = "status"),
    @Index(name = "idx_blog_posts_category_id", columnList = "category_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BlogPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String title;
    
    @Column(nullable = false, unique = true, length = 191)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String excerpt;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 191)
    private String featuredImage;
    
    @Column(length = 60)
    @Builder.Default
    private String status = "draft"; // draft, published, archived
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Column(length = 191)
    private String metaTitle;
    
    @Column(columnDefinition = "TEXT")
    private String metaDescription;
    
    @Column(length = 191)
    private String metaKeywords;
    
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "blog_post_tags",
        joinColumns = @JoinColumn(name = "blog_post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<BlogTag> tags;
}
