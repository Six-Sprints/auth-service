# OTP-Based Authentication

This document covers the OTP (One-Time Password) based authentication functionality provided by the Auth Service.

## Overview

OTP-based authentication provides a secure, passwordless authentication mechanism using one-time passwords sent via SMS, email, or other communication channels. This is particularly useful for mobile applications and scenarios where traditional password authentication is not suitable.

## Key Components

### OtpBasedAuthService Interface

The `OtpBasedAuthService` interface extends the base `AuthService` to provide OTP-specific functionality:

```java
public interface OtpBasedAuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO>
    extends AuthService<T, DTO, DETAIL_DTO> {
    
    DTO sendOtpForAuthAndRegisterIfNotExists(String authId);
}
```

### Otp Entity

The `Otp` entity represents a one-time password:

```java
@Document
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Otp extends AbstractMongoEntity {
    
    @NotNull
    @Size(min = 1, max = 128)
    private String otp;
    
    @NotNull
    @Indexed(unique = true)
    @Size(min = 1, max = 256)
    private String authId;
}
```

### OtpLoginDto

DTO for OTP-based login:

```java
public record OtpLoginDto(
    @NotBlank String authId,
    @NotBlank String passcode
) implements Authenticable {
    
    @Override
    public String authId() {
        return authId;
    }
    
    @Override
    public String passcode() {
        return passcode;
    }
}
```

## Authentication Flow

### 1. Send OTP for Authentication

```java
// Create OTP login DTO
OtpLoginDto otpLogin = OtpLoginDto.builder()
    .authId("john@example.com")
    .build();

// Send OTP and auto-register if user doesn't exist
UserDto user = otpAuthService.sendOtpForAuthAndRegisterIfNotExists(otpLogin.authId());

// User receives OTP via configured channel (SMS, email, etc.)
```

**Process:**
1. Checks if user exists with the provided authId
2. If user doesn't exist, automatically registers a new user
3. Generates a random OTP
4. Stores OTP in database with expiration
5. Sends OTP via configured notification channel
6. Returns user information

### 2. Login with OTP

```java
// Create login DTO with received OTP
OtpLoginDto loginWithOtp = OtpLoginDto.builder()
    .authId("john@example.com")
    .passcode("123456") // OTP received by user
    .build();

// Authenticate with OTP
AuthResponseDto<UserDetailDto> response = otpAuthService.login(loginWithOtp);

// Use token for subsequent requests
String token = response.getToken();
```

**Process:**
1. Validates the provided OTP
2. Checks OTP expiration
3. Verifies OTP matches stored value
4. Generates JWT token
5. Marks OTP as used (optional)
6. Returns authentication response

## Controller Implementation

### OTP-Based Auth Controller

```java
@RestController
@RequestMapping("/api/auth")
public class UserOtpAuthController extends AbstractOtpBasedAuthController<User, UserDto, UserDetailDto> {
    
    public UserOtpAuthController(UserOtpAuthService service) {
        super(service);
    }
    
    // Inherits all basic auth endpoints plus:
    // POST /api/auth/send-otp-login - Send OTP for authentication
}
```

### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/send-otp-login` | Send OTP and auto-register if needed |
| POST | `/api/auth/login` | Login using OTP (overridden) |
| POST | `/api/auth/register` | Register a new user (inherited) |
| POST | `/api/auth/send-otp` | Send OTP for password reset (inherited) |
| POST | `/api/auth/reset` | Reset password using OTP (inherited) |
| POST | `/api/auth/validate-token` | Validate authentication token (inherited) |
| POST | `/api/auth/logout` | Logout and invalidate token (inherited) |

## OTP Management

### OTP Generation

```java
@Service
public class OtpServiceImpl implements OtpService {
    
    @Override
    public Otp generateOtp(String authId) {
        String otpCode = generateRandomOtp();
        
        return Otp.builder()
            .authId(authId)
            .otp(otpCode)
            .build();
    }
    
    private String generateRandomOtp() {
        // Generate random OTP based on configuration
        int length = otpConfig.getLength(); // Default: 6
        return RandomStringUtils.randomNumeric(length);
    }
}
```

### OTP Validation

```java
@Override
public Otp validateOtp(String authId, String otp) throws EntityInvalidException {
    Otp storedOtp = otpRepository.findByAuthId(authId)
        .orElseThrow(() -> EntityInvalidException.childBuilder()
            .error("OTP not found")
            .build());
    
    // Check expiration
    if (isOtpExpired(storedOtp)) {
        throw EntityInvalidException.childBuilder()
            .error("OTP has expired")
            .build();
    }
    
    // Check if OTP matches
    if (!storedOtp.getOtp().equals(otp)) {
        throw EntityInvalidException.childBuilder()
            .error("Invalid OTP")
            .build();
    }
    
    return storedOtp;
}
```

### OTP Expiration

```java
private boolean isOtpExpired(Otp otp) {
    LocalDateTime expiryTime = otp.getDateCreated()
        .plusMinutes(otpConfig.getExpiryInMinutes());
    return LocalDateTime.now().isAfter(expiryTime);
}
```

## Notification Integration

### Custom OTP Notification Service

```java
@Service
public class CustomOtpService extends OtpServiceImpl {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Override
    protected void sendOtpNotification(String authId, String otp) {
        if (isEmail(authId)) {
            emailService.sendOtp(authId, otp);
        } else if (isPhoneNumber(authId)) {
            smsService.sendOtp(authId, otp);
        }
    }
    
    private boolean isEmail(String authId) {
        return authId.contains("@");
    }
    
    private boolean isPhoneNumber(String authId) {
        return authId.matches("^\\+?[1-9]\\d{1,14}$");
    }
}
```

### Email Notification Example

```java
@Service
public class EmailService {
    
    public void sendOtp(String email, String otp) {
        String subject = "Your OTP for Authentication";
        String body = String.format("Your OTP is: %s. Valid for %d minutes.", 
            otp, otpConfig.getExpiryInMinutes());
        
        emailTemplateService.sendEmail(email, subject, body);
    }
}
```

### SMS Notification Example

```java
@Service
public class SmsService {
    
    public void sendOtp(String phoneNumber, String otp) {
        String message = String.format("Your OTP is: %s. Valid for %d minutes.", 
            otp, otpConfig.getExpiryInMinutes());
        
        smsProvider.sendSms(phoneNumber, message);
    }
}
```

## Configuration

### OTP Configuration

```yaml
otp:
  expiry-in-minutes: 10
  length: 6
  max-attempts: 3
  resend-cooldown-minutes: 1
```

### Notification Configuration

```yaml
notification:
  email:
    enabled: true
    from: noreply@yourapp.com
  sms:
    enabled: true
    provider: twilio
    from: +1234567890
```

## Security Considerations

### OTP Security

1. **Random Generation**: Use cryptographically secure random number generation
2. **Expiration**: Set appropriate expiration times (typically 5-15 minutes)
3. **Single Use**: Consider marking OTPs as used after successful authentication
4. **Rate Limiting**: Implement rate limiting for OTP requests
5. **Attempt Limiting**: Limit number of failed OTP attempts

### Implementation Example

```java
@Override
public Otp generateOtp(String authId) {
    // Check rate limiting
    if (isRateLimited(authId)) {
        throw EntityInvalidException.childBuilder()
            .error("Too many OTP requests. Please try again later.")
            .build();
    }
    
    // Generate secure OTP
    String otpCode = generateSecureOtp();
    
    // Store with expiration
    Otp otp = Otp.builder()
        .authId(authId)
        .otp(otpCode)
        .build();
    
    return otpRepository.save(otp);
}

private String generateSecureOtp() {
    SecureRandom random = new SecureRandom();
    StringBuilder otp = new StringBuilder();
    
    for (int i = 0; i < otpConfig.getLength(); i++) {
        otp.append(random.nextInt(10));
    }
    
    return otp.toString();
}
```

## Error Handling

### Common OTP Exceptions

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `EntityInvalidException` | 400 | Invalid OTP, expired OTP, or rate limited |
| `EntityNotFoundException` | 404 | User not found during auto-registration |
| `EntityAlreadyExistsException` | 409 | User already exists during auto-registration |

### Example Error Handling

```java
try {
    UserDto user = otpAuthService.sendOtpForAuthAndRegisterIfNotExists(authId);
} catch (EntityInvalidException e) {
    // Handle invalid OTP, expired OTP, or rate limiting
    return ResponseEntity.badRequest()
        .body(RestResponse.error(e.getMessage()));
} catch (EntityNotFoundException e) {
    // Handle user not found during auto-registration
    return ResponseEntity.notFound().build();
}
```

## Testing

```java
@SpringBootTest
@ActiveProfiles("test")
public class OtpAuthTest extends BaseControllerTest {

    @Test
    public void testSendOtpForNewUser() {
        String authId = "newuser@example.com";
        
        UserDto user = otpAuthService.sendOtpForAuthAndRegisterIfNotExists(authId);
        
        assertThat(user.getEmail()).isEqualTo(authId);
        assertThat(user.getId()).isNotNull();
    }

    @Test
    public void testOtpLogin() {
        String authId = "test@example.com";
        String otp = "123456";
        
        // Send OTP first
        otpAuthService.sendOtpForAuthAndRegisterIfNotExists(authId);
        
        // Login with OTP
        OtpLoginDto loginDto = OtpLoginDto.builder()
            .authId(authId)
            .passcode(otp)
            .build();
        
        AuthResponseDto<UserDetailDto> response = otpAuthService.login(loginDto);
        
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getData().getEmail()).isEqualTo(authId);
    }

    @Test
    public void testExpiredOtp() {
        String authId = "test@example.com";
        String expiredOtp = "123456";
        
        // Create expired OTP
        Otp otp = Otp.builder()
            .authId(authId)
            .otp(expiredOtp)
            .dateCreated(LocalDateTime.now().minusHours(1)) // Expired
            .build();
        otpRepository.save(otp);
        
        // Try to login with expired OTP
        OtpLoginDto loginDto = OtpLoginDto.builder()
            .authId(authId)
            .passcode(expiredOtp)
            .build();
        
        assertThatThrownBy(() -> otpAuthService.login(loginDto))
            .isInstanceOf(EntityInvalidException.class)
            .hasMessageContaining("OTP has expired");
    }
}
```

## Best Practices

1. **OTP Length**: Use 6-8 digits for good balance of security and usability
2. **Expiration Time**: 5-15 minutes is typically appropriate
3. **Rate Limiting**: Implement per-user and per-IP rate limiting
4. **Notification Channels**: Support multiple channels (SMS, email, push)
5. **Audit Logging**: Log all OTP generation and validation attempts
6. **Fallback Authentication**: Provide alternative authentication methods
7. **User Experience**: Clear error messages and user guidance
