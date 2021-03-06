package com.sixsprints.auth.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

  private String id;

  private String slug;

  private String name;

  private List<PermissionDto> permissions;

}
