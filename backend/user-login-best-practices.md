# Stoneridge Marketplace - Complete Authentication System with Proper Package Structure

## 📁 Package Structure
```
com.marketplace.StoneRidgeMarketplace/
├── entity/
│   ├── User.java
│   ├── TrustedDevice.java
│   └── enums/
│       └── Role.java
├── dto/
│   ├── request/
│   │   ├── RegistrationRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── RegistrationResponse.java
│       ├── LoginResponse.java
│       ├── VerificationResponse.java
│       ├── UserDto.java
│       └── TrustedDeviceDto.java
├── service/
│   ├── AuthService.java
│   ├── EmailService.java
│   └── DeviceFingerprintService.java
├── controller/
│   └── AuthController.java
├── repository/
│   ├── UserRepository.java
│   └── TrustedDeviceRepository.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── DeviceTrustFilter.java
├── config/
│   └── SecurityConfig.java
├── exception/
│   ├── EmailNotVerifiedException.java
│   ├── InvalidTokenException.java
│   └── TokenExpiredException.java
└── util/
    └── ApiResponse.java
```

## 📊 Database Entities

### 1. Role.java (Enum)
```java
package com.marketplace.StoneRidgeMarketplace.entity.enums;

public enum Role {
    USER,
    ADMIN
}
```

### 2. User.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Name fields - flexible for single or dual names
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = true) // Optional - for people with single names
    private String lastName;
    
    @Column(name = "display_name")
    private String displayName; // Auto-generated or custom
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    // Stoneridge specific fields
    @Column(name = "apartment_number", nullable = false)
    private String apartmentNumber;
    
    @Column(name = "building_name", nullable = false)
    private String buildingName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    
    // Account status
    @Column(nullable = false)
    private boolean enabled = false;
    
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;
    
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;
    
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;
    
    // Security tracking
    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;
    
    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    
    // Email verification
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;
    
    // Password reset
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrustedDevice> trustedDevices = new HashSet<>();
    
    // Helper method to get full name
    public String getFullName() {
        if (lastName == null || lastName.trim().isEmpty()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
    
    // Helper method to get display name
    public String getEffectiveDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        return getFullName();
    }
}
```

### 3. TrustedDevice.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustedDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "device_token", nullable = false, unique = true)
    private String deviceToken;
    
    @Column(name = "device_fingerprint", nullable = false)
    private String deviceFingerprint;
    
    @Column(name = "device_name")
    private String deviceName;
    
    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean active = true;
}
```

## 📝 DTOs (Data Transfer Objects)

### 4. request/RegistrationRequest.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;
    
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName; // Optional - can be null or empty
    
    @Size(max = 50, message = "Display name must not exceed 50 characters")
    private String displayName; // Optional - custom display name
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;
    
    @NotBlank(message = "Apartment number is required")
    @Size(max = 10, message = "Apartment number must not exceed 10 characters")
    private String apartmentNumber;
    
    @NotBlank(message = "Building name is required")
    @Size(max = 50, message = "Building name must not exceed 50 characters")
    private String buildingName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phoneNumber;
    
    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean acceptTerms;
}
```

### 5. request/LoginRequest.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Username or Email is required")
    private String usernameOrEmail;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private boolean rememberDevice = false;
    
    // Optional device info for better fingerprinting
    private String screenResolution;
    private String timezone;
}
```

### 6. response/UserDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String displayName;
    private String fullName;
    private String username;
    private String email;
    private Role role;
    private String apartmentNumber;
    private String buildingName;
    private String phoneNumber;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String profileImageUrl;
}
```

### 7. response/LoginResponse.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserDto user;
    private String deviceToken;
    private boolean isDeviceTrusted;
    private List<TrustedDeviceDto> trustedDevices;
}
```

### 8. response/TrustedDeviceDto.java
```java
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
```

## 🔧 Services

### 9. service/AuthService.java
```java
package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.*;
import com.marketplace.StoneRidgeMarketplace.dto.response.*;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;
import com.marketplace.StoneRidgeMarketplace.exception.*;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import com.marketplace.StoneRidgeMarketplace.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final AuthenticationManager authenticationManager;
    
    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;
    
    @Value("${app.security.lock-duration-minutes:30}")
    private long lockDurationMinutes;
    
    @Value("${app.security.email-verification-expiry-hours:24}")
    private long emailVerificationExpiryHours;
    
    @Value("${app.security.trusted-device-expiry-days:30}")
    private long trustedDeviceExpiryDays;
    
    @Value("${app.security.max-trusted-devices:5}")
    private int maxTrustedDevices;
    
    /**
     * Register new Stoneridge resident
     */
    public RegistrationResponse register(RegistrationRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());
        
        // Validate uniqueness
        if (userRepository.existsByEmailOrUsername(
                request.getEmail().toLowerCase(), 
                request.getUsername().toLowerCase())) {
            throw new DuplicateResourceException("Email or username already taken");
        }
        
        // Build user entity
        User user = User.builder()
            .firstName(request.getFirstName().trim())
            .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
            .displayName(determineDisplayName(request))
            .email(request.getEmail().toLowerCase())
            .username(request.getUsername().toLowerCase())
            .password(passwordEncoder.encode(request.getPassword()))
            .apartmentNumber(request.getApartmentNumber())
            .buildingName(request.getBuildingName())
            .phoneNumber(request.getPhoneNumber())
            .role(Role.USER)
            .enabled(false)
            .emailVerified(false)
            .phoneVerified(false)
            .accountNonLocked(true)
            .build();
        
        // Generate verification token
        String verificationToken = generateSecureToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(
            LocalDateTime.now().plusHours(emailVerificationExpiryHours)
        );
        
        user = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(
            user.getEmail(), 
            user.getFullName(), 
            verificationToken
        );
        
        log.info("New resident registered: {} from Apartment: {}-{}", 
            user.getUsername(), user.getBuildingName(), user.getApartmentNumber());
        
        return RegistrationResponse.builder()
            .message("Registration successful! Please check your email to verify your account.")
            .email(user.getEmail())
            .username(user.getUsername())
            .requiresVerification(true)
            .build();
    }
    
    /**
     * Login with device trust support
     */
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String userIdentifier = request.getUsernameOrEmail().toLowerCase();
        
        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(userIdentifier, userIdentifier)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        // Check email verification
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(
                "Please verify your email before logging in. Check your inbox for the verification link."
            );
        }
        
        // Check account lock status
        checkAccountLockStatus(user);
        
        // Authenticate
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new BadCredentialsException(
                String.format("Invalid credentials. %d attempts remaining", 
                    maxFailedAttempts - user.getFailedLoginAttempts())
            );
        }
        
        // Handle successful login
        handleSuccessfulLogin(user, httpRequest);
        
        // Check device trust
        String deviceFingerprint = deviceFingerprintService.generateFingerprint(httpRequest);
        boolean isDeviceTrusted = isDeviceTrusted(user, deviceFingerprint);
        
        // Create trusted device if requested
        String deviceToken = null;
        if (request.isRememberDevice() && !isDeviceTrusted) {
            deviceToken = createTrustedDevice(user, deviceFingerprint, httpRequest);
        }
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Get trusted devices
        List<TrustedDeviceDto> trustedDevices = getTrustedDevices(user.getId());
        
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration())
            .user(mapToUserDto(user))
            .deviceToken(deviceToken)
            .isDeviceTrusted(isDeviceTrusted)
            .trustedDevices(trustedDevices)
            .build();
    }
    
    /**
     * Verify email address
     */
    public VerificationResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));
        
        // Check token expiry
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException(
                "Verification token has expired. Please request a new one."
            );
        }
        
        // Verify email
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
        
        log.info("Email verified for user: {}", user.getUsername());
        
        return VerificationResponse.builder()
            .success(true)
            .message("Email verified successfully! You can now log in to Stoneridge Marketplace.")
            .username(user.getUsername())
            .build();
    }
    
    /**
     * Helper method to determine display name
     */
    private String determineDisplayName(RegistrationRequest request) {
        if (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty()) {
            return request.getDisplayName().trim();
        }
        
        // Generate from name
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            return request.getFirstName().trim();
        }
        
        return request.getFirstName().trim() + " " + request.getLastName().trim();
    }
    
    /**
     * Map User entity to UserDto
     */
    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .displayName(user.getEffectiveDisplayName())
            .fullName(user.getFullName())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .apartmentNumber(user.getApartmentNumber())
            .buildingName(user.getBuildingName())
            .phoneNumber(user.getPhoneNumber())
            .emailVerified(user.isEmailVerified())
            .phoneVerified(user.isPhoneVerified())
            .build();
    }
    
    /**
     * Get user's trusted devices
     */
    public List<TrustedDeviceDto> getTrustedDevices(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return user.getTrustedDevices().stream()
            .filter(TrustedDevice::isActive)
            .filter(device -> device.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(this::mapToTrustedDeviceDto)
            .collect(Collectors.toList());
    }
    
    private TrustedDeviceDto mapToTrustedDeviceDto(TrustedDevice device) {
        return TrustedDeviceDto.builder()
            .id(device.getId())
            .deviceName(device.getDeviceName())
            .deviceType(device.getDeviceType())
            .lastUsedAt(device.getLastUsedAt())
            .location(device.getIpAddress()) // Could enhance with geolocation
            .expiresAt(device.getExpiresAt())
            .build();
    }
    
    // Other helper methods remain the same as in previous version...
    
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    private void checkAccountLockStatus(User user) {
        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null && 
                user.getLockTime().plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now())) {
                // Unlock account
                user.setAccountNonLocked(true);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            } else {
                throw new LockedException(
                    "Account is locked due to too many failed attempts. Please try again later."
                );
            }
        }
    }
    
    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
            
            // Send security alert
            emailService.sendSecurityAlert(
                user.getEmail(), 
                "Your Stoneridge Marketplace account has been locked due to multiple failed login attempts"
            );
        }
        
        userRepository.save(user);
    }
    
    private void handleSuccessfulLogin(User user, HttpServletRequest request) {
        user.setFailedLoginAttempts(0);
        user.setLockTime(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(getClientIP(request));
        userRepository.save(user);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
    
    private boolean isDeviceTrusted(User user, String fingerprint) {
        return trustedDeviceRepository
            .findByUserAndDeviceFingerprintAndActiveTrue(user, fingerprint)
            .map(device -> {
                if (device.getExpiresAt().isBefore(LocalDateTime.now())) {
                    device.setActive(false);
                    trustedDeviceRepository.save(device);
                    return false;
                }
                device.setLastUsedAt(LocalDateTime.now());
                trustedDeviceRepository.save(device);
                return true;
            })
            .orElse(false);
    }
    
    private String createTrustedDevice(User user, String fingerprint, HttpServletRequest request) {
        // Remove oldest device if limit reached
        if (user.getTrustedDevices().size() >= maxTrustedDevices) {
            TrustedDevice oldestDevice = user.getTrustedDevices().stream()
                .filter(TrustedDevice::isActive)
                .min(Comparator.comparing(TrustedDevice::getLastUsedAt))
                .orElse(null);
            
            if (oldestDevice != null) {
                oldestDevice.setActive(false);
                trustedDeviceRepository.save(oldestDevice);
            }
        }
        
        String deviceToken = generateSecureToken();
        String userAgent = request.getHeader("User-Agent");
        
        TrustedDevice trustedDevice = TrustedDevice.builder()
            .user(user)
            .deviceToken(deviceToken)
            .deviceFingerprint(fingerprint)
            .deviceName(deviceFingerprintService.extractDeviceName(userAgent))
            .deviceType(deviceFingerprintService.detectDeviceType(userAgent))
            .userAgent(userAgent)
            .ipAddress(getClientIP(request))
            .createdAt(LocalDateTime.now())
            .lastUsedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(trustedDeviceExpiryDays))
            .active(true)
            .build();
        
        trustedDeviceRepository.save(trustedDevice);
        
        // Notify user of new device
        emailService.sendNewDeviceAlert(
            user.getEmail(),
            trustedDevice.getDeviceName(),
            trustedDevice.getIpAddress()
        );
        
        log.info("New trusted device added for user: {}", user.getUsername());
        
        return deviceToken;
    }
}
```

### 10. controller/AuthController.java
```java
package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.*;
import com.marketplace.StoneRidgeMarketplace.dto.response.*;
import com.marketplace.StoneRidgeMarketplace.service.AuthService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Register new Stoneridge resident")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @Valid @RequestBody RegistrationRequest request) {
        
        log.info("Registration request received for username: {}", request.getUsername());
        
        RegistrationResponse response = authService.register(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<RegistrationResponse>builder()
                .success(true)
                .message("Registration successful")
                .data(response)
                .build()
        );
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login with optional device trust")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        log.info("Login attempt for: {}", request.getUsernameOrEmail());
        
        try {
            LoginResponse response = authService.login(request, httpRequest);
            
            // Set device token cookie if device was trusted
            if (response.getDeviceToken() != null) {
                Cookie deviceCookie = new Cookie("device_token", response.getDeviceToken());
                deviceCookie.setHttpOnly(true);
                deviceCookie.setSecure(true); // HTTPS only in production
                deviceCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                deviceCookie.setPath("/");
                deviceCookie.setSameSite("Strict");
                httpResponse.addCookie(deviceCookie);
            }
            
            return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(response)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Login failed for: {}", request.getUsernameOrEmail(), e);
            throw e;
        }
    }
    
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address with token")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyEmail(
            @RequestParam String token) {
        
        log.info("Email verification attempt with token: {}", token.substring(0, 8) + "...");
        
        VerificationResponse response = authService.verifyEmail(token);
        
        return ResponseEntity.ok(
            ApiResponse.<VerificationResponse>builder()
                .success(true)
                .message("Email verified successfully")
                .data(response)
                .build()
        );
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification link")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestParam String email) {
        
        log.info("Resend verification requested for email: {}", email);
        
        authService.resendVerificationEmail(email);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Verification email sent")
                .build()
        );
    }
    
    @GetMapping("/trusted-devices")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's trusted devices")
    public ResponseEntity<ApiResponse<List<TrustedDeviceDto>>> getTrustedDevices(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        List<TrustedDeviceDto> devices = authService.getTrustedDevices(principal.getId());
        
        return ResponseEntity.ok(
            ApiResponse.<List<TrustedDeviceDto>>builder()
                .success(true)
                .data(devices)
                .build()
        );
    }
    
    @DeleteMapping("/trusted-devices/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Revoke a trusted device")
    public ResponseEntity<ApiResponse<Void>> revokeTrustedDevice(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long deviceId) {
        
        log.info("User {} revoking device {}", principal.getUsername(), deviceId);
        
        authService.revokeTrustedDevice(principal.getId(), deviceId);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Device revoked successfully")
                .build()
        );
    }
    
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout from current device")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request) {
        
        log.info("User {} logging out", principal.getUsername());
        
        authService.logout(principal.getId(), request);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Logged out successfully")
                .build()
        );
    }
    
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutFromAllDevices(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("User {} logging out from all devices", principal.getUsername());
        
        authService.logoutFromAllDevices(principal.getId());
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Logged out from all devices")
                .build()
        );
    }
    
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        
        RefreshTokenResponse response = authService.refreshAccessToken(refreshToken);
        
        return ResponseEntity.ok(
            ApiResponse.<RefreshTokenResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(response)
                .build()
        );
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestParam String email) {
        
        log.info("Password reset requested for email: {}", email);
        
        authService.initiatePasswordReset(email);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("If the email exists, a password reset link has been sent")
                .build()
        );
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        authService.resetPassword(request.getToken(), request.getNewPassword());
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Password reset successfully")
                .build()
        );
    }
}
        