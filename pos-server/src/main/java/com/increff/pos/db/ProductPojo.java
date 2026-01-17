package com.increff.pos.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Database entity for Product
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "products")
public class ProductPojo extends AbstractPojo {

    @Indexed(unique = true)
    @Field("barcode")
    private String barcode;

    @Field("clientId")
    private String clientId;

    @Field("name")
    private String name;

    @Field("mrp")
    private Double mrp;

    @Field("imageUrl")
    private String imageUrl;
}
