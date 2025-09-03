package com.sixsprints.auth.service;

import com.sixsprints.auth.domain.Otp;
import com.sixsprints.core.service.GenericCrudService;

/**
 * Service interface for managing One-Time Password (OTP) operations.
 * Provides functionality for generating, validating, and managing OTPs
 * used in authentication and password reset flows.
 *
 * @author SixSprints
 * @since 1.0
 */
public interface OtpService extends GenericCrudService<Otp> {

  /**
   * Generates a new One-Time Password (OTP) for the specified authentication identifier.
   * Creates a random OTP of the specified length and associates it with the authId.
   * The generated OTP will have an expiration time and can be used for authentication
   * or password reset operations.
   *
   * @param authId the authentication identifier (email, phone number, etc.)
   * @param length the length of the OTP to generate (typically 4-8 digits)
   * @return Otp object containing the generated OTP details including the code and expiration time
   */
  Otp generate(String authId, int length);

  /**
   * Finds an OTP by authentication identifier and OTP code.
   * Searches for an existing OTP that matches both the authId and the provided OTP code.
   * This method is typically used for OTP validation during authentication or password reset flows.
   *
   * @param authId the authentication identifier
   * @param otp the OTP code to search for
   * @return Otp object if found, null if no matching OTP exists
   */
  Otp findByAuthIdAndOtp(String authId, String otp);

}
