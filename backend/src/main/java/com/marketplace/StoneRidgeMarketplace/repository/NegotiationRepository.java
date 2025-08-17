package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.Negotiation;
import com.marketplace.StoneRidgeMarketplace.entity.enums.NegotiationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {
    
    List<Negotiation> findByChatIdOrderByCreatedAtDesc(Long chatId);
    
    @Query("SELECT n FROM Negotiation n WHERE n.chat.id = :chatId AND n.status = 'PENDING' AND n.expiresAt > :now")
    List<Negotiation> findActivePendingOffers(@Param("chatId") Long chatId, @Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Negotiation n WHERE n.expiresAt < :now AND n.status = 'PENDING'")
    List<Negotiation> findExpiredOffers(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Negotiation n WHERE n.chat.seller.id = :sellerId AND n.status = 'PENDING' AND n.expiresAt > :now")
    List<Negotiation> findPendingOffersForSeller(@Param("sellerId") Long sellerId, @Param("now") LocalDateTime now);
}
