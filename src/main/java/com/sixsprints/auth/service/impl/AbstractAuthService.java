package com.sixsprints.auth.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.service.AuthService;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.auth.service.RoleService;
import com.sixsprints.auth.util.Messages;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.service.AbstractCrudService;
import com.sixsprints.core.transformer.GenericMapper;
import com.sixsprints.core.utils.AppConstants;
import com.sixsprints.core.utils.AuthUtil;
import com.sixsprints.core.utils.EncryptionUtil;
import com.sixsprints.notification.dto.MessageDto;
import com.sixsprints.notification.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthService<T extends AbstractAuthenticableEntity, CD> extends AbstractCrudService<T>
  implements AuthService<T, CD> {

  private final GenericMapper<T, CD> mapper;

  private final NotificationService notificationService;

  @Autowired
  private OtpService otpService;

  @Autowired
  private RoleService roleService;

  public AbstractAuthService(GenericMapper<T, CD> mapper, NotificationService notificationService) {
    this.mapper = mapper;
    this.notificationService = notificationService;
  }

  @Override
  public AuthResponseDto<CD> register(CD dto) throws EntityAlreadyExistsException, EntityInvalidException {
    return generateToken(create(mapper.toDomain(dto)));
  }

  @Override
  public AuthResponseDto<CD> login(Authenticable authenticable)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException {
    T user = findByAuthId(authenticable.authId());
    if (user == null) {
      throw notFoundException(authenticable.authId());
    }
    if (wrongPassword(user.getPassword(), authenticable.passcode()) || !user.getActive()) {
      throw loginFailedException(authenticable);
    }
    return generateToken(user);
  }

  @Override
  public Otp sendOtp(String authId) throws EntityNotFoundException {
    Otp otp = otpService.generate(authId, otpLength());
    sendMessageToUser(otp);
    return otp;
  }

  @Override
  public Otp validateOtp(String authId, String otp) throws EntityInvalidException {
    Otp otpFromDb = otpService.findByAuthIdAndOtp(authId, otp);
    if (otpFromDb == null) {
      throw invalidOtpError(authId, otp);
    }
    return otpFromDb;
  }

  @Override
  @Transactional
  public void resetPassword(String authId, String otp, String newPassword)
    throws EntityInvalidException, EntityNotFoundException {
    Otp otpFromDb = validateOtp(authId, otp);
    T user = findByAuthId(authId);
    if (user == null) {
      throw notFoundException(authId);
    }
    user.setPassword(EncryptionUtil.encrypt(newPassword));
    otpService.delete(otpFromDb);
    preResetPassword(otpFromDb, user);
    save(user);
  }

  protected void preResetPassword(Otp otpFromDb, T user) {

  }

  @Override
  public AuthResponseDto<CD> validateToken(T user) {
    return generateToken(user);
  }

  @Override
  public void logout(T user, String token) {
    if (StringUtils.isEmpty(token)) {
      return;
    }
    if (CollectionUtils.isEmpty(user.getInvalidTokens())) {
      user.setInvalidTokens(new ArrayList<>());
    }
    List<String> invalidTokens = user.getInvalidTokens();
    if (invalidTokens.size() > 5) {
      invalidTokens.remove(0);
    }
    invalidTokens.add(token);
    save(user);
  }

  protected abstract T findByAuthId(String authId);

  protected void sendMessageToUser(Otp otp) {
    notificationService.sendMessage(otpMessage(otp));
  }

  protected MessageDto otpMessage(Otp otp) {
    return MessageDto.builder().to(otp.getAuthId()).subject("OTP Generated Successfully")
      .content(String.format("Your OTP: %s", otp.getOtp())).build();
  }

  protected int otpLength() {
    return 4;
  }

  protected NotAuthenticatedException loginFailedException(Authenticable authenticable)
    throws NotAuthenticatedException {
    return NotAuthenticatedException.childBuilder().error((Messages.LOGIN_FAILED))
      .arg(authenticable.authId()).data(authenticable.authId()).build();
  }

  protected AuthResponseDto<CD> generateToken(T domain) {

    String roleSlug = domain.getRoleSlug();
    Role role = fetchRole(roleSlug);

    return AuthResponseDto.<CD>builder()
      .token(AuthUtil.createToken(domain.getId(), tokenExpiryInDays()))
      .data(mapper.toDto(domain))
      .roleName(role.getName())
      .permissions(role.getPermissions())
      .build();
  }

  private Role fetchRole(String roleSlug) {
    Role role = Role.builder().build();
    try {
      if (StringUtils.isNotBlank(roleSlug)) {
        role = roleService.findBySlug(roleSlug);
      }
    } catch (EntityNotFoundException ex) {
      log.error("Role not found with slug = {}", roleSlug);
      log.error(ex.getMessage(), ex);
    }
    return role == null ? Role.builder().build() : role;
  }

  protected int tokenExpiryInDays() {
    return AppConstants.TOKEN_EXPIRY_IN_DAYS;
  }

  protected EntityInvalidException invalidOtpError(String authId, String otp) {
    return EntityInvalidException.childBuilder().arg(authId).arg(otp).build();
  }

  private boolean wrongPassword(String passcodeFromDb, String passcode2) {
    return !passcodeFromDb.equals(EncryptionUtil.encrypt(passcode2));
  }

}
