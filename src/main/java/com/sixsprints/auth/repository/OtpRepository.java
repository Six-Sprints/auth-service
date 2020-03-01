package com.sixsprints.auth.repository;

import com.sixsprints.auth.domain.Otp;
import com.sixsprints.core.repository.GenericRepository;

public interface OtpRepository extends GenericRepository<Otp> {

  Otp findByAuthIdAndOtp(String authId, String otp);

  Otp findByAuthId(String authId);

}
