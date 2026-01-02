package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDto {
    
    private Long id; // For response only
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 1000, message = "Justification must not exceed 1000 characters")
    private String justification;
    
    private Long parentCategoryId; // Optional for subcategories
    
    // Additional fields for response
    private String status;
    private Long requestedById;
    private String requestedByUsername;
    private String parentCategoryName;
    private java.time.LocalDateTime createdAt;
}
