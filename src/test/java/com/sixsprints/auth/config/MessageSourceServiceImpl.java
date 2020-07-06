package com.sixsprints.auth.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.sixsprints.core.service.MessageSourceService;

@Component
public class MessageSourceServiceImpl implements MessageSourceService {

  @Autowired
  private MessageSource messageSource;

  @Override
  public MessageSource messageSource() {
    return messageSource;
  }

  @Override
  public String genericError() {
    return "error";
  }

  @Override
  public Locale defaultLocale() {
    return Locale.ENGLISH;
  }

}
