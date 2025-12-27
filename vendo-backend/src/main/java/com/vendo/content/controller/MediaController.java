package com.vendo.content.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.content.dto.response.MediaFileResponse;
import com.vendo.content.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    
    private final MediaService mediaService;
    
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<Page<MediaFileResponse>>> getAllFiles(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MediaFileResponse> files = mediaService.getAllFiles(userId, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(files));
    }
    
    @GetMapping("/files/{id}")
    public ResponseEntity<ApiResponse<MediaFileResponse>> getFileById(@PathVariable Long id) {
        MediaFileResponse file = mediaService.getFileById(id);
        return ResponseEntity.ok(ApiResponse.success(file));
    }
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<MediaFileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        Long userId = Long.parseLong(authentication.getName());
        MediaFileResponse uploadedFile = mediaService.uploadFile(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", uploadedFile));
    }
    
    @DeleteMapping("/files/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) throws IOException {
        mediaService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
    }
    
    @GetMapping("/files/{id}/url")
    public ResponseEntity<ApiResponse<String>> getFileUrl(@PathVariable Long id) {
        String url = mediaService.getFileUrl(id);
        return ResponseEntity.ok(ApiResponse.success(url));
    }
    
    @PostMapping("/files/{id}/resize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<MediaFileResponse>> resizeImage(
            @PathVariable Long id,
            @RequestParam int width,
            @RequestParam int height) throws IOException {
        MediaFileResponse resizedFile = mediaService.resizeImage(id, width, height);
        return ResponseEntity.ok(ApiResponse.success("Image resized successfully", resizedFile));
    }
    
    @PostMapping("/files/{id}/optimize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<MediaFileResponse>> optimizeImage(@PathVariable Long id) throws IOException {
        MediaFileResponse optimizedFile = mediaService.optimizeImage(id);
        return ResponseEntity.ok(ApiResponse.success("Image optimized successfully", optimizedFile));
    }
}
