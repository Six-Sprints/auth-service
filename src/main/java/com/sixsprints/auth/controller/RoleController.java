package com.sixsprints.auth.controller;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.dto.RoleDto;
import com.sixsprints.auth.mapper.RoleMapper;
import com.sixsprints.auth.service.RoleService;
import com.sixsprints.core.controller.AbstractCrudController;

public abstract class RoleController extends AbstractCrudController<Role, RoleDto> {

  public RoleController(RoleService service, RoleMapper mapper) {
    super(service, mapper);
  }

}