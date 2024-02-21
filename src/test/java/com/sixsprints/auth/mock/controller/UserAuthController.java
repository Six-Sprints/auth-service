package com.sixsprints.auth.mock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sixsprints.auth.controller.AbstractOtpBasedAuthController;
import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.auth.mock.dto.UserDto;
import com.sixsprints.auth.mock.service.UserService;
import com.sixsprints.core.auth.BasicAuth;
import com.sixsprints.core.utils.ApplicationContext;
import com.sixsprints.core.utils.RestUtil;

@RestController
@RequestMapping(value = "/api/v1/auth")
public class UserAuthController extends AbstractOtpBasedAuthController<User, UserDto, UserDto> {

  private final UserService userService;

  public UserAuthController(UserService userService) {
    super(userService);
    this.userService = userService;
  }

  @BasicAuth(required = false)
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader(name = "X-AUTH-TOKEN", required = false) String token) {
    User user = ApplicationContext.getCurrentUser();
    if (user != null) {
      userService.logout(user, token);
    }
    return RestUtil.successResponse(null);
  }

}
