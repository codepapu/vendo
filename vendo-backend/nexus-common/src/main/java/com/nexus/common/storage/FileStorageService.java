package com.nexus.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(S3Client.class)
public class FileStorageService {
    
    private final S3Client s3Client;
    
    @Value("${app.storage.s3.bucket:nexus-uploads}")
    private String bucketName;
    
    @Value("${app.storage.s3.region:us-east-1}")
    private String region;
    
    @Value("${app.storage.s3.endpoint:}")
    private String endpoint;
    
    @Value("${app.storage.type:s3}")
    private String storageType; // s3 or minio
    
    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    
    /**
     * Upload file to S3/MinIO
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key = folder != null ? folder + "/" + fileName : fileName;
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .acl(ObjectCannedACL.PRIVATE)
                .build();
        
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        log.info("File uploaded successfully: {}", key);
        return key;
    }
    
    /**
     * Upload file from InputStream
     */
    public String uploadFile(InputStream inputStream, String fileName, String contentType, String folder) throws IOException {
        String key = folder != null ? folder + "/" + fileName : fileName;
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PRIVATE)
                .build();
        
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));
        
        log.info("File uploaded successfully: {}", key);
        return key;
    }
    
    /**
     * Get file URL (presigned URL for private files)
     */
    public String getFileUrl(String key) {
        return getPresignedUrl(key, Duration.ofHours(1));
    }
    
    /**
     * Get presigned URL
     * Note: For full presigned URL support, S3Presigner needs to be configured
     */
    public String getPresignedUrl(String key, Duration expiration) {
        // For now, return a simple URL
        // TODO: Implement proper presigned URL generation with S3Presigner
        if (endpoint != null && !endpoint.isEmpty()) {
            // MinIO endpoint
            return endpoint + "/" + bucketName + "/" + key;
        }
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }
    
    /**
     * Delete file
     */
    public void deleteFile(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        
        s3Client.deleteObject(deleteRequest);
        log.info("File deleted successfully: {}", key);
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
    
    /**
     * Generate unique file name
     */
    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
