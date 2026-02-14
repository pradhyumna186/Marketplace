package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.AdminCategoryCreateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.request.AdminCategoryUpdateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.request.CategoryRequestDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.CategoryDto;
import com.marketplace.StoneRidgeMarketplace.entity.Admin;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.CategoryStatus;
import com.marketplace.StoneRidgeMarketplace.exception.DuplicateResourceException;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryRequestRepository categoryRequestRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    /**
     * Get all active categories in hierarchical structure
     */
    public List<CategoryDto> getAllCategories() {
        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return mapToCategoryDto(category);
    }

    /**
     * Request new category creation
     */
    public void requestCategoryCreation(CategoryRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if category already exists
        if (categoryRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Category already exists");
        }

        // Check if there's already a pending request for this category
        if (categoryRequestRepository.existsByNameIgnoreCaseAndStatus(request.getName(), CategoryStatus.PENDING)) {
            throw new DuplicateResourceException("Category request already pending approval");
        }

        // Check user's pending requests limit
        Integer pendingCount = categoryRequestRepository.countPendingRequestsByUser(userId);
        if (pendingCount >= 3) { // Limit to 3 pending requests per user
            throw new IllegalStateException("You have too many pending category requests. Please wait for approval.");
        }

        Category parentCategory = null;
        if (request.getParentCategoryId() != null) {
            parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .justification(request.getJustification())
                .parentCategory(parentCategory)
                .requestedBy(user)
                .status(CategoryStatus.PENDING)
                .build();

        categoryRequestRepository.save(categoryRequest);

        log.info("Category creation requested: {} by user: {}", request.getName(), user.getUsername());
    }

    /**
     * Get user's category requests
     */
    public List<CategoryRequestDto> getUserCategoryRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return categoryRequestRepository.findByRequestedByAndStatusOrderByCreatedAtDesc(user, CategoryStatus.PENDING)
                .stream()
                .map(this::mapToCategoryRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Get all pending category requests
     */
    public List<CategoryRequestDto> getPendingCategoryRequests() {
        return categoryRequestRepository.findPendingRequests()
                .stream()
                .map(this::mapToCategoryRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Approve category request
     */
    public CategoryDto approveCategoryRequest(Long requestId, Long adminId) {
        CategoryRequest request = categoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Category request not found"));

        Admin adminEntity = adminRepository.findById(adminId).orElse(null);
        User userAdmin = adminEntity == null ? userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found")) : null;

        if (request.getStatus() != CategoryStatus.PENDING) {
            throw new IllegalStateException("Category request is not pending");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Category already exists");
        }

        Category.CategoryBuilder categoryBuilder = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(request.getParentCategory())
                .createdBy(request.getRequestedBy())
                .active(true);
        if (adminEntity != null) {
            categoryBuilder.approvedByAdmin(adminEntity);
        } else {
            categoryBuilder.approvedBy(userAdmin);
        }
        Category category = categoryRepository.save(categoryBuilder.build());

        request.setStatus(CategoryStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        if (adminEntity != null) {
            request.setReviewedByAdmin(adminEntity);
        } else {
            request.setReviewedBy(userAdmin);
        }
        categoryRequestRepository.save(request);

        String adminName = adminEntity != null ? adminEntity.getUsername() : userAdmin.getUsername();
        log.info("Category approved: {} by admin: {}", category.getName(), adminName);

        return mapToCategoryDto(category);
    }

    /**
     * Admin: Reject category request
     */
    public void rejectCategoryRequest(Long requestId, Long adminId, String reviewNotes) {
        CategoryRequest request = categoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Category request not found"));

        Admin adminEntity = adminRepository.findById(adminId).orElse(null);
        User userAdmin = adminEntity == null ? userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found")) : null;

        if (request.getStatus() != CategoryStatus.PENDING) {
            throw new IllegalStateException("Category request is not pending");
        }

        request.setStatus(CategoryStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(reviewNotes);
        if (adminEntity != null) {
            request.setReviewedByAdmin(adminEntity);
        } else {
            request.setReviewedBy(userAdmin);
        }
        categoryRequestRepository.save(request);

        String adminName = adminEntity != null ? adminEntity.getUsername() : userAdmin.getUsername();
        log.info("Category rejected: {} by admin: {}", request.getName(), adminName);
    }

    /**
     * Admin: Create category directly (without user request)
     */
    public CategoryDto createCategoryByAdmin(AdminCategoryCreateRequest request, Long adminId) {
        Admin adminEntity = adminRepository.findById(adminId).orElse(null);
        User userAdmin = adminEntity == null ? userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found")) : null;

        if (categoryRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName().trim())) {
            throw new DuplicateResourceException("Category already exists");
        }

        Category parent = null;
        if (request.getParentCategoryId() != null) {
            parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        Category.CategoryBuilder b = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .parent(parent)
                .active(true);
        if (adminEntity != null) {
            b.createdByAdmin(adminEntity).approvedByAdmin(adminEntity);
        } else {
            b.createdBy(userAdmin).approvedBy(userAdmin);
        }
        Category category = categoryRepository.save(b.build());
        String adminName = adminEntity != null ? adminEntity.getUsername() : userAdmin.getUsername();
        log.info("Category {} created by admin: {}", category.getName(), adminName);
        return mapToCategoryDto(category);
    }

    /**
     * Admin: Update category name/description
     */
    public CategoryDto updateCategory(Long categoryId, AdminCategoryUpdateRequest request, Long adminId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!request.getName().trim().equalsIgnoreCase(category.getName())
                    && categoryRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName().trim())) {
                throw new DuplicateResourceException("Category name already exists");
            }
            category.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().trim());
        }

        category = categoryRepository.save(category);
        log.info("Category {} updated by admin: {}", category.getName(), adminId);
        return mapToCategoryDto(category);
    }

    /**
     * Admin: Deactivate category (soft delete; hides from listing)
     */
    public void deactivateCategory(Long categoryId, Long adminId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Category {} deactivated by admin: {}", category.getName(), adminId);
    }

    /**
     * Search categories by name
     */
    public List<CategoryDto> searchCategories(String keyword) {
        List<Category> categories = categoryRepository.findByActiveTrue();
        return categories.stream()
                .filter(category -> category.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    private CategoryDto mapToCategoryDto(Category category) {
        List<CategoryDto> subcategories = (category.getSubcategories() != null) ? category.getSubcategories().stream()
                .filter(Category::isActive)
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList()) : new ArrayList<>();

        Integer productCount = categoryRepository.countActiveProductsByCategory(category.getId());

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .fullPath(category.getFullPath())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .subcategories(subcategories)
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .active(category.isActive())
                .build();
    }

    private CategoryRequestDto mapToCategoryRequestDto(CategoryRequest request) {
        return CategoryRequestDto.builder()
                .id(request.getId())
                .name(request.getName())
                .description(request.getDescription())
                .justification(request.getJustification())
                .parentCategoryId(request.getParentCategory() != null ? request.getParentCategory().getId() : null)
                .parentCategoryName(request.getParentCategory() != null ? request.getParentCategory().getName() : null)
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .requestedById(request.getRequestedBy() != null ? request.getRequestedBy().getId() : null)
                .requestedByUsername(request.getRequestedBy() != null ? request.getRequestedBy().getUsername() : null)
                .createdAt(request.getCreatedAt())
                .build();
    }
}
