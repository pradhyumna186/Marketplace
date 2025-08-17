package com.marketplace.StoneRidgeMarketplace.entity;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ChatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatStatus status = ChatStatus.ACTIVE;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Negotiation> negotiations = new ArrayList<>();

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "buyer_last_read_at")
    private LocalDateTime buyerLastReadAt;

    @Column(name = "seller_last_read_at")
    private LocalDateTime sellerLastReadAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean hasUnreadMessages(User user) {
        if (lastMessageAt == null) return false;
        
        LocalDateTime lastReadAt = user.getId().equals(buyer.getId()) ? 
            buyerLastReadAt : sellerLastReadAt;
            
        return lastReadAt == null || lastMessageAt.isAfter(lastReadAt);
    }

    public void markAsRead(User user) {
        if (user.getId().equals(buyer.getId())) {
            buyerLastReadAt = LocalDateTime.now();
        } else if (user.getId().equals(seller.getId())) {
            sellerLastReadAt = LocalDateTime.now();
        }
    }
}
