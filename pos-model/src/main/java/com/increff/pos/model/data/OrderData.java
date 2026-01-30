package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderData {
    private String id;
    private String orderId;

    private String status;
    private Integer totalItems;
    private Double totalAmount;
    private String createdAt;
    private Boolean hasInvoice;
    private List<OrderItemData> items;

    // Unfulfillable order tracking
    private Boolean fulfillable; // true if all items were fulfilled
    private List<UnfulfillableItemData> unfulfillableItems; // items that couldn't be fulfilled
}
