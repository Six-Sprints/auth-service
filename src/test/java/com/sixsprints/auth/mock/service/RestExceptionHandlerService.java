package com.sixsprints.auth.mock.service;

import org.springframework.web.bind.annotation.ControllerAdvice;

import com.sixsprints.core.service.MessageSourceService;
import com.sixsprints.core.utils.RestExceptionHandler;

@ControllerAdvice
public class RestExceptionHandlerService extends RestExceptionHandler {

  public RestExceptionHandlerService(MessageSourceService messageSourceService) {
    super(messageSourceService);
  }

}
