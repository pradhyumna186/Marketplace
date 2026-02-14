package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String fullPath;
    private Long parentId;
    private String parentName;
    private List<CategoryDto> subcategories;
    private Integer productCount;
    private LocalDateTime createdAt;
    private boolean active;
}
