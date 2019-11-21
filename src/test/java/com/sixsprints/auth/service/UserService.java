package com.sixsprints.auth.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.sixsprints.auth.domain.mock.PasswordResetOtp;
import com.sixsprints.auth.domain.mock.User;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.repository.PasswordResetOtpRepository;
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

@Service
public class UserService extends AbstractAuthService<User> {

  @Resource
  private UserRepository userRepository;

  @Resource
  private PasswordResetOtpRepository passwordResetOtpRepository;

  // @Resource
  // private JavaMailSender mailSender;

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
    return findByAuthCriteria(entity.getEmail());
  }

  @Override
  protected void preCreate(User entity) {
    entity.setPassword(EncryptionUtil.encrypt(entity.getPassword()));
  }

  @Override
  protected User findByAuthCriteria(String authId) {
    return userRepository.findByEmail(authId);
  }

  @Override
  protected boolean isPasscodeSame(User domain, String passcode) {
    return domain.getPassword().equals(EncryptionUtil.encrypt(passcode));
  }

  @Override
  protected void genSaveMailOTP(User user) {
    // generate otp while avoiding duplicate otp
    String otp;
    do {
      String num = "0123456789";
      Random random = new Random();
      StringBuilder tempOtp = new StringBuilder();
      for (int i = 1; i <= 6; i++) {
        tempOtp.append(num.charAt(random.nextInt(10)));
      }
      otp = tempOtp.toString();
    } while (isAgain(otp));

    // save otp
    PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder().otp(otp).user(user).build();
    passwordResetOtp.calculateExpiryDate();
    generateSlugIfRequired(user);
    passwordResetOtpRepository.save(passwordResetOtp);

    // mail otp
    // mailSender.send(constructEmail(otp, user));
    SimpleMailMessage email = constructEmail(otp, user);
    System.out.println(email.toString());
  }

  // avoid duplicate otp
  private boolean isAgain(String otp) {
    List<PasswordResetOtp> otpList = passwordResetOtpRepository.findAll();
    for (Iterator<PasswordResetOtp> iterator = otpList.iterator(); iterator.hasNext();) {
      PasswordResetOtp passwordResetOtp = (PasswordResetOtp) iterator.next();
      if (passwordResetOtp.getOtp().equals(otp)) {
        if (passwordResetOtp.getExpiryDate().before(new Date())) {
          passwordResetOtpRepository.deleteAllByExpiryDateBefore(new Date());
          return false;
        }
        return true;
      }
    }
    return false;
  }

  private SimpleMailMessage constructEmail(String otp, User user) {
    SimpleMailMessage email = new SimpleMailMessage();
    email.setSubject("");
    email.setText("" + otp);
    email.setTo(user.getEmail());
    email.setFrom("");// env.getProperty("support.email"));
    return email;
  }

  @Override
  protected void updatePassword(Authenticable authenticable) throws EntityInvalidException {
    // take otp, find T
    PasswordResetOtp passwordResetOtp = passwordResetOtpRepository.findByOtp(authenticable.authId());

    // verify for valid otp
    if (passwordResetOtp == null)
      throw EntityInvalidException.childBuilder().error("Invalid OTP").data(passwordResetOtp).build();
    // verify for expired otp
    if (passwordResetOtp.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0)
      throw EntityInvalidException.childBuilder().error("OTP expired").data(passwordResetOtp).build();

    // if all ok then password update of T
    User user = passwordResetOtp.getUser();
    user.setPassword(EncryptionUtil.encrypt(authenticable.passcode()));
    save(user);
    // expire otp to prevent another use of same otp
    passwordResetOtp.setExpiryDate(new Date());
    passwordResetOtpRepository.save(passwordResetOtp);
  }

  @Override
  protected MetaData<User> metaData(User entity) {
    return MetaData.<User>builder().collection("user").prefix("U")
      .classType(User.class).build();
  }

  @Override
  protected EntityInvalidException invalidException(User domain) {
    return EntityInvalidException.childBuilder().error(Messages.USER_IS_INVALID).data(domain).build();
  }

  @Override
  protected EntityAlreadyExistsException alreadyExistsException(User domain) {
    return EntityAlreadyExistsException.childBuilder().error(Messages.USER_ALREADY_EXISTS)
      .arguments(new String[] { domain.getEmail() }).data(domain).build();
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
