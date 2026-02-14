package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.response.AdminDashboardDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.AdminUserDto;
import com.marketplace.StoneRidgeMarketplace.entity.User;
import com.marketplace.StoneRidgeMarketplace.entity.enums.CategoryStatus;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.CategoryRepository;
import com.marketplace.StoneRidgeMarketplace.repository.CategoryRequestRepository;
import com.marketplace.StoneRidgeMarketplace.repository.ProductRepository;
import com.marketplace.StoneRidgeMarketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryRequestRepository categoryRequestRepository;

    public AdminDashboardDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByStatus(ProductStatus.ACTIVE);
        long totalCategories = categoryRepository.countByActiveTrue();
        long pendingCategoryRequests = categoryRequestRepository.countByStatus(CategoryStatus.PENDING);

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalCategories(totalCategories)
                .pendingCategoryRequests(pendingCategoryRequests)
                .build();
    }

    public Page<AdminUserDto> getUsers(String search, Pageable pageable) {
        Page<User> users = search != null && !search.isBlank()
                ? userRepository.searchByEmailOrUsername(search.trim(), pageable)
                : userRepository.findAll(pageable);

        return users.map(this::mapToAdminUserDto);
    }

    public void suspendUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User {} suspended by admin {}", user.getUsername(), adminId);
    }

    public void unsuspendUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User {} unsuspended by admin {}", user.getUsername(), adminId);
    }

    public void lockUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAccountNonLocked(false);
        userRepository.save(user);
        log.info("User {} locked by admin {}", user.getUsername(), adminId);
    }

    public void unlockUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
        log.info("User {} unlocked by admin {}", user.getUsername(), adminId);
    }

    private AdminUserDto mapToAdminUserDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getEffectiveDisplayName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
