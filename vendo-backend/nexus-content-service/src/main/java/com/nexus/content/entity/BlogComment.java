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
@Table(name = "blog_comments", indexes = {
    @Index(name = "idx_blog_comments_post_id", columnList = "post_id"),
    @Index(name = "idx_blog_comments_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BlogComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "post_id", nullable = false)
    private Long postId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(length = 191)
    private String name;
    
    @Column(length = 191)
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "parent_id")
    private Long parentId; // For nested comments
    
    @Column(length = 60)
    @Builder.Default
    private String status = "pending"; // pending, approved, rejected
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
