package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreationResult {
    private String orderId;
    private boolean fulfillable;
    private List<UnfulfillableItemData> unfulfillableItems;
}
