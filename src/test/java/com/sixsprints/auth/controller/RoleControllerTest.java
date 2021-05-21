package com.sixsprints.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.service.RoleService;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;

public class RoleControllerTest extends BaseControllerTest {

  @Autowired
  private RoleService roleService;

  @Test
  public void shouldNotSaveDuplicateRoleName() throws EntityAlreadyExistsException, EntityInvalidException {

    roleService.create(Role.builder().name("ADMIN").build());
    roleService.create(Role.builder().name("User").build());

  }

}
