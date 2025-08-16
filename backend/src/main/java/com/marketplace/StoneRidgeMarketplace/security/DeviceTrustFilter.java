package com.marketplace.StoneRidgeMarketplace.security;

import com.marketplace.StoneRidgeMarketplace.service.DeviceFingerprintService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceTrustFilter extends OncePerRequestFilter {

    private final DeviceFingerprintService deviceFingerprintService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // Skip processing for public endpoints
        if (request.getRequestURI().startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract device token from cookie
        Cookie[] cookies = request.getCookies();
        String deviceToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("device_token".equals(cookie.getName())) {
                    deviceToken = cookie.getValue();
                    break;
                }
            }
        }

        // Generate device fingerprint
        String deviceFingerprint = deviceFingerprintService.generateFingerprint(request);

        // Store in request attributes for use in controllers/services
        request.setAttribute("device_token", deviceToken);
        request.setAttribute("device_fingerprint", deviceFingerprint);

        filterChain.doFilter(request, response);
    }
}
