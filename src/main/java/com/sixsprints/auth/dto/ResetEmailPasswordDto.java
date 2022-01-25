package com.sixsprints.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ResetEmailPasswordDto extends EmailLoginDto implements ResetPasscode {

  private String otp;

  @Override
  public String otp() {
    return otp;
  }

}
