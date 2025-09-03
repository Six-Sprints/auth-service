package com.sixsprints.auth.mock.service;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.sixsprints.core.utils.RestExceptionHandler;

@ControllerAdvice
public class RestExceptionHandlerService extends RestExceptionHandler {

  public RestExceptionHandlerService(MessageSource messageSourceService) {
    super(messageSourceService);
  }

}
