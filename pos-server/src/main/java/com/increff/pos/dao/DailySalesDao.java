package com.increff.pos.dao;

import com.increff.pos.db.DailySalesPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySalesDao extends MongoRepository<DailySalesPojo, String> {

    @Query("{ 'date': { $gte: ?0, $lte: ?1 } }")
    List<DailySalesPojo> findByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("{ 'date': { $gte: ?0, $lte: ?1 }, 'clientId': ?2 }")
    List<DailySalesPojo> findByDateRangeAndClientId(LocalDate startDate, LocalDate endDate, String clientId);

    DailySalesPojo findByDateAndClientId(LocalDate date, String clientId);

    List<DailySalesPojo> findByDate(LocalDate date);
}
