package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Username or Email is required")
    private String usernameOrEmail;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private boolean rememberDevice = false;
    
    // Optional device info for better fingerprinting
    private String screenResolution;
    private String timezone;
}
