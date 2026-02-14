package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.CategoryRequestDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.CategoryDto;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
import com.marketplace.StoneRidgeMarketplace.service.CategoryService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Product category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all active categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryDto>>builder()
                        .success(true)
                        .data(categories)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        CategoryDto category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(
                ApiResponse.<CategoryDto>builder()
                        .success(true)
                        .data(category)
                        .build());
    }

    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Request new category creation")
    public ResponseEntity<ApiResponse<Void>> requestCategoryCreation(
            @Valid @RequestBody CategoryRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {

        categoryService.requestCategoryCreation(request, principal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Category creation request submitted for admin review")
                        .build());
    }

    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's category requests")
    public ResponseEntity<ApiResponse<List<CategoryRequestDto>>> getUserCategoryRequests(
            @AuthenticationPrincipal UserPrincipal principal) {

        List<CategoryRequestDto> requests = categoryService.getUserCategoryRequests(principal.getId());

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryRequestDto>>builder()
                        .success(true)
                        .data(requests)
                        .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories by keyword")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> searchCategories(
            @RequestParam String keyword) {

        List<CategoryDto> categories = categoryService.searchCategories(keyword);

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryDto>>builder()
                        .success(true)
                        .data(categories)
                        .build());
    }
}
