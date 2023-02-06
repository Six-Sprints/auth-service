package com.sixsprints.auth.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.sixsprints.core.config.ParentMongoConfig;

@Configuration
@EnableMongoRepositories(basePackages = { "com.sixsprints.core", "com.sixsprints.auth" })
@EntityScan(basePackages = { "com.sixsprints.core", "com.sixsprints.auth" })
@EnableMongoAuditing
public class MongoConfig extends ParentMongoConfig {

}
