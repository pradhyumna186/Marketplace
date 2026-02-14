package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private Long id;
    private String imageUrl;
    private String fileName;
    private boolean primary;
    private Integer displayOrder;
}
