package com.increff.invoice.exception;

public class InvoiceException extends Exception {
    public InvoiceException(String message) {
        super(message);
    }

    public InvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

