package com.sixsprints.auth.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthController<T extends AbstractAuthenticableEntity, CD, L extends Authenticable, R extends ResetPasscode> {

  private final AuthService<T, CD> authService;

  public AbstractAuthController(AuthService<T, CD> service) {
    this.authService = service;
  }

  @PostMapping("/register")
  public ResponseEntity<RestResponse<AuthResponseDto<CD>>> register(@RequestBody @Valid CD dto)
    throws EntityAlreadyExistsException, EntityInvalidException {
    log.info("Request to register {}", dto);
    return RestUtil.successResponse(authService.register(dto), HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<RestResponse<AuthResponseDto<CD>>> login(@RequestBody @Valid L authDto)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException {
    log.info("Request to login {}", authDto.authId());
    return RestUtil.successResponse(authService.login(authDto));
  }

  @PostMapping("/send-otp")
  public ResponseEntity<RestResponse<String>> sendOtp(@RequestBody @Valid L auth)
    throws EntityNotFoundException {
    log.info("Request to send otp for {}", auth.authId());
    authService.sendOtp(auth.authId());
    return RestUtil.successResponse("OTP Sent Successfully", HttpStatus.OK);
  }

  @PostMapping("/reset")
  public ResponseEntity<RestResponse<String>> resetPassword(@RequestBody @Valid R resetDto)
    throws EntityInvalidException, EntityNotFoundException {
    log.info("Request to reset password for {}", resetDto.authId());
    authService.resetPassword(resetDto.authId(), resetDto.otp(), resetDto.passcode());
    return RestUtil.successResponse("Reset Done", HttpStatus.OK);
  }

  @Authenticated
  @PostMapping("/validate-token")
  public ResponseEntity<RestResponse<AuthResponseDto<CD>>> validateToken() {
    T user = ApplicationContext.getCurrentUser();
    log.info("Validating token for {}", user.authId());
    return RestUtil.successResponse(authService.validateToken(user));
  }

  @Authenticated(required = false)
  @PostMapping("/logout")
  public ResponseEntity<?> logout(String token) {
    T user = ApplicationContext.getCurrentUser();
    if (user != null) {
      log.info("Request to logout for {}", user.authId());
      authService.logout(user, token);
    }
    return RestUtil.successResponse(null);
  }

}