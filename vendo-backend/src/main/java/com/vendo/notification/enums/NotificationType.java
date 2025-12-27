package com.vendo.notification.enums;

/**
 * Enum representing different types of notifications that users can configure preferences for.
 */
public enum NotificationType {
    // Order-related notifications
    ORDER_CONFIRMED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    ORDER_REFUNDED,
    
    // Payment notifications
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_PENDING,
    INVOICE_GENERATED,
    
    // Promotion and marketing
    PROMOTION,
    DISCOUNT_OFFER,
    NEW_PRODUCT,
    PRICE_DROP,
    
    // Account and security
    ACCOUNT_VERIFIED,
    PASSWORD_CHANGED,
    SECURITY_ALERT,
    LOGIN_ALERT,
    
    // Shipping and delivery
    SHIPMENT_TRACKING,
    DELIVERY_REMINDER,
    
    // General
    SYSTEM_UPDATE,
    MAINTENANCE_NOTICE,
    GENERAL
}

