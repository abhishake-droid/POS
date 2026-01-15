package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "counters")
public class CounterPojo {

    @Id
    private String id;
    private long sequence;
}
