package com.sixsprints.auth.mock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sixsprints.auth.annotation.Authenticated;
import com.sixsprints.auth.controller.AbstractOtpBasedAuthController;
import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.auth.mock.dto.UserDto;
import com.sixsprints.auth.mock.service.AuthenticatedArgumentResolver;
import com.sixsprints.auth.mock.service.UserService;
import com.sixsprints.auth.mock.transformer.UserMapper;
import com.sixsprints.core.utils.RestResponse;

@RestController
@RequestMapping(value = "/api/v1/auth")
public class UserAuthController extends AbstractOtpBasedAuthController<User, UserDto> {

  public UserAuthController(UserService service, UserMapper mapper) {
    super(service, mapper);
  }

  public ResponseEntity<?> logout(@Authenticated(required = false) User user,
    @RequestHeader(value = AuthenticatedArgumentResolver.TOKEN, required = false) String token) {
    return super.logout(user, token);
  }

  @Override
  public ResponseEntity<RestResponse<UserDto>> validateToken(@Authenticated User user) {
    return super.validateToken(user);
  }

}
