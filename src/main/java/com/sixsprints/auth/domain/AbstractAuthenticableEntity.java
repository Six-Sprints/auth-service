package com.sixsprints.auth.domain;

import java.io.Serial;
import java.util.List;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.core.domain.AbstractMongoEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@Document
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractAuthenticableEntity extends AbstractMongoEntity implements Authenticable {

  public final static String PASSWORD = "password";

  public final static String INVALID_TOKENS = "invalidTokens";

  @Serial
  private static final long serialVersionUID = 4277363213912119827L;

  @NotNull
  @Size(min = 1, max = 256)
  private String password;

  @Singular
  @Size(max = 128)
  private List<String> invalidTokens;

  @Indexed
  @NotNull
  @Size(min = 1, max = 64)
  private String roleSlug;

  @Override
  public String passcode() {
    return getPassword();
  }

}
