package com.sixsprints.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.sixsprints.auth.AuthServiceApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseControllerTest {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Test
  public void contextLoads() {
    assertThat(Boolean.TRUE).isTrue();
  }

  @After
  public void tearDown() {
    mongoTemplate.getDb().drop();
  }

}
