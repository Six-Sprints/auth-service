package com.sixsprints.auth.repository;

import java.util.Date;

import org.springframework.stereotype.Repository;

import com.sixsprints.auth.domain.mock.PasswordResetOtp;
import com.sixsprints.core.generic.GenericRepository;

@Repository
public interface PasswordResetOtpRepository extends GenericRepository<PasswordResetOtp> {

  PasswordResetOtp findByOtp(String Otp);

  // to clear expired tokens
  // @Query("delete from PasswordResetToken t where t.expiryDate <= ?1")
  void deleteAllByExpiryDateBefore(Date now);

}
