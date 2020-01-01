package com.sixsprints.auth.domain.mock;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.core.domain.AbstractMongoEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document
public class PasswordReset extends AbstractMongoEntity {

  private static final long serialVersionUID = 3907769974134268344L;

  @Indexed(unique = true)
  private String email;

  private String otp;

}
