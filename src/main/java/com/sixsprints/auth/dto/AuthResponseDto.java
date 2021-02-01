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
public class AuthResponseDto<T> {

  private String token;

  private T data;

  private String roleName;

  private List<PermissionDto> permissions;

}
