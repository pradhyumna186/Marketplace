package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private ProductCondition condition;
    private ProductStatus status;
    private String primaryImageUrl;
    private String categoryName;
    private String sellerName;
    private String sellerBuilding;
    private Integer viewCount;
    private boolean negotiable;
    private LocalDateTime createdAt;
    private boolean isOwner;
}
