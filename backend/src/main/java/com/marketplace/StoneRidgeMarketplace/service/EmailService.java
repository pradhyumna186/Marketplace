package com.marketplace.StoneRidgeMarketplace.service;

public interface EmailService {
    void sendVerificationEmail(String email, String fullName, String token);

    void sendSecurityAlert(String email, String message);

    void sendNewDeviceAlert(String email, String deviceName, String ipAddress);

    void sendPasswordResetEmail(String email, String fullName, String token);

    void resendVerificationEmail(String email, String fullName, String token);
}
