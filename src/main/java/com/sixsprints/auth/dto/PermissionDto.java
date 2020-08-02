package com.sixsprints.auth.dto;

import java.io.Serializable;
import java.util.List;

import com.sixsprints.core.enums.AccessPermission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private String entityPermission;

  @Singular
  private List<AccessPermission> accessPermissions;

}
