package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;
import com.marketplace.StoneRidgeMarketplace.dto.response.UserDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {
    private String message;
    private String email;
    private String username;
    private boolean requiresVerification;
    private UserDto user; // Include user data for frontend
}
