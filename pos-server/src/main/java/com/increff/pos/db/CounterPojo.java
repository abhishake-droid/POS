package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "counters")
public class CounterPojo {

    @Id
    private String id;
    private long sequence;
}
