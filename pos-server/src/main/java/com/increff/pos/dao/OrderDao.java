package com.increff.pos.dao;

import com.increff.pos.db.OrderPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    public List<OrderPojo> findByStatus(String status) {
        Query query = Query.query(Criteria.where("status").is(status));
        return mongoOperations.find(query, OrderPojo.class);
    }

    public List<OrderPojo> findByDateRange(Instant fromDate, Instant toDate) {
        Query query = Query.query(
                Criteria.where("orderDate").gte(fromDate).lte(toDate));
        return mongoOperations.find(query, OrderPojo.class);
    }

    public List<OrderPojo> findWithFilters(String orderId, String status, Instant fromDate, Instant toDate) {
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
        return mongoOperations.find(query, OrderPojo.class);
    }

    @Override
    public Page<OrderPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}
