package com.increff.pos.util;

public class OrderCalculator {
    private int totalItems = 0;
    private double totalAmount = 0.0;

    public void addItem(int quantity, double lineTotal) {
        this.totalItems += quantity;
        this.totalAmount += lineTotal;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void reset() {
        this.totalItems = 0;
        this.totalAmount = 0.0;
    }
}
