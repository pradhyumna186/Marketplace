package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;
    
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName; // Optional - can be null or empty
    
    @Size(max = 50, message = "Display name must not exceed 50 characters")
    private String displayName; // Optional - custom display name
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;
    
    @NotBlank(message = "Apartment number is required")
    @Size(max = 10, message = "Apartment number must not exceed 10 characters")
    private String apartmentNumber;
    
    @NotBlank(message = "Building name is required")
    @Size(max = 50, message = "Building name must not exceed 50 characters")
    private String buildingName;
    
    @Pattern(regexp = "^$|^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number or leave blank")
    private String phoneNumber; // Optional
    
    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean acceptTerms;
}
