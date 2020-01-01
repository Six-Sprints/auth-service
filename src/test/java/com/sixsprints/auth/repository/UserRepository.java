package com.sixsprints.auth.repository;

import org.springframework.stereotype.Repository;

import com.sixsprints.auth.domain.mock.User;
import com.sixsprints.core.generic.GenericRepository;

@Repository
public interface UserRepository extends GenericRepository<User> {

  User findByEmail(String email);

  User findByEmailOrMobileNumber(String email, String mobileNumber);

}
