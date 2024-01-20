package com.sixsprints.auth.service.impl;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.service.OtpBasedAuthService;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.transformer.GenericMapper;
import com.sixsprints.notification.service.NotificationService;

public abstract class AbstractOtpBasedAuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO>
  extends AbstractAuthService<T, DTO, DETAIL_DTO> implements OtpBasedAuthService<T, DTO, DETAIL_DTO> {

  private final GenericMapper<T, DTO> mapper;

  public AbstractOtpBasedAuthService(GenericMapper<T, DTO> mapper, GenericMapper<T, DETAIL_DTO> detailMapper,
    NotificationService notificationService) {
    super(mapper, detailMapper, notificationService);
    this.mapper = mapper;
  }

  @Override
  public AuthResponseDto<DETAIL_DTO> login(Authenticable authenticable)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException {
    T user = findByAuthId(authenticable.authId());
    if (user == null) {
      throw notFoundException(authenticable.authId());
    }
    validateOtp(user.authId(), authenticable.passcode());
    return generateToken(user);
  }

  @Override
  public DTO sendOtpForAuth(String authId)
    throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidException {
    T user = findByAuthId(authId);
    if (user == null) {
      user = newUser(authId);
      create(user);
    }
    super.sendOtp(user.authId());
    return mapper.toDto(user);
  }

  protected abstract T newUser(String authId);

}
