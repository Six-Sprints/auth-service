package com.sixsprints.auth.repository;

import com.sixsprints.auth.domain.Otp;
import com.sixsprints.core.repository.GenericCrudRepository;

public interface OtpRepository extends GenericCrudRepository<Otp> {

  Otp findByAuthIdAndOtp(String authId, String otp);

  Otp findByAuthId(String authId);

}
