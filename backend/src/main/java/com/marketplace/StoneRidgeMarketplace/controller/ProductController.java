package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.ProductCreateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.request.ProductUpdateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductSummaryDto;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create new product listing")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ProductDto product = productService.createProduct(request, principal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductDto>builder()
                        .success(true)
                        .message("Product created successfully")
                        .data(product)
                        .build());
    }

    @GetMapping
    @Operation(summary = "Get all active products with pagination")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getAllProducts(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        Page<ProductSummaryDto> products = productService.getAllProducts(pageable, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<Page<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        ProductDto product = productService.getProductById(id, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<ProductDto>builder()
                        .success(true)
                        .data(product)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ProductDto product = productService.updateProduct(id, request, principal.getId());

        return ResponseEntity.ok(
                ApiResponse.<ProductDto>builder()
                        .success(true)
                        .message("Product updated successfully")
                        .data(product)
                        .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        productService.deleteProduct(id, principal.getId());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .build());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        Page<ProductSummaryDto> products = productService.getProductsByCategory(categoryId, pageable, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<Page<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        Page<ProductSummaryDto> products = productService.searchProducts(keyword, pageable, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<Page<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter products by category and price range")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> filterProducts(
            @RequestParam Long categoryId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        Page<ProductSummaryDto> products = productService.getProductsByFilters(
                categoryId, minPrice, maxPrice, pageable, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<Page<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @GetMapping("/building/{building}")
    @Operation(summary = "Get products from same building")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getProductsByBuilding(
            @PathVariable String building,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        Page<ProductSummaryDto> products = productService.getProductsByBuilding(building, pageable, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<Page<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @GetMapping("/my-products")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's products")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getUserProducts(
            @RequestParam(defaultValue = "ACTIVE") ProductStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        Page<ProductSummaryDto> products = productService.getUserProducts(principal.getId(), status, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @PostMapping("/{id}/mark-sold")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark product as sold")
    public ResponseEntity<ApiResponse<Void>> markProductAsSold(
            @PathVariable Long id,
            @RequestParam Long buyerId,
            @RequestParam BigDecimal soldPrice,
            @AuthenticationPrincipal UserPrincipal principal) {

        productService.markProductAsSold(id, buyerId, soldPrice, principal.getId());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Product marked as sold")
                        .build());
    }

    @GetMapping("/trending")
    @Operation(summary = "Get most viewed products")
    public ResponseEntity<ApiResponse<List<ProductSummaryDto>>> getMostViewedProducts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        List<ProductSummaryDto> products = productService.getMostViewedProducts(limit, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<List<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recently posted products")
    public ResponseEntity<ApiResponse<List<ProductSummaryDto>>> getRecentProducts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = principal != null ? principal.getId() : null;
        List<ProductSummaryDto> products = productService.getRecentProducts(limit, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.<List<ProductSummaryDto>>builder()
                        .success(true)
                        .data(products)
                        .build());
    }
}
