package com.increff.pos.dao;

import com.increff.pos.db.OrderItemPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDao extends AbstractDao<OrderItemPojo> {
    public OrderItemDao(MongoOperations mongoOperations) {
        super(
            new MongoRepositoryFactory(mongoOperations)
                .getEntityInformation(OrderItemPojo.class),
            mongoOperations
        );
    }

    public List<OrderItemPojo> findByOrderId(String orderId) {
        Query query = Query.query(Criteria.where("orderId").is(orderId));
        return mongoOperations.find(query, OrderItemPojo.class);
    }

    public List<OrderItemPojo> findByProductId(String productId) {
        Query query = Query.query(Criteria.where("productId").is(productId));
        return mongoOperations.find(query, OrderItemPojo.class);
    }
}
