package com.nexus.content.service;

import com.nexus.common.exception.BadRequestException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.content.dto.request.BlogTagRequest;
import com.nexus.content.dto.response.BlogTagResponse;
import com.nexus.content.entity.BlogTag;
import com.nexus.content.repository.BlogTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogTagService {
    
    private final BlogTagRepository blogTagRepository;
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    
    @Transactional(readOnly = true)
    public Page<BlogTagResponse> getAllTags(Pageable pageable) {
        return blogTagRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<BlogTagResponse> getAllTagsList() {
        return blogTagRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BlogTagResponse getTagById(Long id) {
        BlogTag tag = blogTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog tag", "id", id));
        return mapToResponse(tag);
    }
    
    @Transactional(readOnly = true)
    public BlogTagResponse getTagBySlug(String slug) {
        BlogTag tag = blogTagRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog tag", "slug", slug));
        return mapToResponse(tag);
    }
    
    @Transactional
    public BlogTagResponse createTag(BlogTagRequest request) {
        // Check if name already exists
        if (blogTagRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
        }
        
        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        }
        
        // Check if slug already exists
        if (blogTagRepository.findBySlug(slug).isPresent()) {
            throw new BadRequestException("Tag with slug '" + slug + "' already exists");
        }
        
        BlogTag tag = BlogTag.builder()
                .name(request.getName())
                .slug(slug)
                .postCount(0L)
                .build();
        
        BlogTag savedTag = blogTagRepository.save(tag);
        log.info("Blog tag created: ID={}, Name={}", savedTag.getId(), savedTag.getName());
        
        return mapToResponse(savedTag);
    }
    
    @Transactional
    public BlogTagResponse updateTag(Long id, BlogTagRequest request) {
        BlogTag tag = blogTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog tag", "id", id));
        
        // Check name uniqueness if changed
        if (!tag.getName().equals(request.getName())) {
            if (blogTagRepository.findByName(request.getName()).isPresent()) {
                throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
            }
        }
        
        tag.setName(request.getName());
        
        // Update slug if provided or if name changed
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        }
        
        // Check slug uniqueness if changed
        if (!tag.getSlug().equals(slug)) {
            if (blogTagRepository.findBySlug(slug).isPresent()) {
                throw new BadRequestException("Tag with slug '" + slug + "' already exists");
            }
            tag.setSlug(slug);
        }
        
        BlogTag updatedTag = blogTagRepository.save(tag);
        log.info("Blog tag updated: ID={}", id);
        
        return mapToResponse(updatedTag);
    }
    
    @Transactional
    public void deleteTag(Long id) {
        BlogTag tag = blogTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog tag", "id", id));
        
        // Check if tag is used in any posts
        if (tag.getPostCount() != null && tag.getPostCount() > 0) {
            throw new BadRequestException("Cannot delete tag that is used in blog posts");
        }
        
        blogTagRepository.delete(tag);
        log.info("Blog tag deleted: ID={}", id);
    }
    
    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase();
    }
    
    private BlogTagResponse mapToResponse(BlogTag tag) {
        return BlogTagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .postCount(tag.getPostCount())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}
