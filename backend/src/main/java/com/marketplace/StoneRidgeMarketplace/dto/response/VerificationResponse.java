package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResponse {
    private boolean success;
    private String message;
    private String username;
}
