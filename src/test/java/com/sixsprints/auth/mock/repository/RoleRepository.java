package com.sixsprints.auth.mock.repository;

import com.sixsprints.auth.mock.domain.Role;
import com.sixsprints.auth.repository.AbstractRoleRepository;

public interface RoleRepository extends AbstractRoleRepository<Role> {
  
  Role findByNameIgnoreCase(String name);

}
