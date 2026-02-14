package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCategoryCreateRequest {
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50)
    private String name;

    @Size(max = 500)
    private String description;

    private Long parentCategoryId;
}
