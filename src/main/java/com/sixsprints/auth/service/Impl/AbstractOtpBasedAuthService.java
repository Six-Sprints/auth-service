package com.sixsprints.auth.service.Impl;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.service.OtpBasedAuthService;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.transformer.GenericTransformer;
import com.sixsprints.notification.service.NotificationService;

public abstract class AbstractOtpBasedAuthService<T extends AbstractAuthenticableEntity, DTO>
  extends AbstractAuthService<T, DTO> implements OtpBasedAuthService<T, DTO> {

  private final GenericTransformer<T, DTO> mapper;

  public AbstractOtpBasedAuthService(GenericTransformer<T, DTO> mapper, NotificationService notificationService) {
    super(mapper, notificationService);
    this.mapper = mapper;
  }

  @Override
  public AuthResponseDto<DTO> login(Authenticable authenticable)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException {
    T user = findByAuthId(authenticable.authId());
    if (user == null) {
      throw notFoundException(authenticable.authId());
    }
    validateOtp(authenticable.authId(), authenticable.passcode());
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
    super.sendOtp(authId);
    return mapper.toDto(user);
  }

  protected abstract T newUser(String authId);

}
