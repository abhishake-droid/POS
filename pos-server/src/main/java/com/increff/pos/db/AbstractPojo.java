package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class AbstractPojo {

    @Id
    private String id;

    @CreatedDate
    @Indexed
    private ZonedDateTime createdAt;

    @LastModifiedDate
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}