package com.marketplace.StoneRidgeMarketplace.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class DeviceFingerprintServiceImpl implements DeviceFingerprintService {

    @Override
    public String generateFingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");
        String ipAddress = getClientIP(request);

        String fingerprintData = String.join("|",
                userAgent != null ? userAgent : "",
                acceptLanguage != null ? acceptLanguage : "",
                acceptEncoding != null ? acceptEncoding : "",
                ipAddress != null ? ipAddress : "");

        return hashString(fingerprintData);
    }

    @Override
    public String extractDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }

        // Simple device name extraction
        if (userAgent.contains("Windows")) {
            return "Windows PC";
        } else if (userAgent.contains("Mac")) {
            return "Mac";
        } else if (userAgent.contains("iPhone")) {
            return "iPhone";
        } else if (userAgent.contains("Android")) {
            return "Android Device";
        } else if (userAgent.contains("Linux")) {
            return "Linux PC";
        } else {
            return "Unknown Device";
        }
    }

    @Override
    public String detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "unknown";
        }

        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            return "mobile";
        } else if (userAgent.contains("Tablet")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf(input.hashCode());
        }
    }
}
