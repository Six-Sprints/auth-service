# Basic Authentication

This document covers the basic password-based authentication functionality provided by the Auth Service.

## Overview

Basic authentication in Auth Service provides traditional username/email and password-based authentication using JWT tokens. This is the foundation for all authentication operations.

## Key Components

### AbstractAuthenticableEntity

All authenticable entities must extend `AbstractAuthenticableEntity`:

```java
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User extends AbstractAuthenticableEntity {
    private String name;
    private String email;
    private String department;
    // ... other fields
}
```

**Required Fields:**
- `password` - Encrypted password (automatically handled)
- `roleSlug` - User role identifier
- `invalidTokens` - List of invalidated JWT tokens

### AuthService Interface

The `AuthService` interface provides core authentication operations:

```java
public interface AuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO> 
    extends GenericCrudService<T> {
    
    AuthResponseDto<DETAIL_DTO> register(DTO dto);
    AuthResponseDto<DETAIL_DTO> login(Authenticable authenticable);
    AuthResponseDto<DETAIL_DTO> validateToken(T user);
    Otp sendOtp(String authId);
    Otp validateOtp(String authId, String otp);
    void resetPassword(String authId, String otp, String newPassword);
    void logout(T user, String token);
}
```

## Authentication Flow

### 1. User Registration

```java
// Create user DTO
UserDto userDto = UserDto.builder()
    .name("John Doe")
    .email("john@example.com")
    .password("securePassword123")
    .roleSlug("user")
    .build();

// Register user
AuthResponseDto<UserDetailDto> response = userAuthService.register(userDto);

// Response contains JWT token and user details
String token = response.getToken();
UserDetailDto userDetails = response.getData();
```

**Registration Process:**
1. Validates input data
2. Checks if user already exists
3. Encrypts password
4. Creates user entity
5. Generates JWT token
6. Returns authentication response

### 2. User Login

```java
// Create login DTO
EmailLoginDto loginDto = EmailLoginDto.builder()
    .authId("john@example.com")
    .passcode("securePassword123")
    .build();

// Authenticate user
AuthResponseDto<UserDetailDto> response = userAuthService.login(loginDto);

// Use token for subsequent requests
String token = response.getToken();
```

**Login Process:**
1. Validates credentials
2. Finds user by authentication ID
3. Verifies password
4. Generates new JWT token
5. Returns authentication response

### 3. Token Validation

```java
// Validate existing token
AuthResponseDto<UserDetailDto> response = userAuthService.validateToken(currentUser);

// Token is valid if no exception is thrown
if (response != null) {
    // User is authenticated
    UserDetailDto userDetails = response.getData();
}
```

### 4. User Logout

```java
// Logout user and invalidate token
userAuthService.logout(currentUser, token);

// Token is now invalid and cannot be used
```

## Controller Implementation

### Basic Auth Controller

```java
@RestController
@RequestMapping("/api/auth")
public class UserAuthController extends AbstractAuthController<User, UserDto, UserDetailDto, EmailLoginDto, ResetEmailPasswordDto> {
    
    public UserAuthController(UserAuthService service) {
        super(service);
    }
    
    // All endpoints are automatically provided:
    // POST /api/auth/register
    // POST /api/auth/login
    // POST /api/auth/send-otp
    // POST /api/auth/reset
    // POST /api/auth/validate-token
    // POST /api/auth/logout
}
```

### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login with credentials |
| POST | `/api/auth/send-otp` | Send OTP for password reset |
| POST | `/api/auth/reset` | Reset password using OTP |
| POST | `/api/auth/validate-token` | Validate authentication token |
| POST | `/api/auth/logout` | Logout and invalidate token |

## Password Reset Flow

### 1. Send OTP

```java
// Send OTP to user's email/phone
String message = userAuthService.sendOtp("john@example.com");
// Returns: "OTP sent successfully"
```

### 2. Reset Password

```java
// Create reset DTO
ResetEmailPasswordDto resetDto = ResetEmailPasswordDto.builder()
    .authId("john@example.com")
    .otp("123456")
    .passcode("newSecurePassword123")
    .build();

// Reset password
userAuthService.resetPassword(resetDto.authId(), resetDto.otp(), resetDto.passcode());
```

## Security Features

### Password Encryption

Passwords are automatically encrypted using BCrypt:

```java
// Password is encrypted before storage
user.setPassword(passwordEncoder.encode(rawPassword));
```

### Token Management

JWT tokens include:
- User ID and role information
- Expiration time
- Issuer information
- Custom claims

### Token Invalidation

Invalidated tokens are stored in the user entity:

```java
// Add token to invalid list
user.getInvalidTokens().add(token);
userService.updateOneById(user.getId(), user);
```

## Error Handling

### Common Exceptions

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `EntityAlreadyExistsException` | 409 | User already exists during registration |
| `EntityNotFoundException` | 404 | User not found during login |
| `NotAuthenticatedException` | 401 | Invalid credentials |
| `EntityInvalidException` | 400 | Invalid input data or OTP |

### Example Error Handling

```java
try {
    AuthResponseDto<UserDetailDto> response = userAuthService.login(loginDto);
} catch (NotAuthenticatedException e) {
    // Returns HTTP 401 with error message
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(RestResponse.error("Invalid credentials"));
} catch (EntityNotFoundException e) {
    // Returns HTTP 404 with error message
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(RestResponse.error("User not found"));
}
```

## Configuration

### JWT Configuration

```yaml
token:
  expiry-in-days: 30
  shared-secret: your-secret-key
  issuer: your-app-name
```

### OTP Configuration

```yaml
otp:
  expiry-in-minutes: 10
  length: 6
```

## Best Practices

1. **Password Requirements**: Implement strong password policies
2. **Token Expiry**: Use appropriate token expiration times
3. **HTTPS**: Always use HTTPS in production
4. **Rate Limiting**: Implement rate limiting for login attempts
5. **Audit Logging**: Log all authentication events
6. **Token Refresh**: Consider implementing token refresh mechanism

## Testing

```java
@SpringBootTest
@ActiveProfiles("test")
public class BasicAuthTest extends BaseControllerTest {

    @Test
    public void testUserRegistration() {
        UserDto userDto = UserDto.builder()
            .name("Test User")
            .email("test@example.com")
            .password("testPassword123")
            .roleSlug("user")
            .build();

        AuthResponseDto<UserDetailDto> response = userAuthService.register(userDto);
        
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getData().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testUserLogin() {
        EmailLoginDto loginDto = EmailLoginDto.builder()
            .authId("test@example.com")
            .passcode("testPassword123")
            .build();

        AuthResponseDto<UserDetailDto> response = userAuthService.login(loginDto);
        
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getData().getEmail()).isEqualTo("test@example.com");
    }
}
```
