package com.sixsprints.auth.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.sixsprints.auth.service.OtpService;
import com.sixsprints.core.config.ParentBeans;
import com.sixsprints.notification.service.NotificationService;

@Configuration
public class Beans extends ParentBeans {

  @Bean
  @Primary
  protected NotificationService notificationService() {
    return Mockito.mock(NotificationService.class);
  }

  @Bean
  @Primary
  protected OtpService otpService() {
    return Mockito.mock(OtpService.class);
  }

}
