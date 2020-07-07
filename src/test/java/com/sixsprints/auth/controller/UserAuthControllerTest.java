package com.sixsprints.auth.controller;

import java.util.UUID;
import java.util.concurrent.Future;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.OtpLoginDto;
import com.sixsprints.auth.mock.dto.UserDto;
import com.sixsprints.auth.mock.service.AuthenticatedArgumentResolver;
import com.sixsprints.auth.mock.service.UserService;
import com.sixsprints.auth.service.OtpService;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.notification.service.NotificationService;

public class UserAuthControllerTest extends BaseControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private OtpService otpService;

  @Autowired
  private UserService userService;

  @Before
  public void mockServices() {
    @SuppressWarnings("unchecked")
    Future<String> future = Mockito.mock(Future.class);
    Mockito.when(notificationService.sendMessage(Mockito.any())).thenReturn(future);

    Mockito.when(otpService.generate(Mockito.anyString(), Mockito.anyInt())).thenAnswer(inv -> {
      String num = inv.getArgument(0);
      return Otp.builder().authId(num).otp("1234").build();
    });

    Mockito.when(otpService.findByAuthIdAndOtp(Mockito.anyString(), Mockito.eq("1234"))).thenAnswer(inv -> {
      String num = inv.getArgument(0);
      String otp = inv.getArgument(1);
      return Otp.builder().authId(num).otp(otp).build();
    });

  }

  @Test
  public void shouldRegisterUser() throws Exception {
    String mobileNumber = "9810306710";
    String email = "kgujral@gmail.com";
    String userJson = userJson(mobileNumber, email);
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/register")
        .content(userJson)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isCreated())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.token", CoreMatchers.notNullValue()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.data.mobileNumber", CoreMatchers.is(mobileNumber)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.data.email", CoreMatchers.is(email)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.data.slug", CoreMatchers.notNullValue()));
  }

  @Test
  public void shouldThrowExceptionIfAlreadyRegistered() throws Exception {

    shouldRegisterUser();

    String mobileNumber = "9810306710";
    String email = "kgujral@gmail.com";
    String userJson = userJson(mobileNumber, email);
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/register")
        .content(userJson)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.FALSE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.notNullValue()));
  }

  @Test
  public void shouldGenerateOtpForANewUser() throws Exception {
    String mobileNumber = "9810306710";
    String otpJson = mapper.writeValueAsString(OtpLoginDto.builder().mobileNumber(mobileNumber).build());
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/send-otp-login")
        .content(otpJson)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.mobileNumber", CoreMatchers.is(mobileNumber)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.email", CoreMatchers.notNullValue()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.slug", CoreMatchers.notNullValue()));
  }

  @Test
  public void shouldGenerateOtpForAnExistingUser() throws Exception {

    shouldRegisterUser();

    String mobileNumber = "9810306710";
    String otpJson = mapper.writeValueAsString(OtpLoginDto.builder().mobileNumber(mobileNumber).build());
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/send-otp-login")
        .content(otpJson)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.mobileNumber", CoreMatchers.is(mobileNumber)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.email", CoreMatchers.notNullValue()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.slug", CoreMatchers.notNullValue()));
  }

  @Test
  public void shouldLogin() throws Exception {

    shouldGenerateOtpForANewUser();

    String mobileNumber = "9810306710";
    String otp = "1234";
    String otpJson = mapper.writeValueAsString(OtpLoginDto.builder().mobileNumber(mobileNumber).otp(otp).build());
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/login")
        .content(otpJson)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.token", CoreMatchers.notNullValue()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.data.mobileNumber", CoreMatchers.is(mobileNumber)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.data.slug", CoreMatchers.notNullValue()));
  }

  @Test
  public void shouldNotLogin() throws Exception {

    shouldGenerateOtpForANewUser();

    String mobileNumber = "9810306710";
    String otp = "xxxx";
    String otpJson = mapper.writeValueAsString(OtpLoginDto.builder().mobileNumber(mobileNumber).otp(otp).build());
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/login")
        .content(otpJson)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.FALSE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.notNullValue()));
  }

  @Test
  public void shouldLogout() throws Exception {
    String mobileNumber = "9810306710";
    AuthResponseDto<UserDto> user = saveUser(mobileNumber);
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/logout").header(AuthenticatedArgumentResolver.TOKEN, user.getToken()))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)));
  }

  @Test
  public void shouldLogoutIfInvalidTokenOrNoToken() throws Exception {
    String mobileNumber = "9810306710";
    saveUser(mobileNumber);
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/logout").header(AuthenticatedArgumentResolver.TOKEN, "DUMMY_TOKEN"))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)));

    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/logout"))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)));
  }

  @Test
  public void shouldValidateToken() throws Exception {
    String mobileNumber = "9810306710";
    AuthResponseDto<UserDto> user = saveUser(mobileNumber);
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/validate-token")
        .header(AuthenticatedArgumentResolver.TOKEN, user.getToken()).contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.TRUE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.mobileNumber", CoreMatchers.is(mobileNumber)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.data.slug", CoreMatchers.notNullValue()));

  }

  @Test
  public void shouldNotValidateTokenIfInvalidToken() throws Exception {
    String mobileNumber = "9810306710";
    saveUser(mobileNumber);
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/validate-token")
        .header(AuthenticatedArgumentResolver.TOKEN, "DUMMY_TOKEN").contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.FALSE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.notNullValue()));
  }

  @Test
  public void shouldNotValidateTokenIfEmptyToken() throws Exception {
    String mobileNumber = "9810306710";
    saveUser(mobileNumber);
    mvc.perform(
      MockMvcRequestBuilders.post("/api/v1/auth/validate-token").contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(Boolean.FALSE)))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.notNullValue()));
  }

  private AuthResponseDto<UserDto> saveUser(String mobileNumber)
    throws EntityAlreadyExistsException, EntityInvalidException {
    return userService.register(userDto(UUID.randomUUID().toString().concat("@gmail.com"), mobileNumber));
  }

  private String userJson(String mobileNumber, String email) throws JsonProcessingException {
    return mapper.writeValueAsString(userDto(email, mobileNumber));
  }

  private UserDto userDto(String email, String mobileNumber) {
    return UserDto.builder().email(email).mobileNumber(mobileNumber).build();
  }

}
