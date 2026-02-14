package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String senderDisplayName;
    private String content;
    private String messageType;
    private boolean systemMessage;
    private LocalDateTime createdAt;
    private boolean sentByCurrentUser;
}
