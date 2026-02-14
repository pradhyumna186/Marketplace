package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.Product;
import com.marketplace.StoneRidgeMarketplace.entity.User;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

       Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);

       Page<Product> findByCategoryIdAndStatusOrderByCreatedAtDesc(Long categoryId, ProductStatus status,
                     Pageable pageable);

       Page<Product> findBySellerAndStatusOrderByCreatedAtDesc(User seller, ProductStatus status, Pageable pageable);

       @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND " +
                     "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                     "ORDER BY p.createdAt DESC")
       Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

       @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND " +
                     "p.category.id = :categoryId AND " +
                     "p.price BETWEEN :minPrice AND :maxPrice " +
                     "ORDER BY p.createdAt DESC")
       Page<Product> findByCategoryAndPriceRange(@Param("categoryId") Long categoryId,
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice,
                     Pageable pageable);

       @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND " +
                     "p.seller.buildingName = :building " +
                     "ORDER BY p.createdAt DESC")
       Page<Product> findBySellerBuilding(@Param("building") String building, Pageable pageable);

       @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.status = 'ACTIVE'")
       Integer countActiveProductsBySeller(@Param("sellerId") Long sellerId);

       @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.viewCount DESC")
       List<Product> findMostViewedProducts(Pageable pageable);

       @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.createdAt >= :sevenDaysAgo ORDER BY p.createdAt DESC")
       List<Product> findRecentProducts(@Param("sevenDaysAgo") java.time.LocalDateTime sevenDaysAgo, Pageable pageable);

       Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

       long countByStatus(ProductStatus status);
}
