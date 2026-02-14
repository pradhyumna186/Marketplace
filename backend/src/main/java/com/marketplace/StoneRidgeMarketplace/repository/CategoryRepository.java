package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByNameIgnoreCaseAndActiveTrue(String name);
    
    List<Category> findByActiveTrue();
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.name")
    List<Category> findRootCategories();
    
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true ORDER BY c.name")
    List<Category> findSubcategories(@Param("parentId") Long parentId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    Integer countActiveProductsByCategory(@Param("categoryId") Long categoryId);
    
    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    long countByActiveTrue();
}
