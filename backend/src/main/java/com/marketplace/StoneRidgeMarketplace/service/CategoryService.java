package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.CategoryRequestDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.CategoryDto;
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

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (request.getStatus() != CategoryStatus.PENDING) {
            throw new IllegalStateException("Category request is not pending");
        }

        // Check if category name is still available
        if (categoryRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Category already exists");
        }

        // Create the category
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(request.getParentCategory())
                .createdBy(request.getRequestedBy())
                .approvedBy(admin)
                .active(true)
                .build();

        category = categoryRepository.save(category);

        // Update request status
        request.setStatus(CategoryStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        categoryRequestRepository.save(request);

        log.info("Category approved: {} by admin: {}", category.getName(), admin.getUsername());

        return mapToCategoryDto(category);
    }

    /**
     * Admin: Reject category request
     */
    public void rejectCategoryRequest(Long requestId, Long adminId, String reviewNotes) {
        CategoryRequest request = categoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Category request not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (request.getStatus() != CategoryStatus.PENDING) {
            throw new IllegalStateException("Category request is not pending");
        }

        request.setStatus(CategoryStatus.REJECTED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(reviewNotes);
        categoryRequestRepository.save(request);

        log.info("Category rejected: {} by admin: {}", request.getName(), admin.getUsername());
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
                .name(request.getName())
                .description(request.getDescription())
                .justification(request.getJustification())
                .parentCategoryId(request.getParentCategory() != null ? request.getParentCategory().getId() : null)
                .build();
    }
}
