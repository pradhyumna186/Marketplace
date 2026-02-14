package com.marketplace.StoneRidgeMarketplace.security;

import com.marketplace.StoneRidgeMarketplace.entity.Admin;
import com.marketplace.StoneRidgeMarketplace.entity.User;
import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.access-token-expiration:900000}") // 15 minutes default
    private long accessTokenExpiration;

    @Value("${app.security.jwt.refresh-token-expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    /** Token for admin; subject is "admin:username" so UserDetailsService loads from admins table. */
    public String generateAccessTokenForAdmin(Admin admin) {
        return generateTokenForAdmin(admin.getUsername(), admin.getId(), accessTokenExpiration);
    }

    public String generateRefreshTokenForAdmin(Admin admin) {
        return generateTokenForAdmin(admin.getUsername(), admin.getId(), refreshTokenExpiration);
    }

    private String generateTokenForAdmin(String username, Long adminId, long expiration) {
        String subject = AdminPrincipal.ADMIN_PREFIX + username;
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", Role.ADMIN.name());
        claims.put("adminId", adminId);
        claims.put("username", username);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateToken(User user, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}
