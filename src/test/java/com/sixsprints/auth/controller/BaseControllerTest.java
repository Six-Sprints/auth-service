package com.sixsprints.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.sixsprints.auth.AuthServiceApplication;
import com.sixsprints.core.utils.MessageSourceHolder;

@SpringBootTest(classes = AuthServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MessageSourceHolder.class)
public class BaseControllerTest {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Test
  public void contextLoads() {
    assertThat(Boolean.TRUE).isTrue();
  }

  @AfterEach
  public void tearDown() {
    mongoTemplate.getDb().drop();
  }

}
