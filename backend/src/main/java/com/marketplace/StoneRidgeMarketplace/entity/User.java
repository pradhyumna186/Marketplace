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

    // Stoneridge specific fields (null for admin/system accounts that don't have an address)
    @Column(name = "apartment_number")
    private String apartmentNumber;

    @Column(name = "building_name")
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