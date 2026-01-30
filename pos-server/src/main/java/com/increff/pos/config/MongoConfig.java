package com.increff.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;

@Configuration
@EnableMongoRepositories(basePackages = "com.increff.pos.dao")
@EnableMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new MongoConverters.DateToZonedDateTimeConverter(),
                new MongoConverters.ZonedDateTimeToDateConverter(),
                new MongoConverters.DateToLocalDateConverter(),
                new MongoConverters.LocalDateToDateConverter()));
    }
}