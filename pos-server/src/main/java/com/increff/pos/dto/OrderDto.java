package com.increff.pos.dto;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderSearchForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        validateOrderForm(form);

        // Convert form lines to OrderItemPojos
        List<OrderItemPojo> orderItems = convertToOrderItems(form);

        // Create order through flow
        OrderPojo order = orderFlow.createOrder(orderItems);

        // Get order items
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(order.getOrderId());

        // Convert to DTO
        OrderData orderData = OrderHelper.convertToDto(order, false);
        orderData.setItems(OrderHelper.convertItemsToDtoList(savedItems));
        return orderData;
    }

    public OrderData getById(String orderId) throws ApiException {
        OrderPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemPojo> items = orderFlow.getOrderItems(orderId);

        // Check if invoice exists
        boolean hasInvoice = hasInvoiceForOrder(orderId);

        OrderData orderData = OrderHelper.convertToDto(order, hasInvoice);
        orderData.setItems(OrderHelper.convertItemsToDtoList(items));
        return orderData;
    }

    public Page<OrderData> getAll(OrderSearchForm form) throws ApiException {
        com.increff.pos.model.form.PageForm pageForm = new com.increff.pos.model.form.PageForm();
        pageForm.setPage(form.getPage());
        pageForm.setSize(form.getSize());
        ValidationUtil.validatePageForm(pageForm);

        // Parse dates
        Instant fromDate = null;
        Instant toDate = null;
        try {
            if (form.getFromDate() != null && !form.getFromDate().trim().isEmpty()) {
                try {
                    // Try parsing as ISO-8601 first
                    fromDate = Instant.parse(form.getFromDate());
                } catch (DateTimeParseException e) {
                    // Fall back to parsing as LocalDate (yyyy-MM-dd format from HTML date input)
                    fromDate = java.time.LocalDate.parse(form.getFromDate())
                            .atStartOfDay(java.time.ZoneOffset.UTC)
                            .toInstant();
                }
            }
            if (form.getToDate() != null && !form.getToDate().trim().isEmpty()) {
                try {
                    // Try parsing as ISO-8601 first
                    toDate = Instant.parse(form.getToDate());
                } catch (DateTimeParseException e) {
                    // Fall back to parsing as LocalDate and set to end of day
                    toDate = java.time.LocalDate.parse(form.getToDate())
                            .atTime(23, 59, 59)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toInstant();
                }
            }
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use yyyy-MM-dd format (e.g., 2024-01-01)");
        }

        // Get filtered orders
        List<OrderPojo> orders = orderFlow.getOrderWithFilters(
                form.getOrderId(),
                form.getStatus() != null && !form.getStatus().isEmpty() ? form.getStatus() : null,
                fromDate,
                toDate);

        // Convert to DTOs
        List<OrderData> orderDataList = orders.stream()
                .map(order -> {
                    boolean hasInvoice = "INVOICED".equals(order.getStatus());
                    return OrderHelper.convertToDto(order, hasInvoice);
                })
                .collect(Collectors.toList());

        // Apply pagination
        int page = form.getPage() != null ? form.getPage() : 0;
        int size = form.getSize() != null ? form.getSize() : 10;
        int start = page * size;
        int end = Math.min(start + size, orderDataList.size());
        List<OrderData> pageContent = orderDataList.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(page, size), orderDataList.size());
    }

    public OrderData cancel(String orderId) throws ApiException {
        OrderPojo cancelled = orderFlow.cancelOrder(orderId);

        // Check if invoice exists based on order status
        boolean hasInvoice = "INVOICED".equals(cancelled.getStatus());

        List<OrderItemPojo> items = orderFlow.getOrderItems(orderId);
        OrderData orderData = OrderHelper.convertToDto(cancelled, hasInvoice);
        orderData.setItems(OrderHelper.convertItemsToDtoList(items));
        return orderData;
    }

    private void validateOrderForm(OrderForm form) throws ApiException {

        if (form.getLines() == null || form.getLines().isEmpty()) {
            throw new ApiException("Order must have at least one line item");
        }

        for (int i = 0; i < form.getLines().size(); i++) {
            var line = form.getLines().get(i);
            if (line.getProductId() == null || line.getProductId().trim().isEmpty()) {
                throw new ApiException("Product ID is required for line item " + (i + 1));
            }
            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new ApiException("Quantity must be positive for line item " + (i + 1));
            }
            if (line.getMrp() == null || line.getMrp() < 0) {
                throw new ApiException("MRP cannot be negative for line item " + (i + 1));
            }
        }
    }

    public OrderData update(String orderId, OrderForm form) throws ApiException {
        validateOrderForm(form);

        // Convert form lines to OrderItemPojos
        List<OrderItemPojo> orderItems = convertToOrderItems(form);

        // Update order through flow
        OrderPojo order = orderFlow.updateOrder(orderId, orderItems);

        // Get updated order items
        List<OrderItemPojo> savedItems = orderFlow.getOrderItems(order.getOrderId());

        // Check if invoice exists
        boolean hasInvoice = hasInvoiceForOrder(orderId);

        // Convert to DTO
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

    private boolean hasInvoiceForOrder(String orderId) {
        // Note: This method is not actively used, invoice status is checked directly
        // via order.getStatus()
        return false;
    }

}
