package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserDto user;
    private String deviceToken;
    private boolean isDeviceTrusted;
    private List<TrustedDeviceDto> trustedDevices;
}
