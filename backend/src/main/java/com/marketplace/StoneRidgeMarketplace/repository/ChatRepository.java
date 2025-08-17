package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.Chat;
import com.marketplace.StoneRidgeMarketplace.entity.Product;
import com.marketplace.StoneRidgeMarketplace.entity.User;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ChatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    Optional<Chat> findByProductAndBuyerAndStatus(Product product, User buyer, ChatStatus status);
    
    @Query("SELECT c FROM Chat c WHERE (c.buyer.id = :userId OR c.seller.id = :userId) AND c.status = :status ORDER BY c.lastMessageAt DESC")
    Page<Chat> findUserChats(@Param("userId") Long userId, @Param("status") ChatStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Chat c WHERE c.product.id = :productId AND c.status = 'ACTIVE'")
    List<Chat> findActiveChatsForProduct(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.product.id = :productId AND c.status = 'ACTIVE'")
    Integer countActiveChatsForProduct(@Param("productId") Long productId);
    
    @Query("SELECT c FROM Chat c WHERE (c.buyer.id = :userId OR c.seller.id = :userId) AND " +
           "((c.buyer.id = :userId AND c.buyerLastReadAt < c.lastMessageAt) OR " +
           "(c.seller.id = :userId AND c.sellerLastReadAt < c.lastMessageAt))")
    List<Chat> findChatsWithUnreadMessages(@Param("userId") Long userId);
}
