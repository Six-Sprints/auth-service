package com.sixsprints.auth.mock.repository;

import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.core.repository.GenericRepository;

public interface UserRepository extends GenericRepository<User> {

  User findByMobileNumber(String mobileNumber);

  User findByEmailOrMobileNumber(String email, String mobileNumber);

}
