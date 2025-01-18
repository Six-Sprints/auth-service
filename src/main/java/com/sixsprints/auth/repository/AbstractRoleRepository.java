package com.sixsprints.auth.repository;

import com.sixsprints.auth.domain.AbstractRole;
import com.sixsprints.core.repository.GenericCrudRepository;

public interface AbstractRoleRepository<T extends AbstractRole> extends GenericCrudRepository<T> {

}