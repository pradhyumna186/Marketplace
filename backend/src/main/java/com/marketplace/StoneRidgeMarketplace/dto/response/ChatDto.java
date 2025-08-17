package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ChatStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDto {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productImageUrl;
    private Long buyerId;
    private String buyerName;
    private String buyerDisplayName;
    private Long sellerId;
    private String sellerName;
    private String sellerDisplayName;
    private ChatStatus status;
    private boolean hasUnreadMessages;
    private ChatMessageDto lastMessage;
    private List<ChatMessageDto> recentMessages;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
}
