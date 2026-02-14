package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustedDeviceDto {
    private Long id;
    private String deviceName;
    private String deviceType;
    private LocalDateTime lastUsedAt;
    private String location;
    private boolean isCurrent;
    private LocalDateTime expiresAt;
}
