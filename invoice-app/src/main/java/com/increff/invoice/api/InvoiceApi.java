package com.increff.invoice.api;

import com.increff.invoice.db.InvoicePojo;
import com.increff.invoice.exception.InvoiceException;

public interface InvoiceApi {
    InvoicePojo add(InvoicePojo invoicePojo) throws InvoiceException;

    InvoicePojo get(String id) throws InvoiceException;

    InvoicePojo getByInvoiceId(String invoiceId) throws InvoiceException;

    InvoicePojo getByOrderId(String orderId) throws InvoiceException;
}
