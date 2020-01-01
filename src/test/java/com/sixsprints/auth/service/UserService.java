package com.sixsprints.auth.service;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.sixsprints.auth.domain.mock.PasswordReset;
import com.sixsprints.auth.domain.mock.User;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.dto.PasswordResetDTO;
import com.sixsprints.auth.repository.PasswordResetRepository;
import com.sixsprints.auth.repository.UserRepository;
import com.sixsprints.auth.service.Impl.AbstractAuthService;
import com.sixsprints.auth.util.Messages;
import com.sixsprints.core.dto.MetaData;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.generic.GenericRepository;
import com.sixsprints.core.utils.EncryptionUtil;
import com.sixsprints.core.utils.RandomUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService extends AbstractAuthService<User> {

  @Resource
  private UserRepository userRepository;

  @Resource
  private PasswordResetRepository passwordResetRepository;

  @Override
  protected GenericRepository<User> repository() {
    return userRepository;
  }

  @Override
  protected boolean isInvalid(User domain) {
    return domain == null || StringUtils.isBlank(domain.getEmail());
  }

  @Override
  protected User findDuplicate(User entity) {
    return userRepository.findByEmailOrMobileNumber(entity.getEmail(), entity.getMobileNumber());
  }

  // required checks and operations, before creating new User
  @Override
  protected void preCreate(User user) {

    // if password blank then set default password (email itself) else encrypt it
    if (StringUtils.isBlank(user.getPassword())) {
      user.setPassword(EncryptionUtil.encrypt(user.getEmail()));
    } else
      user.setPassword(EncryptionUtil.encrypt(user.getPassword()));

  }

  @Override
  protected User findByAuthCriteria(String authId) {
    return userRepository.findByEmail(authId);
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  protected boolean isPasscodeSame(User domain, String passcode) {
    return domain.getPassword().equals(EncryptionUtil.encrypt(passcode));
  }

  @Override
  protected void genSaveMailOTP(User user) {
    // generate otp
    String otp = String.valueOf(RandomUtil.randomInt(1000, 9999));

    // save otp
    PasswordReset passwordReset = PasswordReset.builder().otp(otp).email(user.getEmail()).build();
    passwordResetRepository.save(passwordReset);

    // mail otp
    log.info("Your OTP: " + otp);
  }

  @Override
  protected void validateOTP(Authenticable authenticable) throws EntityInvalidException {
    PasswordResetDTO passwordResetDTO = (PasswordResetDTO) authenticable;
    // take otp, find T
    PasswordReset passwordReset = passwordResetRepository.findByEmail(passwordResetDTO.getEmail());

    // verify for valid otp
    if (passwordReset == null || !StringUtils.equals(passwordReset.getOtp(), passwordResetDTO.getOtp())) {
      throw EntityInvalidException.childBuilder().error("Invalid OTP").data(passwordReset).build();
    }
    // verify for expired otp
    if (DateTime.now()
      .isAfter(new DateTime(passwordReset.getDateCreated()).plusMinutes(10))) {
      throw EntityInvalidException.childBuilder().error("OTP expired").data(passwordReset).build();
    }
  }

  @Override
  protected void updatePassword(Authenticable authenticable) throws EntityInvalidException {
    PasswordResetDTO passwordResetDTO = (PasswordResetDTO) authenticable;
    // take otp, find T
    PasswordReset passwordReset = passwordResetRepository.findByEmail(passwordResetDTO.getEmail());

    // verify for valid otp
    if (passwordReset == null || !StringUtils.equals(passwordReset.getOtp(), passwordResetDTO.getOtp())) {
      throw EntityInvalidException.childBuilder().error("Invalid OTP").data(passwordReset).build();
    }
    // verify for expired otp
    if (DateTime.now()
      .isAfter(new DateTime(passwordReset.getDateCreated()).plusMinutes(10))) {
      throw EntityInvalidException.childBuilder().error("OTP expired").data(passwordReset).build();
    }

    // if all ok then password update of T
    User user = findByAuthCriteria(passwordResetDTO.getEmail());
    user.setPassword(EncryptionUtil.encrypt(authenticable.passcode()));
    save(user);
    passwordResetRepository.delete(passwordReset);
  }

  @Override
  protected MetaData<User> metaData(User entity) {
    return MetaData.<User>builder().collection("user").prefix("USR").classType(User.class).build();
  }

  @Override
  protected EntityInvalidException invalidException(User domain) {
    return EntityInvalidException.childBuilder().error(Messages.USER_IS_INVALID).data(domain).build();
  }

  @Override
  protected EntityAlreadyExistsException alreadyExistsException(User domain) {
    return EntityAlreadyExistsException.childBuilder().error(Messages.USER_ALREADY_EXISTS)
      .arguments(new String[] { domain.getEmail(), domain.getMobileNumber() }).build();
  }

  @Override
  protected NotAuthenticatedException notAuthenticatedException(User domain) {
    return NotAuthenticatedException.childBuilder().error(Messages.LOGIN_FAILED_MISMATCH).data(domain).build();
  }

  @Override
  protected EntityNotFoundException notRegisteredException(String email) {
    return EntityNotFoundException.childBuilder().error(Messages.LOGIN_FAILED_UNREGISTERED).data(email).build();
  }

}