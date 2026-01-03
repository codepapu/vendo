package com.vendo.common.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface IFileStorageService {
    String uploadFile(MultipartFile file, String folder) throws IOException;
    String uploadFile(InputStream inputStream, String fileName, String contentType, String folder) throws IOException;
    String getFileUrl(String key);
    void deleteFile(String key);
    boolean fileExists(String key);
    InputStream downloadFile(String key) throws IOException;
    byte[] downloadFileAsBytes(String key) throws IOException;
}

