# Environment Variables Setup Guide

This project uses environment variables for sensitive configuration. Follow these steps to set up your environment.

## Quick Setup

1. **Copy the example file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` file** with your actual credentials:
   - Database username and password
   - Gmail email and app password
   - JWT secret key

3. **Load environment variables** before running the application (see methods below)

## Environment Variables

### Database Configuration
- `DB_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

### Email Configuration
- `EMAIL_HOST` - SMTP host (default: smtp.gmail.com)
- `EMAIL_PORT` - SMTP port (default: 587)
- `EMAIL_USERNAME` - Your Gmail address
- `EMAIL_PASSWORD` - Gmail App Password (not your regular password)

### JWT Configuration
- `JWT_SECRET` - 256-bit secret key for JWT tokens
- `JWT_ACCESS_TOKEN_EXPIRATION` - Access token expiration in milliseconds (default: 900000 = 15 minutes)
- `JWT_REFRESH_TOKEN_EXPIRATION` - Refresh token expiration in milliseconds (default: 604800000 = 7 days)

### Application Configuration
- `APP_BASE_URL` - Base URL of the application (default: http://localhost:8080)

## Loading Environment Variables

### Option 1: Using IntelliJ IDEA
1. Go to **Run** → **Edit Configurations**
2. Select your Spring Boot configuration
3. Under **Environment variables**, click the folder icon
4. Click **+** and add variables from `.env` file, or
5. Use **EnvFile** plugin to automatically load `.env` files

### Option 2: Using VS Code
1. Install the **DotENV** extension
2. The extension will automatically load `.env` files

### Option 3: Export in Terminal (macOS/Linux)
```bash
# Load .env file and export variables
export $(cat .env | xargs)

# Then run your Spring Boot application
mvn spring-boot:run
```

### Option 4: Using a Script
Create a `run.sh` script:
```bash
#!/bin/bash
export $(cat .env | grep -v '^#' | xargs)
mvn spring-boot:run
```

### Option 5: Using Maven with dotenv-java (Recommended for Production)
Add to `pom.xml`:
```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

Then create a configuration class to load `.env` automatically.

## Generating JWT Secret

Generate a secure JWT secret key:
```bash
openssl rand -base64 32
```

## Gmail App Password Setup

1. Go to your Google Account settings
2. Enable **2-Step Verification**
3. Go to **App passwords**
4. Generate a new app password for "Mail"
5. Use that 16-character password in `EMAIL_PASSWORD`

## Security Notes

- ⚠️ **Never commit `.env` file to version control** (already in `.gitignore`)
- ✅ Always use `.env.example` as a template
- ✅ Use strong, unique passwords
- ✅ Rotate secrets regularly in production
- ✅ Use different credentials for development and production

## Default Values

If environment variables are not set, the application will use default values from `application.properties`. However, it's **strongly recommended** to set all values in your `.env` file.

