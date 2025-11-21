# Quick Test - Registration Fix

## Issue Found
**Problem**: CORS configuration was only allowing requests from `http://localhost:4200`  
**Solution**: Updated to allow all origins during development using `allowedOriginPatterns`

## What Was Changed
File: `SecurityConfig.java`
- Changed from: `configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"))`
- Changed to: `configuration.setAllowedOriginPatterns(Collections.singletonList("*"))`

This allows requests from any origin (Postman, curl, browser, etc.)

---

## Test Registration Now

### Required Fields for Registration:
```json
{
  "firstName": "string (min 2, max 50 chars)",
  "lastName": "string (min 2, max 50 chars)",
  "email": "valid email",
  "password": "string (min 8 chars)",
  "role": "FREELANCER or CLIENT or ADMIN"
}
```

### Optional Fields:
- `phone` (max 15 chars)
- `bio` (max 2000 chars)
- `website` (max 255 chars)
- `company` (max 100 chars)
- `position` (max 100 chars)

---

## PowerShell Test Commands

### 1. Restart Your Application First!
Stop and restart your application to apply the CORS changes.

### 2. Test Registration
```powershell
$body = @{
    firstName = "John"
    lastName = "Doe"
    email = "john.doe@example.com"
    password = "SecurePass123!"
    role = "FREELANCER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/register" -Method Post -Body $body -ContentType "application/json"
```

### 3. Test Login
```powershell
$loginBody = @{
    email = "john.doe@example.com"
    password = "SecurePass123!"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$response
```

### 4. Save Token for Authenticated Requests
```powershell
$token = $response.token
Write-Host "Token: $token"
```

---

## Using curl (Alternative)

### Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"password\":\"SecurePass123!\",\"role\":\"FREELANCER\"}"
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"john.doe@example.com\",\"password\":\"SecurePass123!\"}"
```

---

## Using Postman

### Setup
1. **Method**: POST
2. **URL**: `http://localhost:8080/auth/register`
3. **Headers**: 
   - `Content-Type`: `application/json`
4. **Body** (raw JSON):
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "role": "FREELANCER"
}
```

### Expected Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "...",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "FREELANCER"
  }
}
```

---

## Common Validation Errors

### If you get validation errors:

**Error: "First name is required"**
- Make sure `firstName` field is included and not empty

**Error: "Email is required"** or **"Valid email format is required"**
- Ensure email field has valid format: `user@domain.com`

**Error: "Password must be at least 8 characters"**
- Password should be minimum 8 characters

**Error: Field name mismatch**
- Use exact field names: `firstName`, `lastName`, `email`, `password`, `role`
- Note: NOT `username`, it's `email` for login

---

## Important Notes

⚠️ **RESTART YOUR APPLICATION** after the CORS fix!

📧 **Login uses EMAIL, not username**
```json
{
  "email": "user@example.com",
  "password": "your-password"
}
```

🔒 **Available Roles**:
- `FREELANCER`
- `CLIENT`
- `ADMIN`

---

**After successful registration, you'll receive a JWT token that you can use for authenticated endpoints.**
