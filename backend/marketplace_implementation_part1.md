# StoneRidge Marketplace - Product & Chat System Implementation

## 📁 Updated Package Structure
```
com.marketplace.StoneRidgeMarketplace/
├── entity/
│   ├── User.java (existing)
│   ├── TrustedDevice.java (existing)
│   ├── Category.java (new)
│   ├── CategoryRequest.java (new)
│   ├── Product.java (new)
│   ├── ProductImage.java (new)
│   ├── Chat.java (new)
│   ├── ChatMessage.java (new)
│   ├── Negotiation.java (new)
│   └── enums/
│       ├── Role.java (existing)
│       ├── CategoryStatus.java (new)
│       ├── ProductStatus.java (new)
│       ├── ProductCondition.java (new)
│       ├── ChatStatus.java (new)
│       └── NegotiationStatus.java (new)
├── dto/
│   ├── request/
│   │   ├── CategoryRequestDto.java (new)
│   │   ├── ProductCreateRequest.java (new)
│   │   ├── ProductUpdateRequest.java (new)
│   │   ├── ChatMessageRequest.java (new)
│   │   └── NegotiationRequest.java (new)
│   └── response/
│       ├── CategoryDto.java (new)
│       ├── ProductDto.java (new)
│       ├── ProductSummaryDto.java (new)
│       ├── ChatDto.java (new)
│       ├── ChatMessageDto.java (new)
│       └── NegotiationDto.java (new)
├── service/
│   ├── CategoryService.java (new)
│   ├── ProductService.java (new)
│   ├── ChatService.java (new)
│   ├── NegotiationService.java (new)
│   └── FileStorageService.java (new)
├── controller/
│   ├── CategoryController.java (new)
│   ├── ProductController.java (new)
│   ├── ChatController.java (new)
│   └── AdminController.java (new)
├── repository/
│   ├── CategoryRepository.java (new)
│   ├── CategoryRequestRepository.java (new)
│   ├── ProductRepository.java (new)
│   ├── ProductImageRepository.java (new)
│   ├── ChatRepository.java (new)
│   ├── ChatMessageRepository.java (new)
│   └── NegotiationRepository.java (new)
└── websocket/
    ├── ChatWebSocketHandler.java (new)
    └── config/
        └── WebSocketConfig.java (new)
```

## 🗃️ Database Entities

### 1. Enums

#### CategoryStatus.java
```java
package com.marketplace.StoneRidgeMarketplace.entity.enums;

public enum CategoryStatus {
    PENDING,
    APPROVED,
    REJECTED
}
```

#### ProductStatus.java
```java
package com.marketplace.StoneRidgeMarketplace.entity.enums;

public enum ProductStatus {
    ACTIVE,
    SOLD,
    INACTIVE,
    PENDING_APPROVAL
}
```

#### ProductCondition.java
```java
package com.marketplace.StoneRidgeMarketplace.entity.enums;

public enum ProductCondition {
    NEW,
    LIKE_NEW,
    GOOD,
    FAIR,
    POOR
}
```

#### ChatStatus.java
```java
package com.marketplace.StoneRidgeMarketplace.entity.enums;

public enum ChatStatus {
    ACTIVE,
    CLOSED,
    ARCHIVED
}
```

#### NegotiationStatus.java
```java
package com.marketplace.StoneRidgeMarketplace.entity.enums;

public enum NegotiationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COUNTER_OFFERED,
    FINALIZED
}
```

### 2. Core Entities

#### Category.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Category> subcategories = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Product> products = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    // Helper method to get full category path
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }
}
```

#### CategoryRequest.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import com.marketplace.StoneRidgeMarketplace.entity.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "category_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String justification; // Why this category is needed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status = CategoryStatus.PENDING;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### Product.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer; // Set when sold

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<Chat> chats = new HashSet<>();

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "is_negotiable")
    private boolean negotiable = true;

    @Column(name = "location_details")
    private String locationDetails; // Apartment pickup details

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @Column(name = "sold_price", precision = 10, scale = 2)
    private BigDecimal soldPrice;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isSold() {
        return status == ProductStatus.SOLD;
    }

    public boolean isOwnedBy(User user) {
        return seller.getId().equals(user.getId());
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }
}
```

#### ProductImage.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_primary")
    private boolean primary = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### Chat.java
```java
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
```

#### ChatMessage.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(length = 2000, nullable = false)
    private String content;

    @Column(name = "message_type")
    private String messageType = "text"; // text, image, offer, system

    @Column(name = "is_system_message")
    private boolean systemMessage = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean isSentBy(User user) {
        return sender.getId().equals(user.getId());
    }
}
```

#### Negotiation.java
```java
package com.marketplace.StoneRidgeMarketplace.entity;

import com.marketplace.StoneRidgeMarketplace.entity.enums.NegotiationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiations")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negotiation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_by", nullable = false)
    private User offeredBy;

    @Column(name = "offered_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal offeredPrice;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NegotiationStatus status = NegotiationStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == NegotiationStatus.PENDING && !isExpired();
    }
}
```

## 📝 DTOs (Data Transfer Objects)

### Request DTOs

#### CategoryRequestDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDto {
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 1000, message = "Justification must not exceed 1000 characters")
    private String justification;
    
    private Long parentCategoryId; // Optional for subcategories
}
```

#### ProductCreateRequest.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.request;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    
    @NotBlank(message = "Product title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;
    
    @NotBlank(message = "Product description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    private BigDecimal price;
    
    @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Original price must be less than 1,000,000")
    private BigDecimal originalPrice;
    
    @NotNull(message = "Product condition is required")
    private ProductCondition condition;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    private boolean negotiable = true;
    
    @Size(max = 200, message = "Location details must not exceed 200 characters")
    private String locationDetails;
    
    private List<String> imageUrls; // URLs of uploaded images
}
```

#### ProductUpdateRequest.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.request;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {
    
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;
    
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;
    
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    private BigDecimal price;
    
    @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Original price must be less than 1,000,000")
    private BigDecimal originalPrice;
    
    private ProductCondition condition;
    
    private ProductStatus status;
    
    private Long categoryId;
    
    private Boolean negotiable;
    
    @Size(max = 200, message = "Location details must not exceed 200 characters")
    private String locationDetails;
    
    private List<String> imageUrls;
}
```

#### ChatMessageRequest.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    
    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
    private String content;
    
    private String messageType = "text"; // text, image, offer
}
```

#### NegotiationRequest.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationRequest {
    
    @NotNull(message = "Offered price is required")
    @DecimalMin(value = "0.01", message = "Offered price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Offered price must be less than 1,000,000")
    private BigDecimal offeredPrice;
    
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
    
    private Integer validityHours = 24; // How long the offer is valid
}
```

### Response DTOs

#### CategoryDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String fullPath;
    private Long parentId;
    private String parentName;
    private List<CategoryDto> subcategories;
    private Integer productCount;
    private LocalDateTime createdAt;
    private boolean active;
}
```

#### ProductDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private ProductCondition condition;
    private ProductStatus status;
    
    // Category info
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
    
    // Seller info
    private Long sellerId;
    private String sellerName;
    private String sellerDisplayName;
    private String sellerBuilding;
    private String sellerApartment;
    
    // Buyer info (if sold)
    private Long buyerId;
    private String buyerName;
    
    // Images
    private List<ProductImageDto> images;
    
    // Interaction data
    private Integer viewCount;
    private boolean negotiable;
    private String locationDetails;
    private Integer activeChatsCount;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime soldAt;
    private BigDecimal soldPrice;
    
    // Helper flags for frontend
    private boolean isOwner;
    private boolean canEdit;
    private boolean canChat;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductImageDto {
    private Long id;
    private String imageUrl;
    private String fileName;
    private boolean primary;
    private Integer displayOrder;
}
```

#### ProductSummaryDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductCondition;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private ProductCondition condition;
    private ProductStatus status;
    private String primaryImageUrl;
    private String categoryName;
    private String sellerName;
    private String sellerBuilding;
    private Integer viewCount;
    private boolean negotiable;
    private LocalDateTime createdAt;
    private boolean isOwner;
}
```

#### ChatDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.ChatStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDto {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productImageUrl;
    private Long buyerId;
    private String buyerName;
    private String buyerDisplayName;
    private Long sellerId;
    private String sellerName;
    private String sellerDisplayName;
    private ChatStatus status;
    private boolean hasUnreadMessages;
    private ChatMessageDto lastMessage;
    private List<ChatMessageDto> recentMessages;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
}
```

#### ChatMessageDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String senderDisplayName;
    private String content;
    private String messageType;
    private boolean systemMessage;
    private LocalDateTime createdAt;
    private boolean sentByCurrentUser;
}
```

#### NegotiationDto.java
```java
package com.marketplace.StoneRidgeMarketplace.dto.response;

import com.marketplace.StoneRidgeMarketplace.entity.enums.NegotiationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationDto {
    private Long id;
    private Long chatId;
    private Long offeredById;
    private String offeredByName;
    private BigDecimal offeredPrice;
    private BigDecimal originalPrice;
    private String message;
    private NegotiationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private boolean isExpired;
    private boolean canRespond;
    private boolean isOwnOffer;
}
```

## 🏪 Repositories

#### CategoryRepository.java
```java
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
}
```

#### CategoryRequestRepository.java
```java
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
}
```

#### ProductRepository.java
```java
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
    
    Page<Product> findByCategoryIdAndStatusOrderByCreatedAtDesc(Long categoryId, ProductStatus status, Pageable pageable);
    
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
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.createdAt >= CURRENT_DATE - 7 ORDER BY p.createdAt DESC")
    List<Product> findRecentProducts(Pageable pageable);
}
```

#### ProductImageRepository.java
```java
package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);
    
    Optional<ProductImage> findByProductIdAndPrimaryTrue(Long productId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.displayOrder ASC")
    List<ProductImage> findProductImages(@Param("productId") Long productId);
    
    void deleteByProductId(Long productId);
}
```

#### ChatRepository.java
```java
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
```

#### ChatMessageRepository.java
```java
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
```

#### NegotiationRepository.java
```java
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
```

## 🔧 Services

#### CategoryService.java
```java
package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.CategoryRequestDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.CategoryDto;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.CategoryStatus;
import com.marketplace.StoneRidgeMarketplace.exception.DuplicateResourceException;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryRequestRepository categoryRequestRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all active categories in hierarchical structure
     */
    public List<CategoryDto> getAllCategories() {
        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category by ID
     */
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return mapToCategoryDto(category);
    }
    
    /**
     * Request new category creation
     */
    public void requestCategoryCreation(CategoryRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if category already exists
        if (categoryRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Category already exists");
        }
        
        // Check if there's already a pending request for this category
        if (categoryRequestRepository.existsByNameIgnoreCaseAndStatus(request.getName(), CategoryStatus.PENDING)) {
            throw new DuplicateResourceException("Category request already pending approval");
        }
        
        // Check user's pending requests limit
        Integer pendingCount = categoryRequestRepository.countPendingRequestsByUser(userId);
        if (pendingCount >= 3) { // Limit to 3 pending requests per user
            throw new IllegalStateException("You have too many pending category requests. Please wait for approval.");
        }
        
        Category parentCategory = null;
        if (request.getParentCategoryId() != null) {
            parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }
        
        CategoryRequest categoryRequest = CategoryRequest.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .justification(request.getJustification())
                .parentCategory(parentCategory)
                .requestedBy(user)
                .status(CategoryStatus.PENDING)
                .build();
        
        categoryRequestRepository.save(categoryRequest);
        
        log.info("Category creation requested: {} by user: {}", request.getName(), user.getUsername());
    }
    
    /**
     * Get user's category requests
     */
    public List<CategoryRequestDto> getUserCategoryRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return categoryRequestRepository.findByRequestedByAndStatusOrderByCreatedAtDesc(user, CategoryStatus.PENDING)
                .stream()
                .map(this::mapToCategoryRequestDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Admin: Get all pending category requests
     */
    public List<CategoryRequestDto> getPendingCategoryRequests() {
        return categoryRequestRepository.findPendingRequests()
                .stream()
                .map(this::mapToCategoryRequestDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Admin: Approve category request
     */
    public CategoryDto approveCategoryRequest(Long requestId, Long adminId) {
        CategoryRequest request = categoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Category request not found"));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        
        if (request.getStatus() != CategoryStatus.PENDING) {
            throw new IllegalStateException("Category request is not pending");
        }
        
        // Check if category name is still available
        if (categoryRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Category already exists");
        }
        
        // Create the category
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(request.getParentCategory())
                .createdBy(request.getRequestedBy())
                .approvedBy(admin)
                .active(true)
                .build();
        
        category = categoryRepository.save(category);
        
        // Update request status
        request.setStatus(CategoryStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        categoryRequestRepository.save(request);
        
        log.info("Category approved: {} by admin: {}", category.getName(), admin.getUsername());
        
        return mapToCategoryDto(category);
    }
    
    /**
     * Admin: Reject category request
     */
    public void rejectCategoryRequest(Long requestId, Long adminId, String reviewNotes) {
        CategoryRequest request = categoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Category request not found"));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        
        if (request.getStatus() != CategoryStatus.PENDING) {
            throw new IllegalStateException("Category request is not pending");
        }
        
        request.setStatus(CategoryStatus.REJECTED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(reviewNotes);
        categoryRequestRepository.save(request);
        
        log.info("Category rejected: {} by admin: {}", request.getName(), admin.getUsername());
    }
    
    /**
     * Search categories by name
     */
    public List<CategoryDto> searchCategories(String keyword) {
        List<Category> categories = categoryRepository.findByActiveTrue();
        return categories.stream()
                .filter(category -> category.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }
    
    private CategoryDto mapToCategoryDto(Category category) {
        List<CategoryDto> subcategories = category.getSubcategories().stream()
                .filter(Category::isActive)
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
        
        Integer productCount = categoryRepository.countActiveProductsByCategory(category.getId());
        
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .fullPath(category.getFullPath())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .subcategories(subcategories)
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .active(category.isActive())
                .build();
    }
    
    private CategoryRequestDto mapToCategoryRequestDto(CategoryRequest request) {
        return CategoryRequestDto.builder()
                .name(request.getName())
                .description(request.getDescription())
                .justification(request.getJustification())
                .parentCategoryId(request.getParentCategory() != null ? request.getParentCategory().getId() : null)
                .build();
    }
}
```

#### ProductService.java
```java
package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.ProductCreateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.request.ProductUpdateRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ProductSummaryDto;
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
        List<Product> products = productRepository.findRecentProducts(pageable);
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
                .sellerBuilding(product.getSeller().getBuildingName())
                .sellerApartment(product.getSeller().getApartmentNumber())
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
                .sellerBuilding(product.getSeller().getBuildingName())
                .viewCount(product.getViewCount())
                .negotiable(product.isNegotiable())
                .createdAt(product.getCreatedAt())
                .isOwner(isOwner)
                .build();
    }
    
    private ProductDto.ProductImageDto mapToProductImageDto(ProductImage image) {
        return ProductDto.ProductImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .fileName(image.getFileName())
                .primary(image.isPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
```

#### ChatService.java
```java
package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.ChatMessageRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.ChatDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.ChatMessageDto;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.ChatStatus;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {
    
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    
    /**
     * Start or get existing chat for a product
     */
    public ChatDto startOrGetChat(Long productId, Long buyerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));
        
        // Check if buyer is trying to chat with themselves
        if (product.getSeller().getId().equals(buyerId)) {
            throw new IllegalStateException("You cannot chat about your own product");
        }
        
        // Check if chat already exists
        Optional<Chat> existingChat = chatRepository.findByProductAndBuyerAndStatus(product, buyer, ChatStatus.ACTIVE);
        
        Chat chat;
        if (existingChat.isPresent()) {
            chat = existingChat.get();
        } else {
            // Create new chat
            chat = Chat.builder()
                    .product(product)
                    .buyer(buyer)
                    .seller(product.getSeller())
                    .status(ChatStatus.ACTIVE)
                    .build();
            chat = chatRepository.save(chat);
            
            // Send welcome message
            sendSystemMessage(chat, String.format("%s is interested in your product: %s", 
                    buyer.getEffectiveDisplayName(), product.getTitle()));
            
            log.info("New chat started for product: {} between buyer: {} and seller: {}", 
                    product.getTitle(), buyer.getUsername(), product.getSeller().getUsername());
        }
        
        return mapToChatDto(chat, buyerId);
    }
    
    /**
     * Send message in chat
     */
    public ChatMessageDto sendMessage(Long chatId, ChatMessageRequest request, Long senderId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(senderId) && !chat.getSeller().getId().equals(senderId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        if (chat.getStatus() != ChatStatus.ACTIVE) {
            throw new IllegalStateException("This chat is not active");
        }
        
        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .systemMessage(false)
                .build();
        
        message = chatMessageRepository.save(message);
        
        // Update chat's last message time
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
        
        log.info("Message sent in chat {}: {} characters by user: {}", 
                chatId, request.getContent().length(), sender.getUsername());
        
        return mapToChatMessageDto(message, senderId);
    }
    
    /**
     * Get user's chats
     */
    public Page<ChatDto> getUserChats(Long userId, Pageable pageable) {
        Page<Chat> chats = chatRepository.findUserChats(userId, ChatStatus.ACTIVE, pageable);
        return chats.map(chat -> mapToChatDto(chat, userId));
    }
    
    /**
     * Get chat by ID
     */
    public ChatDto getChatById(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        return mapToChatDto(chat, userId);
    }
    
    /**
     * Get chat messages with pagination
     */
    public Page<ChatMessageDto> getChatMessages(Long chatId, Long userId, Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        Page<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
        return messages.map(message -> mapToChatMessageDto(message, userId));
    }
    
    /**
     * Mark chat as read
     */
    public void markChatAsRead(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        chat.markAsRead(user);
        chatRepository.save(chat);
    }
    
    /**
     * Close chat
     */
    public void closeChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        chat.setStatus(ChatStatus.CLOSED);
        chatRepository.save(chat);
        
        // Send system message
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            sendSystemMessage(chat, String.format("Chat closed by %s", user.getEffectiveDisplayName()));
        }
        
        log.info("Chat {} closed by user: {}", chatId, userId);
    }
    
    /**
     * Get chats with unread messages
     */
    public List<ChatDto> getChatsWithUnreadMessages(Long userId) {
        List<Chat> chats = chatRepository.findChatsWithUnreadMessages(userId);
        return chats.stream()
                .map(chat -> mapToChatDto(chat, userId))
                .collect(Collectors.toList());
    }
    
    private void sendSystemMessage(Chat chat, String content) {
        ChatMessage systemMessage = ChatMessage.builder()
                .chat(chat)
                .sender(chat.getSeller()) // System messages from seller's perspective
                .content(content)
                .messageType("system")
                .systemMessage(true)
                .build();
        
        chatMessageRepository.save(systemMessage);
        
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
    }
    
    private ChatDto mapToChatDto(Chat chat, Long currentUserId) {
        ProductImage primaryImage = productImageRepository.findByProductIdAndPrimaryTrue(chat.getProduct().getId()).orElse(null);
        
        // Get recent messages (last 5)
        Pageable recentMessagesPageable = PageRequest.of(0, 5);
        List<ChatMessage> recentMessages = chatMessageRepository.findRecentMessages(chat.getId(), recentMessagesPageable);
        
        ChatMessage lastMessage = recentMessages.isEmpty() ? null : recentMessages.get(0);
        
        return ChatDto.builder()
                .id(chat.getId())
                .productId(chat.getProduct().getId())
                .productTitle(chat.getProduct().getTitle())
                .productImageUrl(primaryImage != null ? primaryImage.getImageUrl() : null)
                .buyerId(chat.getBuyer().getId())
                .buyerName(chat.getBuyer().getFullName())
                .buyerDisplayName(chat.getBuyer().getEffectiveDisplayName())
                .sellerId(chat.getSeller().getId())
                .sellerName(chat.getSeller().getFullName())
                .sellerDisplayName(chat.getSeller().getEffectiveDisplayName())
                .status(chat.getStatus())
                .hasUnreadMessages(chat.hasUnreadMessages(userRepository.findById(currentUserId).orElse(null)))
                .lastMessage(lastMessage != null ? mapToChatMessageDto(lastMessage, currentUserId) : null)
                .recentMessages(recentMessages.stream()
                        .map(msg -> mapToChatMessageDto(msg, currentUserId))
                        .collect(Collectors.toList()))
                .createdAt(chat.getCreatedAt())
                .lastMessageAt(chat.getLastMessageAt())
                .build();
    }
    
    private ChatMessageDto mapToChatMessageDto(ChatMessage message, Long currentUserId) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderDisplayName(message.getSender().getEffectiveDisplayName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .systemMessage(message.isSystemMessage())
                .createdAt(message.getCreatedAt())
                .sentByCurrentUser(message.getSender().getId().equals(currentUserId))
                .build();
    }
}
```

#### NegotiationService.java
```java
package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.dto.request.NegotiationRequest;
import com.marketplace.StoneRidgeMarketplace.dto.response.NegotiationDto;
import com.marketplace.StoneRidgeMarketplace.entity.*;
import com.marketplace.StoneRidgeMarketplace.entity.enums.NegotiationStatus;
import com.marketplace.StoneRidgeMarketplace.exception.ResourceNotFoundException;
import com.marketplace.StoneRidgeMarketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NegotiationService {
    
    private final NegotiationRepository negotiationRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    
    /**
     * Make a price offer
     */
    public NegotiationDto makeOffer(Long chatId, NegotiationRequest request, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        // Check if product is still available
        if (chat.getProduct().isSold()) {
            throw new IllegalStateException("This product has already been sold");
        }
        
        // Check if product is negotiable
        if (!chat.getProduct().isNegotiable()) {
            throw new IllegalStateException("This product is not open for negotiation");
        }
        
        // Expire any existing pending offers from this user in this chat
        List<Negotiation> existingOffers = negotiationRepository.findActivePendingOffers(chatId, LocalDateTime.now());
        existingOffers.stream()
                .filter(offer -> offer.getOfferedBy().getId().equals(userId))
                .forEach(offer -> {
                    offer.setStatus(NegotiationStatus.COUNTER_OFFERED);
                    negotiationRepository.save(offer);
                });
        
        // Create new offer
        Negotiation negotiation = Negotiation.builder()
                .chat(chat)
                .offeredBy(user)
                .offeredPrice(request.getOfferedPrice())
                .message(request.getMessage())
                .status(NegotiationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(request.getValidityHours()))
                .build();
        
        negotiation = negotiationRepository.save(negotiation);
        
        // Send notification message in chat
        String offerMessage = String.format("💰 %s made an offer of $%.2f%s", 
                user.getEffectiveDisplayName(),
                request.getOfferedPrice(),
                request.getMessage() != null ? " - " + request.getMessage() : "");
        
        sendOfferMessage(chat, user, offerMessage);
        
        log.info("Offer made in chat {}: $%.2f by user: {}", 
                chatId, request.getOfferedPrice(), user.getUsername());
        
        return mapToNegotiationDto(negotiation, userId);
    }
    
    /**
     * Accept an offer
     */
    public void acceptOffer(Long negotiationId, Long userId) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation not found"));
        
        Chat chat = negotiation.getChat();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Only seller can accept offers
        if (!chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("Only the seller can accept offers");
        }
        
        if (!negotiation.isPending()) {
            throw new IllegalStateException("This offer is no longer valid");
        }
        
        // Accept the offer
        negotiation.setStatus(NegotiationStatus.ACCEPTED);
        negotiation.setRespondedAt(LocalDateTime.now());
        negotiationRepository.save(negotiation);
        
        // Mark product as sold
        productService.markProductAsSold(
                chat.getProduct().getId(),
                negotiation.getOfferedBy().getId(),
                negotiation.getOfferedPrice(),
                userId
        );
        
        // Send confirmation message
        String confirmationMessage = String.format("✅ Offer of $%.2f accepted! Product sold to %s", 
                negotiation.getOfferedPrice(),
                negotiation.getOfferedBy().getEffectiveDisplayName());
        
        sendOfferMessage(chat, user, confirmationMessage);
        
        // Expire all other pending offers for this product
        List<Negotiation> otherOffers = negotiationRepository.findActivePendingOffers(chat.getId(), LocalDateTime.now());
        otherOffers.stream()
                .filter(offer -> !offer.getId().equals(negotiationId))
                .forEach(offer -> {
                    offer.setStatus(NegotiationStatus.REJECTED);
                    negotiationRepository.save(offer);
                });
        
        log.info("Offer accepted: ${} for product: {} by seller: {}", 
                negotiation.getOfferedPrice(), chat.getProduct().getTitle(), user.getUsername());
    }
    
    /**
     * Reject an offer
     */
    public void rejectOffer(Long negotiationId, Long userId, String reason) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation not found"));
        
        Chat chat = negotiation.getChat();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Only seller can reject offers
        if (!chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("Only the seller can reject offers");
        }
        
        if (!negotiation.isPending()) {
            throw new IllegalStateException("This offer is no longer valid");
        }
        
        // Reject the offer
        negotiation.setStatus(NegotiationStatus.REJECTED);
        negotiation.setRespondedAt(LocalDateTime.now());
        negotiationRepository.save(negotiation);
        
        // Send rejection message
        String rejectionMessage = String.format("❌ Offer of $%.2f was declined%s", 
                negotiation.getOfferedPrice(),
                reason != null ? " - " + reason : "");
        
        sendOfferMessage(chat, user, rejectionMessage);
        
        log.info("Offer rejected: ${} for product: {} by seller: {}", 
                negotiation.getOfferedPrice(), chat.getProduct().getTitle(), user.getUsername());
    }
    
    /**
     * Get negotiations for a chat
     */
    public List<NegotiationDto> getChatNegotiations(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        // Verify user is part of this chat
        if (!chat.getBuyer().getId().equals(userId) && !chat.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this chat");
        }
        
        List<Negotiation> negotiations = negotiationRepository.findByChatIdOrderByCreatedAtDesc(chatId);
        return negotiations.stream()
                .map(negotiation -> mapToNegotiationDto(negotiation, userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get pending offers for seller
     */
    public List<NegotiationDto> getPendingOffersForSeller(Long sellerId) {
        List<Negotiation> negotiations = negotiationRepository.findPendingOffersForSeller(sellerId, LocalDateTime.now());
        return negotiations.stream()
                .map(negotiation -> mapToNegotiationDto(negotiation, sellerId))
                .collect(Collectors.toList());
    }
    
    /**
     * Expire old offers (scheduled task)
     */
    @Transactional
    public void expireOldOffers() {
        List<Negotiation> expiredOffers = negotiationRepository.findExpiredOffers(LocalDateTime.now());
        expiredOffers.forEach(offer -> {
            offer.setStatus(NegotiationStatus.REJECTED);
            negotiationRepository.save(offer);
        });
        
        if (!expiredOffers.isEmpty()) {
            log.info("Expired {} old offers", expiredOffers.size());
        }
    }
    
    private void sendOfferMessage(Chat chat, User sender, String content) {
        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(content)
                .messageType("offer")
                .systemMessage(false)
                .build();
        
        chatMessageRepository.save(message);
        
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
    }
    
    private NegotiationDto mapToNegotiationDto(Negotiation negotiation, Long currentUserId) {
        boolean canRespond = negotiation.isPending() && 
                             negotiation.getChat().getSeller().getId().equals(currentUserId) &&
                             !negotiation.getOfferedBy().getId().equals(currentUserId);
        
        boolean isOwnOffer = negotiation.getOfferedBy().getId().equals(currentUserId);
        
        return NegotiationDto.builder()
                .id(negotiation.getId())
                .chatId(negotiation.getChat().getId())
                .offeredById(negotiation.getOfferedBy().getId())
                .offeredByName(negotiation.getOfferedBy().getEffectiveDisplayName())
                .offeredPrice(negotiation.getOfferedPrice())
                .originalPrice(negotiation.getChat().getProduct().getPrice())
                .message(negotiation.getMessage())
                .status(negotiation.getStatus())
                .expiresAt(negotiation.getExpiresAt())
                .respondedAt(negotiation.getRespondedAt())
                .createdAt(negotiation.getCreatedAt())
                .isExpired(negotiation.isExpired())
                .canRespond(canRespond)
                .isOwnOffer(isOwnOffer)
                .build();
    }
}
```

## 🎮 Controllers

#### CategoryController.java
```java
package com.marketplace.StoneRidgeMarketplace.controller;

import com.marketplace.StoneRidgeMarketplace.dto.request.CategoryRequestDto;
import com.marketplace.StoneRidgeMarketplace.dto.response.CategoryDto;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
import com.marketplace.StoneRidgeMarketplace.service.CategoryService;
import com.marketplace.StoneRidgeMarketplace.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Product category management APIs")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "Get all active categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryDto>>builder()
                        .success(true)
                        .data(categories)
                        .build()
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        CategoryDto category = categoryService.getCategoryById(id);
        
        return ResponseEntity.ok(
                ApiResponse.<CategoryDto>builder()
                        .success(true)
                        .data(category)
                        .build()
        );
    }
    
    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Request new category creation")
    public ResponseEntity<ApiResponse<Void>> requestCategoryCreation(
            @Valid @RequestBody CategoryRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        categoryService.requestCategoryCreation(request, principal.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Category creation request submitted for admin review")
                        .build()
        );
    }
    
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's category requests")
    public ResponseEntity<ApiResponse<List<CategoryRequestDto>>> getUserCategoryRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
    