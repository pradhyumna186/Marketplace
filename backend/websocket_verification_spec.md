# WebSocket Email Verification Specification

## Overview
This document specifies how the backend should implement WebSocket endpoints to provide real-time email verification notifications to the frontend.

## WebSocket Endpoint

### URL
```
ws://your-backend-domain:port/ws/verification?username={username}&email={email}
```

### Connection Parameters
- `username`: The username of the user waiting for verification
- `email`: The email address of the user

### Message Format

#### 1. Email Verified Event
When a user clicks the verification link in their email:

```json
{
  "type": "email_verified",
  "username": "akash123",
  "message": "Email verified successfully for user: akash123",
  "timestamp": "2025-08-16T22:05:34Z",
  "userId": 13
}
```

#### 2. Verification Status Update
For status updates or when checking if user is already verified:

```json
{
  "type": "verification_status",
  "username": "akash123",
  "isVerified": true,
  "message": "User account is verified",
  "timestamp": "2025-08-16T22:05:34Z"
}
```

#### 3. Error Event
When something goes wrong:

```json
{
  "type": "error",
  "message": "Failed to verify email",
  "errorCode": "VERIFICATION_FAILED"
}
```

## Backend Implementation Steps

### 1. WebSocket Handler
```java
@ServerEndpoint("/ws/verification")
public class VerificationWebSocket {
    
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("email") String email) {
        String key = username + ":" + email;
        userSessions.put(key, session);
        log.info("WebSocket connected for user: {}", username);
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("username") String username, @PathParam("email") String email) {
        String key = username + ":" + email;
        userSessions.remove(key);
        log.info("WebSocket disconnected for user: {}", username);
    }
}
```

### 2. Email Verification Notification
When email verification happens (user clicks link):

```java
public void notifyEmailVerified(String username, String email) {
    String key = username + ":" + email;
    Session session = userSessions.get(key);
    
    if (session != null && session.isOpen()) {
        VerificationEvent event = new VerificationEvent(
            "email_verified",
            username,
            "Email verified successfully for user: " + username
        );
        
        session.getAsyncRemote().sendText(JsonUtils.toJson(event));
        log.info("Notified user {} that email was verified", username);
    }
}
```

### 3. Integration with Email Verification
In your existing email verification endpoint:

```java
@GetMapping("/verify-email")
public ResponseEntity<?> verifyEmail(@RequestParam String token) {
    try {
        // Your existing verification logic
        boolean verified = emailVerificationService.verifyEmail(token);
        
        if (verified) {
            String username = getUsernameFromToken(token);
            String email = getEmailFromToken(token);
            
            // Notify frontend via WebSocket
            verificationWebSocket.notifyEmailVerified(username, email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully!"
            ));
        }
        
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Invalid verification token"
        ));
        
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of(
            "success", false,
            "message", "Verification failed: " + e.getMessage()
        ));
    }
}
```

## Frontend Integration

The frontend is already implemented to:
1. Connect to WebSocket when verification screen loads
2. Listen for verification events
3. Auto-redirect to login when verification is detected
4. Fall back to polling if WebSocket fails

## Testing

### Test Flow
1. User registers → Gets verification email
2. Frontend connects to WebSocket
3. User clicks email link → Backend verifies account
4. Backend sends WebSocket notification
5. Frontend receives notification → Auto-redirects to login

### Manual Testing
```bash
# Test WebSocket connection
wscat -c "ws://localhost:8080/ws/verification?username=test&email=test@example.com"

# Send test message
{"type": "email_verified", "username": "test", "message": "Test verification"}
```

## Security Considerations

1. **Authentication**: Consider adding JWT token validation to WebSocket connections
2. **Rate Limiting**: Prevent WebSocket spam
3. **Input Validation**: Validate username and email parameters
4. **Session Management**: Clean up disconnected sessions

## Fallback Strategy

If WebSocket fails, the frontend will automatically fall back to polling the verification status every 5 seconds via HTTP endpoint:

```
GET /api/auth/verification-status?username={username}&email={email}
```

This ensures the verification flow works even if WebSocket is unavailable.
