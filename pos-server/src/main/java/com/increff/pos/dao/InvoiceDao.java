package com.increff.pos.dao;

import com.increff.pos.db.InvoicePojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class InvoiceDao extends AbstractDao<InvoicePojo> {
    public InvoiceDao(MongoOperations mongoOperations) {
        super(
            new MongoRepositoryFactory(mongoOperations)
                .getEntityInformation(InvoicePojo.class),
            mongoOperations
        );
    }

    public InvoicePojo findByInvoiceId(String invoiceId) {
        Query query = Query.query(Criteria.where("invoiceId").is(invoiceId));
        return mongoOperations.findOne(query, InvoicePojo.class);
    }

    public InvoicePojo findByOrderId(String orderId) {
        Query query = Query.query(Criteria.where("orderId").is(orderId));
        return mongoOperations.findOne(query, InvoicePojo.class);
    }
}
