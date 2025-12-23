package com.vendo.order.enums;

/**
 * Enumeration of standard cancellation reasons for orders.
 */
public enum CancellationReason {
    CUSTOMER_REQUESTED("Customer requested cancellation"),
    OUT_OF_STOCK("Item out of stock"),
    PRICE_MISMATCH("Price mismatch"),
    DUPLICATE_ORDER("Duplicate order"),
    WRONG_ADDRESS("Wrong shipping address"),
    PAYMENT_FAILED("Payment failed"),
    FRAUDULENT_ORDER("Fraudulent order"),
    CUSTOMER_CHANGED_MIND("Customer changed mind"),
    DELIVERY_DELAY("Delivery delay"),
    PRODUCT_DAMAGED("Product damaged"),
    OTHER("Other reason");

    private final String description;

    CancellationReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
