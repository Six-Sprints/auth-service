package com.sixsprints.auth.domain;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.auth.domain.embedded.ModulePermission;
import com.sixsprints.core.domain.AbstractMongoEntity;

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

  private String name;

  private String description;

  @Singular
  private List<ModulePermission> modulePermissions;

}
