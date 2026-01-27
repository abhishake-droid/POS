package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchForm {
    private Integer page;
    private Integer size;
    private String orderId;
    private String status;
    private String fromDate;
    private String toDate;
}
