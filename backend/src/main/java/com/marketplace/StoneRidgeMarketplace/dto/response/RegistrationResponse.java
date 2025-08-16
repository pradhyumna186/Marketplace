package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {
    private String message;
    private String email;
    private String username;
    private boolean requiresVerification;
}
