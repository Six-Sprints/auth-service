package com.sixsprints.auth.repository;

import org.springframework.stereotype.Repository;

import com.sixsprints.auth.domain.mock.PasswordReset;
import com.sixsprints.core.generic.GenericRepository;

@Repository
public interface PasswordResetRepository extends GenericRepository<PasswordReset> {

  PasswordReset findByEmail(String email);

}
