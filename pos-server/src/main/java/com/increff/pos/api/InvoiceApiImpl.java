package com.increff.pos.api;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.db.InvoicePojo;
import com.increff.pos.exception.ApiException;
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
    @Transactional(rollbackFor = ApiException.class)
    public InvoicePojo add(InvoicePojo invoicePojo) throws ApiException {
        logger.info("Creating invoice with invoiceId: {}", invoicePojo.getInvoiceId());

        if (invoiceDao.findByInvoiceId(invoicePojo.getInvoiceId()) != null) {
            throw new ApiException("Invoice with ID " + invoicePojo.getInvoiceId() + " already exists");
        }

        if (invoiceDao.findByOrderId(invoicePojo.getOrderId()) != null) {
            throw new ApiException("Order " + invoicePojo.getOrderId() + " already has an invoice");
        }

        if (invoicePojo.getInvoiceDate() == null) {
            invoicePojo.setInvoiceDate(java.time.Instant.now());
        }

        return invoiceDao.save(invoicePojo);
    }

    @Override
    public InvoicePojo get(String id) throws ApiException {
        InvoicePojo invoicePojo = invoiceDao.findById(id).orElse(null);
        if (invoicePojo == null) {
            throw new ApiException("Invoice with ID " + id + " does not exist");
        }
        return invoicePojo;
    }

    @Override
    public InvoicePojo getByInvoiceId(String invoiceId) throws ApiException {
        InvoicePojo invoicePojo = invoiceDao.findByInvoiceId(invoiceId);
        if (invoicePojo == null) {
            throw new ApiException("Invoice with invoiceId " + invoiceId + " does not exist");
        }
        return invoicePojo;
    }

    @Override
    public InvoicePojo getByOrderId(String orderId) throws ApiException {
        InvoicePojo invoicePojo = invoiceDao.findByOrderId(orderId);
        if (invoicePojo == null) {
            throw new ApiException("Invoice for order " + orderId + " does not exist");
        }
        return invoicePojo;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InvoicePojo update(String id, InvoicePojo invoicePojo) throws ApiException {
        InvoicePojo existing = get(id);
        
        // Update fields
        if (invoicePojo.getPdfPath() != null) {
            existing.setPdfPath(invoicePojo.getPdfPath());
        }
        if (invoicePojo.getTotalAmount() != null) {
            existing.setTotalAmount(invoicePojo.getTotalAmount());
        }

        return invoiceDao.save(existing);
    }
}
