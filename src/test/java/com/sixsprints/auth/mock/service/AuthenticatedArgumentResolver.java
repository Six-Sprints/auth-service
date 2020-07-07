package com.sixsprints.auth.mock.service;

import org.springframework.stereotype.Component;

import com.sixsprints.auth.annotation.AbstractAuthenticatedArgumentResolver;
import com.sixsprints.auth.mock.domain.User;

@Component
public class AuthenticatedArgumentResolver extends AbstractAuthenticatedArgumentResolver<User> {

  public static final String TOKEN = "X-AUTH-TOKEN";

  public AuthenticatedArgumentResolver(UserService userService) {
    super(userService);
  }

  @Override
  protected String auhtTokenKey() {
    return TOKEN;
  }

}