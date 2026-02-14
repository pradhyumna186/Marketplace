package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.*;
import com.marketplace.StoneRidgeMarketplace.dto.response.*;
import com.marketplace.StoneRidgeMarketplace.dto.response.RefreshTokenResponse;
import com.marketplace.StoneRidgeMarketplace.entity.Admin;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;
import com.marketplace.StoneRidgeMarketplace.exception.*;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import com.marketplace.StoneRidgeMarketplace.security.AdminPrincipal;
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
    private final AdminRepository adminRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final DeviceFingerprintService deviceFingerprintService;

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
                LocalDateTime.now().plusHours(emailVerificationExpiryHours));

        user = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verificationToken);

        log.info("New resident registered: {} from Apartment: {}-{}",
                user.getUsername(),
                user.getBuildingName() != null ? user.getBuildingName() : "—",
                user.getApartmentNumber() != null ? user.getApartmentNumber() : "—");

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
                    "Please verify your email before logging in. Check your inbox for the verification link.");
        }

        // Check account lock status
        checkAccountLockStatus(user);

        // Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new BadCredentialsException(
                    String.format("Invalid credentials. %d attempts remaining",
                            maxFailedAttempts - user.getFailedLoginAttempts()));
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
     * Admin login: authenticate against admins table and return JWT with admin identity.
     */
    public LoginResponse adminLogin(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim().toLowerCase();
        Admin admin = adminRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!admin.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        String accessToken = jwtService.generateAccessTokenForAdmin(admin);
        String refreshToken = jwtService.generateRefreshTokenForAdmin(admin);
        UserDto adminAsUserDto = mapAdminToUserDto(admin);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .user(adminAsUserDto)
                .deviceToken(null)
                .isDeviceTrusted(false)
                .trustedDevices(Collections.emptyList())
                .build();
    }

    private UserDto mapAdminToUserDto(Admin admin) {
        return UserDto.builder()
                .id(admin.getId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .displayName(admin.getFullName())
                .fullName(admin.getFullName())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .role(Role.ADMIN)
                .apartmentNumber(null)
                .buildingName(null)
                .phoneNumber(null)
                .emailVerified(true)
                .phoneVerified(false)
                .build();
    }

    /**
     * Verify email address
     */
    @Transactional
    public VerificationResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        // Check token expiry
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException(
                    "Verification token has expired. Please request a new one.");
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
                        "Account is locked due to too many failed attempts. Please try again later.");
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
                    "Your Stoneridge Marketplace account has been locked due to multiple failed login attempts");
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
                trustedDevice.getIpAddress());

        log.info("New trusted device added for user: {}", user.getUsername());

        return deviceToken;
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = generateSecureToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(
                LocalDateTime.now().plusHours(emailVerificationExpiryHours));
        userRepository.save(user);

        // Send verification email
        emailService.resendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verificationToken);

        log.info("Verification email resent for user: {}", user.getUsername());
    }

    /**
     * Revoke trusted device
     */
    public void revokeTrustedDevice(Long userId, Long deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TrustedDevice device = user.getTrustedDevices().stream()
                .filter(d -> d.getId().equals(deviceId) && d.isActive())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        device.setActive(false);
        trustedDeviceRepository.save(device);

        log.info("Device {} revoked for user: {}", deviceId, user.getUsername());
    }

    /**
     * Logout from current device
     */
    public void logout(Long userId, HttpServletRequest request) {
        // Implementation for logout logic
        log.info("User {} logged out from device", userId);
    }

    /**
     * Logout from all devices
     */
    public void logoutFromAllDevices(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.getTrustedDevices().stream()
                .filter(TrustedDevice::isActive)
                .forEach(device -> {
                    device.setActive(false);
                    trustedDeviceRepository.save(device);
                });

        log.info("User {} logged out from all devices", userId);
    }

    /**
     * Delete user account and all associated data
     */
    public void deleteUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Deleting user account: {}", user.getUsername());

        // First, delete all trusted devices
        List<TrustedDevice> devices = trustedDeviceRepository.findByUser(user);
        if (!devices.isEmpty()) {
            log.info("Deleting {} trusted devices for user {}", devices.size(), user.getUsername());
            trustedDeviceRepository.deleteAll(devices);
        }

        // Clear the trusted devices collection in the user entity
        user.getTrustedDevices().clear();

        // Now delete the user
        userRepository.delete(user);

        log.info("User account deleted successfully: {}", user.getUsername());
    }

    /**
     * Refresh access token: validate refresh token and issue a new access token.
     */
    public RefreshTokenResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token is required");
        }
        try {
            String subject = jwtService.extractUsername(refreshToken);
            if (subject != null && subject.startsWith(AdminPrincipal.ADMIN_PREFIX)) {
                String adminUsername = subject.substring(AdminPrincipal.ADMIN_PREFIX.length());
                Admin admin = adminRepository.findByUsername(adminUsername)
                        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
                if (!jwtService.validateToken(refreshToken, subject)) {
                    throw new BadCredentialsException("Invalid or expired refresh token");
                }
                if (!admin.isEnabled()) {
                    throw new BadCredentialsException("Account is disabled");
                }
                String newAccessToken = jwtService.generateAccessTokenForAdmin(admin);
                log.info("Token refreshed for admin: {}", adminUsername);
                return RefreshTokenResponse.builder()
                        .accessToken(newAccessToken)
                        .tokenType("Bearer")
                        .expiresIn(jwtService.getAccessTokenExpiration())
                        .build();
            }
            User user = userRepository.findByUsername(subject)
                    .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
            if (!jwtService.validateToken(refreshToken, subject)) {
                throw new BadCredentialsException("Invalid or expired refresh token");
            }
            if (!user.isEnabled()) {
                throw new BadCredentialsException("Account is disabled");
            }
            String newAccessToken = jwtService.generateAccessToken(user);
            log.info("Token refreshed for user: {}", subject);
            return RefreshTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .build();
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
    }

    /**
     * Initiate password reset
     */
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user != null) {
            // Generate password reset token
            String resetToken = generateSecureToken();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(
                    LocalDateTime.now().plusHours(1) // 1 hour expiry
            );
            userRepository.save(user);

            // Send password reset email
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(),
                    resetToken);

            log.info("Password reset initiated for user: {}", user.getUsername());
        }
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        // Check token expiry
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Password reset token has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset for user: {}", user.getUsername());
    }

    /**
     * Update user profile (all fields except username)
     */
    public UserDto updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if email is being changed and if it's already taken by another user
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail().toLowerCase(), userId)) {
                throw new DuplicateResourceException("Email is already taken by another user");
            }

            // If email is changing, reset verification status
            user.setEmailVerified(false);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiry(null);

            // Send new verification email
            String verificationToken = generateSecureToken();
            user.setEmailVerificationToken(verificationToken);
            user.setEmailVerificationTokenExpiry(
                    LocalDateTime.now().plusHours(emailVerificationExpiryHours));

            emailService.sendVerificationEmail(
                    request.getEmail().toLowerCase(),
                    user.getFullName(),
                    verificationToken);
        }

        // Update user fields
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName().trim() : null);
        user.setEmail(request.getEmail().toLowerCase());
        user.setApartmentNumber(request.getApartmentNumber() != null && !request.getApartmentNumber().isBlank() ? request.getApartmentNumber().trim() : null);
        user.setBuildingName(request.getBuildingName() != null && !request.getBuildingName().isBlank() ? request.getBuildingName().trim() : null);
        user.setPhoneNumber(request.getPhoneNumber());

        user = userRepository.save(user);

        log.info("Profile updated for user: {}", user.getUsername());

        return mapToUserDto(user);
    }
}
