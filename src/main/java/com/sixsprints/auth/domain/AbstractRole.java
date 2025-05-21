package com.sixsprints.auth.domain;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.auth.domain.embedded.ModulePermission;
import com.sixsprints.core.domain.AbstractMongoEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public abstract class AbstractRole extends AbstractMongoEntity {

  private static final long serialVersionUID = 6464769316000227488L;

  @NotNull
  @Size(min = 1, max = 256)
  private String name;

  @Size(max = 1024)
  private String description;

  @Singular
  @Size(max = 512)
  private List<@Valid ModulePermission> modulePermissions;

}
