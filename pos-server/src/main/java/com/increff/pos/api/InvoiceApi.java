package com.increff.pos.api;

import com.increff.pos.db.InvoicePojo;
import com.increff.pos.exception.ApiException;

public interface InvoiceApi {
    InvoicePojo add(InvoicePojo invoicePojo) throws ApiException;
    InvoicePojo get(String id) throws ApiException;
    InvoicePojo getByInvoiceId(String invoiceId) throws ApiException;
    InvoicePojo getByOrderId(String orderId) throws ApiException;
    InvoicePojo update(String id, InvoicePojo invoicePojo) throws ApiException;
}
