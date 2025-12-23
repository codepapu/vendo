package com.vendo.content.service;

import com.vendo.content.dto.response.BlogPostResponse;
import com.vendo.content.dto.response.ContentResponse;
import com.vendo.content.dto.response.MediaFileResponse;
import com.vendo.content.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {
    
    private final BlogService blogService;
    private final PageService pageService;
    private final MediaService mediaService;
    
    @Transactional(readOnly = true)
    public ContentResponse getContentOverview(Integer blogLimit, Integer pageLimit, Integer mediaLimit) {
        Pageable blogPageable = PageRequest.of(0, blogLimit != null ? blogLimit : 10);
        Pageable pagePageable = PageRequest.of(0, pageLimit != null ? pageLimit : 10);
        Pageable mediaPageable = PageRequest.of(0, mediaLimit != null ? mediaLimit : 20);
        
        // Get published blog posts
        Page<BlogPostResponse> blogPosts = blogService.getAllPosts("published", null, null, blogPageable);
        
        // Get published pages
        Page<PageResponse> pagesPage = pageService.getAllPages("published", null, pagePageable);
        List<PageResponse> pages = pagesPage.getContent();
        
        // Get recent media files
        Page<MediaFileResponse> mediaFiles = mediaService.getAllFiles(null, null, mediaPageable);
        
        return ContentResponse.builder()
                .blogPosts(blogPosts.getContent())
                .pages(pages)
                .mediaFiles(mediaFiles.getContent())
                .build();
    }
    
    @Transactional(readOnly = true)
    public ContentResponse getContentByType(String type, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 10);
        
        ContentResponse.ContentResponseBuilder builder = ContentResponse.builder();
        
        switch (type.toLowerCase()) {
            case "blog":
                Page<BlogPostResponse> blogPosts = blogService.getAllPosts("published", null, null, pageable);
                builder.blogPosts(blogPosts.getContent());
                break;
            case "pages":
                Page<PageResponse> pagesPage = pageService.getAllPages("published", null, pageable);
                builder.pages(pagesPage.getContent());
                break;
            case "media":
                Page<MediaFileResponse> mediaFiles = mediaService.getAllFiles(null, null, pageable);
                builder.mediaFiles(mediaFiles.getContent());
                break;
            default:
                throw new IllegalArgumentException("Invalid content type: " + type);
        }
        
        return builder.build();
    }
}
