package com.nexus.content.repository;

import com.nexus.content.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    
    Optional<Page> findBySlug(String slug);
    
    Optional<Page> findBySlugAndStatus(String slug, String status);
}
