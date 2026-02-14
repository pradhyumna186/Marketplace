package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.ChatMessageRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.ChatDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ChatMessageDto;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
import com.marketplace.StoneRidgeMarketplace.service.ChatService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "Chat management APIs")
@PreAuthorize("isAuthenticated()")
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping("/start/{productId}")
    @Operation(summary = "Start or get existing chat for a product")
    public ResponseEntity<ApiResponse<ChatDto>> startOrGetChat(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        ChatDto chat = chatService.startOrGetChat(productId, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<ChatDto>builder()
                        .success(true)
                        .data(chat)
                        .build()
        );
    }
    
    @GetMapping
    @Operation(summary = "Get user's chats")
    public ResponseEntity<ApiResponse<Page<ChatDto>>> getUserChats(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        Page<ChatDto> chats = chatService.getUserChats(principal.getId(), pageable);
        
        return ResponseEntity.ok(
                ApiResponse.<Page<ChatDto>>builder()
                        .success(true)
                        .data(chats)
                        .build()
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get chat by ID")
    public ResponseEntity<ApiResponse<ChatDto>> getChatById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        ChatDto chat = chatService.getChatById(id, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<ChatDto>builder()
                        .success(true)
                        .data(chat)
                        .build()
        );
    }
    
    @GetMapping("/{id}/messages")
    @Operation(summary = "Get chat messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageDto>>> getChatMessages(
            @PathVariable Long id,
            @PageableDefault(size = 50) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        Page<ChatMessageDto> messages = chatService.getChatMessages(id, principal.getId(), pageable);
        
        return ResponseEntity.ok(
                ApiResponse.<Page<ChatMessageDto>>builder()
                        .success(true)
                        .data(messages)
                        .build()
        );
    }
    
    @PostMapping("/{id}/messages")
    @Operation(summary = "Send message in chat")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        ChatMessageDto message = chatService.sendMessage(id, request, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<ChatMessageDto>builder()
                        .success(true)
                        .data(message)
                        .build()
        );
    }
    
    @PostMapping("/{id}/mark-read")
    @Operation(summary = "Mark chat as read")
    public ResponseEntity<ApiResponse<Void>> markChatAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        chatService.markChatAsRead(id, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Chat marked as read")
                        .build()
        );
    }
    
    @PostMapping("/{id}/close")
    @Operation(summary = "Close chat")
    public ResponseEntity<ApiResponse<Void>> closeChat(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        chatService.closeChat(id, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Chat closed")
                        .build()
        );
    }
    
    @GetMapping("/unread")
    @Operation(summary = "Get chats with unread messages")
    public ResponseEntity<ApiResponse<List<ChatDto>>> getChatsWithUnreadMessages(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        List<ChatDto> chats = chatService.getChatsWithUnreadMessages(principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<List<ChatDto>>builder()
                        .success(true)
                        .data(chats)
                        .build()
        );
    }
}
