package com.sixsprints.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sixsprints.notification.dto.MessageAuthDto;
import com.sixsprints.notification.service.NotificationService;
import com.sixsprints.notification.service.impl.EmailServiceSmtp;

@Configuration
public class Beans {

  @Bean
  public NotificationService notificationService() {
    return new EmailServiceSmtp(MessageAuthDto.builder()
      .from("Test Email")
      .hostName("mail.sixsprints.com")
      .username("test@sixsprints.com")
      .password("abcd123!")
      .sslEnabled(true)
      .build());
  }

}
