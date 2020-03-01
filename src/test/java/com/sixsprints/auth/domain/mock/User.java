package com.sixsprints.auth.domain.mock;

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

  @Indexed(unique = true)
  private String email;

  private String mobileNumber;

  @Override
  public String authId() {
    return getEmail();
  }

}
