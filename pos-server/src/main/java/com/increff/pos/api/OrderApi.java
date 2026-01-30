package com.increff.pos.api;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderApi {
    OrderPojo add(OrderPojo orderPojo) throws ApiException;

    OrderPojo getCheck(String id) throws ApiException;

    OrderPojo getCheckByOrderId(String orderId) throws ApiException;

    Page<OrderPojo> getAll(PageForm form);

    OrderPojo update(String id, OrderPojo orderPojo) throws ApiException;

    List<OrderPojo> getWithFilters(String orderId, String status, java.time.ZonedDateTime fromDate,
            java.time.ZonedDateTime toDate);
}
