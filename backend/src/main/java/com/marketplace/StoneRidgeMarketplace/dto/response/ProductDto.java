package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductImageDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private ProductCondition condition;
    private ProductStatus status;
    
    // Category info
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
    
    // Seller info
    private Long sellerId;
    private String sellerName;
    private String sellerDisplayName;
    private String sellerBuilding;
    private String sellerApartment;
    
    // Buyer info (if sold)
    private Long buyerId;
    private String buyerName;
    
    // Images
    private List<ProductImageDto> images;
    
    // Interaction data
    private Integer viewCount;
    private boolean negotiable;
    private String locationDetails;
    private Integer activeChatsCount;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime soldAt;
    private BigDecimal soldPrice;
    
    // Helper flags for frontend
    private boolean isOwner;
    private boolean canEdit;
    private boolean canChat;
}
