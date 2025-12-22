package com.vendo.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCommentResponse {
    
    private Long id;
    private Long postId;
    private Long userId;
    private String userName;
    private String name;
    private String email;
    private String comment;
    private Long parentId;
    private String status;
    private List<BlogCommentResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
