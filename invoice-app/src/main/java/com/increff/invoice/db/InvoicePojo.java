package com.increff.invoice.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "invoices")
public class InvoicePojo extends AbstractPojo {

    @Indexed(unique = true)
    @Field("invoiceId")
    private String invoiceId;

    @Indexed
    @Field("orderId")
    private String orderId;

    @Field("billingAddress")
    private String billingAddress;

    @Field("totalAmount")
    private Double totalAmount;

    @Field("pdfPath")
    private String pdfPath;

    @Field("invoiceDate")
    private Instant invoiceDate;
}
