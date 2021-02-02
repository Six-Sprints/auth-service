package com.sixsprints.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.repository.OtpRepository;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.core.dto.MetaData;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.service.AbstractCrudService;
import com.sixsprints.core.utils.RandomUtil;

@Service
public class OtpServiceImpl extends AbstractCrudService<Otp> implements OtpService {

  @Autowired
  private OtpRepository otpRepository;

  @Override
  public Otp generate(String authId, int length) {
    double min = Math.pow(10, length - 1);
    double max = Math.pow(10, length) - 1;
    String otp = String.valueOf((int) RandomUtil.randomDouble(min, max, 0));
    try {
      return upsert(Otp.builder().authId(authId).otp(otp).build());
    } catch (EntityInvalidException e) {
    }
    return null;
  }

  @Override
  public Otp findByAuthIdAndOtp(String authId, String otp) {
    return otpRepository.findByAuthIdAndOtp(authId, otp);
  }

  @Override
  protected OtpRepository repository() {
    return otpRepository;
  }

  @Override
  protected MetaData<Otp> metaData() {
    return MetaData.<Otp>builder().classType(Otp.class).build();
  }

  @Override
  protected Otp findDuplicate(Otp otp) {
    return otpRepository.findByAuthId(otp.getAuthId());
  }

}
