package com.marketplace.StoneRidgeMarketplace.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String email, String fullName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Verify Your StoneRidge Marketplace Account");

            String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;

            String emailContent = String.format(
                    "Hello %s,\n\n" +
                            "Welcome to StoneRidge Marketplace! Please verify your email address by clicking the link below:\n\n"
                            +
                            "%s\n\n" +
                            "This link will expire in 24 hours.\n\n" +
                            "If you didn't create this account, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "StoneRidge Marketplace Team",
                    fullName, verificationUrl);

            message.setText(emailContent);

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            // Don't throw exception - we don't want to fail registration if email fails
        }
    }

    @Override
    public void sendSecurityAlert(String email, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(email);
            mailMessage.setSubject("Security Alert - StoneRidge Marketplace");
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            log.info("Security alert email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send security alert email to: {}", email, e);
        }
    }

    @Override
    public void sendNewDeviceAlert(String email, String deviceName, String ipAddress) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(email);
            mailMessage.setSubject("New Device Login - StoneRidge Marketplace");

            String messageContent = String.format(
                    "Hello,\n\n" +
                            "A new device has logged into your StoneRidge Marketplace account:\n\n" +
                            "Device: %s\n" +
                            "IP Address: %s\n" +
                            "Time: %s\n\n" +
                            "If this wasn't you, please contact support immediately.\n\n" +
                            "Best regards,\n" +
                            "StoneRidge Marketplace Team",
                    deviceName, ipAddress, java.time.LocalDateTime.now());

            mailMessage.setText(messageContent);

            mailSender.send(mailMessage);
            log.info("New device alert email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send new device alert email to: {}", email, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String fullName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Password Reset - StoneRidge Marketplace");

            String resetUrl = "http://localhost:8080/api/auth/reset-password?token=" + token;

            String emailContent = String.format(
                    "Hello %s,\n\n" +
                            "You requested a password reset for your StoneRidge Marketplace account.\n\n" +
                            "Click the link below to reset your password:\n\n" +
                            "%s\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you didn't request this reset, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "StoneRidge Marketplace Team",
                    fullName, resetUrl);

            message.setText(emailContent);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
        }
    }

    @Override
    public void resendVerificationEmail(String email, String fullName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Email Verification - StoneRidge Marketplace");

            String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;

            String emailContent = String.format(
                    "Hello %s,\n\n" +
                            "You requested to resend the email verification for your StoneRidge Marketplace account.\n\n"
                            +
                            "Please verify your email address by clicking the link below:\n\n" +
                            "%s\n\n" +
                            "This link will expire in 24 hours.\n\n" +
                            "If you didn't request this, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "StoneRidge Marketplace Team",
                    fullName, verificationUrl);

            message.setText(emailContent);

            mailSender.send(message);
            log.info("Resend verification email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send resend verification email to: {}", email, e);
        }
    }
}
