# Authentication Hooks

This document explains how to use the authentication hook interfaces provided by the auth-service library to add custom logic during registration and login operations.

## Overview

The auth-service library provides a hook system that allows you to intercept and add custom business logic at different stages of the authentication lifecycle. These hooks are automatically executed by the `AbstractAuthService` and provide a clean way to add validation, logging, notifications, or any other custom logic without modifying the core authentication implementation.

All hooks are optional and use Spring's `@Autowired(required = false)` mechanism, meaning your service will work perfectly fine without implementing any hooks.

## Available Hook Interfaces

### 1. PreRegisterHook

**Purpose**: Executed before a user registration is completed, after DTO mapping but before the entity is persisted to the database.

**Full Qualified Name**: `com.sixsprints.auth.hooks.PreRegisterHook`

**Interface**:

```java
public interface PreRegisterHook<T extends AbstractAuthenticableEntity> {
    void preRegister(T entity);
}
```

**Use Cases**:

- Validate custom business rules before registration
- Set additional default values or computed fields
- Check external systems or APIs
- Throw exceptions to prevent registration if needed

**Example Implementation**:

```java
package com.example.user.hooks;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.hooks.PreRegisterHook;
import com.example.user.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserPreRegisterHook implements PreRegisterHook<User> {

    private final MessageSource messageSource;

    @Override
    public void preRegister(User user) {
        // Example: Validate email domain
        if (user.getEmail() != null && !user.getEmail().endsWith("@company.com")) {
            String message = messageSource.getMessage(
                "user.email.domain.invalid",
                null,
                Locale.ENGLISH
            );
            throw new IllegalArgumentException(message);
        }

        // Example: Set default values
        if (user.getDepartment() == null) {
            user.setDepartment("General");
        }
    }
}
```

**Note**: Hook methods do not declare checked exceptions, so you must use unchecked exceptions like `IllegalArgumentException`, `IllegalStateException`, etc. The error message key `user.email.domain.invalid` should be defined in your `messages.properties` file (see [Error Message Keys](#error-message-keys) section below).

### 2. PostRegisterHook

**Purpose**: Executed after a user registration has been successfully completed and the entity has been persisted with a valid ID.

**Full Qualified Name**: `com.sixsprints.auth.hooks.PostRegisterHook`

**Interface**:

```java
public interface PostRegisterHook<T extends AbstractAuthenticableEntity> {
    void postRegister(T entity);
}
```

**Use Cases**:

- Send welcome emails or notifications
- Log registration events
- Create related entities (profiles, settings, etc.)
- Trigger webhooks or external system notifications
- Update analytics or metrics

**Example Implementation**:

```java
package com.example.user.hooks;

import org.springframework.stereotype.Component;
import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.hooks.PostRegisterHook;
import com.sixsprints.notification.service.NotificationService;
import com.example.user.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserPostRegisterHook implements PostRegisterHook<User> {

    private final NotificationService notificationService;

    @Override
    public void postRegister(User user) {
        // Send welcome email
        notificationService.sendMessage(
            MessageDto.builder()
                .to(user.getEmail())
                .subject("Welcome to Our Platform!")
                .content("Thank you for registering, " + user.getName())
                .build()
        );

        // Log the registration
        log.info("New user registered: {} (ID: {})", user.getEmail(), user.getId());
    }
}
```

### 3. PostLoginHook

**Purpose**: Executed after password validation succeeds but before token generation. This allows for additional validation checks that might prevent login even after password is correct.

**Full Qualified Name**: `com.sixsprints.auth.hooks.PostLoginHook`

**Interface**:

```java
public interface PostLoginHook<T extends AbstractAuthenticableEntity> {
    void postLogin(T user);
}
```

**Use Cases**:

- Check if user account is active/enabled
- Validate IP address or geolocation
- Enforce business hours login restrictions
- Check for pending actions (e.g., password reset required, terms acceptance)
- Track login attempts or update last login timestamp
- Throw exceptions to prevent login if additional conditions aren't met

**Example Implementation**:

```java
package com.example.user.hooks;

import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.hooks.PostLoginHook;
import com.example.user.User;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserPostLoginHook implements PostLoginHook<User> {

    private final MessageSource messageSource;
    private final UserService userService;

    @Override
    public void postLogin(User user) {
        // Check if account is active
        if (!user.isActive()) {
            String message = messageSource.getMessage(
                "user.account.disabled",
                null,
                Locale.ENGLISH
            );
            throw new IllegalStateException(message);
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            String message = messageSource.getMessage(
                "user.email.not.verified",
                null,
                Locale.ENGLISH
            );
            throw new IllegalStateException(message);
        }

        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userService.patchUpdateOneById(
            user.getId(),
            user,
            User.Fields.lastLoginAt
        );

        // Log successful login
        log.info("User logged in: {} at {}", user.getEmail(), LocalDateTime.now());
    }
}
```

**Note**: Hook methods do not declare checked exceptions, so you must use unchecked exceptions like `IllegalStateException`, `IllegalArgumentException`, etc. The error message keys (`user.account.disabled`, `user.email.not.verified`) should be defined in your `messages.properties` file (see [Error Message Keys](#error-message-keys) section below).

## Hook Execution Order

### During Registration (`register()` method):

1. DTO is mapped to domain entity
2. **PreRegisterHook** is executed
3. Entity is validated
4. Entity is persisted to database
5. **PostRegisterHook** is executed
6. Token is generated and returned

### During Login (`login()` method):

1. User is fetched by auth ID
2. Password is validated
3. **PostLoginHook** is executed
4. Token is generated and returned

## Important Notes

### Multiple Hooks

You can register multiple hooks of the same type. They will be executed in the order Spring registers them as beans. All hooks will be executed sequentially.

```java
@Component
public class UserValidationHook implements PreRegisterHook<User> {
    // First hook
}

@Component
public class UserEnrichmentHook implements PreRegisterHook<User> {
    // Second hook
}
```

### Exception Handling Behavior

- **PreRegisterHook** and **PostLoginHook**: Throwing unchecked exceptions will prevent the operation from completing. The exception will be propagated to the caller.
- **PostRegisterHook**: Throwing exceptions will not rollback the registration (user is already saved), but will prevent token generation. Use with caution.

### Generic Type Binding

Hooks are bound to specific entity types through generics. A hook for `User` will only be invoked for `User` operations, not for other authenticable entities.

### Exception Handling in Hooks

**Important**: Hook interface methods do not declare any checked exceptions (e.g., `EntityInvalidException`, `NotAuthenticatedException`). This means you can only throw unchecked (runtime) exceptions from hook implementations.

**Allowed exceptions:**

- `IllegalArgumentException` - For invalid input or validation failures
- `IllegalStateException` - For invalid state conditions
- `RuntimeException` - Generic runtime exception
- Any other custom unchecked exceptions

**Not allowed:**

- `EntityInvalidException` (checked exception)
- `NotAuthenticatedException` (checked exception)
- `EntityNotFoundException` (checked exception)

### Required Imports

When implementing hooks, you'll typically need:

```java
import org.springframework.stereotype.Component;
import org.springframework.context.MessageSource;
import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.hooks.PreRegisterHook;  // or PostRegisterHook or PostLoginHook
import java.util.Locale;
import lombok.RequiredArgsConstructor;
```

## Error Message Keys and MessageSource

**Important**: It is strongly recommended to use Spring's `MessageSource` with message keys instead of hard-coded error messages when throwing exceptions in hooks.

### Why Use Message Keys with MessageSource?

- **Internationalization**: Easy to support multiple languages
- **Consistency**: Centralized error message management
- **Maintainability**: Update messages without changing code
- **Flexibility**: Change error messages without recompiling code

### Configuration

Define your message keys in `src/main/resources/messages.properties`:

```properties
# Pre-registration validation messages
user.email.domain.invalid=Only company email addresses are allowed
user.age.below.minimum=User must be at least 18 years old

# Post-login validation messages
user.account.disabled=Account is disabled. Please contact support.
user.email.not.verified=Please verify your email before logging in.
user.password.expired=Your password has expired. Please reset it.
user.terms.not.accepted=You must accept the terms and conditions to continue.
```

### Usage in Hooks with MessageSource

Always inject `MessageSource` and use it to resolve message keys:

```java
@Component
@RequiredArgsConstructor
public class UserPreRegisterHook implements PreRegisterHook<User> {

    private final MessageSource messageSource;

    @Override
    public void preRegister(User user) {
        if (user.getEmail() != null && !user.getEmail().endsWith("@company.com")) {
            // Good - Using MessageSource with message key
            String message = messageSource.getMessage(
                "user.email.domain.invalid",
                null,
                Locale.ENGLISH
            );
            throw new IllegalArgumentException(message);
        }
    }
}
```

**Bad - Hard-coded message**:

```java
@Override
public void preRegister(User user) {
    if (user.getEmail() != null && !user.getEmail().endsWith("@company.com")) {
        // Bad - Hard-coded message
        throw new IllegalArgumentException("Only company email addresses are allowed");
    }
}
```

### Message Keys with Arguments

You can pass arguments that will be interpolated into the message:

**messages.properties**:

```properties
user.email.domain.invalid=Only {0} email addresses are allowed
user.minimum.age.required=User must be at least {0} years old
```

**Hook Implementation**:

```java
@Override
public void preRegister(User user) {
    if (user.getAge() < 18) {
        String message = messageSource.getMessage(
            "user.minimum.age.required",
            new Object[]{18},  // Arguments array
            Locale.ENGLISH
        );
        throw new IllegalArgumentException(message);
    }
}
```

## Best Practices

1. **Keep hooks focused**: Each hook should have a single responsibility
2. **Use appropriate hook type**: Pre-hooks for validation, post-hooks for side effects
3. **Handle exceptions carefully**: Consider the impact of throwing exceptions at each stage
4. **Use only unchecked exceptions**: Hook methods don't declare checked exceptions, so use `IllegalArgumentException`, `IllegalStateException`, or other runtime exceptions
5. **Always use MessageSource with message keys**: Inject Spring's `MessageSource` and define error messages in `messages.properties` for better maintainability and internationalization support
6. **Avoid heavy operations in pre-hooks**: They block the main flow
7. **Use async processing for post-hooks when possible**: For operations like sending emails or calling external APIs
8. **Log important actions**: Especially in post-hooks for audit trails
