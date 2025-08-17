package com.marketplace.StoneRidgeMarketplace.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    
    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.file.max-size:5242880}") // 5MB default
    private long maxFileSize;
    
    public String storeFile(MultipartFile file, String category) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty file");
            }
            
            if (file.getSize() > maxFileSize) {
                throw new IllegalArgumentException("File size exceeds maximum allowed size");
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, category);
            Files.createDirectories(uploadPath);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String filename = UUID.randomUUID().toString() + fileExtension;
            
            // Store file
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path for URL generation
            return "/" + category + "/" + filename;
            
        } catch (IOException e) {
            log.error("Error storing file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store file", e);
        }
    }
    
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir + filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
        }
    }
    
    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }
}
