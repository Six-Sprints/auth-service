package com.sixsprints.auth.mock.service;

import org.springframework.stereotype.Component;

import com.sixsprints.auth.interceptor.AbstractRoleBasedAuthInterceptor;
import com.sixsprints.auth.mock.domain.User;

@Component
public class AuthInterceptor extends AbstractRoleBasedAuthInterceptor<User> {

  public AuthInterceptor(UserService userService) {
    super(userService);
  }

  public static final String TOKEN = "X-AUTH-TOKEN";

  @Override
  protected String auhtTokenKey() {
    return TOKEN;
  }

}