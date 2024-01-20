package com.sixsprints.auth.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.OtpLoginDto;
import com.sixsprints.auth.dto.ResetPasscode;
import com.sixsprints.auth.service.OtpBasedAuthService;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.utils.RestResponse;
import com.sixsprints.core.utils.RestUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOtpBasedAuthController<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO>
  extends AbstractAuthController<T, DTO, DETAIL_DTO, OtpLoginDto, ResetPasscode> {

  private final OtpBasedAuthService<T, DTO, DETAIL_DTO> service;

  public AbstractOtpBasedAuthController(OtpBasedAuthService<T, DTO, DETAIL_DTO> service) {
    super(service);
    this.service = service;
  }

  @PostMapping("/send-otp-login")
  public ResponseEntity<RestResponse<DTO>> sendOtpForAuth(@RequestBody @Valid OtpLoginDto auth)
    throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidException {
    log.info("Request to send otp for {}", auth.authId());
    DTO user = service.sendOtpForAuth(auth.authId());
    return RestUtil.successResponse(user);
  }

  @Override
  public ResponseEntity<RestResponse<AuthResponseDto<DETAIL_DTO>>> login(@RequestBody @Valid OtpLoginDto authDto)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException {
    return super.login(authDto);
  }

}