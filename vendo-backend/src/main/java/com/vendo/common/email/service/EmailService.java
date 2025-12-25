package com.vendo.common.email.service;

import com.vendo.order.dto.response.OrderResponse;
import com.vendo.payment.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Email Service
 * High-level service for sending various email types using templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final EmailTemplateService emailTemplateService;
    
    @Value("${app.name:Vendo}")
    private String appName;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
    
    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmationEmail(String email, String customerName, OrderResponse order) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName != null ? customerName : "Customer");
            variables.put("order", order);
            variables.put("orderNumber", order.getCode());
            variables.put("orderDate", order.getCreatedAt().format(DATE_FORMATTER));
            variables.put("orderTotal", formatCurrency(order.getAmount(), "USD"));
            variables.put("orderItems", order.getItems());
            variables.put("shippingAddress", order.getShippingAddress());
            variables.put("billingAddress", order.getBillingAddress());
            variables.put("orderUrl", frontendUrl + "/orders/" + order.getId());
            variables.put("trackOrderUrl", frontendUrl + "/orders/" + order.getId());
            
            emailTemplateService.sendMultipartEmail(
                email,
                "Order Confirmation - " + order.getCode(),
                "order-confirmation",
                variables
            );
            log.info("Order confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", email, e);
            // Don't throw - order is still created
        }
    }
    
    /**
     * Send shipping notification email
     */
    public void sendShippingNotificationEmail(String email, String customerName, OrderResponse order, 
                                               String trackingNumber, String carrier, String trackingUrl) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName != null ? customerName : "Customer");
            variables.put("order", order);
            variables.put("orderNumber", order.getCode());
            variables.put("trackingNumber", trackingNumber);
            variables.put("carrier", carrier != null ? carrier : "Shipping Carrier");
            variables.put("trackingUrl", trackingUrl);
            variables.put("orderUrl", frontendUrl + "/orders/" + order.getId());
            variables.put("trackOrderUrl", frontendUrl + "/orders/" + order.getId());
            
            emailTemplateService.sendMultipartEmail(
                email,
                "Your Order Has Shipped - " + order.getCode(),
                "shipping-notification",
                variables
            );
            log.info("Shipping notification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send shipping notification email to: {}", email, e);
            // Don't throw - shipping is still processed
        }
    }
    
    /**
     * Send payment receipt email
     */
    public void sendPaymentReceiptEmail(String email, String customerName, PaymentResponse payment, OrderResponse order) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName != null ? customerName : "Customer");
            variables.put("payment", payment);
            variables.put("order", order);
            variables.put("orderNumber", order != null ? order.getCode() : "N/A");
            variables.put("paymentDate", payment.getCreatedAt().format(DATETIME_FORMATTER));
            variables.put("paymentAmount", formatCurrency(payment.getAmount(), 
                payment.getCurrency() != null ? payment.getCurrency() : "USD"));
            variables.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A");
            variables.put("transactionId", payment.getTransactionId() != null ? payment.getTransactionId() : payment.getChargeId());
            variables.put("orderUrl", order != null ? frontendUrl + "/orders/" + order.getId() : frontendUrl);
            variables.put("receiptUrl", frontendUrl + "/payment/success?chargeId=" + payment.getChargeId());
            
            emailTemplateService.sendMultipartEmail(
                email,
                "Payment Receipt - " + (payment.getTransactionId() != null ? payment.getTransactionId() : payment.getChargeId()),
                "payment-receipt",
                variables
            );
            log.info("Payment receipt email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send payment receipt email to: {}", email, e);
            // Don't throw - payment is still processed
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token + "&email=" + email;
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("resetUrl", resetUrl);
            variables.put("expirationHours", 1);
            
            emailTemplateService.sendMultipartEmail(
                email,
                "Password Reset Request",
                "password-reset",
                variables
            );
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send email verification email
     */
    public void sendVerificationEmail(String email, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("verificationUrl", verificationUrl);
            variables.put("expirationHours", 24);
            
            emailTemplateService.sendMultipartEmail(
                email,
                "Verify Your Email Address",
                "email-verification",
                variables
            );
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send order cancellation email
     */
    public void sendOrderCancellationEmail(String email, String customerName, OrderResponse order, String reason) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName != null ? customerName : "Customer");
            variables.put("order", order);
            variables.put("orderNumber", order.getCode());
            variables.put("orderDate", order.getCreatedAt().format(DATE_FORMATTER));
            variables.put("cancellationDate", java.time.LocalDateTime.now().format(DATE_FORMATTER));
            variables.put("cancellationReason", reason != null ? reason : "Customer Request");
            variables.put("refundAmount", formatCurrency(order.getAmount(), "USD"));
            variables.put("orderUrl", frontendUrl + "/orders/" + order.getId());
            variables.put("supportUrl", frontendUrl + "/support");
            
            emailTemplateService.sendMultipartEmail(
                email,
                "Order Cancelled - " + order.getCode(),
                "order-cancellation",
                variables
            );
            log.info("Order cancellation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send order cancellation email to: {}", email, e);
            // Don't throw - cancellation is still processed
        }
    }
    
    /**
     * Format currency amount
     */
    private String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null) {
            return "$0.00";
        }
        String currencySymbol = getCurrencySymbol(currency != null ? currency : "USD");
        return currencySymbol + amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
    
    /**
     * Get currency symbol
     */
    private String getCurrencySymbol(String currency) {
        return switch (currency != null ? currency.toUpperCase() : "USD") {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "INR" -> "₹";
            case "JPY" -> "¥";
            default -> "$";
        };
    }
}

