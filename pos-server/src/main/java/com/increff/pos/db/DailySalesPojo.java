package com.increff.pos.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "daily_sales")
@CompoundIndex(name = "date_client_idx", def = "{'date': 1, 'clientId': 1}", unique = true)
public class DailySalesPojo extends AbstractPojo {

    @Field("date")
    private LocalDate date;

    @Field("clientId")
    private String clientId;

    @Field("clientName")
    private String clientName;

    @Field("totalOrders")
    private Integer invoicedOrdersCount = 0;

    @Field("totalItems")
    private Integer invoicedItemsCount = 0;

    @Field("totalRevenue")
    private Double totalRevenue = 0.0;
}
