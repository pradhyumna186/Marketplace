# StoneRidge Marketplace

A full-stack residential marketplace application for listing, browsing, and transacting items within a community. The platform includes a public storefront, user authentication, product and category management, real-time chat with negotiations, and a separate admin portal for moderation.

---

## Tech Stack

| Layer      | Technology |
|-----------|------------|
| Backend   | Java 17, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA, PostgreSQL |
| API       | REST, Swagger/OpenAPI 3 |
| Frontend  | React 18, Vite 5, Axios |
| Admin     | React 18, Vite 5 (separate app) |

---

## Prerequisites

- **Java 17+**
- **Node.js 18+** and npm
- **PostgreSQL** (e.g. 14+)
- **Maven 3.6+** (or use the included wrapper)

---

## Project Structure

```
Marketplace/
├── backend/          # Spring Boot API (port 8080)
├── frontend/         # Main marketplace UI (Vite dev server)
├── admin-portal/     # Admin dashboard (separate Vite app)
└── README.md
```

---

## Environment Configuration

The backend reads configuration from environment variables or, for admin bootstrap only, from a `.env` file in `backend/`. Other settings use `application.properties` with `${VAR:default}` placeholders.

Create `backend/.env` (or set system environment variables) with at least:

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL (e.g. `jdbc:postgresql://localhost:5432/stoneridge_marketplace`) |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for JWT (e.g. 256-bit); **change in production** |
| `ADMIN_EMAIL` | Email for the first admin (created on startup from `.env` if set) |
| `ADMIN_PASSWORD` | Password for the first admin |

Optional: `EMAIL_*` for SMTP, `ADMIN_USERNAME`, `ADMIN_FIRST_NAME`, `ADMIN_LAST_NAME`, `APP_BASE_URL`, `JWT_*_EXPIRATION`.

To load all `.env` values into the environment before starting the backend:

```bash
cd backend && export $(grep -v '^#' .env | xargs) && mvn spring-boot:run
```

---

## Running the Application

### 1. Database

Ensure PostgreSQL is running and the database exists (e.g. `stoneridge_marketplace`). The application will create or update schema on startup (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

API base: **http://localhost:8080**  
Swagger UI: **http://localhost:8080/swagger-ui**

### 3. Frontend (marketplace)

```bash
cd frontend
npm install
npm run dev
```

Use the URL shown (e.g. **http://localhost:3001**). The Vite dev server proxies `/api` to the backend when using the default base URL.

### 4. Admin portal

```bash
cd admin-portal
npm install
npm run dev
```

Open the URL shown (e.g. **http://localhost:3002**). Sign in with an admin account via **POST /api/auth/admin-login**. Admins are stored in the `admins` table and can be bootstrapped from `ADMIN_EMAIL` and `ADMIN_PASSWORD` in `backend/.env`.

---

## API Overview

- **Auth:** `/api/auth/register`, `/api/auth/login`, `/api/auth/admin-login`, `/api/auth/refresh-token`, verify email, password reset.
- **Products:** CRUD, search, filter, my-products, mark sold.
- **Categories:** List, request new category (user); approve/reject, create, update, deactivate (admin).
- **Chat & negotiations:** Start chat, messages, offers, accept/reject.
- **Admin:** Dashboard stats, user list/suspend/lock, product list/deactivate/delete, category requests, category CRUD.

JWT is required for protected endpoints; include `Authorization: Bearer <accessToken>`.

---

## License

Proprietary. All rights reserved.
