package com.sixsprints.auth.mock.repository;

import org.springframework.stereotype.Repository;

import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.core.repository.GenericRepository;

@Repository
public interface UserRepository extends GenericRepository<User> {

  User findByMobileNumber(String mobileNumber);

  User findByEmailOrMobileNumber(String email, String mobileNumber);

}
