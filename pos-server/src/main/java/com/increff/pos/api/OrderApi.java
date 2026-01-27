package com.increff.pos.api;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderApi {
    OrderPojo add(OrderPojo orderPojo) throws ApiException;
    OrderPojo get(String id) throws ApiException;
    OrderPojo getByOrderId(String orderId) throws ApiException;
    Page<OrderPojo> getAll(int page, int size);
    OrderPojo update(String id, OrderPojo orderPojo) throws ApiException;
    List<OrderPojo> getWithFilters(String orderId, String status, java.time.Instant fromDate, java.time.Instant toDate);
}
