package com.sixsprints.auth.util;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;

public class ThreadContext {

  private static InheritableThreadLocal<Object> threadData = new InheritableThreadLocal<>();

  public static <T extends AbstractAuthenticableEntity> void setCurrentUser(T user) {
    threadData.set(user);
  }

  @SuppressWarnings("unchecked")
  public static <T extends AbstractAuthenticableEntity> T getCurrentUser() {
    return (T) threadData.get();
  }
}