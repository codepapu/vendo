package com.vendo.content.repository;

import com.vendo.content.entity.MediaFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    
    List<MediaFile> findByUserId(Long userId);
    
    List<MediaFile> findByType(String type);
    
    Page<MediaFile> findByUserId(Long userId, Pageable pageable);
    
    Page<MediaFile> findByType(String type, Pageable pageable);
}
