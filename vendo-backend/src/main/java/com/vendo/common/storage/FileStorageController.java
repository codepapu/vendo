package com.vendo.common.storage;

import com.vendo.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class FileStorageController {
    
    private final FileStorageService fileStorageService;
    
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        try {
            String fileKey = fileStorageService.uploadFile(file, folder);
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileKey));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
    
    @GetMapping("/url/{key}")
    public ResponseEntity<ApiResponse<String>> getFileUrl(@PathVariable String key) {
        String url = fileStorageService.getFileUrl(key);
        return ResponseEntity.ok(ApiResponse.success("File URL generated", url));
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String key) {
        fileStorageService.deleteFile(key);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
    
    @GetMapping("/exists/{key}")
    public ResponseEntity<ApiResponse<Boolean>> fileExists(@PathVariable String key) {
        boolean exists = fileStorageService.fileExists(key);
        return ResponseEntity.ok(ApiResponse.success("File existence checked", exists));
    }
}
