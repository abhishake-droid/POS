package com.increff.pos.controller;

import com.increff.pos.dto.OrderDto;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderSearchForm;
import com.increff.pos.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@Tag(name = "Order Management", description = "APIs for managing orders")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @Operation(summary = "Create a new order")
    @PostMapping("/create")
    public OrderData create(@RequestBody OrderForm form) throws ApiException {
        return orderDto.create(form);
    }

    @Operation(summary = "Get all orders with pagination and filters")
    @PostMapping("/get-all-paginated")
    public Page<OrderData> getAll(@RequestBody OrderSearchForm form) throws ApiException {
        return orderDto.getAll(form);
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/get-by-id/{orderId}")
    public OrderData getById(@PathVariable String orderId) throws ApiException {
        return orderDto.getById(orderId);
    }

    @Operation(summary = "Cancel an order (only if not invoiced)")
    @PostMapping("/cancel/{orderId}")
    public OrderData cancel(@PathVariable String orderId) throws ApiException {
        return orderDto.cancel(orderId);
    }

    @Operation(summary = "Update an order (only if PLACED)")
    @PutMapping("/update/{orderId}")
    public OrderData update(@PathVariable String orderId, @RequestBody OrderForm form) throws ApiException {
        return orderDto.update(orderId, form);
    }

    @Operation(summary = "Retry an UNFULFILLABLE order (with optional item updates)")
    @PostMapping("/retry/{orderId}")
    public OrderData retry(@PathVariable String orderId, @RequestBody(required = false) OrderForm form)
            throws ApiException {
        return orderDto.retry(orderId, form);
    }
}
