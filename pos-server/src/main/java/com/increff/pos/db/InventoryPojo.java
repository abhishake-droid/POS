package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
@Document(collection = "inventory")
public class InventoryPojo extends AbstractPojo {

    @Indexed(unique = true)
    @Field("productId")
    private String productId;

    @Field("quantity")
    private Integer quantity;
}
