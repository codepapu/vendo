package com.vendo.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileResponse {
    
    private Long id;
    private String filename;
    private String originalName;
    private String path;
    private String url;
    private String mimeType;
    private Long fileSize;
    private String type;
    private Long userId;
    private String alt;
    private String description;
    private Integer width;
    private Integer height;
    private Integer duration;
    private String cdnUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
