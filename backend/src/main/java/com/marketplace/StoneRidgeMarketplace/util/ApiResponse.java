package com.marketplace.StoneRidgeMarketplace.util;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();
}
