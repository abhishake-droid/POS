package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "order_items")
public class OrderItemPojo extends AbstractPojo {

    @Field("orderId")
    private String orderId;
    @Field("productId")
    private String productId;
    @Field("barcode")
    private String barcode;
    @Field("productName")
    private String productName;
    @Field("quantity")
    private Integer quantity;
    @Field("mrp")
    private Double mrp;
    @Field("lineTotal")
    private Double lineTotal;
}
