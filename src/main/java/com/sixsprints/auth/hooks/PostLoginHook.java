package com.sixsprints.auth.hooks;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;

public interface PostLoginHook<T extends AbstractAuthenticableEntity> {

  /**
   * Called after a user login has been successfully validated and before token generation.
   * This method can be used to perform additional validation checks, logging,
   * or any operations that should occur after password validation succeeds.
   *
   * @param user the user that successfully logged in, must not be null
   */
  void postLogin(T user);

}

