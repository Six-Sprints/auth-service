package com.sixsprints.auth.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.dto.ResetPasscode;
import com.sixsprints.auth.service.AuthService;
import com.sixsprints.core.annotation.Authenticated;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.utils.ApplicationContext;
import com.sixsprints.core.utils.RestResponse;
import com.sixsprints.core.utils.RestUtil;

public abstract class AbstractAuthController<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO, L extends Authenticable, R extends ResetPasscode> {

  private final AuthService<T, DTO, DETAIL_DTO> authService;

  public AbstractAuthController(AuthService<T, DTO, DETAIL_DTO> service) {
    this.authService = service;
  }

  @PostMapping("/register")
  public ResponseEntity<RestResponse<AuthResponseDto<DETAIL_DTO>>> register(@RequestBody @Valid DTO dto)
    throws EntityAlreadyExistsException, EntityInvalidException {
    return RestUtil.successResponse(authService.register(dto), HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<RestResponse<AuthResponseDto<DETAIL_DTO>>> login(@RequestBody @Valid L authDto)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException {
    return RestUtil.successResponse(authService.login(authDto));
  }

  @PostMapping("/send-otp")
  public ResponseEntity<RestResponse<String>> sendOtp(@RequestParam String authId)
    throws EntityNotFoundException {
    authService.sendOtp(authId);
    return RestUtil.successResponse("OTP sent successfully", HttpStatus.OK);
  }

  @PostMapping("/reset")
  public ResponseEntity<RestResponse<String>> resetPassword(@RequestBody @Valid R resetDto)
    throws EntityInvalidException, EntityNotFoundException {
    authService.resetPassword(resetDto.authId(), resetDto.otp(), resetDto.passcode());
    return RestUtil.successResponse("Password has been reset successfully", HttpStatus.OK);
  }

  @Authenticated
  @PostMapping("/validate-token")
  public ResponseEntity<RestResponse<AuthResponseDto<DETAIL_DTO>>> validateToken() {
    T user = ApplicationContext.getCurrentUser();
    return RestUtil.successResponse(authService.validateToken(user));
  }

  @Authenticated(required = false)
  @PostMapping("/logout")
  public ResponseEntity<?> logout(String token) {
    T user = ApplicationContext.getCurrentUser();
    if (user != null) {
      authService.logout(user, token);
    }
    return RestUtil.successResponse(null);
  }

}