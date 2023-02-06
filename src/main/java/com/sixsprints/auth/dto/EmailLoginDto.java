package com.sixsprints.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class EmailLoginDto implements Authenticable {

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String password;

  @Override
  public String authId() {
    return email;
  }

  @Override
  public String passcode() {
    return password;
  }
}
