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
@ToString(exclude = "password")
public class EmailLoginDto implements Authenticable {

  private String email;

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
