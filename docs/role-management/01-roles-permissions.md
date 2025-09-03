# Role Management

This document covers the role-based access control (RBAC) functionality provided by the Auth Service.

## Overview

Role-based access control provides a flexible and scalable way to manage user permissions. The Auth Service includes built-in support for roles and permissions, allowing you to control access to different parts of your application based on user roles.

## Key Components

### AbstractRole Entity

All role entities must extend `AbstractRole`:

```java
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Role extends AbstractRole {
    private String description;
    private boolean isActive;
    // ... other role-specific fields
}
```

### ModulePermission Entity

The `ModulePermission` entity represents permissions for specific modules:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModulePermission {

    @NotNull
    private String moduleName;

    @NotNull
    private String permissionName;

    private boolean granted;
}
```

### AbstractRoleBasedAuthInterceptor

The interceptor handles role-based authentication and authorization:

```java
@Component
public class AuthInterceptor extends AbstractRoleBasedAuthInterceptor<User> {

    public AuthInterceptor(UserService userService) {
        super(userService);
    }

    @Override
    protected void checkUserPermissions(User user, ModuleDefinition module,
                                      PermissionDefinition permission, boolean required) {
        // Custom permission checking logic
    }
}
```

## Role Structure

### Role Entity Fields

```java
public abstract class AbstractRole extends AbstractMongoEntity {

    @NotNull
    @Size(min = 1, max = 64)
    private String name;

    @NotNull
    @Size(min = 1, max = 64)
    private String slug;

    @Singular
    private List<ModulePermission> permissions;

    private boolean isDefault;
    private boolean isActive;
}
```

### Permission Structure

```java
public class ModulePermission {
    private String moduleName;    // e.g., "USER", "PRODUCT", "ORDER"
    private String permissionName; // e.g., "READ", "WRITE", "DELETE", "ADMIN"
    private boolean granted;      // true if permission is granted
}
```

## Role Management

### Creating Roles

```java
@Service
public class RoleService extends AbstractRoleService<Role> {

    public RoleService(AbstractRoleRepository<Role> repository) {
        super(repository);
    }

    public Role createAdminRole() {
        List<ModulePermission> permissions = Arrays.asList(
            ModulePermission.builder()
                .moduleName("USER")
                .permissionName("READ")
                .granted(true)
                .build(),
            ModulePermission.builder()
                .moduleName("USER")
                .permissionName("WRITE")
                .granted(true)
                .build(),
            ModulePermission.builder()
                .moduleName("USER")
                .permissionName("DELETE")
                .granted(true)
                .build(),
            ModulePermission.builder()
                .moduleName("PRODUCT")
                .permissionName("READ")
                .granted(true)
                .build(),
            ModulePermission.builder()
                .moduleName("PRODUCT")
                .permissionName("WRITE")
                .granted(true)
                .build()
        );

        return Role.builder()
            .name("Administrator")
            .slug("admin")
            .permissions(permissions)
            .isDefault(false)
            .isActive(true)
            .build();
    }

    public Role createUserRole() {
        List<ModulePermission> permissions = Arrays.asList(
            ModulePermission.builder()
                .moduleName("USER")
                .permissionName("READ")
                .granted(true)
                .build(),
            ModulePermission.builder()
                .moduleName("PRODUCT")
                .permissionName("READ")
                .granted(true)
                .build()
        );

        return Role.builder()
            .name("User")
            .slug("user")
            .permissions(permissions)
            .isDefault(true)
            .isActive(true)
            .build();
    }
}
```

### Role Assignment

```java
// Assign role to user during registration
UserDto userDto = UserDto.builder()
    .name("John Doe")
    .email("john@example.com")
    .password("securePassword123")
    .roleSlug("user") // Assign user role
    .build();

AuthResponseDto<UserDetailDto> response = userAuthService.register(userDto);
```

### Role Updates

```java
// Update user role
User user = userService.findOneById(userId).orElseThrow();
user.setRoleSlug("admin");
userService.updateOneById(userId, user);
```

## Permission Checking

### Basic Permission Annotation

```java
@RestController
public class UserController {

    @BasicAuth(module = BasicModuleEnum.USER, permission = BasicPermissionEnum.READ)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // Only users with USER:READ permission can access
        return ResponseEntity.ok(userService.findAllList());
    }

    @BasicAuth(module = BasicModuleEnum.USER, permission = BasicPermissionEnum.WRITE)
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
        // Only users with USER:WRITE permission can access
        return ResponseEntity.ok(userService.insertOne(userDto));
    }

    @BasicAuth(module = BasicModuleEnum.USER, permission = BasicPermissionEnum.DELETE)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        // Only users with USER:DELETE permission can access
        userService.deleteOneById(id);
        return ResponseEntity.ok().build();
    }
}
```

### Custom Permission Checking

```java
@Component
public class AuthInterceptor extends AbstractRoleBasedAuthInterceptor<User> {

    public AuthInterceptor(UserService userService) {
        super(userService);
    }

    @Override
    protected void checkUserPermissions(User user, ModuleDefinition module,
                                      PermissionDefinition permission, boolean required) {

        // Get user's role
        Role role = roleService.findBySlug(user.getRoleSlug())
            .orElseThrow(() -> new UnauthorizedException("User role not found"));

        // Check if role has the required permission
        boolean hasPermission = role.getPermissions().stream()
            .anyMatch(p -> p.getModuleName().equals(module.getName()) &&
                          p.getPermissionName().equals(permission.getName()) &&
                          p.isGranted());

        if (!hasPermission && required) {
            throw new UnauthorizedException(
                String.format("User does not have %s:%s permission",
                    module.getName(), permission.getName()));
        }
    }
}
```

### Advanced Permission Logic

```java
@Override
protected void checkUserPermissions(User user, ModuleDefinition module,
                                  PermissionDefinition permission, boolean required) {

    // Admin users have all permissions
    if ("admin".equals(user.getRoleSlug())) {
        return;
    }

    // Get user's role
    Role role = roleService.findBySlug(user.getRoleSlug())
        .orElseThrow(() -> new UnauthorizedException("User role not found"));

    // Check specific permissions
    boolean hasPermission = false;

    switch (module.getName()) {
        case "USER":
            hasPermission = checkUserPermissions(user, role, permission);
            break;
        case "PRODUCT":
            hasPermission = checkProductPermissions(user, role, permission);
            break;
        case "ORDER":
            hasPermission = checkOrderPermissions(user, role, permission);
            break;
        default:
            hasPermission = checkDefaultPermissions(role, module, permission);
    }

    if (!hasPermission && required) {
        throw new UnauthorizedException("Insufficient permissions");
    }
}

private boolean checkUserPermissions(User user, Role role, PermissionDefinition permission) {
    // Users can only read their own data unless they have USER:READ permission
    if ("READ".equals(permission.getName())) {
        return role.getPermissions().stream()
            .anyMatch(p -> "USER".equals(p.getModuleName()) &&
                          "READ".equals(p.getPermissionName()) &&
                          p.isGranted());
    }

    // Check role permissions for other operations
    return role.getPermissions().stream()
        .anyMatch(p -> "USER".equals(p.getModuleName()) &&
                      permission.getName().equals(p.getPermissionName()) &&
                      p.isGranted());
}
```

## Role Service Operations

### Role CRUD Operations

```java
@Service
public class RoleService extends AbstractRoleService<Role> {

    // Create role
    public Role createRole(Role role) {
        return insertOne(role);
    }

    // Update role
    public Role updateRole(String roleId, Role role) {
        return updateOneById(roleId, role);
    }

    // Delete role
    public void deleteRole(String roleId) {
        deleteOneById(roleId);
    }

    // Find role by slug
    public Optional<Role> findBySlug(String slug) {
        return findOneByCriteria(Criteria.where(Role.Fields.slug).is(slug));
    }

    // Get all active roles
    public List<Role> getActiveRoles() {
        return filterByCriteria(Criteria.where(Role.Fields.isActive).is(true));
    }

    // Get default role
    public Optional<Role> getDefaultRole() {
        return findOneByCriteria(Criteria.where(Role.Fields.isDefault).is(true));
    }
}
```

### Permission Management

```java
// Add permission to role
public Role addPermission(String roleId, String moduleName, String permissionName) {
    Role role = findOneById(roleId).orElseThrow();

    ModulePermission permission = ModulePermission.builder()
        .moduleName(moduleName)
        .permissionName(permissionName)
        .granted(true)
        .build();

    role.getPermissions().add(permission);
    return updateOneById(roleId, role);
}

// Remove permission from role
public Role removePermission(String roleId, String moduleName, String permissionName) {
    Role role = findOneById(roleId).orElseThrow();

    role.getPermissions().removeIf(p ->
        p.getModuleName().equals(moduleName) &&
        p.getPermissionName().equals(permissionName));

    return updateOneById(roleId, role);
}

// Check if role has permission
public boolean hasPermission(String roleSlug, String moduleName, String permissionName) {
    return findBySlug(roleSlug)
        .map(role -> role.getPermissions().stream()
            .anyMatch(p -> p.getModuleName().equals(moduleName) &&
                          p.getPermissionName().equals(permissionName) &&
                          p.isGranted()))
        .orElse(false);
}
```

## Configuration

### Role Configuration

```yaml
roles:
  default-role: user
  admin-role: admin
  auto-assign-default: true
```

### Permission Configuration

```yaml
permissions:
  modules:
    - name: USER
      permissions: [READ, WRITE, DELETE, ADMIN]
    - name: PRODUCT
      permissions: [READ, WRITE, DELETE, ADMIN]
    - name: ORDER
      permissions: [READ, WRITE, DELETE, ADMIN]
    - name: REPORT
      permissions: [READ, ADMIN]
```

## Testing

```java
@SpringBootTest
@ActiveProfiles("test")
public class RoleManagementTest extends BaseControllerTest {

    @Test
    public void testCreateRole() {
        Role role = Role.builder()
            .name("Manager")
            .slug("manager")
            .permissions(Arrays.asList(
                ModulePermission.builder()
                    .moduleName("USER")
                    .permissionName("READ")
                    .granted(true)
                    .build(),
                ModulePermission.builder()
                    .moduleName("PRODUCT")
                    .permissionName("WRITE")
                    .granted(true)
                    .build()
            ))
            .isActive(true)
            .build();

        Role created = roleService.createRole(role);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getSlug()).isEqualTo("manager");
        assertThat(created.getPermissions()).hasSize(2);
    }

    @Test
    public void testPermissionChecking() {
        // Create user with specific role
        User user = User.builder()
            .name("Test User")
            .email("test@example.com")
            .roleSlug("user")
            .build();

        // Test permission checking
        boolean hasReadPermission = roleService.hasPermission("user", "USER", "READ");
        boolean hasWritePermission = roleService.hasPermission("user", "USER", "WRITE");

        assertThat(hasReadPermission).isTrue();
        assertThat(hasWritePermission).isFalse();
    }

    @Test
    public void testRoleBasedAccess() {
        // Test endpoint access with different roles
        User adminUser = createUserWithRole("admin");
        User regularUser = createUserWithRole("user");

        // Admin should have access to all endpoints
        String adminToken = authenticateUser(adminUser);
        ResponseEntity<String> adminResponse = testClient.getForEntity("/api/users", String.class, adminToken);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Regular user should have limited access
        String userToken = authenticateUser(regularUser);
        ResponseEntity<String> userResponse = testClient.getForEntity("/api/users", String.class, userToken);
        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## Best Practices

1. **Principle of Least Privilege**: Grant only necessary permissions
2. **Role Hierarchy**: Design clear role hierarchies
3. **Permission Granularity**: Use appropriate permission granularity
4. **Regular Audits**: Regularly audit role assignments and permissions
5. **Default Roles**: Provide sensible default roles for new users
6. **Permission Caching**: Cache permission checks for performance
7. **Audit Logging**: Log all permission checks and role changes
8. **Documentation**: Document all roles and their permissions clearly
