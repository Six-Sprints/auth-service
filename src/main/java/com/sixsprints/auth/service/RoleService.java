package com.sixsprints.auth.service;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.core.service.GenericCrudService;

public interface RoleService extends GenericCrudService<Role> {

  Role findByName(String name);

}