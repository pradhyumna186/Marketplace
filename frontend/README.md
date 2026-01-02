# Marketplace Frontend - API Tester

A minimal React UI for testing all backend API endpoints.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

The app will be available at `http://localhost:3000`

## Features

This UI provides testing interfaces for all backend endpoints:

- **Authentication**: Register, login, profile management, trusted devices, password reset
- **Categories**: List, search, request new categories
- **Products**: Create, read, update, delete, search, filter products
- **Chats**: Start chats, send messages, view chat history
- **Negotiations**: Make offers, accept/reject offers
- **File Upload**: Upload product images and category icons
- **Admin**: Approve/reject category requests (admin only)

## Usage

1. Start the backend server (should be running on `http://localhost:8080`)
2. Open the frontend in your browser
3. Navigate through the tabs to test different endpoints
4. Most endpoints require authentication - use the Authentication tab to login first
5. Responses are displayed in JSON format below each form

## API Configuration

The API base URL is configured in `src/services/api.js`. By default, it points to `http://localhost:8080/api`.

If your backend runs on a different port or URL, update the `API_BASE_URL` constant in that file.

## Authentication

- Tokens are automatically stored in localStorage after login
- The Authorization header is automatically added to all authenticated requests
- Use the logout button to clear tokens

