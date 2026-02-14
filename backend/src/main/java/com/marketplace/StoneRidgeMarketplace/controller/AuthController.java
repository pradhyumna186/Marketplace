package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.*;
import com.marketplace.StoneRidgeMarketplace.dto.response.*;
import com.marketplace.StoneRidgeMarketplace.service.AuthService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
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
import java.util.Map;

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
                                                .build());
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
                                httpResponse.addCookie(deviceCookie);
                        }

                        return ResponseEntity.ok(
                                        ApiResponse.<LoginResponse>builder()
                                                        .success(true)
                                                        .message("Login successful")
                                                        .data(response)
                                                        .build());

                } catch (Exception e) {
                        log.error("Login failed for: {}", request.getUsernameOrEmail(), e);
                        throw e;
                }
        }

        @PostMapping("/admin-login")
        @Operation(summary = "Admin login (authenticates against admins table)")
        public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
                log.info("Admin login attempt for: {}", request.getUsernameOrEmail());
                try {
                        LoginResponse response = authService.adminLogin(request);
                        return ResponseEntity.ok(
                                        ApiResponse.<LoginResponse>builder()
                                                        .success(true)
                                                        .message("Login successful")
                                                        .data(response)
                                                        .build());
                } catch (Exception e) {
                        log.error("Admin login failed for: {}", request.getUsernameOrEmail(), e);
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
                                                .build());
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
                                                .build());
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
                                                .build());
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
                                                .build());
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
                                                .build());
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
                                                .build());
        }

        @DeleteMapping("/account")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Delete user account")
        public ResponseEntity<ApiResponse<Void>> deleteAccount(
                        @AuthenticationPrincipal UserPrincipal principal) {

                log.info("User {} requesting account deletion", principal.getUsername());

                authService.deleteUserAccount(principal.getId());

                return ResponseEntity.ok(
                                ApiResponse.<Void>builder()
                                                .success(true)
                                                .message("Account deleted successfully")
                                                .build());
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
                                                .build());
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
                                                .build());
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
                                                .build());
        }

        @PutMapping("/profile")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update user profile")
        public ResponseEntity<ApiResponse<UserDto>> updateProfile(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @Valid @RequestBody ProfileUpdateRequest request) {

                UserDto updatedUser = authService.updateProfile(principal.getId(), request);

                return ResponseEntity.ok(
                                ApiResponse.<UserDto>builder()
                                                .success(true)
                                                .message("Profile updated successfully")
                                                .data(updatedUser)
                                                .build());
        }


}
