package com.sixsprints.auth.mock.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sixsprints.auth.mock.domain.Role;
import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.auth.mock.dto.UserDto;
import com.sixsprints.auth.mock.repository.UserRepository;
import com.sixsprints.auth.mock.transformer.UserMapper;
import com.sixsprints.auth.service.OtpBasedAuthService;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.auth.service.impl.AbstractOtpBasedAuthService;
import com.sixsprints.auth.util.Messages;
import com.sixsprints.core.dto.MetaData;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.repository.GenericRepository;
import com.sixsprints.notification.service.NotificationService;

@Service
public class UserService extends AbstractOtpBasedAuthService<User, UserDto, UserDto, Role>
  implements OtpBasedAuthService<User, UserDto, UserDto> {

  public UserService(UserMapper mapper, NotificationService notificationService, UserRepository userRepository,
    OtpService otpService, RoleServiceImpl roleService) {
    super(mapper, mapper, notificationService, otpService, roleService);
    this.userRepository = userRepository;
  }

  private final UserRepository userRepository;

  @Override
  protected GenericRepository<User> repository() {
    return userRepository;
  }

  @Override
  protected User findDuplicate(User entity) {
    return userRepository.findByEmailOrMobileNumber(entity.getEmail(), entity.getMobileNumber());
  }

  @Override
  protected MetaData<User> metaData() {
    return MetaData.<User>builder().classType(User.class).build();
  }

  @Override
  protected EntityInvalidException invalidException(User domain, List<String> errors) {
    return EntityInvalidException.childBuilder().error(Messages.USER_IS_INVALID).data(domain).build();
  }

  @Override
  protected EntityAlreadyExistsException alreadyExistsException(User fromDb, User domain) {
    return EntityAlreadyExistsException.childBuilder().error(Messages.USER_ALREADY_EXISTS)
      .arg(domain.getEmail()).arg(domain.getMobileNumber()).build();
  }

  @Override
  protected User findByAuthId(String authId) {
    return userRepository.findByMobileNumber(authId);
  }

  @Override
  protected User newUser(String authId) {
    return User.builder().mobileNumber(authId).email(UUID.randomUUID().toString().concat("@gmail.com")).build();
  }

}