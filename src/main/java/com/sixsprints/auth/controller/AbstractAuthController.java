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
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.transformer.GenericTransformer;
import com.sixsprints.core.utils.RestResponse;
import com.sixsprints.core.utils.RestUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthController<T extends AbstractAuthenticableEntity, D> {

  private final AuthService<T, D> service;

  private final GenericTransformer<T, D> mapper;

  public AbstractAuthController(AuthService<T, D> service, GenericTransformer<T, D> mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping("/register")
  public ResponseEntity<RestResponse<AuthResponseDto<D>>> register(@RequestBody @Valid D dto)
    throws EntityAlreadyExistsException, EntityInvalidException {
    log.info("Request to register {}", dto);
    return RestUtil.successResponse(service.register(dto), HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<RestResponse<AuthResponseDto<D>>> login(@RequestBody @Valid Authenticable authDto)
    throws NotAuthenticatedException, EntityNotFoundException {
    log.info("Request to login {}", authDto.getAuthId());
    return RestUtil.successResponse(service.login(authDto));
  }

  @PostMapping("/send-otp")
  public ResponseEntity<RestResponse<String>> sendOtp(@RequestBody @Valid Authenticable auth)
    throws EntityNotFoundException {
    log.info("Request to send otp for {}", auth.getAuthId());
    service.sendOtp(auth.getAuthId());
    return RestUtil.successResponse("OTP Sent Successfully", HttpStatus.OK);
  }

  @PostMapping("/reset")
  public ResponseEntity<RestResponse<String>> resetPassword(@RequestBody @Valid ResetPasscode resetDto)
    throws EntityInvalidException {
    log.info("Request to reset password for {}", resetDto.getAuthId());
    service.resetPassword(resetDto.getAuthId(), resetDto.getOtp(), resetDto.getPasscode());
    return RestUtil.successResponse("Reset Done", HttpStatus.OK);
  }

  @PostMapping("/validate-token")
  public ResponseEntity<RestResponse<D>> validateToken(T user) {
    log.info("Validating token for {}", user);
    return RestUtil.successResponse(mapper.toDto(user));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(T user, String token) {
    log.info("Request to logout for {}", user.getAuthId());
    service.logout(user, token);
    return RestUtil.successResponse(null);
  }

}