package com.sixsprints.auth.hooks;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;

public interface PostRegisterHook<T extends AbstractAuthenticableEntity> {

  /**
   * Called after a user registration has been successfully completed.
   * This method can be used to perform post-registration actions such as logging,
   * notifications, or any operations that depend on the entity having a valid ID.
   *
   * @param entity the entity that was registered, must not be null
   */
  void postRegister(T entity);

}

