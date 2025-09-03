# Auth Service

A comprehensive Spring Boot authentication service built on top of mongo-core, providing JWT-based authentication, OTP-based authentication, role-based access control, and seamless integration with MongoDB.

## 🎯 Philosophy

Auth Service was built with a clear philosophy: **provide secure, flexible authentication without the complexity of heavy security frameworks**.

### Design Principles

- **Lightweight Security**: JWT-based authentication without Spring Security overhead
- **Flexible Authentication**: Support for both password-based and OTP-based authentication
- **Role-Based Access Control**: Built-in role and permission management
- **MongoDB Integration**: Seamless integration with mongo-core framework
- **Developer Experience**: Simple setup with opinionated defaults
- **Type Safety**: Compile-time validation through Lombok's `@FieldNameConstants`

### Why Not Spring Security?

While Spring Security is powerful, it can be overkill for many applications. Auth Service provides:

- **Simpler Setup**: No complex security configuration
- **Faster Development**: Opinionated defaults that work immediately
- **Less Dependencies**: Fewer transitive dependencies to manage
- **Easier Testing**: Simpler authentication mocking in tests
- **Better Performance**: Lighter weight authentication layer

## 🚀 Features

### Core Components

- **JWT Authentication**: Secure token-based authentication with configurable expiry
- **OTP-Based Authentication**: One-time password authentication for enhanced security
- **Role-Based Access Control**: Flexible role and permission management
- **Password Reset**: Secure password reset using OTP validation
- **Token Management**: Token validation, refresh, and logout capabilities
- **MongoDB Integration**: Built on mongo-core for seamless database operations

### Key Capabilities

- **Dual Authentication Modes**: Support for both password and OTP-based authentication
- **Automatic Registration**: OTP-based auto-registration for new users
- **Token Invalidation**: Secure logout with token blacklisting
- **Permission Validation**: Built-in permission checking utilities
- **Audit Support**: Complete audit trails for authentication events
- **Exception Handling**: Comprehensive error handling with proper HTTP responses

## 📋 Requirements

- Java 17+
- Spring Boot 3.5.5+
- MongoDB 4.4+
- Maven 3.6+
- mongo-core 3.5.500+

## 🛠️ Installation

### Maven Dependency

```xml
<dependency>
    <groupId>com.sixsprints</groupId>
    <artifactId>auth-service</artifactId>
    <version>3.5.500</version>
</dependency>
```

### Gradle Dependency

```gradle
implementation 'com.sixsprints:auth-service:3.5.500'
```

## ⚙️ Configuration

### Basic Setup

1. **Extend the authenticable entity**:

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

2. **Create an authentication service**:

```java
@Service
public class UserAuthService extends AbstractAuthService<User, UserDto, UserDetailDto> {
    public UserAuthService(GenericCrudRepository<User> repository,
                          OtpService otpService) {
        super(repository, otpService);
    }
}
```

3. **Create an authentication controller**:

```java
@RestController
@RequestMapping("/api/auth")
public class UserAuthController extends AbstractAuthController<User, UserDto, UserDetailDto, EmailLoginDto, ResetEmailPasswordDto> {
    public UserAuthController(UserAuthService service) {
        super(service);
    }
}
```

### MongoDB Configuration

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/your-database
      database: your-database
```

### Authentication Configuration

```yaml
# JWT Configuration
token:
  expiry-in-days: 30
  shared-secret: your-secret-key
  issuer: your-app-name

# OTP Configuration
otp:
  expiry-in-minutes: 10
  length: 6
```

## 📚 Documentation

### Authentication Features

Comprehensive documentation for all authentication features:

- **[Basic Authentication](docs/authentication/01-basic-auth.md)** - Password-based authentication
- **[OTP-Based Authentication](docs/otp-based-auth/01-otp-auth.md)** - One-time password authentication
- **[Role Management](docs/role-management/01-roles-permissions.md)** - Role and permission management
- **[API Reference](docs/api-reference/01-endpoints.md)** - Complete API endpoint documentation

### Quick Start Examples

#### User Registration

```java
UserDto userDto = UserDto.builder()
    .name("John Doe")
    .email("john@example.com")
    .password("securePassword123")
    .roleSlug("user")
    .build();

AuthResponseDto<UserDetailDto> response = userAuthService.register(userDto);
```

#### User Login

```java
EmailLoginDto loginDto = EmailLoginDto.builder()
    .authId("john@example.com")
    .passcode("securePassword123")
    .build();

AuthResponseDto<UserDetailDto> response = userAuthService.login(loginDto);
```

#### OTP-Based Authentication

```java
// Send OTP for login
OtpLoginDto otpLogin = OtpLoginDto.builder()
    .authId("john@example.com")
    .build();

UserDto user = otpAuthService.sendOtpForAuthAndRegisterIfNotExists(otpLogin.authId());

// Login with OTP
OtpLoginDto loginWithOtp = OtpLoginDto.builder()
    .authId("john@example.com")
    .passcode("123456") // OTP received
    .build();

AuthResponseDto<UserDetailDto> response = otpAuthService.login(loginWithOtp);
```

#### Password Reset

```java
// Send OTP for password reset
String otp = userAuthService.sendOtp("john@example.com");

// Reset password with OTP
ResetEmailPasswordDto resetDto = ResetEmailPasswordDto.builder()
    .authId("john@example.com")
    .otp("123456")
    .passcode("newSecurePassword123")
    .build();

userAuthService.resetPassword(resetDto.authId(), resetDto.otp(), resetDto.passcode());
```

## 🔐 Authentication & Authorization

### Basic Authentication

```java
@RestController
public class UserController {

    @BasicAuth(module = BasicModuleEnum.USER, permission = BasicPermissionEnum.READ)
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        User currentUser = ApplicationContext.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }
}
```

### Role-Based Authentication

```java
@Component
public class AuthInterceptor extends AbstractRoleBasedAuthInterceptor<User> {

    public AuthInterceptor(UserService userService) {
        super(userService);
    }

    @Override
    protected void checkUserPermissions(User user, ModuleDefinition module,
                                      PermissionDefinition permission, boolean required) {
        // Custom permission logic based on user roles
        if (user.getRoleSlug().equals("admin")) {
            return; // Admin has all permissions
        }

        // Check specific permissions for other roles
        if (module.getName().equals("USER") && permission.getName().equals("WRITE")) {
            if (!user.getRoleSlug().equals("manager")) {
                throw new UnauthorizedException("Insufficient permissions");
            }
        }
    }
}
```

## 🎯 Key Features

### Type-Safe Field References

```java
// Using Lombok @FieldNameConstants
Criteria criteria = Criteria.where(User.Fields.email).is("john@example.com");
Sort sort = Sort.by(User.Fields.name).ascending();
List<String> fields = Arrays.asList(User.Fields.name, User.Fields.email);
```

### Automatic Audit Fields

All authenticable entities automatically include:

- `id` - Unique identifier
- `slug` - Human-readable identifier
- `sequence` - Auto-incrementing sequence
- `password` - Encrypted password
- `roleSlug` - User role identifier
- `invalidTokens` - List of invalidated tokens
- `dateCreated` - Creation timestamp
- `dateModified` - Last modification timestamp
- `createdBy` - Creator user ID
- `lastModifiedBy` - Last modifier user ID

### Exception Handling

```java
// Exceptions are automatically converted to HTTP responses
try {
    AuthResponseDto<UserDetailDto> response = userAuthService.login(loginDto);
} catch (NotAuthenticatedException e) {
    // Automatically returns HTTP 401 with proper error message
} catch (EntityNotFoundException e) {
    // Automatically returns HTTP 404 with proper error message
}
```

## 🧪 Testing

The framework includes comprehensive test utilities:

```java
@SpringBootTest
@ActiveProfiles("test")
public class UserAuthServiceTest extends BaseControllerTest {

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
    public void testOtpAuthentication() {
        String authId = "test@example.com";
        UserDto user = otpAuthService.sendOtpForAuthAndRegisterIfNotExists(authId);
        assertThat(user.getEmail()).isEqualTo(authId);
    }
}
```

## 🔧 Advanced Configuration

### Custom OTP Service

```java
@Service
public class CustomOtpService extends OtpServiceImpl {

    @Override
    protected void sendOtpNotification(String authId, String otp) {
        // Custom OTP sending logic (SMS, email, etc.)
        emailService.sendOtp(authId, otp);
    }
}
```

### Custom Role Service

```java
@Service
public class CustomRoleService extends AbstractRoleService<Role> {

    public CustomRoleService(AbstractRoleRepository<Role> repository) {
        super(repository);
    }

    @Override
    protected void validateRolePermissions(Role role) {
        // Custom role validation logic
    }
}
```

## 📖 API Reference

### Authentication Endpoints

#### Registration & Login

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login with credentials
- `POST /api/auth/validate-token` - Validate authentication token
- `POST /api/auth/logout` - Logout and invalidate token

#### OTP Operations

- `POST /api/auth/send-otp` - Send OTP for password reset
- `POST /api/auth/send-otp-login` - Send OTP for authentication
- `POST /api/auth/reset` - Reset password using OTP

### OTP-Based Authentication Endpoints

- `POST /api/auth/send-otp-login` - Send OTP and auto-register if needed
- `POST /api/auth/login` - Login using OTP (overridden from base controller)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For questions and support:

- Check the [documentation](docs/)
- Open an issue on GitHub
- Contact the development team

## 🔄 Version History

- **3.5.500** - Current version with OTP-based authentication and role management
- **3.5.0** - Initial release with basic JWT authentication

---

**Built with ❤️ by the SixSprints team**
