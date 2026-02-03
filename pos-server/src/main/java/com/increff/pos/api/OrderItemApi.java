package com.increff.pos.api;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.exception.ApiException;

import java.util.List;

public interface OrderItemApi {
    OrderItemPojo add(OrderItemPojo orderItemPojo) throws ApiException;

    OrderItemPojo getCheck(String id) throws ApiException;

    List<OrderItemPojo> getByOrderId(String orderId);

    List<OrderItemPojo> addBulk(List<OrderItemPojo> orderItemPojos) throws ApiException;

    void delete(String id) throws ApiException;

    void deleteByOrderId(String orderId);
}
