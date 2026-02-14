package com.marketplace.StoneRidgeMarketplace.dto.request;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Original price must be less than 1,000,000")
    private BigDecimal originalPrice;

    private ProductCondition condition;

    private ProductStatus status;

    private Long categoryId;

    private Boolean negotiable;

    @Size(max = 200, message = "Location details must not exceed 200 characters")
    private String locationDetails;

    private List<String> imageUrls;
}
