package com.sixsprints.auth.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.core.domain.AbstractMongoEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Document
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Otp extends AbstractMongoEntity {

  private static final long serialVersionUID = -6052126621706116633L;

  @NotNull
  @Size(min = 1, max = 128)
  private String otp;

  @NotNull
  @Indexed(unique = true)
  @Size(min = 1, max = 256)
  private String authId;

}
