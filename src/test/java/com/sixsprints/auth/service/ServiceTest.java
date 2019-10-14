package com.sixsprints.auth.service;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.util.Assert;

import com.sixsprints.auth.AuthServiceApplicationTests;
import com.sixsprints.auth.domain.mock.User;
import com.sixsprints.auth.dto.AuthResponseDTO;
import com.sixsprints.auth.dto.LoginDTO;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.NotAuthenticatedException;

public class ServiceTest extends AuthServiceApplicationTests {

  @Resource
  private AuthService<User> authService;

  @Test
  public void registerUserShouldSucceed() throws EntityAlreadyExistsException, EntityInvalidException {
    User user = User.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    AuthResponseDTO<User> register = authService.register(user);
    Assert.notNull(register.getData().getId(), "Not registered");
  }

  @Test(expected = EntityInvalidException.class)
  public void registerUserShouldNotSucceedEmptyEmail() throws EntityAlreadyExistsException, EntityInvalidException {
    User user = User.builder().name("Sudip").password("12345").build();
    AuthResponseDTO<User> register = authService.register(user);
    Assert.notNull(register.getData().getId(), "Should not registered but did");
  }

  @Test(expected = EntityAlreadyExistsException.class)
  public void registerUserShouldNotSucceedDuplicateEmail() throws EntityAlreadyExistsException, EntityInvalidException {
    User user = User.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    authService.register(user);
    user = User.builder().name("Qwerty").email("sudip@email.com").password("14341").build();
    AuthResponseDTO<User> register = authService.register(user);
    Assert.notNull(register.getData().getId(), "Should not registered but did");
  }

  @Test
  public void loginUserShouldSucceed()
    throws EntityAlreadyExistsException, EntityInvalidException, NotAuthenticatedException {
    User user = User.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    authService.register(user);

    LoginDTO loginDTO = LoginDTO.builder().email("sudip@email.com").password("12345").build();
    AuthResponseDTO<User> login = authService.login(loginDTO);
    Assert.notNull(login, "Login failed");
  }

  @Test(expected = NotAuthenticatedException.class)
  public void loginUserShouldNotSucceedWrongPswrd()
    throws EntityAlreadyExistsException, EntityInvalidException, NotAuthenticatedException {
    User user = User.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    authService.register(user);

    LoginDTO loginDTO = LoginDTO.builder().email("sudip@email.com").password("1234").build();
    AuthResponseDTO<User> login = authService.login(loginDTO);
    Assert.isNull(login, "Login should gets failed but didn't");
  }

  @Test(expected = NotAuthenticatedException.class)
  public void loginUserShouldNotSucceedNotRegistered()
    throws EntityAlreadyExistsException, EntityInvalidException, NotAuthenticatedException {
    User user = User.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    authService.register(user);

    LoginDTO loginDTO = LoginDTO.builder().email("qwerty@email.com").password("12345").build();
    AuthResponseDTO<User> login = authService.login(loginDTO);
    Assert.isNull(login, "Login should gets failed but didn't");
  }
}

//RestTemplate restTemplate = new RestTemplate();
//final String baseUrl = "http://localhost:3214/auth/login/user";
//URI uri = new URI(baseUrl);
//ResponseEntity<RestResponse> result = restTemplate.postForEntity(uri, loginDTO, RestResponse.class);
//assertEquals(200, result.getStatusCodeValue());