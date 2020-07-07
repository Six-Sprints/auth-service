package com.sixsprints.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sixsprints.auth.annotation.Authenticated;
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
public abstract class AbstractAuthController<T extends AbstractAuthenticableEntity, DTO, L extends Authenticable, R extends ResetPasscode> {

  private final AuthService<T, DTO> service;

  private final GenericTransformer<T, DTO> mapper;

  public AbstractAuthController(AuthService<T, DTO> service, GenericTransformer<T, DTO> mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping("/register")
  public ResponseEntity<RestResponse<AuthResponseDto<DTO>>> register(@RequestBody @Validated DTO dto)
    throws EntityAlreadyExistsException, EntityInvalidException {
    log.info("Request to register {}", dto);
    return RestUtil.successResponse(service.register(dto), HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<RestResponse<AuthResponseDto<DTO>>> login(@RequestBody @Validated L authDto)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException {
    log.info("Request to login {}", authDto.authId());
    return RestUtil.successResponse(service.login(authDto));
  }

  @PostMapping("/send-otp")
  public ResponseEntity<RestResponse<String>> sendOtp(@RequestBody @Validated L auth)
    throws EntityNotFoundException {
    log.info("Request to send otp for {}", auth.authId());
    service.sendOtp(auth.authId());
    return RestUtil.successResponse("OTP Sent Successfully", HttpStatus.OK);
  }

  @PostMapping("/reset")
  public ResponseEntity<RestResponse<String>> resetPassword(@RequestBody @Validated R resetDto)
    throws EntityInvalidException {
    log.info("Request to reset password for {}", resetDto.authId());
    service.resetPassword(resetDto.authId(), resetDto.otp(), resetDto.passcode());
    return RestUtil.successResponse("Reset Done", HttpStatus.OK);
  }

  @PostMapping("/validate-token")
  public ResponseEntity<RestResponse<DTO>> validateToken(@Authenticated T user) {
    log.info("Validating token for {}", user.authId());
    return RestUtil.successResponse(mapper.toDto(user));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@Authenticated(required = false) T user, String token) {
    if (user != null) {
      log.info("Request to logout for {}", user.authId());
      service.logout(user, token);
    }
    return RestUtil.successResponse(null);
  }

}