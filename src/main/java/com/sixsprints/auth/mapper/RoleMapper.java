package com.sixsprints.auth.mapper;

import org.mapstruct.Mapper;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.dto.RoleDto;
import com.sixsprints.core.transformer.GenericMapper;

@Mapper(componentModel = "spring")
public abstract class RoleMapper extends GenericMapper<Role, RoleDto> {

  @Override
  public abstract RoleDto toDto(Role role);

  @Override
  public abstract Role toDomain(RoleDto dto);

}