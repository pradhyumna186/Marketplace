package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String displayName;
    private String fullName;
    private String username;
    private String email;
    private Role role;
    private String apartmentNumber;
    private String buildingName;
    private String phoneNumber;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
