package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.CategoryRequestDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.CategoryDto;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
import com.marketplace.StoneRidgeMarketplace.service.CategoryService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    @GetMapping("/category-requests/pending")
    @Operation(summary = "Get pending category requests")
    public ResponseEntity<ApiResponse<List<CategoryRequestDto>>> getPendingCategoryRequests() {
        
        List<CategoryRequestDto> requests = categoryService.getPendingCategoryRequests();
        
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryRequestDto>>builder()
                        .success(true)
                        .data(requests)
                        .build()
        );
    }
    
    @PostMapping("/category-requests/{id}/approve")
    @Operation(summary = "Approve category request")
    public ResponseEntity<ApiResponse<CategoryDto>> approveCategoryRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        CategoryDto category = categoryService.approveCategoryRequest(id, principal.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<CategoryDto>builder()
                        .success(true)
                        .message("Category request approved")
                        .data(category)
                        .build()
        );
    }
    
    @PostMapping("/category-requests/{id}/reject")
    @Operation(summary = "Reject category request")
    public ResponseEntity<ApiResponse<Void>> rejectCategoryRequest(
            @PathVariable Long id,
            @RequestParam String reviewNotes,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        categoryService.rejectCategoryRequest(id, principal.getId(), reviewNotes);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Category request rejected")
                        .build()
        );
    }
}
