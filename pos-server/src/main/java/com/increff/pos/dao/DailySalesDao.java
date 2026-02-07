package com.increff.pos.dao;

import com.increff.pos.db.DailySalesPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySalesDao extends MongoRepository<DailySalesPojo, String> {

    DailySalesPojo findByDateAndClientId(LocalDate date, String clientId);

    List<DailySalesPojo> findByDate(LocalDate date);
}
