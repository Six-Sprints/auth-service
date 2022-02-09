package com.sixsprints.auth.controller;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.dto.RoleDto;
import com.sixsprints.auth.service.RoleService;
import com.sixsprints.core.controller.AbstractCrudController;
import com.sixsprints.core.transformer.GenericMapper;

public abstract class RoleController extends AbstractCrudController<Role, RoleDto, RoleDto, RoleDto> {

  public RoleController(RoleService service, GenericMapper<Role, RoleDto> mapper) {
    super(service, mapper, mapper, mapper);
  }

}