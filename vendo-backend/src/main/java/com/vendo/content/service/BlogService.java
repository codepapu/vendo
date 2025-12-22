package com.vendo.content.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.content.dto.request.BlogPostRequest;
import com.vendo.content.dto.response.BlogPostResponse;
import com.vendo.content.dto.response.BlogTagResponse;
import com.vendo.content.entity.BlogPost;
import com.vendo.content.entity.BlogTag;
import com.vendo.content.repository.BlogPostRepository;
import com.vendo.content.repository.BlogTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {
    
    private final BlogPostRepository blogPostRepository;
    private final BlogTagRepository blogTagRepository;
    
    @Transactional(readOnly = true)
    public Page<BlogPostResponse> getAllPosts(String status, Long categoryId, String keyword, Pageable pageable) {
        Page<BlogPost> posts;
        
        if (keyword != null && !keyword.isEmpty()) {
            posts = blogPostRepository.searchByKeyword(status != null ? status : "published", keyword, pageable);
        } else if (categoryId != null && status != null) {
            posts = blogPostRepository.findByStatusAndCategoryId(status, categoryId, pageable);
        } else if (categoryId != null) {
            posts = blogPostRepository.findByCategoryId(categoryId, pageable);
        } else if (status != null) {
            posts = blogPostRepository.findByStatus(status, pageable);
        } else {
            posts = blogPostRepository.findAll(pageable);
        }
        
        return posts.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public BlogPostResponse getPostById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));
        return mapToResponse(post);
    }
    
    @Transactional(readOnly = true)
    public BlogPostResponse getPostBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "slug", slug));
        return mapToResponse(post);
    }
    
    @Transactional
    public BlogPostResponse createPost(BlogPostRequest request, Long authorId) {
        // Check if slug already exists
        if (blogPostRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BadRequestException("Blog post with slug '" + request.getSlug() + "' already exists");
        }
        
        BlogPost post = BlogPost.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .featuredImage(request.getFeaturedImage())
                .status(request.getStatus() != null ? request.getStatus() : "draft")
                .categoryId(request.getCategoryId())
                .authorId(authorId)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .viewCount(0L)
                .build();
        
        if ("published".equals(post.getStatus())) {
            post.setPublishedAt(LocalDateTime.now());
        }
        
        // Add tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<BlogTag> tags = blogTagRepository.findAllById(request.getTagIds());
            post.setTags(tags);
            // Update post count for tags
            updateTagPostCounts(tags, 1);
        }
        
        BlogPost savedPost = blogPostRepository.save(post);
        log.info("Blog post created: ID={}, Title={}", savedPost.getId(), savedPost.getTitle());
        
        return mapToResponse(savedPost);
    }
    
    @Transactional
    public BlogPostResponse updatePost(Long id, BlogPostRequest request) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));
        
        // Check slug uniqueness if changed
        if (!post.getSlug().equals(request.getSlug())) {
            if (blogPostRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new BadRequestException("Blog post with slug '" + request.getSlug() + "' already exists");
            }
        }
        
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setFeaturedImage(request.getFeaturedImage());
        
        if (request.getStatus() != null) {
            String oldStatus = post.getStatus();
            post.setStatus(request.getStatus());
            
            // Set published date if status changed to published
            if (!"published".equals(oldStatus) && "published".equals(request.getStatus()) && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
        }
        
        post.setCategoryId(request.getCategoryId());
        post.setMetaTitle(request.getMetaTitle());
        post.setMetaDescription(request.getMetaDescription());
        post.setMetaKeywords(request.getMetaKeywords());
        
        // Update tags
        if (request.getTagIds() != null) {
            // Get old tags to decrement their post count
            List<BlogTag> oldTags = post.getTags() != null ? post.getTags() : List.of();
            if (!oldTags.isEmpty()) {
                // Reload tags from database to ensure they're managed entities
                List<Long> oldTagIds = oldTags.stream().map(BlogTag::getId).toList();
                List<BlogTag> managedOldTags = blogTagRepository.findAllById(oldTagIds);
                updateTagPostCounts(managedOldTags, -1);
            }
            
            // Get new tags and increment their post count
            List<BlogTag> newTags = blogTagRepository.findAllById(request.getTagIds());
            post.setTags(newTags);
            updateTagPostCounts(newTags, 1);
        }
        
        BlogPost updatedPost = blogPostRepository.save(post);
        log.info("Blog post updated: ID={}", id);
        
        return mapToResponse(updatedPost);
    }
    
    @Transactional
    public void deletePost(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));
        
        // Decrement post count for tags
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            // Reload tags from database to ensure they're managed entities
            List<Long> tagIds = post.getTags().stream().map(BlogTag::getId).toList();
            List<BlogTag> managedTags = blogTagRepository.findAllById(tagIds);
            updateTagPostCounts(managedTags, -1);
        }
        
        blogPostRepository.delete(post);
        log.info("Blog post deleted: ID={}", id);
    }
    
    @Transactional
    public BlogPostResponse incrementViewCount(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));
        post.setViewCount(post.getViewCount() + 1);
        BlogPost updatedPost = blogPostRepository.save(post);
        return mapToResponse(updatedPost);
    }
    
    private void updateTagPostCounts(List<BlogTag> tags, int increment) {
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(tag -> {
                Long currentCount = tag.getPostCount() != null ? tag.getPostCount() : 0L;
                tag.setPostCount(Math.max(0, currentCount + increment));
                blogTagRepository.save(tag);
            });
        }
    }
    
    private BlogPostResponse mapToResponse(BlogPost post) {
        List<BlogTagResponse> tagResponses = null;
        if (post.getTags() != null) {
            tagResponses = post.getTags().stream()
                    .map(tag -> BlogTagResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .slug(tag.getSlug())
                            .postCount(tag.getPostCount())
                            .createdAt(tag.getCreatedAt())
                            .updatedAt(tag.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return BlogPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .content(post.getContent())
                .featuredImage(post.getFeaturedImage())
                .status(post.getStatus())
                .categoryId(post.getCategoryId())
                .authorId(post.getAuthorId())
                .metaTitle(post.getMetaTitle())
                .metaDescription(post.getMetaDescription())
                .metaKeywords(post.getMetaKeywords())
                .viewCount(post.getViewCount())
                .publishedAt(post.getPublishedAt())
                .tags(tagResponses)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
