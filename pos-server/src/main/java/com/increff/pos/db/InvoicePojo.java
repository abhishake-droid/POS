package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.ZonedDateTime;

@Data
@Document(collection = "invoices")
public class InvoicePojo extends AbstractPojo {

    @Indexed(unique = true)
    @Field("invoiceId")
    private String invoiceId;

    @Indexed
    @Field("orderId")
    private String orderId;

    @Field("pdfPath")
    private String pdfPath;

    @Field("invoiceDate")
    private ZonedDateTime invoiceDate;
}
