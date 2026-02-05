package com.increff.pos.dao;

import com.increff.pos.db.InvoicePojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface InvoiceDao extends MongoRepository<InvoicePojo, String> {

    MongoOperations mongoOperations = null;

    InvoicePojo findByInvoiceId(String invoiceId);

    InvoicePojo findByOrderId(String orderId);

    List<InvoicePojo> findByInvoiceDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);
}
