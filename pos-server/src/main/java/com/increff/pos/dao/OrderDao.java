package com.increff.pos.dao;

import com.increff.pos.db.OrderPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class OrderDao extends AbstractDao<OrderPojo> {
    public OrderDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(OrderPojo.class),
                mongoOperations);
    }

    public OrderPojo findByOrderId(String orderId) {
        Query query = Query.query(Criteria.where("orderId").is(orderId));
        return mongoOperations.findOne(query, OrderPojo.class);
    }

    public List<OrderPojo> findByDateRange(ZonedDateTime fromDate, ZonedDateTime toDate) {
        Query query = Query.query(
                Criteria.where("orderDate").gte(fromDate).lte(toDate));
        return mongoOperations.find(query, OrderPojo.class);
    }

    public List<OrderPojo> findWithFilters(String orderId, String status, ZonedDateTime fromDate,
            ZonedDateTime toDate) {
        Criteria criteria = new Criteria();

        if (orderId != null && !orderId.trim().isEmpty()) {
            criteria = criteria.and("orderId").regex(orderId, "i");
        }
        if (status != null && !status.trim().isEmpty()) {
            criteria = criteria.and("status").is(status);
        }
        if (fromDate != null && toDate != null) {
            criteria = criteria.and("orderDate").gte(fromDate).lte(toDate);
        } else if (fromDate != null) {
            criteria = criteria.and("orderDate").gte(fromDate);
        } else if (toDate != null) {
            criteria = criteria.and("orderDate").lte(toDate);
        }

        Query query = Query.query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "orderDate"));
        return mongoOperations.find(query, OrderPojo.class);
    }

    public Page<OrderPojo> findWithFilters(String orderId, String status, ZonedDateTime fromDate,
            ZonedDateTime toDate, Pageable pageable) {
        Criteria criteria = new Criteria();

        if (orderId != null && !orderId.trim().isEmpty()) {
            criteria = criteria.and("orderId").regex(orderId, "i");
        }
        if (status != null && !status.trim().isEmpty()) {
            criteria = criteria.and("status").is(status);
        }
        if (fromDate != null && toDate != null) {
            criteria = criteria.and("orderDate").gte(fromDate).lte(toDate);
        } else if (fromDate != null) {
            criteria = criteria.and("orderDate").gte(fromDate);
        } else if (toDate != null) {
            criteria = criteria.and("orderDate").lte(toDate);
        }

        Query query = Query.query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "orderDate"));

        long total = mongoOperations.count(query, OrderPojo.class);
        query.with(pageable);
        List<OrderPojo> orders = mongoOperations.find(query, OrderPojo.class);

        return new PageImpl<>(orders, pageable, total);
    }

    @Override
    public Page<OrderPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}
