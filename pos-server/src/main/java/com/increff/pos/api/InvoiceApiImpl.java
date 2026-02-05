package com.increff.pos.api;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.db.InvoicePojo;
import com.increff.pos.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class InvoiceApiImpl implements InvoiceApi {

    @Autowired
    private InvoiceDao invoiceDao;

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InvoicePojo add(InvoicePojo invoicePojo) throws ApiException {
        return invoiceDao.save(invoicePojo);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoicePojo getCheck(String id) throws ApiException {
        return invoiceDao.findById(id)
                .orElseThrow(() -> new ApiException("Invoice not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoicePojo getCheckByInvoiceId(String invoiceId) throws ApiException {
        InvoicePojo invoice = invoiceDao.findByInvoiceId(invoiceId);
        if (invoice == null) {
            throw new ApiException("Invoice not found with invoiceId: " + invoiceId);
        }
        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoicePojo getCheckByOrderId(String orderId) throws ApiException {
        InvoicePojo invoice = invoiceDao.findByOrderId(orderId);
        if (invoice == null) {
            throw new ApiException("Invoice not found for order: " + orderId);
        }
        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoicePojo> getByInvoiceDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return invoiceDao.findByInvoiceDateBetween(startDate, endDate);
    }
}
