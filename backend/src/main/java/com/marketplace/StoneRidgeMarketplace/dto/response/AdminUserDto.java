package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String fullName;
    private Role role;
    private boolean enabled;
    private boolean accountNonLocked;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
