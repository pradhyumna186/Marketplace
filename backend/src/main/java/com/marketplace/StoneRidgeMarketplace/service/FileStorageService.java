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
    
    private static final java.util.Set<String> ALLOWED_IMAGE_TYPES = java.util.Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "image/heic", "image/heif"  // Apple iPhone photos
    );

    private static final java.util.Set<String> ALLOWED_IMAGE_EXTENSIONS = java.util.Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".heic", ".heif"
    );

    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            String ext = name.substring(name.lastIndexOf(".")).toLowerCase();
            return ALLOWED_IMAGE_EXTENSIONS.contains(ext);
        }
        return false;
    }
}
