package com.sixsprints.auth.dto;

import java.util.List;

import com.sixsprints.auth.enums.AccessPermission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {

  private String entityPermission;

  @Singular
  private List<AccessPermission> accessPermissions;

}
