package com.sixsprints.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.mongodb.MongoClient;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration {
  @Value(value = "${spring.data.mongodb.host}")
  private String host;

  @Value(value = "${spring.data.mongodb.database}")
  private String database;

  @Value(value = "${spring.data.mongodb.port}")
  private int port;

  @Bean
  public MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
    return new MongoTransactionManager(dbFactory);
  }

  @Override
  protected String getDatabaseName() {
    return database;
  }

  @Bean
  @Override
  public MongoClient mongoClient() {
    return new MongoClient(host, port);
  }
}
