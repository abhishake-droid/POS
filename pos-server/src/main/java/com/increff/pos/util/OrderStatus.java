package com.increff.pos.util;

public enum OrderStatus {
    PLACED("PLACED"),
    UNFULFILLABLE("UNFULFILLABLE"),
    INVOICED("INVOICED"),
    CANCELLED("CANCELLED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
