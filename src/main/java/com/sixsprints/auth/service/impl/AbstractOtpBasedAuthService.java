package com.sixsprints.auth.service.impl;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.AbstractRole;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.service.AbstractRoleService;
import com.sixsprints.auth.service.OtpBasedAuthService;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.auth.util.AuthMessageKeys;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.mapper.GenericCrudMapper;
import com.sixsprints.notification.service.NotificationService;

public abstract class AbstractOtpBasedAuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO, ROLE extends AbstractRole>
    extends AbstractAuthService<T, DTO, DETAIL_DTO, ROLE>
    implements OtpBasedAuthService<T, DTO, DETAIL_DTO> {

  private final GenericCrudMapper<T, DTO> mapper;

  public AbstractOtpBasedAuthService(GenericCrudMapper<T, DTO> mapper,
      GenericCrudMapper<T, DETAIL_DTO> detailMapper, NotificationService notificationService,
      OtpService otpService, AbstractRoleService<ROLE> roleService) {
    super(mapper, detailMapper, notificationService, otpService, roleService);
    this.mapper = mapper;
  }

  @Override
  public AuthResponseDto<DETAIL_DTO> login(Authenticable authenticable)
      throws NotAuthenticatedException {
    T user = findByAuthId(authenticable.authId());
    if (user == null) {
      throw NotAuthenticatedException.childBuilder().error(AuthMessageKeys.LOGIN_FAILED)
          .arg(authenticable.authId()).build();
    }
    try {
      validateOtp(user.authId(), authenticable.passcode());
    } catch (EntityInvalidException e) {
      throw NotAuthenticatedException.childBuilder().error(AuthMessageKeys.LOGIN_FAILED)
          .arg(authenticable.authId()).build();
    }
    return generateToken(user);
  }


  @Override
  public DTO sendOtpForAuthAndRegisterIfNotExists(String authId)
      throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidException {
    T user = findByAuthId(authId);
    if (user == null) {
      user = newUser(authId);
      insertOne(user);
    }
    super.sendOtp(user.authId());
    return mapper.toDto(user);
  }

  protected abstract T newUser(String authId);

}
