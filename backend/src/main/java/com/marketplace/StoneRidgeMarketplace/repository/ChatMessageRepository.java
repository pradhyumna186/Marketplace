package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chat.id = :chatId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatIdOrderByCreatedAtAsc(@Param("chatId") Long chatId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chat.id = :chatId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessages(@Param("chatId") Long chatId, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chat.id = :chatId ORDER BY cm.createdAt DESC LIMIT 1")
    ChatMessage findLastMessageByChat(@Param("chatId") Long chatId);
}
