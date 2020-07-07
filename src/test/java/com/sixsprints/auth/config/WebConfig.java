package com.sixsprints.auth.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sixsprints.auth.mock.service.AuthenticatedArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private AuthenticatedArgumentResolver authenticatedArgumentResolver;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(authenticatedArgumentResolver);
  }

}
