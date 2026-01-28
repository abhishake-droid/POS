package com.increff.invoice.api;

import com.increff.invoice.dao.InvoiceDao;
import com.increff.invoice.db.InvoicePojo;
import com.increff.invoice.exception.InvoiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceApiImpl implements InvoiceApi {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceApiImpl.class);

    private final InvoiceDao invoiceDao;

    public InvoiceApiImpl(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    @Override
    @Transactional(rollbackFor = InvoiceException.class)
    public InvoicePojo add(InvoicePojo invoicePojo) throws InvoiceException {
        logger.info("Creating invoice with invoiceId: {}", invoicePojo.getInvoiceId());

        if (invoiceDao.findByInvoiceId(invoicePojo.getInvoiceId()) != null) {
            throw new InvoiceException("Invoice with ID " + invoicePojo.getInvoiceId() + " already exists");
        }

        if (invoiceDao.findByOrderId(invoicePojo.getOrderId()) != null) {
            throw new InvoiceException("Order " + invoicePojo.getOrderId() + " already has an invoice");
        }

        if (invoicePojo.getInvoiceDate() == null) {
            invoicePojo.setInvoiceDate(java.time.Instant.now());
        }

        return invoiceDao.save(invoicePojo);
    }

    @Override
    public InvoicePojo get(String id) throws InvoiceException {
        InvoicePojo invoicePojo = invoiceDao.findById(id).orElse(null);
        if (invoicePojo == null) {
            throw new InvoiceException("Invoice with ID " + id + " does not exist");
        }
        return invoicePojo;
    }

    @Override
    public InvoicePojo getByInvoiceId(String invoiceId) throws InvoiceException {
        InvoicePojo invoicePojo = invoiceDao.findByInvoiceId(invoiceId);
        if (invoicePojo == null) {
            throw new InvoiceException("Invoice with invoiceId " + invoiceId + " does not exist");
        }
        return invoicePojo;
    }

    @Override
    public InvoicePojo getByOrderId(String orderId) throws InvoiceException {
        InvoicePojo invoicePojo = invoiceDao.findByOrderId(orderId);
        if (invoicePojo == null) {
            throw new InvoiceException("Invoice for order " + orderId + " does not exist");
        }
        return invoicePojo;
    }
}
