package com.sixsprints.auth.repository;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.core.repository.GenericRepository;

public interface RoleRepository extends GenericRepository<Role> {

  Role findByNameIgnoreCase(String name);

}