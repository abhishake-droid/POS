package com.increff.pos.dto;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderSearchForm;
import com.increff.pos.model.data.OrderCreationResult;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    public OrderData create(OrderForm form) throws ApiException {
        ValidationUtil.validate(form);
        List<OrderItemPojo> orderItems = convertToOrderItems(form);
        OrderCreationResult creationResult = orderFlow.createOrder(orderItems);

        String orderId = creationResult.getOrderId();
        OrderPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(orderId);

        OrderData orderData = OrderHelper.convertToData(order, false);
        orderData.setItems(OrderHelper.convertItemsToDtoList(savedItems));
        orderData.setFulfillable(creationResult.isFulfillable());
        orderData.setUnfulfillableItems(creationResult.getUnfulfillableItems());
        return orderData;
    }

    public OrderData getById(String orderId) throws ApiException {
        orderId = com.increff.pos.util.NormalizeUtil.normalizeOrderId(orderId);
        OrderPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemPojo> items = orderFlow.getOrderItems(orderId);
        boolean hasInvoice = "INVOICED".equals(order.getStatus());

        OrderData orderData = OrderHelper.convertToData(order, hasInvoice);
        orderData.setItems(OrderHelper.convertItemsToDtoList(items));
        return orderData;
    }

    public Page<OrderData> getAll(OrderSearchForm form) throws ApiException {
        PageForm pageForm = new PageForm();
        pageForm.setPage(form.getPage());
        pageForm.setSize(form.getSize());
        ValidationUtil.validate(pageForm);

        ZonedDateTime fromDate = null;
        ZonedDateTime toDate = null;
        try {
            if (form.getFromDate() != null && !form.getFromDate().trim().isEmpty()) {
                try {
                    fromDate = ZonedDateTime.parse(form.getFromDate());
                } catch (DateTimeParseException e) {
                    fromDate = LocalDate.parse(form.getFromDate()).atStartOfDay(java.time.ZoneOffset.UTC);
                }
            }
            if (form.getToDate() != null && !form.getToDate().trim().isEmpty()) {
                try {
                    toDate = ZonedDateTime.parse(form.getToDate());
                } catch (DateTimeParseException e) {
                    toDate = LocalDate.parse(form.getToDate()).atTime(23, 59, 59)
                            .atZone(java.time.ZoneOffset.UTC);
                }
            }
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use yyyy-MM-dd format (e.g., 2024-01-01)");
        }

        int page = form.getPage() != null ? form.getPage() : 0;
        int size = form.getSize() != null ? form.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size);

        Page<OrderPojo> orderPage = orderFlow.getOrderWithFilters(
                form.getOrderId(),
                form.getStatus() != null && !form.getStatus().isEmpty() ? form.getStatus() : null,
                fromDate,
                toDate,
                pageable);

        List<OrderData> orderDataList = orderPage.getContent().stream()
                .map(order -> {
                    boolean hasInvoice = "INVOICED".equals(order.getStatus());
                    return OrderHelper.convertToData(order, hasInvoice);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(orderDataList, pageable, orderPage.getTotalElements());
    }

    public OrderData cancel(String orderId) throws ApiException {
        OrderPojo cancelled = orderFlow.cancelOrder(orderId);
        List<OrderItemPojo> items = orderFlow.getOrderItems(cancelled.getOrderId());

        OrderData orderData = OrderHelper.convertToData(cancelled, false);
        orderData.setItems(OrderHelper.convertItemsToDtoList(items));
        return orderData;
    }

    public OrderData update(String orderId, OrderForm form) throws ApiException {
        ValidationUtil.validate(form);
        List<OrderItemPojo> orderItems = convertToOrderItems(form);
        OrderPojo order = orderFlow.updateOrder(orderId, orderItems);
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(order.getOrderId());

        OrderData orderData = OrderHelper.convertToData(order, false);
        orderData.setItems(OrderHelper.convertItemsToDtoList(savedItems));
        return orderData;
    }

    private List<OrderItemPojo> convertToOrderItems(OrderForm form) {
        return form.getLines().stream()
                .map(line -> OrderHelper.createOrderItem(
                        line.getProductId(), line.getQuantity(), line.getMrp()))
                .collect(Collectors.toList());
    }

    public OrderData retry(String orderId, OrderForm form) throws ApiException {
        List<OrderItemPojo> orderItems = null;
        if (form != null && form.getLines() != null && !form.getLines().isEmpty()) {
            ValidationUtil.validate(form);
            orderItems = convertToOrderItems(form);
        }

        OrderCreationResult creationResult = orderFlow.retryOrder(orderId, orderItems);
        String resultOrderId = creationResult.getOrderId();
        OrderPojo order = orderFlow.getOrderWithItems(resultOrderId);
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(resultOrderId);

        OrderData orderData = OrderHelper.convertToData(order, false);
        orderData.setItems(OrderHelper.convertItemsToDtoList(savedItems));
        orderData.setFulfillable(creationResult.isFulfillable());
        orderData.setUnfulfillableItems(creationResult.getUnfulfillableItems());
        return orderData;
    }
}
