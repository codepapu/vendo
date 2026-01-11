package com.vendo.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service("s3FileStorageService")
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(S3Client.class)
public class S3FileStorageService implements IFileStorageService {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${app.storage.s3.bucket:vendo-uploads}")
    private String bucketName;
    
    @Value("${app.storage.s3.region:us-east-1}")
    private String region;
    
    @Value("${app.storage.s3.endpoint:}")
    private String endpoint;
    
    @Value("${app.storage.type:s3}")
    private String storageType; // s3 or minio
    
    @Value("${app.storage.s3.presigned-url-expiration-hours:1}")
    private int presignedUrlExpirationHours;
    
    public S3FileStorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
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
     * Get presigned URL for file access (GET request)
     * 
     * @param key Object key
     * @param expiration URL expiration duration
     * @return Presigned URL for GET request
     */
    public String getPresignedUrl(String key, Duration expiration) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key is required");
        }
        
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration != null ? expiration : Duration.ofHours(presignedUrlExpirationHours))
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();
            
            log.debug("Generated presigned GET URL for key: {} with expiration: {}", key, expiration);
            return presignedUrl;
            
        } catch (Exception e) {
            log.error("Error generating presigned URL for key: {}", key, e);
            // Fallback to simple URL if presigning fails
            if (endpoint != null && !endpoint.isEmpty()) {
                return endpoint + "/" + bucketName + "/" + key;
            }
            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        }
    }
    
    /**
     * Get presigned URL for file upload (PUT request)
     * 
     * @param key Object key
     * @param contentType Content type of the file
     * @param expiration URL expiration duration
     * @return Presigned URL for PUT request
     */
    public String getPresignedUploadUrl(String key, String contentType, Duration expiration) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key is required");
        }
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();
            
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(expiration != null ? expiration : Duration.ofHours(presignedUrlExpirationHours))
                    .putObjectRequest(putObjectRequest)
                    .build();
            
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();
            
            log.debug("Generated presigned PUT URL for key: {} with expiration: {}", key, expiration);
            return presignedUrl;
            
        } catch (Exception e) {
            log.error("Error generating presigned upload URL for key: {}", key, e);
            throw new RuntimeException("Failed to generate presigned upload URL", e);
        }
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
     * Download file from S3/MinIO as InputStream
     */
    public InputStream downloadFile(String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        
        return s3Client.getObject(getObjectRequest);
    }
    
    /**
     * Download file from S3/MinIO as byte array
     */
    public byte[] downloadFileAsBytes(String key) throws IOException {
        try (InputStream inputStream = downloadFile(key)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
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
