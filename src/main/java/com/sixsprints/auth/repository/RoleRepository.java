package com.sixsprints.auth.repository;

import org.springframework.stereotype.Repository;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.core.repository.GenericRepository;

@Repository
public interface RoleRepository extends GenericRepository<Role> {

  Role findByName(String name);

}