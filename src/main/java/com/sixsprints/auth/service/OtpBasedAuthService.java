package com.sixsprints.auth.service;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;

/**
 * Extended authentication service interface that provides OTP-based authentication capabilities.
 * This interface extends the base AuthService to include functionality for sending OTPs
 * for authentication and automatic user registration if the user doesn't exist.
 *
 * @param <T> the type of authenticable entity extending AbstractAuthenticableEntity
 * @param <DTO> the data transfer object type for registration and authentication
 * @param <DETAIL_DTO> the detailed data transfer object type for responses
 * @author SixSprints
 * @since 1.0
 */
public interface OtpBasedAuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO>
    extends AuthService<T, DTO, DETAIL_DTO> {

  /**
   * Sends an OTP for authentication and automatically registers the user if they don't exist.
   * This method provides a seamless authentication flow where users can authenticate
   * using OTP without prior registration. If the user doesn't exist, they are automatically
   * registered with the provided authentication identifier.
   *
   * @param authId the authentication identifier (email, phone number, etc.)
   * @return DTO containing the user information and OTP details
   * @throws EntityNotFoundException if the user doesn't exist and registration fails
   * @throws EntityAlreadyExistsException if a user with the same identifier already exists
   * @throws EntityInvalidException if the authentication identifier is invalid or registration data is incomplete
   */
  DTO sendOtpForAuthAndRegisterIfNotExists(String authId)
      throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidException;
}
