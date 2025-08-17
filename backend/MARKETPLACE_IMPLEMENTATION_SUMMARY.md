# StoneRidge Marketplace Implementation Summary

## Overview

This document summarizes the implementation of the StoneRidge Marketplace backend system, which includes product management, category management, chat system, and negotiation features.

## 🏗️ Architecture

### Package Structure

```
com.marketplace.StoneRidgeMarketplace/
├── entity/                    # Database entities
├── dto/                       # Data Transfer Objects
├── repository/                # Data access layer
├── service/                   # Business logic layer
├── controller/                # REST API endpoints
├── websocket/                 # Real-time communication
├── security/                  # Authentication & authorization
├── exception/                 # Custom exceptions
└── util/                      # Utility classes
```

## 🗃️ Core Entities

### 1. Product Management

- **Product**: Core product entity with title, description, price, condition, etc.
- **ProductImage**: Product images with ordering and primary image designation
- **Category**: Hierarchical product categories with admin approval workflow
- **CategoryRequest**: User requests for new categories requiring admin approval

### 2. Communication System

- **Chat**: Conversations between buyers and sellers about products
- **ChatMessage**: Individual messages within chats
- **Negotiation**: Price offers and counter-offers with expiration

### 3. Enums

- **ProductStatus**: ACTIVE, SOLD, INACTIVE, PENDING_APPROVAL
- **ProductCondition**: NEW, LIKE_NEW, GOOD, FAIR, POOR
- **CategoryStatus**: PENDING, APPROVED, REJECTED
- **ChatStatus**: ACTIVE, CLOSED, ARCHIVED
- **NegotiationStatus**: PENDING, ACCEPTED, REJECTED, COUNTER_OFFERED, FINALIZED

## 🔧 Services

### 1. CategoryService

- Get all categories (hierarchical structure)
- Request new category creation
- Admin approval/rejection workflow
- Category search functionality

### 2. ProductService

- CRUD operations for products
- Product search and filtering
- View count tracking
- Image management
- Building-based product discovery

### 3. ChatService

- Start or get existing chats
- Send and receive messages
- Chat history with pagination
- Read receipts and unread message tracking
- System messages for notifications

### 4. NegotiationService

- Make price offers with expiration
- Accept/reject offers
- Automatic offer expiration
- Counter-offer support

### 5. FileStorageService

- Image upload and storage
- File validation and security
- Organized storage by category

### 6. ScheduledTasks

- Automatic expiration of old offers
- Configurable scheduling

## 🌐 REST API Endpoints

### Categories (`/api/categories`)

- `GET /` - Get all active categories
- `GET /{id}` - Get category by ID
- `POST /request` - Request new category creation
- `GET /my-requests` - Get user's category requests
- `GET /search` - Search categories by keyword

### Products (`/api/products`)

- `POST /` - Create new product
- `GET /` - Get all active products (paginated)
- `GET /{id}` - Get product by ID
- `PUT /{id}` - Update product
- `DELETE /{id}` - Delete product
- `GET /category/{categoryId}` - Get products by category
- `GET /search` - Search products by keyword
- `GET /filter` - Filter by category and price range
- `GET /building/{building}` - Get products from same building
- `GET /my-products` - Get user's products
- `POST /{id}/mark-sold` - Mark product as sold
- `GET /trending` - Get most viewed products
- `GET /recent` - Get recently posted products

### Chats (`/api/chats`)

- `POST /start/{productId}` - Start or get existing chat
- `GET /` - Get user's chats
- `GET /{id}` - Get chat by ID
- `GET /{id}/messages` - Get chat messages
- `POST /{id}/messages` - Send message
- `POST /{id}/mark-read` - Mark chat as read
- `POST /{id}/close` - Close chat
- `GET /unread` - Get chats with unread messages

### Negotiations (`/api/negotiations`)

- `POST /chats/{chatId}/offer` - Make price offer
- `POST /{id}/accept` - Accept offer
- `POST /{id}/reject` - Reject offer
- `GET /chats/{chatId}` - Get negotiations for chat
- `GET /pending-offers` - Get pending offers for seller

### Admin (`/api/admin`)

- `GET /category-requests/pending` - Get pending category requests
- `POST /category-requests/{id}/approve` - Approve category request
- `POST /category-requests/{id}/reject` - Reject category request

### File Upload (`/api/files`)

- `POST /upload/product-images` - Upload product images
- `POST /upload/category-icon` - Upload category icon

## 🔌 WebSocket Support

### Real-time Features

- **Chat Notifications**: Instant message delivery
- **Negotiation Updates**: Real-time offer status changes
- **Connection Management**: User session tracking

### WebSocket Endpoint

- `/ws/chat` - Main WebSocket endpoint for real-time communication

## 🔐 Security Features

### Authentication & Authorization

- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Secure password handling
- Device trust management

### API Security

- Input validation and sanitization
- Rate limiting support
- File upload security
- SQL injection prevention

## 📊 Database Features

### Advanced Queries

- Hierarchical category queries
- Product search with full-text capabilities
- Building-based product discovery
- Price range filtering
- View count tracking

### Performance Optimizations

- Lazy loading for relationships
- Pagination support
- Indexed queries
- Efficient joins

## 🚀 Key Features

### 1. **Smart Category System**

- User-requested categories with admin approval
- Hierarchical category structure
- Category request limits (3 pending per user)

### 2. **Advanced Product Management**

- Multiple image support with ordering
- Condition-based categorization
- Negotiable pricing flags
- Building-specific product discovery

### 3. **Real-time Communication**

- WebSocket-based chat system
- System message notifications
- Read receipts and unread tracking
- Chat closure and archiving

### 4. **Sophisticated Negotiation**

- Time-limited offers with expiration
- Automatic offer management
- Counter-offer support
- Offer acceptance workflow

### 5. **File Management**

- Secure image upload system
- Organized storage structure
- File validation and security
- Static resource serving

## 🔧 Configuration

### Application Properties

- File upload limits and storage paths
- Static resource serving configuration
- Scheduling pool configuration
- Base URL configuration

### Dependencies Added

- `spring-boot-starter-websocket` - WebSocket support
- `commons-io` - File processing utilities
- Scheduling enabled via `@EnableScheduling`

## 📱 Frontend Integration

### API Response Format

All API responses use the `ApiResponse<T>` wrapper:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### WebSocket Integration

```javascript
const ws = new WebSocket("ws://localhost:8080/ws/chat?token=" + jwtToken);

ws.onmessage = function (event) {
  const data = JSON.parse(event.data);
  // Handle different message types
};
```

## 🧪 Testing & Development

### Swagger Documentation

- Available at `/swagger-ui.html`
- Complete API documentation
- Interactive testing interface

### Database

- PostgreSQL support
- H2 in-memory database for testing
- Automatic schema updates

## 🚀 Deployment Considerations

### Production Setup

- Configure proper file storage paths
- Set secure JWT secrets
- Configure database connection pools
- Set up proper CORS for WebSocket
- Configure file upload limits
- Set up monitoring and logging

### Security Hardening

- Validate file uploads
- Implement rate limiting
- Set up proper CORS policies
- Configure WebSocket security
- Monitor for suspicious activity

## 📈 Future Enhancements

### Potential Additions

- Product review and rating system
- Advanced search with filters
- Notification system (email, push)
- Analytics and reporting
- Payment integration
- Shipping and delivery tracking
- Multi-language support
- Mobile app API endpoints

## 🎯 Summary

The StoneRidge Marketplace backend provides a comprehensive, production-ready foundation for a modern marketplace application. It includes all essential features for product management, user communication, and business operations while maintaining security, performance, and scalability.

The system is designed with clean architecture principles, comprehensive error handling, and extensive API documentation, making it easy to maintain, extend, and integrate with frontend applications.
