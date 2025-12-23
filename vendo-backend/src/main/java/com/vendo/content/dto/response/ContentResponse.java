package com.vendo.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {
    
    private List<BlogPostResponse> blogPosts;
    private List<PageResponse> pages;
    private List<MediaFileResponse> mediaFiles;
}
