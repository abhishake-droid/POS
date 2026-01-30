package com.increff.pos.dto;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderSearchForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDto {

    private final OrderFlow orderFlow;

    public OrderDto(OrderFlow orderFlow) {
        this.orderFlow = orderFlow;
    }

    public OrderData create(OrderForm form) throws ApiException {
        ValidationUtil.validateOrderForm(form);
        List<OrderItemPojo> orderItems = convertToOrderItems(form);
        com.increff.pos.model.data.OrderCreationResult creationResult = orderFlow.createOrder(orderItems);
        String orderId = creationResult.getOrderId();
        OrderPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(orderId);
        OrderData orderData = OrderHelper.convertToDto(order, false);
        orderData.setItems(OrderHelper.convertItemsToDtoList(savedItems));
        orderData.setFulfillable(creationResult.isFulfillable());
        orderData.setUnfulfillableItems(creationResult.getUnfulfillableItems());
        return orderData;
    }

    public OrderData getById(String orderId) throws ApiException {
        OrderPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemPojo> items = orderFlow.getOrderItems(orderId);
        boolean hasInvoice = "INVOICED".equals(order.getStatus());
        OrderData orderData = OrderHelper.convertToDto(order, hasInvoice);
        orderData.setItems(OrderHelper.convertItemsToDtoList(items));
        return orderData;
    }

    public Page<OrderData> getAll(OrderSearchForm form) throws ApiException {
        com.increff.pos.model.form.PageForm pageForm = new com.increff.pos.model.form.PageForm();
        pageForm.setPage(form.getPage());
        pageForm.setSize(form.getSize());
        ValidationUtil.validatePageForm(pageForm);

        ZonedDateTime fromDate = null;
        ZonedDateTime toDate = null;
        try {
            if (form.getFromDate() != null && !form.getFromDate().trim().isEmpty()) {
                try {
                    fromDate = ZonedDateTime.parse(form.getFromDate());
                } catch (DateTimeParseException e) {
                    fromDate = java.time.LocalDate.parse(form.getFromDate()).atStartOfDay(java.time.ZoneOffset.UTC);
                }
            }
            if (form.getToDate() != null && !form.getToDate().trim().isEmpty()) {
                try {
                    toDate = ZonedDateTime.parse(form.getToDate());
                } catch (DateTimeParseException e) {
                    toDate = java.time.LocalDate.parse(form.getToDate()).atTime(23, 59, 59)
                            .atZone(java.time.ZoneOffset.UTC);
                }
            }
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use yyyy-MM-dd format (e.g., 2024-01-01)");
        }

        List<OrderPojo> orders = orderFlow.getOrderWithFilters(
                form.getOrderId(),
                form.getStatus() != null && !form.getStatus().isEmpty() ? form.getStatus() : null,
                fromDate,
                toDate);

        List<OrderData> orderDataList = orders.stream()
                .map(order -> {
                    boolean hasInvoice = "INVOICED".equals(order.getStatus());
                    return OrderHelper.convertToDto(order, hasInvoice);
                })
                .collect(Collectors.toList());

        int page = form.getPage() != null ? form.getPage() : 0;
        int size = form.getSize() != null ? form.getSize() : 10;
        int start = page * size;
        int end = Math.min(start + size, orderDataList.size());
        List<OrderData> pageContent = orderDataList.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(page, size), orderDataList.size());
    }

    public OrderData cancel(String orderId) throws ApiException {
        OrderPojo cancelled = orderFlow.cancelOrder(orderId);
        boolean hasInvoice = "INVOICED".equals(cancelled.getStatus());
        List<OrderItemPojo> items = orderFlow.getOrderItems(orderId);
        OrderData orderData = OrderHelper.convertToDto(cancelled, hasInvoice);
        orderData.setItems(OrderHelper.convertItemsToDtoList(items));
        return orderData;
    }

    public OrderData update(String orderId, OrderForm form) throws ApiException {
        ValidationUtil.validateOrderForm(form);
        List<OrderItemPojo> orderItems = convertToOrderItems(form);
        OrderPojo order = orderFlow.updateOrder(orderId, orderItems);
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(order.getOrderId());
        boolean hasInvoice = "INVOICED".equals(order.getStatus());
        OrderData orderData = OrderHelper.convertToDto(order, hasInvoice);
        orderData.setItems(OrderHelper.convertItemsToDtoList(savedItems));
        return orderData;
    }

    private List<OrderItemPojo> convertToOrderItems(OrderForm form) {
        return form.getLines().stream()
                .map(line -> {
                    OrderItemPojo item = new OrderItemPojo();
                    item.setProductId(line.getProductId());
                    item.setQuantity(line.getQuantity());
                    item.setMrp(line.getMrp());
                    return item;
                })
                .collect(Collectors.toList());
    }

    public OrderData retry(String orderId, OrderForm form) throws ApiException {
        List<OrderItemPojo> orderItems = null;
        if (form != null && form.getLines() != null && !form.getLines().isEmpty()) {
            ValidationUtil.validateOrderForm(form);
            orderItems = convertToOrderItems(form);
        }

        com.increff.pos.model.data.OrderCreationResult creationResult = orderFlow.retryOrder(orderId, orderItems);
        String resultOrderId = creationResult.getOrderId();
        OrderPojo order = orderFlow.getOrderWithItems(resultOrderId);
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(resultOrderId);
        boolean hasInvoice = "INVOICED".equals(order.getStatus());
        OrderData orderData = OrderHelper.convertToDto(order, hasInvoice);
        orderData.setItems(OrderHelper.convertItemsToDtoList(savedItems));
        orderData.setFulfillable(creationResult.isFulfillable());
        orderData.setUnfulfillableItems(creationResult.getUnfulfillableItems());
        return orderData;
    }
}
