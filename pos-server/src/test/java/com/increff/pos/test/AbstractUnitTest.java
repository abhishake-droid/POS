package com.increff.pos.test;

import com.increff.pos.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
public abstract class AbstractUnitTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    public void cleanup() {
        // Drop all collections after each test
        mongoTemplate.getCollectionNames().forEach(collectionName -> 
            mongoTemplate.dropCollection(collectionName));
    }
} 