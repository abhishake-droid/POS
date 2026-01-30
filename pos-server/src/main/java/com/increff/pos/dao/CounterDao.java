package com.increff.pos.dao;

import com.increff.pos.db.CounterPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterDao extends MongoRepository<CounterPojo, String> {
}