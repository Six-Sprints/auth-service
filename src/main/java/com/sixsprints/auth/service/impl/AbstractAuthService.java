package com.sixsprints.auth.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.AbstractRole;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.service.AbstractRoleService;
import com.sixsprints.auth.service.AuthService;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.auth.util.Messages;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.service.AbstractCrudService;
import com.sixsprints.core.transformer.GenericMapper;
import com.sixsprints.core.utils.AuthUtil;
import com.sixsprints.core.utils.EncryptionUtil;
import com.sixsprints.core.utils.EnvConstants;
import com.sixsprints.notification.dto.MessageDto;
import com.sixsprints.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO, ROLE extends AbstractRole>
  extends AbstractCrudService<T>
  implements AuthService<T, DTO, DETAIL_DTO> {

  private static final String NO_ROLE = "None";

  private final GenericMapper<T, DTO> dtoMapper;

  private final GenericMapper<T, DETAIL_DTO> detailMapper;

  private final NotificationService notificationService;

  private final OtpService otpService;

  private final AbstractRoleService<ROLE> roleService;

  @Override
  protected void preCreate(T user) {
    if (StringUtils.isBlank(user.getPassword())) {
      user.setPassword(EncryptionUtil.encrypt(defaultPassword(user)));
    } else
      user.setPassword(EncryptionUtil.encrypt(user.getPassword()));
  }

  @Override
  public AuthResponseDto<DETAIL_DTO> register(DTO dto) throws EntityAlreadyExistsException, EntityInvalidException {
    T domain = dtoMapper.toDomain(dto);
    preRegister(domain);
    domain = create(domain);
    postRegister(domain);
    return generateToken(domain);
  }

  @Override
  public AuthResponseDto<DETAIL_DTO> login(Authenticable authenticable)
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
    T user = findByAuthId(authId);
    if (user == null) {
      return null;
    }
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
    otpService.delete(otpFromDb);
    patchUpdateRaw(user.getId(), EncryptionUtil.encrypt(newPassword), AbstractAuthenticableEntity.PASSWORD);
  }

  @Override
  public AuthResponseDto<DETAIL_DTO> validateToken(T user) {
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
    patchUpdateRaw(user.getId(), invalidTokens, AbstractAuthenticableEntity.INVALID_TOKENS);
  }

  protected abstract T findByAuthId(String authId);

  protected void preRegister(T domain) {

  }

  protected void postRegister(T domain) {

  }

  protected String defaultPassword(T user) {
    return user.authId();
  }

  protected void sendMessageToUser(Otp otp) {
    notificationService.sendMessage(otpMessage(otp));
  }

  protected MessageDto otpMessage(Otp otp) {
    return MessageDto.builder()
      .to(otp.getAuthId())
      .subject("OTP Generated Successfully")
      .content(String.format("Your OTP: %s", otp.getOtp()))
      .build();
  }

  protected int otpLength() {
    return 4;
  }

  protected NotAuthenticatedException loginFailedException(Authenticable authenticable)
    throws NotAuthenticatedException {
    return NotAuthenticatedException.childBuilder().error((Messages.LOGIN_FAILED))
      .arg(authenticable.authId()).data(authenticable.authId()).build();
  }

  protected AuthResponseDto<DETAIL_DTO> generateToken(T domain) {

    String roleSlug = domain.getRoleSlug();
    ROLE role = fetchRole(roleSlug);

    return AuthResponseDto.<DETAIL_DTO>builder()
      .token(AuthUtil.createToken(domain.getId(), tokenExpiryInDays()))
      .data(detailMapper.toDto(domain))
      .roleName(role == null ? NO_ROLE : role.getName())
      .modulePermissions(role == null ? new ArrayList<>() : role.getModulePermissions())
      .build();
  }

  private ROLE fetchRole(String roleSlug) {
    try {
      if (StringUtils.isNotBlank(roleSlug)) {
        return roleService.findBySlug(roleSlug);
      }
    } catch (EntityNotFoundException ex) {
      log.error("Role not found with slug = {}", roleSlug);
      log.error(ex.getMessage(), ex);
    }
    return null;
  }

  protected int tokenExpiryInDays() {
    return EnvConstants.TOKEN_EXPIRY_IN_DAYS;
  }

  protected EntityInvalidException invalidOtpError(String authId, String otp) {
    return EntityInvalidException.childBuilder().arg(authId).arg(otp).build();
  }

  private boolean wrongPassword(String passcodeFromDb, String passcode2) {
    return !passcodeFromDb.equals(EncryptionUtil.encrypt(passcode2));
  }

}
