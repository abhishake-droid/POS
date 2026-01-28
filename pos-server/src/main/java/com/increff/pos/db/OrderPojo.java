package com.increff.pos.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "orders")
public class OrderPojo extends AbstractPojo {

    @Indexed(unique = true)
    @Field("orderId")
    private String orderId;

    @Field("status")
    private String status; // PLACED, INVOICED, CANCELLED

    @Field("totalItems")
    private Integer totalItems;

    @Field("totalAmount")
    private Double totalAmount;

    @Field("orderDate")
    private Instant orderDate;
}
