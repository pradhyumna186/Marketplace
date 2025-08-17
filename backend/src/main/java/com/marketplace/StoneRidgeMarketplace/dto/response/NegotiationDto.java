package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.NegotiationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationDto {
    private Long id;
    private Long chatId;
    private Long offeredById;
    private String offeredByName;
    private BigDecimal offeredPrice;
    private BigDecimal originalPrice;
    private String message;
    private NegotiationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private boolean isExpired;
    private boolean canRespond;
    private boolean isOwnOffer;
}
