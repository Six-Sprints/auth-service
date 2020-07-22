package com.sixsprints.auth.service.Impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.service.AuthService;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.auth.util.Messages;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.service.AbstractCrudService;
import com.sixsprints.core.transformer.GenericTransformer;
import com.sixsprints.core.utils.AppConstants;
import com.sixsprints.core.utils.AuthUtil;
import com.sixsprints.core.utils.EncryptionUtil;
import com.sixsprints.notification.dto.MessageDto;
import com.sixsprints.notification.service.NotificationService;

public abstract class AbstractAuthService<T extends AbstractAuthenticableEntity, DTO> extends AbstractCrudService<T>
  implements AuthService<T, DTO> {

  private final GenericTransformer<T, DTO> mapper;

  private final NotificationService notificationService;

  @Autowired
  private OtpService otpService;

  public AbstractAuthService(GenericTransformer<T, DTO> mapper, NotificationService notificationService) {
    this.mapper = mapper;
    this.notificationService = notificationService;
  }

  @Override
  public AuthResponseDto<DTO> register(DTO dto) throws EntityAlreadyExistsException, EntityInvalidException {
    return generateToken(create(mapper.toDomain(dto)));
  }

  @Override
  public AuthResponseDto<DTO> login(Authenticable authenticable)
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
  public void resetPassword(String authId, String otp, String newPassword) throws EntityInvalidException {
    Otp otpFromDb = validateOtp(authId, otp);
    T user = findByAuthId(authId);
    user.setPassword(EncryptionUtil.encrypt(newPassword));
    save(user);
    otpService.delete(otpFromDb);
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

  protected AuthResponseDto<DTO> generateToken(T domain) {
    return AuthResponseDto.<DTO>builder().token(AuthUtil.createToken(domain.getId(), tokenExpiryInDays()))
      .data(mapper.toDto(domain))
      .build();
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
