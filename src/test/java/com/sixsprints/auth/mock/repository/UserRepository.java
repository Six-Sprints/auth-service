package com.sixsprints.auth.mock.repository;

import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.core.repository.GenericCrudRepository;

public interface UserRepository extends GenericCrudRepository<User> {

  User findByMobileNumber(String mobileNumber);

  User findByEmailOrMobileNumber(String email, String mobileNumber);

}
