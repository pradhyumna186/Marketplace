package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Size(max = 50, message = "Display name must not exceed 50 characters")
    private String displayName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 10, message = "Apartment number must not exceed 10 characters")
    private String apartmentNumber;

    @Size(max = 50, message = "Building name must not exceed 50 characters")
    private String buildingName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phoneNumber;
}
