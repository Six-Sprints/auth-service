package com.sixsprints.auth.mock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.sixsprints.core.service.MessageSourceService;
import com.sixsprints.core.utils.RestExceptionHandler;

@ControllerAdvice
public class RestExceptionHandlerService extends RestExceptionHandler {

  @Autowired
  private MessageSourceService messageSourceService;

  @Override
  protected MessageSourceService messageSourceService() {
    return messageSourceService;
  }

}
