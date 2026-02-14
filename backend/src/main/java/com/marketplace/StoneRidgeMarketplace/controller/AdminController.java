package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.AdminCategoryCreateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.request.AdminCategoryUpdateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.request.CategoryRequestDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.AdminDashboardDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.AdminUserDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.CategoryDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductSummaryDto;
import com.marketplace.StoneRidgeMarketplace.security.PrincipalWithId;
import com.marketplace.StoneRidgeMarketplace.service.AdminService;
import com.marketplace.StoneRidgeMarketplace.service.CategoryService;
import com.marketplace.StoneRidgeMarketplace.service.ProductService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CategoryService categoryService;
    private final AdminService adminService;
    private final ProductService productService;

    // ---------- Dashboard ----------
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        AdminDashboardDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(
                ApiResponse.<AdminDashboardDto>builder()
                        .success(true)
                        .data(stats)
                        .build());
    }

    // ---------- User management ----------
    @GetMapping("/users")
    @Operation(summary = "List users (optional search by email/username)")
    public ResponseEntity<ApiResponse<Page<AdminUserDto>>> getUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdminUserDto> users = adminService.getUsers(search, pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<AdminUserDto>>builder()
                        .success(true)
                        .data(users)
                        .build());
    }

    @PutMapping("/users/{id}/suspend")
    @Operation(summary = "Suspend user (disable login)")
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalWithId principal) {
        adminService.suspendUser(id, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User suspended")
                        .build());
    }

    @PutMapping("/users/{id}/unsuspend")
    @Operation(summary = "Unsuspend user")
    public ResponseEntity<ApiResponse<Void>> unsuspendUser(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalWithId principal) {
        adminService.unsuspendUser(id, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User unsuspended")
                        .build());
    }

    @PutMapping("/users/{id}/lock")
    @Operation(summary = "Lock user account (e.g. after violations)")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalWithId principal) {
        adminService.lockUser(id, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User locked")
                        .build());
    }

    @PutMapping("/users/{id}/unlock")
    @Operation(summary = "Unlock user account")
    public ResponseEntity<ApiResponse<Void>> unlockUser(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalWithId principal) {
        adminService.unlockUser(id, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User unlocked")
                        .build());
    }

    // ---------- Listing moderation ----------
    @GetMapping("/products")
    @Operation(summary = "List all products (any status) for moderation")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getAllProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductSummaryDto> products = productService.getAllProductsForAdmin(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @PutMapping("/products/{id}/deactivate")
    @Operation(summary = "Deactivate product (hide from listing)")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable Long id) {
        productService.adminDeactivateProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Product deactivated")
                        .build());
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete product (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Product deleted")
                        .build());
    }

    // ---------- Category requests (existing) ----------
    @GetMapping("/category-requests/pending")
    @Operation(summary = "Get pending category requests")
    public ResponseEntity<ApiResponse<List<CategoryRequestDto>>> getPendingCategoryRequests() {
        List<CategoryRequestDto> requests = categoryService.getPendingCategoryRequests();
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryRequestDto>>builder()
                        .success(true)
                        .data(requests)
                        .build());
    }

    @PostMapping("/category-requests/{id}/approve")
    @Operation(summary = "Approve category request")
    public ResponseEntity<ApiResponse<CategoryDto>> approveCategoryRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalWithId principal) {
        CategoryDto category = categoryService.approveCategoryRequest(id, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.<CategoryDto>builder()
                        .success(true)
                        .message("Category request approved")
                        .data(category)
                        .build());
    }

    @PostMapping("/category-requests/{id}/reject")
    @Operation(summary = "Reject category request")
    public ResponseEntity<ApiResponse<Void>> rejectCategoryRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reviewNotes,
            @AuthenticationPrincipal PrincipalWithId principal) {
        categoryService.rejectCategoryRequest(id, principal.getId(), reviewNotes != null ? reviewNotes : "");
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Category request rejected")
                        .build());
    }

    // ---------- Category management (direct) ----------
    @PostMapping("/categories")
    @Operation(summary = "Create category directly")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody AdminCategoryCreateRequest request,
            @AuthenticationPrincipal PrincipalWithId principal) {
        CategoryDto category = categoryService.createCategoryByAdmin(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CategoryDto>builder()
                        .success(true)
                        .message("Category created")
                        .data(category)
                        .build());
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryUpdateRequest request,
            @AuthenticationPrincipal PrincipalWithId principal) {
        CategoryDto category = categoryService.updateCategory(id, request, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.<CategoryDto>builder()
                        .success(true)
                        .message("Category updated")
                        .data(category)
                        .build());
    }

    @PutMapping("/categories/{id}/deactivate")
    @Operation(summary = "Deactivate category (hide from listing)")
    public ResponseEntity<ApiResponse<Void>> deactivateCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalWithId principal) {
        categoryService.deactivateCategory(id, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Category deactivated")
                        .build());
    }
}
