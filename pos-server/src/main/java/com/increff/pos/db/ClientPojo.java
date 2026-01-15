package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;


@Data
@Document(collection = "clients")
public class ClientPojo extends AbstractPojo {

    @Indexed(unique = true)
    @Field("id")
    private String clientId;

    @Field("name")
    private String name;

    @Indexed(unique = true)
    @Field("phone")
    private String phone;

    @Indexed(unique = true)
    @Field("email")
    private String email;

}
