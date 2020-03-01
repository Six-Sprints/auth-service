package com.sixsprints.auth.service;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.sixsprints.auth.domain.mock.User;
import com.sixsprints.auth.dto.UserDto;
import com.sixsprints.auth.repository.UserRepository;
import com.sixsprints.auth.service.Impl.AbstractAuthService;
import com.sixsprints.auth.transformer.UserMapper;
import com.sixsprints.auth.util.Messages;
import com.sixsprints.core.dto.MetaData;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.repository.GenericRepository;
import com.sixsprints.core.utils.EncryptionUtil;
import com.sixsprints.notification.service.NotificationService;

@Service
public class UserService extends AbstractAuthService<User, UserDto> implements AuthService<User, UserDto> {

  public UserService(UserMapper mapper, NotificationService notificationService) {
    super(mapper, notificationService);
  }

  @Resource
  private UserRepository userRepository;

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
      .arg(domain.getEmail()).arg(domain.getMobileNumber()).build();
  }

  @Override
  protected User findByAuthId(String authId) {
    return userRepository.findByEmail(authId);
  }

}