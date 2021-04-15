package com.sixsprints.auth.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.dto.RoleDto;
import com.sixsprints.auth.repository.RoleRepository;
import com.sixsprints.auth.service.RoleService;
import com.sixsprints.core.dto.MetaData;
import com.sixsprints.core.repository.GenericRepository;
import com.sixsprints.core.service.AbstractCrudService;

@Service
public class RoleServiceImpl extends AbstractCrudService<Role> implements RoleService {

  @Autowired
  private RoleRepository roleRepository;

  @Override
  protected GenericRepository<Role> repository() {
    return roleRepository;
  }

  @Override
  protected MetaData<Role> metaData() {
    return MetaData.<Role>builder()
      .classType(Role.class).dtoClassType(RoleDto.class)
      .build();
  }

  @Override
  protected Role findDuplicate(Role role) {
    return findByName(role.getName());
  }

  @Override
  protected List<String> checkValidity(Role role) {

    List<String> errors = new ArrayList<>();
    if (role == null || StringUtils.isBlank(role.getName())) {
      errors.add(roleNameInvalidError());
    }
    return errors;
  }

  private String roleNameInvalidError() {
    return "Role name cannot be blank! Please provide a role name";
  }

  @Override
  public Role findByName(String name) {
    return roleRepository.findByNameIgnoreCase(name);
  }

}
