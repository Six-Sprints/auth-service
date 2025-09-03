# API Reference

This document provides a comprehensive reference for all authentication endpoints provided by the Auth Service.

## Base URL

All authentication endpoints are prefixed with `/api/auth` by default.

## Authentication Headers

Most endpoints require authentication. Include the JWT token in the request header:

```
Authorization: Bearer <jwt-token>
```

## Response Format

All endpoints return responses in the following format:

```json
{
  "success": true,
  "data": { ... },
  "message": "Success message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

Error responses:

```json
{
  "success": false,
  "data": null,
  "message": "Error message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Endpoints

### 1. User Registration

**Endpoint:** `POST /api/auth/register`

**Description:** Register a new user account.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "roleSlug": "user"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "data": {
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "email": "john@example.com",
      "roleSlug": "user",
      "dateCreated": "2024-01-01T00:00:00Z"
    }
  },
  "message": "User registered successfully"
}
```

**Status Codes:**
- `201 Created` - User registered successfully
- `400 Bad Request` - Invalid input data
- `409 Conflict` - User already exists

---

### 2. User Login

**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate user with credentials.

**Request Body:**
```json
{
  "authId": "john@example.com",
  "passcode": "securePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "data": {
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "email": "john@example.com",
      "roleSlug": "user",
      "dateCreated": "2024-01-01T00:00:00Z"
    }
  },
  "message": "Login successful"
}
```

**Status Codes:**
- `200 OK` - Login successful
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Invalid credentials
- `404 Not Found` - User not found

---

### 3. OTP-Based Login

**Endpoint:** `POST /api/auth/send-otp-login`

**Description:** Send OTP for authentication and auto-register if user doesn't exist.

**Request Body:**
```json
{
  "authId": "john@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    "email": "john@example.com",
    "roleSlug": "user",
    "dateCreated": "2024-01-01T00:00:00Z"
  },
  "message": "OTP sent successfully"
}
```

**Status Codes:**
- `200 OK` - OTP sent successfully
- `400 Bad Request` - Invalid input data
- `404 Not Found` - User not found (if auto-registration fails)

---

### 4. Login with OTP

**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate user with OTP (when using OTP-based authentication).

**Request Body:**
```json
{
  "authId": "john@example.com",
  "passcode": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "data": {
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "email": "john@example.com",
      "roleSlug": "user",
      "dateCreated": "2024-01-01T00:00:00Z"
    }
  },
  "message": "Login successful"
}
```

**Status Codes:**
- `200 OK` - Login successful
- `400 Bad Request` - Invalid OTP or expired OTP
- `401 Unauthorized` - Invalid credentials

---

### 5. Send OTP for Password Reset

**Endpoint:** `POST /api/auth/send-otp`

**Description:** Send OTP for password reset.

**Query Parameters:**
- `authId` (string, required) - User's email or phone number

**Example:**
```
POST /api/auth/send-otp?authId=john@example.com
```

**Response:**
```json
{
  "success": true,
  "data": "OTP sent successfully",
  "message": "OTP sent successfully"
}
```

**Status Codes:**
- `200 OK` - OTP sent successfully
- `404 Not Found` - User not found

---

### 6. Reset Password

**Endpoint:** `POST /api/auth/reset`

**Description:** Reset password using OTP.

**Request Body:**
```json
{
  "authId": "john@example.com",
  "otp": "123456",
  "passcode": "newSecurePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "data": "Password reset successfully",
  "message": "Password reset successfully"
}
```

**Status Codes:**
- `200 OK` - Password reset successfully
- `400 Bad Request` - Invalid OTP or expired OTP
- `404 Not Found` - User not found

---

### 7. Validate Token

**Endpoint:** `POST /api/auth/validate-token`

**Description:** Validate authentication token and get current user details.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "data": {
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "email": "john@example.com",
      "roleSlug": "user",
      "dateCreated": "2024-01-01T00:00:00Z"
    }
  },
  "message": "Token is valid"
}
```

**Status Codes:**
- `200 OK` - Token is valid
- `401 Unauthorized` - Invalid or expired token

---

### 8. Logout

**Endpoint:** `POST /api/auth/logout`

**Description:** Logout user and invalidate token.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Logout successful"
}
```

**Status Codes:**
- `200 OK` - Logout successful
- `401 Unauthorized` - Invalid token (optional, depending on configuration)

---

## Error Responses

### Common Error Codes

| Status Code | Description | Example Message |
|-------------|-------------|-----------------|
| `400 Bad Request` | Invalid input data | "Invalid email format" |
| `401 Unauthorized` | Authentication required | "Invalid credentials" |
| `403 Forbidden` | Insufficient permissions | "Access denied" |
| `404 Not Found` | Resource not found | "User not found" |
| `409 Conflict` | Resource already exists | "User already exists" |
| `500 Internal Server Error` | Server error | "Internal server error" |

### Error Response Format

```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "timestamp": "2024-01-01T00:00:00Z",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

## Authentication Flow Examples

### 1. Basic Authentication Flow

```bash
# 1. Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securePassword123",
    "roleSlug": "user"
  }'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "authId": "john@example.com",
    "passcode": "securePassword123"
  }'

# 3. Use token for authenticated requests
curl -X POST http://localhost:8080/api/auth/validate-token \
  -H "Authorization: Bearer <jwt-token>"
```

### 2. OTP-Based Authentication Flow

```bash
# 1. Send OTP for login
curl -X POST http://localhost:8080/api/auth/send-otp-login \
  -H "Content-Type: application/json" \
  -d '{
    "authId": "john@example.com"
  }'

# 2. Login with OTP
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "authId": "john@example.com",
    "passcode": "123456"
  }'
```

### 3. Password Reset Flow

```bash
# 1. Send OTP for password reset
curl -X POST "http://localhost:8080/api/auth/send-otp?authId=john@example.com"

# 2. Reset password with OTP
curl -X POST http://localhost:8080/api/auth/reset \
  -H "Content-Type: application/json" \
  -d '{
    "authId": "john@example.com",
    "otp": "123456",
    "passcode": "newSecurePassword123"
  }'
```

## Rate Limiting

Some endpoints may be rate limited to prevent abuse:

- **OTP Endpoints**: Limited to prevent spam
- **Login Endpoints**: Limited to prevent brute force attacks
- **Registration Endpoints**: Limited to prevent spam registrations

Rate limit headers are included in responses:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 1640995200
```

## CORS Configuration

The service supports CORS for cross-origin requests. Configure allowed origins in your application properties:

```yaml
cors:
  allowed-origins: 
    - "http://localhost:3000"
    - "https://yourdomain.com"
  allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
  allowed-headers: [Authorization, Content-Type]
```

## WebSocket Support

For real-time applications, the service supports WebSocket connections for authentication events:

```javascript
const socket = new WebSocket('ws://localhost:8080/ws/auth');

socket.onmessage = function(event) {
  const data = JSON.parse(event.data);
  if (data.type === 'AUTH_EVENT') {
    // Handle authentication events
  }
};
```

## SDK Examples

### JavaScript/TypeScript

```typescript
class AuthClient {
  private baseUrl: string;
  private token: string | null = null;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  async register(userData: UserDto): Promise<AuthResponse> {
    const response = await fetch(`${this.baseUrl}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    return response.json();
  }

  async login(credentials: LoginDto): Promise<AuthResponse> {
    const response = await fetch(`${this.baseUrl}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    const data = await response.json();
    this.token = data.data.token;
    return data;
  }

  async validateToken(): Promise<AuthResponse> {
    const response = await fetch(`${this.baseUrl}/api/auth/validate-token`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${this.token}` }
    });
    return response.json();
  }
}
```

### Java

```java
@Service
public class AuthClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public AuthResponse register(UserDto userDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<UserDto> request = new HttpEntity<>(userDto, headers);
        
        return restTemplate.postForObject(
            baseUrl + "/api/auth/register", 
            request, 
            AuthResponse.class
        );
    }
    
    public AuthResponse login(LoginDto loginDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto, headers);
        
        return restTemplate.postForObject(
            baseUrl + "/api/auth/login", 
            request, 
            AuthResponse.class
        );
    }
}
```
