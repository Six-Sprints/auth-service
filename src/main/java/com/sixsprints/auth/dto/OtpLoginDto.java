package com.sixsprints.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "otp")
public class OtpLoginDto implements Authenticable {

  private String mobileNumber;

  private String otp;

  @Override
  public String authId() {
    return getMobileNumber();
  }

  @Override
  public String passcode() {
    return getOtp();
  }
}
