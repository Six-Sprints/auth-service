package com.sixsprints.auth.service;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.service.GenericCrudService;

/**
 * Service interface for authentication operations on authenticable entities.
 * Provides comprehensive authentication functionality including registration, login,
 * token validation, OTP-based operations, and password reset capabilities.
 *
 * @param <T> the type of authenticable entity extending AbstractAuthenticableEntity
 * @param <DTO> the data transfer object type for registration and authentication
 * @param <DETAIL_DTO> the detailed data transfer object type for responses
 * @author SixSprints
 * @since 1.0
 */
public interface AuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO>
    extends GenericCrudService<T> {

  /**
   * Registers a new authenticable entity with the system.
   * Creates a new user account and returns authentication response with token and user details.
   *
   * @param dto the data transfer object containing registration information
   * @return AuthResponseDto containing authentication token and user details
   * @throws EntityAlreadyExistsException if an entity with the same identifier already exists
   * @throws EntityInvalidException if the provided data is invalid or incomplete
   */
  AuthResponseDto<DETAIL_DTO> register(DTO dto)
      throws EntityAlreadyExistsException, EntityInvalidException;

  /**
   * Authenticates a user and returns authentication response with token and user details.
   * Validates the provided credentials and generates a new authentication token.
   *
   * @param authenticable the authentication credentials (username/email and password)
   * @return AuthResponseDto containing authentication token and user details
   * @throws NotAuthenticatedException if the provided credentials are invalid
   */
  AuthResponseDto<DETAIL_DTO> login(Authenticable authenticable) throws NotAuthenticatedException;

  /**
   * Validates an existing authentication token and returns current user details.
   * Checks if the token is still valid and returns the associated user information.
   *
   * @param user the user entity to validate the token for
   * @return AuthResponseDto containing the validated token and user details
   */
  AuthResponseDto<DETAIL_DTO> validateToken(T user);

  /**
   * Sends a one-time password (OTP) to the specified authentication identifier.
   * Generates and sends an OTP for password reset or two-factor authentication.
   *
   * @param authId the authentication identifier (email, phone number, etc.)
   * @return Otp object containing the generated OTP details
   * @throws EntityNotFoundException if no user exists with the provided authId
   */
  Otp sendOtp(String authId) throws EntityNotFoundException;

  /**
   * Validates a one-time password (OTP) for the specified authentication identifier.
   * Checks if the provided OTP is valid and not expired.
   *
   * @param authId the authentication identifier
   * @param otp the one-time password to validate
   * @return Otp object if validation is successful
   * @throws EntityInvalidException if the OTP is invalid, expired, or already used
   */
  Otp validateOtp(String authId, String otp) throws EntityInvalidException;

  /**
   * Resets the password for a user using a validated OTP.
   * Updates the user's password after successful OTP validation.
   *
   * @param authId the authentication identifier
   * @param otp the validated one-time password
   * @param newPassword the new password to set
   * @throws EntityInvalidException if the OTP is invalid or expired
   * @throws EntityNotFoundException if no user exists with the provided authId
   */
  void resetPassword(String authId, String otp, String newPassword)
      throws EntityInvalidException, EntityNotFoundException;

  /**
   * Logs out a user by invalidating their authentication token.
   * Removes the token from the active session and marks it as invalid.
   *
   * @param user the user entity to logout
   * @param token the authentication token to invalidate
   */
  void logout(T user, String token);

}
