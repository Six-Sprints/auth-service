package com.sixsprints.auth.hooks;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;

public interface PreRegisterHook<T extends AbstractAuthenticableEntity> {

  /**
   * Called before a user registration is completed.
   * This method can be used to perform pre-registration validation, set default values,
   * or apply any necessary transformations before the entity is persisted.
   *
   * @param entity the entity to be registered, must not be null
   */
  void preRegister(T entity);

}

