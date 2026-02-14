package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.ProductCreateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.request.ProductUpdateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductSummaryDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductImageDto;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    /**
     * Create new product listing
     */
    public ProductDto createProduct(ProductCreateRequest request, Long userId) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .condition(request.getCondition())
                .category(category)
                .seller(seller)
                .negotiable(request.isNegotiable())
                .locationDetails(request.getLocationDetails())
                .status(ProductStatus.ACTIVE)
                .viewCount(0)
                .build();

        product = productRepository.save(product);

        // Handle images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            saveProductImages(product, request.getImageUrls());
        }

        log.info("Product created: {} by user: {}", product.getTitle(), seller.getUsername());

        return mapToProductDto(product, userId);
    }

    /**
     * Get product by ID with view count increment
     */
    public ProductDto getProductById(Long productId, Long currentUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Increment view count (except for owner views)
        if (currentUserId == null || !product.isOwnedBy(userRepository.findById(currentUserId).orElse(null))) {
            product.incrementViewCount();
            productRepository.save(product);
        }

        return mapToProductDto(product, currentUserId);
    }

    /**
     * Get all active products with pagination
     */
    public Page<ProductSummaryDto> getAllProducts(Pageable pageable, Long currentUserId) {
        Page<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE, pageable);
        return products.map(product -> mapToProductSummaryDto(product, currentUserId));
    }

    /**
     * Get products by category
     */
    public Page<ProductSummaryDto> getProductsByCategory(Long categoryId, Pageable pageable, Long currentUserId) {
        Page<Product> products = productRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(
                categoryId, ProductStatus.ACTIVE, pageable);
        return products.map(product -> mapToProductSummaryDto(product, currentUserId));
    }

    /**
     * Search products by keyword
     */
    public Page<ProductSummaryDto> searchProducts(String keyword, Pageable pageable, Long currentUserId) {
        Page<Product> products = productRepository.searchProducts(keyword, pageable);
        return products.map(product -> mapToProductSummaryDto(product, currentUserId));
    }

    /**
     * Get products by price range and category
     */
    public Page<ProductSummaryDto> getProductsByFilters(Long categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, Pageable pageable, Long currentUserId) {
        Page<Product> products = productRepository.findByCategoryAndPriceRange(
                categoryId, minPrice, maxPrice, pageable);
        return products.map(product -> mapToProductSummaryDto(product, currentUserId));
    }

    /**
     * Get products from same building
     */
    public Page<ProductSummaryDto> getProductsByBuilding(String building, Pageable pageable, Long currentUserId) {
        Page<Product> products = productRepository.findBySellerBuilding(building, pageable);
        return products.map(product -> mapToProductSummaryDto(product, currentUserId));
    }

    /**
     * Get user's products
     */
    public Page<ProductSummaryDto> getUserProducts(Long userId, ProductStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Product> products = productRepository.findBySellerAndStatusOrderByCreatedAtDesc(user, status, pageable);
        return products.map(product -> mapToProductSummaryDto(product, userId));
    }

    /**
     * Update product
     */
    public ProductDto updateProduct(Long productId, ProductUpdateRequest request, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!product.isOwnedBy(user)) {
            throw new IllegalStateException("You can only update your own products");
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            product.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getCondition() != null) {
            product.setCondition(request.getCondition());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
        if (request.getNegotiable() != null) {
            product.setNegotiable(request.getNegotiable());
        }
        if (request.getLocationDetails() != null) {
            product.setLocationDetails(request.getLocationDetails());
        }

        // Handle image updates
        if (request.getImageUrls() != null) {
            // Delete existing images
            productImageRepository.deleteByProductId(productId);
            // Save new images
            saveProductImages(product, request.getImageUrls());
        }

        product = productRepository.save(product);

        log.info("Product updated: {} by user: {}", product.getTitle(), user.getUsername());

        return mapToProductDto(product, userId);
    }

    /**
     * Mark product as sold
     */
    public void markProductAsSold(Long productId, Long buyerId, BigDecimal soldPrice, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

        if (!product.isOwnedBy(seller)) {
            throw new IllegalStateException("Only the seller can mark the product as sold");
        }

        product.setStatus(ProductStatus.SOLD);
        product.setBuyer(buyer);
        product.setSoldAt(LocalDateTime.now());
        product.setSoldPrice(soldPrice);

        productRepository.save(product);

        log.info("Product marked as sold: {} to buyer: {}", product.getTitle(), buyer.getUsername());
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!product.isOwnedBy(user)) {
            throw new IllegalStateException("You can only delete your own products");
        }

        productRepository.delete(product);

        log.info("Product deleted: {} by user: {}", product.getTitle(), user.getUsername());
    }

    /**
     * Admin: Get all products (any status) for moderation
     */
    public Page<ProductSummaryDto> getAllProductsForAdmin(Pageable pageable) {
        Page<Product> products = productRepository.findAllByOrderByCreatedAtDesc(pageable);
        return products.map(product -> mapToProductSummaryDto(product, null));
    }

    /**
     * Admin: Deactivate a product (hide from listing)
     */
    public void adminDeactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
        log.info("Product {} deactivated by admin", product.getTitle());
    }

    /**
     * Admin: Delete any product
     */
    public void adminDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productRepository.delete(product);
        log.info("Product {} deleted by admin", product.getTitle());
    }

    /**
     * Get most viewed products
     */
    public List<ProductSummaryDto> getMostViewedProducts(int limit, Long currentUserId) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findMostViewedProducts(pageable);
        return products.stream()
                .map(product -> mapToProductSummaryDto(product, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Get recent products
     */
    public List<ProductSummaryDto> getRecentProducts(int limit, Long currentUserId) {
        Pageable pageable = PageRequest.of(0, limit);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Product> products = productRepository.findRecentProducts(sevenDaysAgo, pageable);
        return products.stream()
                .map(product -> mapToProductSummaryDto(product, currentUserId))
                .collect(Collectors.toList());
    }

    private void saveProductImages(Product product, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrls.get(i))
                    .fileName("image_" + (i + 1))
                    .primary(i == 0) // First image is primary
                    .displayOrder(i + 1)
                    .build();
            productImageRepository.save(image);
        }
    }

    private ProductDto mapToProductDto(Product product, Long currentUserId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
        Integer activeChatsCount = chatRepository.countActiveChatsForProduct(product.getId());

        boolean isOwner = currentUserId != null && product.getSeller().getId().equals(currentUserId);
        boolean canEdit = isOwner && product.getStatus() == ProductStatus.ACTIVE;
        boolean canChat = currentUserId != null && !isOwner && product.getStatus() == ProductStatus.ACTIVE;

        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .condition(product.getCondition())
                .status(product.getStatus())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .categoryPath(product.getCategory().getFullPath())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getFullName())
                .sellerDisplayName(product.getSeller().getEffectiveDisplayName())
                .sellerBuilding(product.getSeller().getBuildingName() != null ? product.getSeller().getBuildingName() : "—")
                .sellerApartment(product.getSeller().getApartmentNumber() != null ? product.getSeller().getApartmentNumber() : "—")
                .buyerId(product.getBuyer() != null ? product.getBuyer().getId() : null)
                .buyerName(product.getBuyer() != null ? product.getBuyer().getFullName() : null)
                .images(images.stream().map(this::mapToProductImageDto).collect(Collectors.toList()))
                .viewCount(product.getViewCount())
                .negotiable(product.isNegotiable())
                .locationDetails(product.getLocationDetails())
                .activeChatsCount(activeChatsCount)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .soldAt(product.getSoldAt())
                .soldPrice(product.getSoldPrice())
                .isOwner(isOwner)
                .canEdit(canEdit)
                .canChat(canChat)
                .build();
    }

    private ProductSummaryDto mapToProductSummaryDto(Product product, Long currentUserId) {
        ProductImage primaryImage = productImageRepository.findByProductIdAndPrimaryTrue(product.getId()).orElse(null);
        boolean isOwner = currentUserId != null && product.getSeller().getId().equals(currentUserId);

        return ProductSummaryDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .condition(product.getCondition())
                .status(product.getStatus())
                .primaryImageUrl(primaryImage != null ? primaryImage.getImageUrl() : null)
                .categoryName(product.getCategory().getName())
                .sellerName(product.getSeller().getEffectiveDisplayName())
                .sellerBuilding(product.getSeller().getBuildingName() != null ? product.getSeller().getBuildingName() : "—")
                .viewCount(product.getViewCount())
                .negotiable(product.isNegotiable())
                .createdAt(product.getCreatedAt())
                .isOwner(isOwner)
                .build();
    }

    private ProductImageDto mapToProductImageDto(ProductImage image) {
        return ProductImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .fileName(image.getFileName())
                .primary(image.isPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
