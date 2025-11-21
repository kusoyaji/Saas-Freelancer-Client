# Freelancer Portal API - Endpoints Documentation

## Server Information
- **Base URL**: `http://localhost:8080`
- **Port**: `8080`
- **Status**: ✅ Running

## Health Check Endpoints

### Actuator Health Check
```bash
GET http://localhost:8080/actuator/health
```
**Response**: Application health status including database connectivity

### Actuator Info
```bash
GET http://localhost:8080/actuator/info
```
**Response**: Application information

---

## Authentication Endpoints (`/auth`)

### Register User
```bash
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "FREELANCER|CLIENT|ADMIN"
}
```
**Access**: Public ✅

### Login
```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```
**Access**: Public ✅
**Returns**: JWT token

### Logout
```bash
POST http://localhost:8080/auth/logout
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

---

## User Management (`/users`)

### Get User Profile
```bash
GET http://localhost:8080/users/me
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Update User Profile
```bash
PUT http://localhost:8080/users/me
Authorization: Bearer <token>
Content-Type: application/json
```
**Access**: Authenticated 🔒

---

## Project Management (`/projects`)

### Get All Projects
```bash
GET http://localhost:8080/projects
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Create Project
```bash
POST http://localhost:8080/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "budget": number,
  "clientId": number
}
```
**Access**: Authenticated 🔒

### Get Project by ID
```bash
GET http://localhost:8080/projects/{id}
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Update Project
```bash
PUT http://localhost:8080/projects/{id}
Authorization: Bearer <token>
Content-Type: application/json
```
**Access**: Authenticated 🔒

### Delete Project
```bash
DELETE http://localhost:8080/projects/{id}
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

---

## Time Entry Management (`/time-entries`)

### Get All Time Entries
```bash
GET http://localhost:8080/time-entries
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Create Time Entry
```bash
POST http://localhost:8080/time-entries
Authorization: Bearer <token>
Content-Type: application/json

{
  "projectId": number,
  "startTime": "ISO-8601 datetime",
  "endTime": "ISO-8601 datetime",
  "description": "string"
}
```
**Access**: Authenticated 🔒

---

## Invoice Management (`/invoices`)

### Get All Invoices
```bash
GET http://localhost:8080/invoices
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Create Invoice
```bash
POST http://localhost:8080/invoices
Authorization: Bearer <token>
Content-Type: application/json

{
  "projectId": number,
  "items": [
    {
      "description": "string",
      "quantity": number,
      "unitPrice": number
    }
  ]
}
```
**Access**: Authenticated 🔒

### Get Invoice by ID
```bash
GET http://localhost:8080/invoices/{id}
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

---

## Payment Management (`/payments`)

### Get All Payments
```bash
GET http://localhost:8080/payments
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Create Payment
```bash
POST http://localhost:8080/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "invoiceId": number,
  "amount": number,
  "paymentMethod": "string"
}
```
**Access**: Authenticated 🔒

---

## Messaging (`/messages` & `/conversations`)

### Get Conversations
```bash
GET http://localhost:8080/conversations
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Get Messages
```bash
GET http://localhost:8080/messages
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Send Message
```bash
POST http://localhost:8080/messages
Authorization: Bearer <token>
Content-Type: application/json

{
  "conversationId": number,
  "content": "string"
}
```
**Access**: Authenticated 🔒

---

## File Management (`/files`)

### Upload File
```bash
POST http://localhost:8080/api/files/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <binary>
```
**Access**: Authenticated 🔒

### Get File
```bash
GET http://localhost:8080/files/{id}
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Download File
```bash
GET http://localhost:8080/files/{id}/download
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

---

## Notifications (`/notifications`)

### Get All Notifications
```bash
GET http://localhost:8080/notifications
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

### Mark as Read
```bash
PUT http://localhost:8080/notifications/{id}/read
Authorization: Bearer <token>
```
**Access**: Authenticated 🔒

---

## WebSocket Endpoints

### Connect to WebSocket
```
ws://localhost:8080/ws
```
**Access**: Public ✅

### Subscribe to Topics
- `/topic/notifications` - Receive notifications
- `/topic/messages` - Receive messages
- `/user/queue/private` - Private messages

---

## Testing with curl

### Test Health Endpoint (After Restart)
```bash
curl http://localhost:8080/actuator/health
```

### Test Registration
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "role": "FREELANCER"
  }'
```

### Test Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Test Authenticated Endpoint
```bash
# Replace <YOUR_JWT_TOKEN> with the token from login response
curl http://localhost:8080/users/me \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

---

## Current Issues Observed

1. ⚠️ **Zipkin Tracing**: Connection to Zipkin (port 9411) is failing - This is non-critical and has been disabled in configuration
2. ⚠️ **Open-in-view Warning**: Has been disabled in configuration

## Notes

- All endpoints (except `/auth/**`, `/actuator/health/**`, and WebSocket endpoints) require JWT authentication
- JWT tokens expire after 24 hours (86400000 ms)
- Refresh tokens expire after 7 days (604800000 ms)
- Database: MySQL on `localhost:3306/freelancerporal`

---

**Last Updated**: 2025-11-21
**Application Version**: Running successfully on port 8080
