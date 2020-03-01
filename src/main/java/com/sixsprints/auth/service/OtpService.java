package com.sixsprints.auth.service;

import com.sixsprints.auth.domain.Otp;
import com.sixsprints.core.service.GenericCrudService;

public interface OtpService extends GenericCrudService<Otp> {

  Otp generate(String authId, int length);

  Otp findByAuthIdAndOtp(String authId, String otp);

}
