# StoneRidge Marketplace – Admin Portal

Separate admin website for marketplace administrators. Only users with role `ADMIN` can sign in.

## Run locally

1. **Backend** must be running (e.g. `http://localhost:8080`).

2. **Install and start the admin portal:**

   ```bash
   cd admin-portal
   npm install
   npm run dev
   ```

3. Open **http://localhost:3001** in the browser.

4. Sign in with an **admin** account (username/email and password).  
   Non-admin users will see an error and cannot access the portal.

## Build for production

```bash
npm run build
```

Static files are in `dist/`. Serve them with any static host. Set `VITE_API_URL` to your API base URL (e.g. `https://api.yourdomain.com/api`) when building if the app is not served from the same origin as the API.

## Ports

- **Admin portal:** 3001 (dev)
- **Main marketplace frontend:** 3000 (dev)
- **Backend API:** 8080

## What’s in the admin portal

- **Dashboard** – Counts for users, products, categories, pending category requests.
- **Users** – List and search; suspend/unsuspend, lock/unlock (non-admin users only).
- **Listings** – All products; deactivate or delete.
- **Category requests** – Approve or reject pending requests.
- **Categories** – Create categories, deactivate existing ones.

Admins use this site only; residents use the main marketplace site.
