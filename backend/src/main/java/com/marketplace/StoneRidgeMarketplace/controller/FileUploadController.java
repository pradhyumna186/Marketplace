package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.service.FileStorageService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload", description = "File upload APIs")
@PreAuthorize("isAuthenticated()")
public class FileUploadController {
    
    private final FileStorageService fileStorageService;
    
    @PostMapping("/upload/product-images")
    @Operation(summary = "Upload product images")
    public ResponseEntity<ApiResponse<List<String>>> uploadProductImages(
            @RequestParam("files") MultipartFile[] files) {
        
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!fileStorageService.isValidImageFile(file)) {
                throw new IllegalArgumentException("Invalid image file: " + file.getOriginalFilename());
            }
            
            String imageUrl = fileStorageService.storeFile(file, "products");
            imageUrls.add("http://localhost:8080/uploads" + imageUrl); // Configure base URL properly
        }
        
        return ResponseEntity.ok(
                ApiResponse.<List<String>>builder()
                        .success(true)
                        .message("Images uploaded successfully")
                        .data(imageUrls)
                        .build()
        );
    }
    
    @PostMapping("/upload/category-icon")
    @Operation(summary = "Upload category icon")
    public ResponseEntity<ApiResponse<String>> uploadCategoryIcon(
            @RequestParam("file") MultipartFile file) {
        
        if (!fileStorageService.isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }
        
        String iconUrl = fileStorageService.storeFile(file, "categories");
        String fullUrl = "http://localhost:8080/uploads" + iconUrl; // Configure base URL properly
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Icon uploaded successfully")
                        .data(fullUrl)
                        .build()
        );
    }
}
