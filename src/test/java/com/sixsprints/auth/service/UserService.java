package com.sixsprints.auth.service;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.sixsprints.auth.domain.mock.User;
import com.sixsprints.auth.repository.UserRepository;
import com.sixsprints.auth.service.Impl.AbstractAuthService;
import com.sixsprints.core.dto.MetaData;
import com.sixsprints.core.generic.GenericRepository;
import com.sixsprints.core.utils.EncryptionUtil;

@Service
public class UserService extends AbstractAuthService<User> {

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
  protected MetaData<User> metaData(User entity) {
    return MetaData.<User>builder().collection("user").prefix("U")
      .classType(User.class).build();
  }
}
