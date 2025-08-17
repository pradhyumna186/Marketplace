package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.NegotiationRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.NegotiationDto;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.NegotiationStatus;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NegotiationService {
    
    private final NegotiationRepository negotiationRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    
    /**
     * Make a price offer
     */
    public NegotiationDto makeOffer(Long chatId, NegotiationRequest request, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        // Check if product is still available
        if (chat.getProduct().isSold()) {
            throw new IllegalStateException("This product has already been sold");
        }
        
        // Check if product is negotiable
        if (!chat.getProduct().isNegotiable()) {
            throw new IllegalStateException("This product is not open for negotiation");
        }
        
        // Expire any existing pending offers from this user in this chat
        List<Negotiation> existingOffers = negotiationRepository.findActivePendingOffers(chatId, LocalDateTime.now());
        existingOffers.stream()
                .filter(offer -> offer.getOfferedBy().getId().equals(userId))
                .forEach(offer -> {
                    offer.setStatus(NegotiationStatus.COUNTER_OFFERED);
                    negotiationRepository.save(offer);
                });
        
        // Create new offer
        Negotiation negotiation = Negotiation.builder()
                .chat(chat)
                .offeredBy(user)
                .offeredPrice(request.getOfferedPrice())
                .message(request.getMessage())
                .status(NegotiationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(request.getValidityHours()))
                .build();
        
        negotiation = negotiationRepository.save(negotiation);
        
        // Send notification message in chat
        String offerMessage = String.format("💰 %s made an offer of $%.2f%s", 
                user.getEffectiveDisplayName(),
                request.getOfferedPrice(),
                request.getMessage() != null ? " - " + request.getMessage() : "");
        
        sendOfferMessage(chat, user, offerMessage);
        
        log.info("Offer made in chat {}: $%.2f by user: {}", 
                chatId, request.getOfferedPrice(), user.getUsername());
        
        return mapToNegotiationDto(negotiation, userId);
    }
    
    /**
     * Accept an offer
     */
    public void acceptOffer(Long negotiationId, Long userId) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation not found"));
        
        Chat chat = negotiation.getChat();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Only seller can accept offers
        if (!chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("Only the seller can accept offers");
        }
        
        if (!negotiation.isPending()) {
            throw new IllegalStateException("This offer is no longer valid");
        }
        
        // Accept the offer
        negotiation.setStatus(NegotiationStatus.ACCEPTED);
        negotiation.setRespondedAt(LocalDateTime.now());
        negotiationRepository.save(negotiation);
        
        // Mark product as sold
        productService.markProductAsSold(
                chat.getProduct().getId(),
                negotiation.getOfferedBy().getId(),
                negotiation.getOfferedPrice(),
                userId
        );
        
        // Send confirmation message
        String confirmationMessage = String.format("✅ Offer of $%.2f accepted! Product sold to %s", 
                negotiation.getOfferedPrice(),
                negotiation.getOfferedBy().getEffectiveDisplayName());
        
        sendOfferMessage(chat, user, confirmationMessage);
        
        // Expire all other pending offers for this product
        List<Negotiation> otherOffers = negotiationRepository.findActivePendingOffers(chat.getId(), LocalDateTime.now());
        otherOffers.stream()
                .filter(offer -> !offer.getId().equals(negotiationId))
                .forEach(offer -> {
                    offer.setStatus(NegotiationStatus.REJECTED);
                    negotiationRepository.save(offer);
                });
        
        log.info("Offer accepted: ${} for product: {} by seller: {}", 
                negotiation.getOfferedPrice(), chat.getProduct().getTitle(), user.getUsername());
    }
    
    /**
     * Reject an offer
     */
    public void rejectOffer(Long negotiationId, Long userId, String reason) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation not found"));
        
        Chat chat = negotiation.getChat();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Only seller can reject offers
        if (!chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("Only the seller can reject offers");
        }
        
        if (!negotiation.isPending()) {
            throw new IllegalStateException("This offer is no longer valid");
        }
        
        // Reject the offer
        negotiation.setStatus(NegotiationStatus.REJECTED);
        negotiation.setRespondedAt(LocalDateTime.now());
        negotiationRepository.save(negotiation);
        
        // Send rejection message
        String rejectionMessage = String.format("❌ Offer of $%.2f was declined%s", 
                negotiation.getOfferedPrice(),
                reason != null ? " - " + reason : "");
        
        sendOfferMessage(chat, user, rejectionMessage);
        
        log.info("Offer rejected: ${} for product: {} by seller: {}", 
                negotiation.getOfferedPrice(), chat.getProduct().getTitle(), user.getUsername());
    }
    
    /**
     * Get negotiations for a chat
     */
    public List<NegotiationDto> getChatNegotiations(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        List<Negotiation> negotiations = negotiationRepository.findByChatIdOrderByCreatedAtDesc(chatId);
        return negotiations.stream()
                .map(negotiation -> mapToNegotiationDto(negotiation, userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get pending offers for seller
     */
    public List<NegotiationDto> getPendingOffersForSeller(Long sellerId) {
        List<Negotiation> negotiations = negotiationRepository.findPendingOffersForSeller(sellerId, LocalDateTime.now());
        return negotiations.stream()
                .map(negotiation -> mapToNegotiationDto(negotiation, sellerId))
                .collect(Collectors.toList());
    }
    
    /**
     * Expire old offers (scheduled task)
     */
    @Transactional
    public void expireOldOffers() {
        List<Negotiation> expiredOffers = negotiationRepository.findExpiredOffers(LocalDateTime.now());
        expiredOffers.forEach(offer -> {
            offer.setStatus(NegotiationStatus.REJECTED);
            negotiationRepository.save(offer);
        });
        
        if (!expiredOffers.isEmpty()) {
            log.info("Expired {} old offers", expiredOffers.size());
        }
    }
    
    private void sendOfferMessage(Chat chat, User sender, String content) {
        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(content)
                .messageType("offer")
                .systemMessage(false)
                .build();
        
        chatMessageRepository.save(message);
        
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
    }
    
    private NegotiationDto mapToNegotiationDto(Negotiation negotiation, Long currentUserId) {
        boolean canRespond = negotiation.isPending() && 
                             negotiation.getChat().getSeller().getId().equals(currentUserId) &&
                             !negotiation.getOfferedBy().getId().equals(currentUserId);
        
        boolean isOwnOffer = negotiation.getOfferedBy().getId().equals(currentUserId);
        
        return NegotiationDto.builder()
                .id(negotiation.getId())
                .chatId(negotiation.getChat().getId())
                .offeredById(negotiation.getOfferedBy().getId())
                .offeredByName(negotiation.getOfferedBy().getEffectiveDisplayName())
                .offeredPrice(negotiation.getOfferedPrice())
                .originalPrice(negotiation.getChat().getProduct().getPrice())
                .message(negotiation.getMessage())
                .status(negotiation.getStatus())
                .expiresAt(negotiation.getExpiresAt())
                .respondedAt(negotiation.getRespondedAt())
                .createdAt(negotiation.getCreatedAt())
                .isExpired(negotiation.isExpired())
                .canRespond(canRespond)
                .isOwnOffer(isOwnOffer)
                .build();
    }
}
