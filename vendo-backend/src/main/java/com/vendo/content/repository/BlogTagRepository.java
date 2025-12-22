package com.vendo.content.repository;

import com.vendo.content.entity.BlogTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {
    
    Optional<BlogTag> findBySlug(String slug);
    
    Optional<BlogTag> findByName(String name);
}
