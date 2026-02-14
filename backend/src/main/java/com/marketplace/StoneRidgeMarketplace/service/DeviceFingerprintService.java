package com.marketplace.StoneRidgeMarketplace.service;

import jakarta.servlet.http.HttpServletRequest;

public interface DeviceFingerprintService {
    String generateFingerprint(HttpServletRequest request);
    String extractDeviceName(String userAgent);
    String detectDeviceType(String userAgent);
}
