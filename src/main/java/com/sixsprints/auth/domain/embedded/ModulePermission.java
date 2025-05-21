package com.sixsprints.auth.domain.embedded;

import java.io.Serializable;
import java.util.List;

import com.sixsprints.core.auth.ModuleDefinition;
import com.sixsprints.core.auth.PermissionDefinition;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

  @NotNull
  private ModuleDefinition module;

  @Singular
  @NotNull
  @Size(max = 512)
  private List<PermissionDefinition> permissions;

  private static final long serialVersionUID = 1L;
}
