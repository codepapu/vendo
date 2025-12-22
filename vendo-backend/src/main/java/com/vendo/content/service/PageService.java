package com.vendo.content.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.content.dto.request.PageRequest;
import com.vendo.content.dto.response.PageResponse;
import com.vendo.content.entity.Page;
import com.vendo.content.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageService {
    
    private final PageRepository pageRepository;
    
    @Transactional(readOnly = true)
    public PageResponse getPageBySlug(String slug) {
        Page page = pageRepository.findBySlugAndStatus(slug, "published")
                .orElseThrow(() -> new ResourceNotFoundException("Page", "slug", slug));
        
        // Increment view count
        page.setViewCount(page.getViewCount() + 1);
        pageRepository.save(page);
        
        return mapToResponse(page);
    }
    
    @Transactional(readOnly = true)
    public PageResponse getPageById(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", "id", id));
        return mapToResponse(page);
    }
    
    @Transactional
    public PageResponse createPage(PageRequest request) {
        // Check if slug already exists
        if (pageRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BadRequestException("Page with slug '" + request.getSlug() + "' already exists");
        }
        
        Page page = Page.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .content(request.getContent())
                .template(request.getTemplate())
                .status(request.getStatus() != null ? request.getStatus() : "draft")
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .viewCount(0L)
                .build();
        
        Page savedPage = pageRepository.save(page);
        log.info("Page created: ID={}, Title={}", savedPage.getId(), savedPage.getTitle());
        
        return mapToResponse(savedPage);
    }
    
    @Transactional
    public PageResponse updatePage(Long id, PageRequest request) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", "id", id));
        
        // Check slug uniqueness if changed
        if (!page.getSlug().equals(request.getSlug())) {
            if (pageRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new BadRequestException("Page with slug '" + request.getSlug() + "' already exists");
            }
        }
        
        page.setTitle(request.getTitle());
        page.setSlug(request.getSlug());
        page.setContent(request.getContent());
        page.setTemplate(request.getTemplate());
        if (request.getStatus() != null) {
            page.setStatus(request.getStatus());
        }
        page.setMetaTitle(request.getMetaTitle());
        page.setMetaDescription(request.getMetaDescription());
        page.setMetaKeywords(request.getMetaKeywords());
        
        Page updatedPage = pageRepository.save(page);
        log.info("Page updated: ID={}", id);
        
        return mapToResponse(updatedPage);
    }
    
    @Transactional
    public void deletePage(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page", "id", id));
        pageRepository.delete(page);
        log.info("Page deleted: ID={}", id);
    }
    
    private PageResponse mapToResponse(Page page) {
        return PageResponse.builder()
                .id(page.getId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .content(page.getContent())
                .template(page.getTemplate())
                .status(page.getStatus())
                .metaTitle(page.getMetaTitle())
                .metaDescription(page.getMetaDescription())
                .metaKeywords(page.getMetaKeywords())
                .viewCount(page.getViewCount())
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .build();
    }
}
