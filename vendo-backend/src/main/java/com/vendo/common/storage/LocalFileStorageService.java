package com.vendo.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service("fileStorageService")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements IFileStorageService {
    
    @Value("${app.storage.local.directory:./uploads}")
    private String uploadDirectory;
    
    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = folder != null ? folder + "/" + fileName : fileName;
        Path targetPath = Paths.get(uploadDirectory, filePath);
        
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File uploaded to local storage: {}", targetPath);
        return filePath;
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, String folder) throws IOException {
        String filePath = folder != null ? folder + "/" + fileName : fileName;
        Path targetPath = Paths.get(uploadDirectory, filePath);
        
        Files.createDirectories(targetPath.getParent());
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File uploaded to local storage: {}", targetPath);
        return filePath;
    }
    
    @Override
    public String getFileUrl(String key) {
        return "/uploads/" + key;
    }
    
    @Override
    public void deleteFile(String key) {
        try {
            Path filePath = Paths.get(uploadDirectory, key);
            Files.deleteIfExists(filePath);
            log.info("File deleted from local storage: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", key, e);
        }
    }
    
    @Override
    public boolean fileExists(String key) {
        Path filePath = Paths.get(uploadDirectory, key);
        return Files.exists(filePath);
    }
    
    @Override
    public InputStream downloadFile(String key) throws IOException {
        Path filePath = Paths.get(uploadDirectory, key);
        return Files.newInputStream(filePath);
    }
    
    @Override
    public byte[] downloadFileAsBytes(String key) throws IOException {
        Path filePath = Paths.get(uploadDirectory, key);
        return Files.readAllBytes(filePath);
    }
    
    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}

