package com.sixsprints.auth.domain.embedded;

import java.io.Serializable;
import java.util.List;

import com.sixsprints.core.auth.ModuleDefinition;
import com.sixsprints.core.auth.PermissionDefinition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ModulePermission implements Serializable {

  private ModuleDefinition module;

  @Singular
  private List<PermissionDefinition> permissions;

  private static final long serialVersionUID = 1L;
}
