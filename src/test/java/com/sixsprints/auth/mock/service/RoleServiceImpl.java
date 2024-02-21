package com.sixsprints.auth.mock.service;

import org.springframework.stereotype.Service;

import com.sixsprints.auth.mock.domain.Role;
import com.sixsprints.auth.mock.repository.RoleRepository;
import com.sixsprints.auth.service.AbstractRoleService;
import com.sixsprints.core.dto.MetaData;
import com.sixsprints.core.repository.GenericRepository;
import com.sixsprints.core.service.AbstractCrudService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends AbstractCrudService<Role> implements AbstractRoleService<Role> {

  private final RoleRepository repository;

  @Override
  public Role findByName(String name) {
    return repository.findByNameIgnoreCase(name);
  }

  @Override
  protected Role findDuplicate(Role entity) {
    return repository.findByNameIgnoreCase(entity.getName());
  }

  @Override
  protected MetaData<Role> metaData() {
    return MetaData.<Role>builder()
      .classType(Role.class)
      .build();
  }

  @Override
  protected GenericRepository<Role> repository() {
    return repository;
  }

}