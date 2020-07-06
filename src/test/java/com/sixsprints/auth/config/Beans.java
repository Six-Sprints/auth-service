package com.sixsprints.auth.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.sixsprints.auth.service.OtpService;
import com.sixsprints.notification.service.NotificationService;

@Configuration
public class Beans {

  @Bean
  @Primary
  public NotificationService notificationService() {
    return Mockito.mock(NotificationService.class);
  }

  @Bean
  @Primary
  public OtpService otpService() {
    return Mockito.mock(OtpService.class);
  }

}
