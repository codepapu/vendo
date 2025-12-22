package com.vendo.content.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.content.dto.request.BlogCommentRequest;
import com.vendo.content.dto.response.BlogCommentResponse;
import com.vendo.content.entity.BlogComment;
import com.vendo.content.entity.BlogPost;
import com.vendo.content.repository.BlogCommentRepository;
import com.vendo.content.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogCommentService {
    
    private final BlogCommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository;
    
    @Transactional(readOnly = true)
    public List<BlogCommentResponse> getCommentsByPostId(Long postId) {
        List<BlogComment> comments = commentRepository.findByPostIdAndStatus(postId, "approved");
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<BlogCommentResponse> getPendingComments(Pageable pageable) {
        Page<BlogComment> comments = commentRepository.findByStatus("pending", pageable);
        return comments.map(this::mapToResponse);
    }
    
    @Transactional
    public BlogCommentResponse createComment(BlogCommentRequest request, Long userId) {
        // Verify post exists
        BlogPost post = blogPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", request.getPostId()));
        
        BlogComment comment = BlogComment.builder()
                .postId(request.getPostId())
                .userId(userId)
                .name(request.getName())
                .email(request.getEmail())
                .comment(request.getComment())
                .parentId(request.getParentId())
                .status("pending")
                .build();
        
        BlogComment savedComment = commentRepository.save(comment);
        log.info("Blog comment created: ID={}, PostID={}", savedComment.getId(), request.getPostId());
        
        return mapToResponse(savedComment);
    }
    
    @Transactional
    public BlogCommentResponse updateCommentStatus(Long id, String status) {
        BlogComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog comment", "id", id));
        
        comment.setStatus(status);
        BlogComment updatedComment = commentRepository.save(comment);
        log.info("Blog comment status updated: ID={}, Status={}", id, status);
        
        return mapToResponse(updatedComment);
    }
    
    @Transactional
    public void deleteComment(Long id) {
        BlogComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog comment", "id", id));
        commentRepository.delete(comment);
        log.info("Blog comment deleted: ID={}", id);
    }
    
    private BlogCommentResponse mapToResponse(BlogComment comment) {
        List<BlogComment> replies = commentRepository.findByParentId(comment.getId());
        List<BlogCommentResponse> replyResponses = replies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return BlogCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .name(comment.getName())
                .email(comment.getEmail())
                .comment(comment.getComment())
                .parentId(comment.getParentId())
                .status(comment.getStatus())
                .replies(replyResponses.isEmpty() ? null : replyResponses)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
