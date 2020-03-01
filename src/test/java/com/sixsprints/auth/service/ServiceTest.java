package com.sixsprints.auth.service;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.util.Assert;

import com.sixsprints.auth.AuthServiceApplicationTests;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.LoginDTO;
import com.sixsprints.auth.dto.UserDto;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;

public class ServiceTest extends AuthServiceApplicationTests {

  @Resource
  private UserService userService;

  @Test
  public void registerUserShouldSucceed() throws EntityAlreadyExistsException, EntityInvalidException {
    UserDto user = UserDto.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    AuthResponseDto<UserDto> register = userService.register(user);
    Assert.notNull(register.getData().getId(), "Not registered");
  }

  @Test(expected = EntityInvalidException.class)
  public void registerUserShouldNotSucceedEmptyEmail() throws EntityAlreadyExistsException, EntityInvalidException {
    UserDto user = UserDto.builder().name("Sudip").password("12345").build();
    AuthResponseDto<UserDto> register = userService.register(user);
    Assert.notNull(register.getData().getId(), "Should not registered but did");
  }

  @Test(expected = EntityAlreadyExistsException.class)
  public void registerUserShouldNotSucceedDuplicateEmail()
    throws EntityAlreadyExistsException, EntityInvalidException, EntityNotFoundException {
    UserDto user = UserDto.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    AuthResponseDto<UserDto> register = userService.register(user);
    user = UserDto.builder().name("Qwerty").email("sudip@email.com").password("14341").build();
    register = userService.register(user);
    Assert.notNull(register.getData().getId(), "Should not registered but did");
  }

  @Test
  public void loginUserShouldSucceed()
    throws EntityAlreadyExistsException, EntityInvalidException, NotAuthenticatedException, EntityNotFoundException {
    UserDto user = UserDto.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    userService.register(user);

    LoginDTO loginDTO = LoginDTO.builder().email("sudip@email.com").password("12345").build();
    AuthResponseDto<UserDto> login = userService.login(loginDTO);
    Assert.notNull(login, "Login failed");
  }

  @Test(expected = NotAuthenticatedException.class)
  public void loginUserShouldNotSucceedWrongPswrd()
    throws EntityAlreadyExistsException, EntityInvalidException, NotAuthenticatedException, EntityNotFoundException {
    UserDto user = UserDto.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    userService.register(user);

    LoginDTO loginDTO = LoginDTO.builder().email("sudip@email.com").password("1234").build();
    AuthResponseDto<UserDto> login = userService.login(loginDTO);
    Assert.isNull(login, "Login should gets failed but didn't");
  }

  @Test(expected = EntityNotFoundException.class)
  public void loginUserShouldNotSucceedNotRegistered()
    throws EntityAlreadyExistsException, EntityInvalidException, NotAuthenticatedException, EntityNotFoundException {
    UserDto user = UserDto.builder().name("Sudip").email("sudip@email.com").password("12345").build();
    userService.register(user);

    LoginDTO loginDTO = LoginDTO.builder().email("qwerty@email.com").password("12345").build();
    AuthResponseDto<UserDto> login = userService.login(loginDTO);
    Assert.isNull(login, "Login should gets failed but didn't");
  }

  @Test
  public void shouldSendOtp() throws EntityAlreadyExistsException, EntityInvalidException, EntityNotFoundException {
    UserDto user = UserDto.builder().name("Sudip").email("kgujral@gmail.com").password("12345").build();
    userService.register(user);
    Otp otp = userService.sendOtp(user.getEmail());
    Assert.notNull(otp.getOtp(), "OTP must not be null");
  }

  @Test
  public void shouldResetPassword()
    throws EntityAlreadyExistsException, EntityInvalidException, EntityNotFoundException, NotAuthenticatedException {
    UserDto user = UserDto.builder().name("Sudip").email("kgujral@gmail.com").password("oldpass").build();
    userService.register(user);
    Otp otp = userService.sendOtp(user.getEmail());

    userService.resetPassword(user.getEmail(), otp.getOtp(), "newpass");

    LoginDTO loginDTO = LoginDTO.builder().email("kgujral@gmail.com").password("newpass").build();
    AuthResponseDto<UserDto> login = userService.login(loginDTO);
    Assert.notNull(login, "Login failed");
  }

  @Test(expected = EntityInvalidException.class)
  public void shouldNotResetPasswordOtpWrong()
    throws EntityAlreadyExistsException, EntityInvalidException, EntityNotFoundException {
    UserDto user = UserDto.builder().name("Sudip").email("kgujral@gmail.com").password("oldpass").build();
    userService.register(user);
    userService.sendOtp(user.getEmail());
    userService.resetPassword(user.getEmail(), "wrongOtp", "newpass");
  }
}