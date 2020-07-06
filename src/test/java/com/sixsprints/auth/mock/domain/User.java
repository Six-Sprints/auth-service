package com.sixsprints.auth.mock.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class User extends AbstractAuthenticableEntity {

  private static final long serialVersionUID = 1L;

  private String name;

  private String email;

  @Indexed(unique = true)
  private String mobileNumber;

  @Override
  public String authId() {
    return getMobileNumber();
  }

}
