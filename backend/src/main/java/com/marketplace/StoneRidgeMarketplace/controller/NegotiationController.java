package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.NegotiationRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.NegotiationDto;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
import com.marketplace.StoneRidgeMarketplace.service.NegotiationService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/negotiations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Negotiations", description = "Price negotiation APIs")
@PreAuthorize("isAuthenticated()")
public class NegotiationController {
    
    private final NegotiationService negotiationService;
    
    @PostMapping("/chats/{chatId}/offer")
    @Operation(summary = "Make a price offer")
    public ResponseEntity<ApiResponse<NegotiationDto>> makeOffer(
            @PathVariable Long chatId,
            @Valid @RequestBody NegotiationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        NegotiationDto negotiation = negotiationService.makeOffer(chatId, request, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<NegotiationDto>builder()
                        .success(true)
                        .message("Offer submitted successfully")
                        .data(negotiation)
                        .build()
        );
    }
    
    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept an offer")
    public ResponseEntity<ApiResponse<Void>> acceptOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        negotiationService.acceptOffer(id, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Offer accepted successfully")
                        .build()
        );
    }
    
    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject an offer")
    public ResponseEntity<ApiResponse<Void>> rejectOffer(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        negotiationService.rejectOffer(id, principal.getId(), reason);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Offer rejected")
                        .build()
        );
    }
    
    @GetMapping("/chats/{chatId}")
    @Operation(summary = "Get negotiations for a chat")
    public ResponseEntity<ApiResponse<List<NegotiationDto>>> getChatNegotiations(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        List<NegotiationDto> negotiations = negotiationService.getChatNegotiations(chatId, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<List<NegotiationDto>>builder()
                        .success(true)
                        .data(negotiations)
                        .build()
        );
    }
    
    @GetMapping("/pending-offers")
    @Operation(summary = "Get pending offers for seller")
    public ResponseEntity<ApiResponse<List<NegotiationDto>>> getPendingOffers(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        List<NegotiationDto> offers = negotiationService.getPendingOffersForSeller(principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<List<NegotiationDto>>builder()
                        .success(true)
                        .data(offers)
                        .build()
        );
    }
}
