package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationRequest {
    
    @NotNull(message = "Offered price is required")
    @DecimalMin(value = "0.01", message = "Offered price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Offered price must be less than 1,000,000")
    private BigDecimal offeredPrice;
    
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
    
    private Integer validityHours = 24; // How long the offer is valid
}
