package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.CategoryRequest;
import com.marketplace.StoneRidgeMarketplace.entity.User;
import com.marketplace.StoneRidgeMarketplace.entity.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRequestRepository extends JpaRepository<CategoryRequest, Long> {
    
    Page<CategoryRequest> findByStatus(CategoryStatus status, Pageable pageable);
    
    List<CategoryRequest> findByRequestedByAndStatusOrderByCreatedAtDesc(User user, CategoryStatus status);
    
    @Query("SELECT cr FROM CategoryRequest cr WHERE cr.status = 'PENDING' ORDER BY cr.createdAt ASC")
    List<CategoryRequest> findPendingRequests();
    
    boolean existsByNameIgnoreCaseAndStatus(String name, CategoryStatus status);
    
    @Query("SELECT COUNT(cr) FROM CategoryRequest cr WHERE cr.requestedBy.id = :userId AND cr.status = 'PENDING'")
    Integer countPendingRequestsByUser(@Param("userId") Long userId);

    long countByStatus(CategoryStatus status);
}
