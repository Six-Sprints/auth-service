package com.sixsprints.auth.util;

/**
 * Constants for auth service messages and error keys
 * This class consolidates all hard-coded strings used in the auth service
 */
public final class AuthMessageKeys {

  private AuthMessageKeys() {
    // Utility class
  }

  // Exception messages (from original Messages interface)
  public static final String EXCEPTION_UNEXPECTED = "exception.unexpected";
  public static final String LOGIN_FAILED = "exception.login.failed";
  public static final String LOGIN_FAILED_UNREGISTERED = "exception.loginfailed.unregistered";
  public static final String LOGIN_FAILED_MISMATCH = "exception.loginfailed.mismatch";
  public static final String USER_ALREADY_EXISTS = "exception.user.already.exists";
  public static final String USER_IS_INVALID = "exception.user.invalid";

  // Success messages
  public static final String OTP_SENT_SUCCESSFULLY = "message.otp.sent.successfully";
  public static final String PASSWORD_RESET_SUCCESSFULLY = "message.password.reset.successfully";

  // Role constants
  public static final String ROLE = "Role";
  public static final String NO_ROLE = "None";

  // OTP message constants
  public static final String OTP_GENERATED_SUBJECT = "message.otp.generated.subject";
  public static final String OTP_GENERATED_CONTENT = "message.otp.generated.content";

  // Log messages
  public static final String ERROR_UPDATING_INVALID_TOKENS = "log.error.updating.invalid.tokens";

}
