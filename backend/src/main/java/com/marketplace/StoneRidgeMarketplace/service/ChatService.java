package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.ChatMessageRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.ChatDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ChatMessageDto;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ChatStatus;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {
    
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    
    /**
     * Start or get existing chat for a product
     */
    public ChatDto startOrGetChat(Long productId, Long buyerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));
        
        // Check if buyer is trying to chat with themselves
        if (product.getSeller().getId().equals(buyerId)) {
            throw new IllegalStateException("You cannot chat about your own product");
        }
        
        // Check if chat already exists
        Optional<Chat> existingChat = chatRepository.findByProductAndBuyerAndStatus(product, buyer, ChatStatus.ACTIVE);
        
        Chat chat;
        if (existingChat.isPresent()) {
            chat = existingChat.get();
        } else {
            // Create new chat
            chat = Chat.builder()
                    .product(product)
                    .buyer(buyer)
                    .seller(product.getSeller())
                    .status(ChatStatus.ACTIVE)
                    .build();
            chat = chatRepository.save(chat);
            
            // Send welcome message
            sendSystemMessage(chat, String.format("%s is interested in your product: %s", 
                    buyer.getEffectiveDisplayName(), product.getTitle()));
            
            log.info("New chat started for product: {} between buyer: {} and seller: {}", 
                    product.getTitle(), buyer.getUsername(), product.getSeller().getUsername());
        }
        
        return mapToChatDto(chat, buyerId);
    }
    
    /**
     * Send message in chat
     */
    public ChatMessageDto sendMessage(Long chatId, ChatMessageRequest request, Long senderId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(senderId) && !chat.getSeller().getId().equals(senderId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        if (chat.getStatus() != ChatStatus.ACTIVE) {
            throw new IllegalStateException("This chat is not active");
        }
        
        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .systemMessage(false)
                .build();
        
        message = chatMessageRepository.save(message);
        
        // Update chat's last message time
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
        
        log.info("Message sent in chat {}: {} characters by user: {}", 
                chatId, request.getContent().length(), sender.getUsername());
        
        return mapToChatMessageDto(message, senderId);
    }
    
    /**
     * Get user's chats
     */
    public Page<ChatDto> getUserChats(Long userId, Pageable pageable) {
        Page<Chat> chats = chatRepository.findUserChats(userId, ChatStatus.ACTIVE, pageable);
        return chats.map(chat -> mapToChatDto(chat, userId));
    }
    
    /**
     * Get chat by ID
     */
    public ChatDto getChatById(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        return mapToChatDto(chat, userId);
    }
    
    /**
     * Get chat messages with pagination
     */
    public Page<ChatMessageDto> getChatMessages(Long chatId, Long userId, Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        Page<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
        return messages.map(message -> mapToChatMessageDto(message, userId));
    }
    
    /**
     * Mark chat as read
     */
    public void markChatAsRead(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        chat.markAsRead(user);
        chatRepository.save(chat);
    }
    
    /**
     * Close chat
     */
    public void closeChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        chat.setStatus(ChatStatus.CLOSED);
        chatRepository.save(chat);
        
        // Send system message
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            sendSystemMessage(chat, String.format("Chat closed by %s", user.getEffectiveDisplayName()));
        }
        
        log.info("Chat {} closed by user: {}", chatId, userId);
    }
    
    /**
     * Get chats with unread messages
     */
    public List<ChatDto> getChatsWithUnreadMessages(Long userId) {
        List<Chat> chats = chatRepository.findChatsWithUnreadMessages(userId);
        return chats.stream()
                .map(chat -> mapToChatDto(chat, userId))
                .collect(Collectors.toList());
    }
    
    private void sendSystemMessage(Chat chat, String content) {
        ChatMessage systemMessage = ChatMessage.builder()
                .chat(chat)
                .sender(chat.getSeller()) // System messages from seller's perspective
                .content(content)
                .messageType("system")
                .systemMessage(true)
                .build();
        
        chatMessageRepository.save(systemMessage);
        
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
    }
    
    private ChatDto mapToChatDto(Chat chat, Long currentUserId) {
        ProductImage primaryImage = productImageRepository.findByProductIdAndPrimaryTrue(chat.getProduct().getId()).orElse(null);
        
        // Get recent messages (last 5)
        Pageable recentMessagesPageable = PageRequest.of(0, 5);
        List<ChatMessage> recentMessages = chatMessageRepository.findRecentMessages(chat.getId(), recentMessagesPageable);
        
        ChatMessage lastMessage = recentMessages.isEmpty() ? null : recentMessages.get(0);
        
        return ChatDto.builder()
                .id(chat.getId())
                .productId(chat.getProduct().getId())
                .productTitle(chat.getProduct().getTitle())
                .productImageUrl(primaryImage != null ? primaryImage.getImageUrl() : null)
                .buyerId(chat.getBuyer().getId())
                .buyerName(chat.getBuyer().getFullName())
                .buyerDisplayName(chat.getBuyer().getEffectiveDisplayName())
                .sellerId(chat.getSeller().getId())
                .sellerName(chat.getSeller().getFullName())
                .sellerDisplayName(chat.getSeller().getEffectiveDisplayName())
                .status(chat.getStatus())
                .hasUnreadMessages(chat.hasUnreadMessages(userRepository.findById(currentUserId).orElse(null)))
                .lastMessage(lastMessage != null ? mapToChatMessageDto(lastMessage, currentUserId) : null)
                .recentMessages(recentMessages.stream()
                        .map(msg -> mapToChatMessageDto(msg, currentUserId))
                        .collect(Collectors.toList()))
                .createdAt(chat.getCreatedAt())
                .lastMessageAt(chat.getLastMessageAt())
                .build();
    }
    
    private ChatMessageDto mapToChatMessageDto(ChatMessage message, Long currentUserId) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderDisplayName(message.getSender().getEffectiveDisplayName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .systemMessage(message.isSystemMessage())
                .createdAt(message.getCreatedAt())
                .sentByCurrentUser(message.getSender().getId().equals(currentUserId))
                .build();
    }
}
