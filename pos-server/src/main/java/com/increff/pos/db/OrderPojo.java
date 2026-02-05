package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.ZonedDateTime;

@Getter
@Setter
@Document(collection = "orders")
public class OrderPojo extends AbstractPojo {

    @Indexed(unique = true)
    @Field("orderId")
    private String orderId;

    @Field("status")
    private String status;

    @Field("totalItems")
    private Integer totalItems;

    @Field("totalAmount")
    private Double totalAmount;

    @Field("orderDate")
    private ZonedDateTime orderDate;
}
