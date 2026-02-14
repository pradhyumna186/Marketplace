package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDto {
    private long totalUsers;
    private long totalProducts;
    private long activeProducts;
    private long totalCategories;
    private long pendingCategoryRequests;
}
