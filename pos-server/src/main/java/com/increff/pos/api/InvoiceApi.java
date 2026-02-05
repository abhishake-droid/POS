package com.increff.pos.api;

import com.increff.pos.db.InvoicePojo;
import com.increff.pos.exception.ApiException;

import java.time.ZonedDateTime;
import java.util.List;

public interface InvoiceApi {
    InvoicePojo add(InvoicePojo invoicePojo) throws ApiException;

    InvoicePojo getCheck(String id) throws ApiException;

    InvoicePojo getCheckByInvoiceId(String invoiceId) throws ApiException;

    InvoicePojo getCheckByOrderId(String orderId) throws ApiException;

    List<InvoicePojo> getByInvoiceDateRange(ZonedDateTime startDate, ZonedDateTime endDate);
}
