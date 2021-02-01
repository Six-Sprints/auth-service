package com.sixsprints.auth.mock.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sixsprints.auth.controller.AbstractOtpBasedAuthController;
import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.auth.mock.dto.UserDto;
import com.sixsprints.auth.mock.service.UserService;

@RestController
@RequestMapping(value = "/api/v1/auth")
public class UserAuthController extends AbstractOtpBasedAuthController<User, UserDto> {

  public UserAuthController(UserService service) {
    super(service);
  }

}
