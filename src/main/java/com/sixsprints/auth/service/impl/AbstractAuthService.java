package com.sixsprints.auth.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.AbstractRole;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.hooks.PostLoginHook;
import com.sixsprints.auth.hooks.PostRegisterHook;
import com.sixsprints.auth.hooks.PreRegisterHook;
import com.sixsprints.auth.service.AbstractRoleService;
import com.sixsprints.auth.service.AuthService;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.auth.util.AuthMessageKeys;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.mapper.GenericCrudMapper;
import com.sixsprints.core.service.AbstractCrudService;
import com.sixsprints.core.utils.AuthUtil;
import com.sixsprints.core.utils.EncryptionUtil;
import com.sixsprints.core.utils.EnvConstants;
import com.sixsprints.core.utils.RandomUtil;
import com.sixsprints.notification.dto.MessageDto;
import com.sixsprints.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO, ROLE extends AbstractRole>
    extends AbstractCrudService<T> implements AuthService<T, DTO, DETAIL_DTO> {

  private static final String NO_ROLE = AuthMessageKeys.NO_ROLE;

  private final GenericCrudMapper<T, DTO> dtoMapper;

  private final GenericCrudMapper<T, DETAIL_DTO> detailMapper;

  private final NotificationService notificationService;

  private final OtpService otpService;

  private final AbstractRoleService<ROLE> roleService;

  @Autowired(required = false)
  protected List<PreRegisterHook<T>> preRegisterHooks;

  @Autowired(required = false)
  protected List<PostRegisterHook<T>> postRegisterHooks;

  @Autowired(required = false)
  protected List<PostLoginHook<T>> postLoginHooks;

  @Override
  protected void enhanceEntity(T user) {
    super.enhanceEntity(user);
    if (StringUtils.isBlank(user.getPassword())) {
      user.setPassword(EncryptionUtil.encrypt(defaultPassword(user)));
    } else {
      user.setPassword(EncryptionUtil.encrypt(user.getPassword()));
    }
  }

  @Override
  public AuthResponseDto<DETAIL_DTO> register(DTO dto)
      throws EntityAlreadyExistsException, EntityInvalidException {
    T domain = dtoMapper.toDomain(dto);
    preRegister(domain);
    domain = insertOne(domain);
    postRegister(domain);
    return generateToken(domain);
  }

  @Override
  public AuthResponseDto<DETAIL_DTO> login(Authenticable authenticable)
      throws NotAuthenticatedException {
    T user = findByAuthId(authenticable.authId());
    if (user == null) {
      throw NotAuthenticatedException.childBuilder().error(AuthMessageKeys.LOGIN_FAILED)
          .arg(authenticable.authId()).build();
    }
    if (wrongPassword(user.getPassword(), authenticable.passcode())) {
      throw loginFailedException(authenticable);
    }
    postLogin(user);
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
    otpService.deleteOneById(otpFromDb.getId());
    user.setPassword(newPassword);
    patchUpdateOneById(user.getId(), user, AbstractAuthenticableEntity.Fields.password);
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
    user.setInvalidTokens(invalidTokens);
    try {
      patchUpdateOneById(user.getId(), user, AbstractAuthenticableEntity.Fields.invalidTokens);
    } catch (Exception e) {
      log.error(AuthMessageKeys.ERROR_UPDATING_INVALID_TOKENS, e);
    }
  }

  protected abstract T findByAuthId(String authId);

  protected void preRegister(T domain) {
    if (!CollectionUtils.isEmpty(preRegisterHooks)) {
      for (PreRegisterHook<T> preRegisterHook : preRegisterHooks) {
        preRegisterHook.preRegister(domain);
      }
    }
  }

  protected void postRegister(T domain) {
    if (!CollectionUtils.isEmpty(postRegisterHooks)) {
      for (PostRegisterHook<T> postRegisterHook : postRegisterHooks) {
        postRegisterHook.postRegister(domain);
      }
    }
  }

  protected void postLogin(T user) {
    if (!CollectionUtils.isEmpty(postLoginHooks)) {
      for (PostLoginHook<T> postLoginHook : postLoginHooks) {
        postLoginHook.postLogin(user);
      }
    }
  }

  protected String defaultPassword(T user) {
    if (StringUtils.isBlank(user.authId())) {
      return RandomUtil.randomAlphaNumericString(9) + "A1!";
    }
    return user.authId();
  }

  protected void sendMessageToUser(Otp otp) {
    notificationService.sendMessage(otpMessage(otp));
  }

  protected MessageDto otpMessage(Otp otp) {
    return MessageDto.builder().to(otp.getAuthId())
        .subject(localisedMessage(AuthMessageKeys.OTP_GENERATED_SUBJECT, null))
        .content(localisedMessage(AuthMessageKeys.OTP_GENERATED_CONTENT, List.of(otp.getOtp())))
        .build();
  }

  protected int otpLength() {
    return 4;
  }

  protected NotAuthenticatedException loginFailedException(Authenticable authenticable)
      throws NotAuthenticatedException {
    return NotAuthenticatedException.childBuilder().error((AuthMessageKeys.LOGIN_FAILED))
        .arg(authenticable.authId()).data(authenticable.authId()).build();
  }

  protected AuthResponseDto<DETAIL_DTO> generateToken(T domain) {

    String roleSlug = domain.getRoleSlug();
    ROLE role = fetchRole(roleSlug);

    return AuthResponseDto.<DETAIL_DTO>builder()
        .token(AuthUtil.createToken(domain.getId(), tokenExpiryInDays()))
        .data(detailMapper.toDto(domain)).roleName(role == null ? NO_ROLE : role.getName())
        .modulePermissions(role == null ? new ArrayList<>() : role.getModulePermissions()).build();
  }

  private ROLE fetchRole(String roleSlug) {
    return roleService.findOneBySlug(roleSlug).orElse(null);
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
