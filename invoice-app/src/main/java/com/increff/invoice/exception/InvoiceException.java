package com.increff.invoice.exception;

public class InvoiceException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvoiceException(String message) {
        super(message);
    }

    public InvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
