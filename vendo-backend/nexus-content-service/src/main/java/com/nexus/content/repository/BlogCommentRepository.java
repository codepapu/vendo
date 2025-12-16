package com.nexus.content.repository;

import com.nexus.content.entity.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
    
    List<BlogComment> findByPostId(Long postId);
    
    List<BlogComment> findByPostIdAndStatus(Long postId, String status);
    
    List<BlogComment> findByParentId(Long parentId);
    
    Page<BlogComment> findByStatus(String status, Pageable pageable);
}
