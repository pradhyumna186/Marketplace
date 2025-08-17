package com.marketplace.StoneRidgeMarketplace.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket connection established for user: {}", userId);
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming WebSocket messages (ping/pong, etc.)
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            log.debug("Received WebSocket message: {}", textMessage.getPayload());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket connection closed for user: {}", userId);
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * Send message to specific user
     */
    public void sendMessageToUser(String userId, Object message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                log.error("Error sending WebSocket message to user: {}", userId, e);
            }
        }
    }
    
    /**
     * Send chat message notification
     */
    public void sendChatMessage(String recipientUserId, ChatMessageNotification notification) {
        sendMessageToUser(recipientUserId, Map.of(
                "type", "new_message",
                "chatId", notification.getChatId(),
                "senderId", notification.getSenderId(),
                "senderName", notification.getSenderName(),
                "content", notification.getContent(),
                "timestamp", notification.getTimestamp()
        ));
    }
    
    /**
     * Send negotiation notification
     */
    public void sendNegotiationNotification(String recipientUserId, NegotiationNotification notification) {
        sendMessageToUser(recipientUserId, Map.of(
                "type", "negotiation_update",
                "negotiationId", notification.getNegotiationId(),
                "chatId", notification.getChatId(),
                "status", notification.getStatus(),
                "message", notification.getMessage(),
                "timestamp", notification.getTimestamp()
        ));
    }
    
    private String getUserIdFromSession(WebSocketSession session) {
        // Extract user ID from session attributes or query parameters
        // This would be set during the WebSocket handshake with JWT validation
        return (String) session.getAttributes().get("userId");
    }
    
    // Notification DTOs
    public static class ChatMessageNotification {
        private Long chatId;
        private Long senderId;
        private String senderName;
        private String content;
        private String timestamp;
        
        // Constructor, getters, setters
        public ChatMessageNotification(Long chatId, Long senderId, String senderName, String content, String timestamp) {
            this.chatId = chatId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.content = content;
            this.timestamp = timestamp;
        }
        
        public Long getChatId() { return chatId; }
        public Long getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }
        public String getContent() { return content; }
        public String getTimestamp() { return timestamp; }
    }
    
    public static class NegotiationNotification {
        private Long negotiationId;
        private Long chatId;
        private String status;
        private String message;
        private String timestamp;
        
        // Constructor, getters, setters
        public NegotiationNotification(Long negotiationId, Long chatId, String status, String message, String timestamp) {
            this.negotiationId = negotiationId;
            this.chatId = chatId;
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public Long getNegotiationId() { return negotiationId; }
        public Long getChatId() { return chatId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
    }
}
