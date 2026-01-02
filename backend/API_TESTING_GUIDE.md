# API Testing Guide - Complete Endpoint Reference

## 🔐 Authentication Required
Most endpoints require authentication. Use your access token:
- **Header:** `Authorization: Bearer <your-access-token>`
- Or use the "Authorize" button in Swagger UI

---

## 📋 **AUTHENTICATION ENDPOINTS** (`/api/auth`)

### 1. **Register User** ✅ (No Auth)
**`POST /api/auth/register`**

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "displayName": "Johnny",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "apartmentNumber": "A101",
  "buildingName": "Building A",
  "phoneNumber": "+1234567890",
  "acceptTerms": true
}
```

**Required Fields:**
- `firstName` (1-50 chars, letters/spaces/hyphens/apostrophes only)
- `username` (3-20 chars, alphanumeric + underscore)
- `email` (valid email format)
- `password` (min 8 chars, must have uppercase, lowercase, number)
- `apartmentNumber` (max 10 chars)
- `buildingName` (max 50 chars)
- `acceptTerms` (must be `true`)

**Optional Fields:**
- `lastName`, `displayName`, `phoneNumber`

---

### 2. **Login** ✅ (No Auth)
**`POST /api/auth/login`**

**Request Body:**
```json
{
  "usernameOrEmail": "johndoe",
  "password": "SecurePass123",
  "rememberDevice": false
}
```

**Response:** Returns `accessToken` and `refreshToken`

---

### 3. **Verify Email** ✅ (No Auth)
**`GET /api/auth/verify-email?token=<verification-token>`**

**Query Parameter:**
- `token` - Email verification token from email

---

### 4. **Resend Verification Email** ✅ (No Auth)
**`POST /api/auth/resend-verification?email=john@example.com`**

**Query Parameter:**
- `email` - User's email address

---

### 5. **Get Trusted Devices** 🔒 (Auth Required)
**`GET /api/auth/trusted-devices`**

**Response:** List of trusted devices

---

### 6. **Revoke Trusted Device** 🔒 (Auth Required)
**`DELETE /api/auth/trusted-devices/{deviceId}`**

**Path Parameter:**
- `deviceId` - ID of device to revoke

---

### 7. **Logout** 🔒 (Auth Required)
**`POST /api/auth/logout`**

Logs out from current device.

---

### 8. **Logout All Devices** 🔒 (Auth Required)
**`POST /api/auth/logout-all`**

Logs out from all devices.

---

### 9. **Delete Account** 🔒 (Auth Required)
**`DELETE /api/auth/account`**

Permanently deletes user account.

---

### 10. **Refresh Token** ✅ (No Auth)
**`POST /api/auth/refresh-token`**

**Header:**
- `Refresh-Token: <your-refresh-token>`

**Response:** New access token

---

### 11. **Forgot Password** ✅ (No Auth)
**`POST /api/auth/forgot-password?email=john@example.com`**

**Query Parameter:**
- `email` - User's email address

---

### 12. **Reset Password** ✅ (No Auth)
**`POST /api/auth/reset-password`**

**Request Body:**
```json
{
  "token": "<password-reset-token>",
  "newPassword": "NewSecurePass123"
}
```

---

### 13. **Update Profile** 🔒 (Auth Required)
**`PUT /api/auth/profile`**

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "displayName": "Johnny",
  "email": "john@example.com",
  "apartmentNumber": "A101",
  "buildingName": "Building A",
  "phoneNumber": "+1234567890"
}
```

**Required:** `firstName`, `email`, `apartmentNumber`, `buildingName`

---

## 📦 **CATEGORY ENDPOINTS** (`/api/categories`)

### 1. **Get All Categories** ✅ (No Auth)
**`GET /api/categories`**

**Response:** List of all active categories

---

### 2. **Get Category by ID** ✅ (No Auth)
**`GET /api/categories/{id}`**

**Path Parameter:**
- `id` - Category ID

---

### 3. **Request Category Creation** 🔒 (Auth Required)
**`POST /api/categories/request`**

**Request Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic items and gadgets",
  "justification": "Many residents sell electronics",
  "parentCategoryId": null
}
```

**Required:**
- `name` (2-50 chars)

**Optional:**
- `description` (max 500 chars)
- `justification` (max 1000 chars)
- `parentCategoryId` (for subcategories)

---

### 4. **Get User's Category Requests** 🔒 (Auth Required)
**`GET /api/categories/my-requests`**

**Response:** List of your category requests

---

### 5. **Search Categories** ✅ (No Auth)
**`GET /api/categories/search?keyword=electronics`**

**Query Parameter:**
- `keyword` - Search term

---

## 🛍️ **PRODUCT ENDPOINTS** (`/api/products`)

### 1. **Create Product** 🔒 (Auth Required)
**`POST /api/products`**

**Request Body:**
```json
{
  "title": "Vintage Coffee Table",
  "description": "Beautiful vintage coffee table in excellent condition. Perfect for any living room. Some minor scratches but overall great quality.",
  "price": 150.00,
  "originalPrice": 300.00,
  "condition": "GOOD",
  "categoryId": 1,
  "negotiable": true,
  "locationDetails": "Building AA, Apartment AA266",
  "imageUrls": []
}
```

**Required:**
- `title` (5-100 chars)
- `description` (20-2000 chars)
- `price` (0.01 to 999999.99)
- `condition` (NEW, LIKE_NEW, GOOD, FAIR, POOR)
- `categoryId` (must exist)

**Optional:**
- `originalPrice` (0.01 to 999999.99)
- `negotiable` (default: true)
- `locationDetails` (max 200 chars)
- `imageUrls` (array of image URLs)

---

### 2. **Get All Products** ✅ (No Auth)
**`GET /api/products`**

**Query Parameters (Pagination):**
- `page` (default: 0)
- `size` (default: 20)
- `sort` (optional)

**Example:** `GET /api/products?page=0&size=10`

---

### 3. **Get Product by ID** ✅ (No Auth)
**`GET /api/products/{id}`**

**Path Parameter:**
- `id` - Product ID

---

### 4. **Update Product** 🔒 (Auth Required - Owner Only)
**`PUT /api/products/{id}`**

**Path Parameter:**
- `id` - Product ID

**Request Body:**
```json
{
  "title": "Updated Coffee Table",
  "description": "Updated description",
  "price": 120.00,
  "originalPrice": 300.00,
  "condition": "GOOD",
  "status": "ACTIVE",
  "categoryId": 1,
  "negotiable": true,
  "locationDetails": "Building AA",
  "imageUrls": []
}
```

**All fields optional** - only include fields you want to update

**Product Status Options:**
- `ACTIVE`
- `SOLD`
- `PENDING`
- `INACTIVE`

---

### 5. **Delete Product** 🔒 (Auth Required - Owner Only)
**`DELETE /api/products/{id}`**

**Path Parameter:**
- `id` - Product ID

---

### 6. **Get Products by Category** ✅ (No Auth)
**`GET /api/products/category/{categoryId}`**

**Path Parameter:**
- `categoryId` - Category ID

**Query Parameters:**
- `page`, `size`, `sort` (pagination)

---

### 7. **Search Products** ✅ (No Auth)
**`GET /api/products/search?keyword=coffee`**

**Query Parameters:**
- `keyword` - Search term (required)
- `page`, `size`, `sort` (pagination)

---

### 8. **Filter Products** ✅ (No Auth)
**`GET /api/products/filter?categoryId=1&minPrice=50&maxPrice=200`**

**Query Parameters:**
- `categoryId` - Category ID (required)
- `minPrice` - Minimum price (required)
- `maxPrice` - Maximum price (required)
- `page`, `size`, `sort` (pagination)

---

### 9. **Get Products by Building** ✅ (No Auth)
**`GET /api/products/building/{building}`**

**Path Parameter:**
- `building` - Building name (e.g., "Building AA")

**Query Parameters:**
- `page`, `size`, `sort` (pagination)

---

### 10. **Get User's Products** 🔒 (Auth Required)
**`GET /api/products/my-products?status=ACTIVE`**

**Query Parameters:**
- `status` - ProductStatus (default: ACTIVE)
  - Options: `ACTIVE`, `SOLD`, `PENDING`, `INACTIVE`
- `page`, `size`, `sort` (pagination)

---

### 11. **Mark Product as Sold** 🔒 (Auth Required - Owner Only)
**`POST /api/products/{id}/mark-sold?buyerId=5&soldPrice=120.00`**

**Path Parameter:**
- `id` - Product ID

**Query Parameters:**
- `buyerId` - ID of buyer (required)
- `soldPrice` - Final sale price (required)

---

### 12. **Get Trending Products** ✅ (No Auth)
**`GET /api/products/trending?limit=10`**

**Query Parameter:**
- `limit` - Number of products (default: 10)

---

### 13. **Get Recent Products** ✅ (No Auth)
**`GET /api/products/recent?limit=10`**

**Query Parameter:**
- `limit` - Number of products (default: 10)

---

## 💬 **CHAT ENDPOINTS** (`/api/chats`) - All Require Auth

### 1. **Start or Get Chat** 🔒
**`POST /api/chats/start/{productId}`**

**Path Parameter:**
- `productId` - Product ID to chat about

**Response:** Returns existing chat or creates new one

---

### 2. **Get User's Chats** 🔒
**`GET /api/chats`**

**Query Parameters:**
- `page`, `size`, `sort` (pagination, default size: 20)

---

### 3. **Get Chat by ID** 🔒
**`GET /api/chats/{id}`**

**Path Parameter:**
- `id` - Chat ID

---

### 4. **Get Chat Messages** 🔒
**`GET /api/chats/{id}/messages`**

**Path Parameter:**
- `id` - Chat ID

**Query Parameters:**
- `page`, `size`, `sort` (pagination, default size: 50)

---

### 5. **Send Message** 🔒
**`POST /api/chats/{id}/messages`**

**Path Parameter:**
- `id` - Chat ID

**Request Body:**
```json
{
  "content": "Hello, is this still available?",
  "messageType": "text"
}
```

**Required:**
- `content` (1-2000 chars)

**Optional:**
- `messageType` (default: "text", options: "text", "image", "offer")

---

### 6. **Mark Chat as Read** 🔒
**`POST /api/chats/{id}/mark-read`**

**Path Parameter:**
- `id` - Chat ID

---

### 7. **Close Chat** 🔒
**`POST /api/chats/{id}/close`**

**Path Parameter:**
- `id` - Chat ID

---

### 8. **Get Unread Chats** 🔒
**`GET /api/chats/unread`**

**Response:** List of chats with unread messages

---

## 💰 **NEGOTIATION ENDPOINTS** (`/api/negotiations`) - All Require Auth

### 1. **Make Offer** 🔒
**`POST /api/negotiations/chats/{chatId}/offer`**

**Path Parameter:**
- `chatId` - Chat ID

**Request Body:**
```json
{
  "offeredPrice": 120.00,
  "message": "Would you accept $120?",
  "validityHours": 24
}
```

**Required:**
- `offeredPrice` (0.01 to 999999.99)

**Optional:**
- `message` (max 500 chars)
- `validityHours` (default: 24)

---

### 2. **Accept Offer** 🔒
**`POST /api/negotiations/{id}/accept`**

**Path Parameter:**
- `id` - Negotiation/Offer ID

---

### 3. **Reject Offer** 🔒
**`POST /api/negotiations/{id}/reject?reason=Price too low`**

**Path Parameter:**
- `id` - Negotiation/Offer ID

**Query Parameter:**
- `reason` - Optional rejection reason

---

### 4. **Get Chat Negotiations** 🔒
**`GET /api/negotiations/chats/{chatId}`**

**Path Parameter:**
- `chatId` - Chat ID

**Response:** List of all negotiations for this chat

---

### 5. **Get Pending Offers (for Seller)** 🔒
**`GET /api/negotiations/pending-offers`**

**Response:** List of pending offers for your products

---

## 📁 **FILE UPLOAD ENDPOINTS** (`/api/files`) - All Require Auth

### 1. **Upload Product Images** 🔒
**`POST /api/files/upload/product-images`**

**Form Data:**
- `files` - Array of image files (multipart/form-data)

**Response:** List of image URLs

**Note:** Max file size: 5MB per file, 25MB total

---

### 2. **Upload Category Icon** 🔒
**`POST /api/files/upload/category-icon`**

**Form Data:**
- `file` - Single image file (multipart/form-data)

**Response:** Image URL

---

## 👑 **ADMIN ENDPOINTS** (`/api/admin`) - Admin Only

### 1. **Get Pending Category Requests** 🔒 (Admin)
**`GET /api/admin/category-requests/pending`**

**Response:** List of pending category requests

---

### 2. **Approve Category Request** 🔒 (Admin)
**`POST /api/admin/category-requests/{id}/approve`**

**Path Parameter:**
- `id` - Category request ID

**Response:** Created category

---

### 3. **Reject Category Request** 🔒 (Admin)
**`POST /api/admin/category-requests/{id}/reject?reviewNotes=Does not fit our marketplace`**

**Path Parameter:**
- `id` - Category request ID

**Query Parameter:**
- `reviewNotes` - Reason for rejection (required)

---

## 🧪 **RECOMMENDED TESTING SEQUENCE**

### Phase 1: Setup & Authentication
1. ✅ `GET /api/categories` - Check available categories
2. ✅ `POST /api/auth/register` - Register (if needed)
3. ✅ `POST /api/auth/login` - Login and get token
4. ✅ Authorize in Swagger UI with token

### Phase 2: Category Management
5. 🔒 `POST /api/categories/request` - Request a new category
6. 🔒 `GET /api/categories/my-requests` - Check your requests
7. ✅ `GET /api/categories/search?keyword=furniture` - Search categories

### Phase 3: Product Management
8. 🔒 `POST /api/products` - Create a product
9. 🔒 `GET /api/products/my-products` - View your products
10. ✅ `GET /api/products` - View all products
11. ✅ `GET /api/products/{id}` - View specific product
12. 🔒 `PUT /api/products/{id}` - Update your product
13. ✅ `GET /api/products/search?keyword=table` - Search products
14. ✅ `GET /api/products/filter?categoryId=1&minPrice=50&maxPrice=200` - Filter products
15. ✅ `GET /api/products/trending?limit=5` - Get trending
16. ✅ `GET /api/products/recent?limit=5` - Get recent

### Phase 4: File Upload
17. 🔒 `POST /api/files/upload/product-images` - Upload images
18. 🔒 `POST /api/products` - Create product with image URLs

### Phase 5: Chat & Negotiation
19. 🔒 `POST /api/chats/start/{productId}` - Start chat about a product
20. 🔒 `GET /api/chats` - View your chats
21. 🔒 `POST /api/chats/{id}/messages` - Send a message
22. 🔒 `GET /api/chats/{id}/messages` - Get chat messages
23. 🔒 `POST /api/negotiations/chats/{chatId}/offer` - Make an offer
24. 🔒 `GET /api/negotiations/chats/{chatId}` - View negotiations
25. 🔒 `POST /api/negotiations/{id}/accept` - Accept offer (as seller)
26. 🔒 `POST /api/products/{id}/mark-sold` - Mark product as sold

### Phase 6: Profile Management
27. 🔒 `GET /api/auth/trusted-devices` - View trusted devices
28. 🔒 `PUT /api/auth/profile` - Update profile
29. 🔒 `GET /api/products/my-products?status=SOLD` - View sold products

---

## 📝 **QUICK REFERENCE**

### Product Condition Values:
- `NEW`
- `LIKE_NEW`
- `GOOD`
- `FAIR`
- `POOR`

### Product Status Values:
- `ACTIVE`
- `SOLD`
- `PENDING`
- `INACTIVE`

### Pagination Defaults:
- Page size: 20 (products, categories)
- Page size: 50 (chat messages)
- Page: 0 (first page)

### Authentication:
- ✅ = No authentication required
- 🔒 = Authentication required (Bearer token)

---

## 💡 **TIPS**

1. **Always get categories first** before creating products
2. **Upload images first**, then use URLs in product creation
3. **Start a chat** before making offers
4. **Use pagination** for large result sets
5. **Check response status codes:**
   - 200 = Success
   - 201 = Created
   - 400 = Bad Request (check parameters)
   - 401 = Unauthorized (check token)
   - 403 = Forbidden (check permissions)
   - 404 = Not Found

