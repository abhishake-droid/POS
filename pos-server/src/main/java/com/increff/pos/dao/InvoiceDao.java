package com.increff.pos.dao;

import com.increff.pos.db.InvoicePojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDao extends MongoRepository<InvoicePojo, String> {

    MongoOperations mongoOperations = null;

    InvoicePojo findByInvoiceId(String invoiceId);

    InvoicePojo findByOrderId(String orderId);
}
