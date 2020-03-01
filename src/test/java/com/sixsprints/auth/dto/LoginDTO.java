package com.sixsprints.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class LoginDTO implements Authenticable {
  @NotBlank
  @Email
  private String email;
  @NotBlank
  private String password;

  @Override
  public String getAuthId() {
    return getEmail();
  }

  @Override
  public String getPasscode() {
    return getPassword();
  }
}
