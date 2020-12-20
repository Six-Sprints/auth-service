package com.sixsprints.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceApplicationTests {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Test
  public void contextLoads() {
  }

  @AfterEach
  public void tearDown() {
    mongoTemplate.getDb().drop();
  }

}
