package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCategoryUpdateRequest {
    @Size(min = 2, max = 50)
    private String name;

    @Size(max = 500)
    private String description;
}
