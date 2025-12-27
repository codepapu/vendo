package com.vendo.b2b.service;

import com.vendo.b2b.dto.response.InvoiceResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.enums.NotificationType;
import com.vendo.notification.service.NotificationService;
import com.vendo.order.entity.Invoice;
import com.vendo.order.entity.InvoiceItem;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.repository.InvoiceRepository;
import com.vendo.order.repository.OrderRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final NotificationService notificationService;
    private final TemplateEngine templateEngine;
    
    @Value("${app.name:Vendo}")
    private String appName;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger invoiceCounter = new AtomicInteger(1);
    
    /**
     * Generate invoice for an order
     */
    @Transactional
    public InvoiceResponse generateInvoice(Long orderId) {
        log.info("Generating invoice for order: {}", orderId);
        
        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Validate order has business account (B2B order)
        if (order.getBusinessAccount() == null) {
            throw new BadRequestException("Invoice can only be generated for B2B orders");
        }
        
        // Check if invoice already exists for this order
        List<Invoice> existingInvoices = invoiceRepository.findByOrderId(orderId);
        if (!existingInvoices.isEmpty()) {
            log.warn("Invoice already exists for order: {}", orderId);
            // Return existing invoice instead of creating duplicate
            return mapToResponse(existingInvoices.get(0));
        }
        
        BusinessAccount businessAccount = order.getBusinessAccount();
        
        // Generate unique invoice number: INV-YYYYMMDD-XXXX
        String invoiceNumber = generateInvoiceNumber();
        
        // Calculate dates
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(businessAccount.getCreditTermsDays() != null ? 
                businessAccount.getCreditTermsDays() : 30);
        
        // Create invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .order(order)
                .businessAccount(businessAccount)
                .issueDate(issueDate)
                .dueDate(dueDate)
                .status("PENDING")
                .subtotal(order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO)
                .taxAmount(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO)
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .build();
        
        // Create invoice items from order items
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem orderItem : order.getOrderItems()) {
                InvoiceItem invoiceItem = InvoiceItem.builder()
                        .invoice(invoice)
                        .productName(orderItem.getProductName())
                        .productImage(orderItem.getProduct() != null && orderItem.getProduct().getImages() != null && 
                                !orderItem.getProduct().getImages().isEmpty() ? 
                                orderItem.getProduct().getImages() : null)
                        .qty(orderItem.getQuantity())
                        .price(orderItem.getUnitPrice())
                        .taxAmount(BigDecimal.ZERO) // Tax is calculated at invoice level
                        .build();
                invoiceItems.add(invoiceItem);
            }
        }
        invoice.setInvoiceItems(invoiceItems);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice generated with ID: {} and number: {}", savedInvoice.getId(), savedInvoice.getInvoiceNumber());
        
        // Send notification to business account owner
        if (businessAccount.getUser() != null) {
            sendInvoiceNotification(savedInvoice, "Invoice Generated", 
                    String.format("Invoice %s has been generated for order %s", 
                            savedInvoice.getInvoiceNumber(), order.getOrderNumber()));
        }
        
        return mapToResponse(savedInvoice);
    }
    
    /**
     * Get invoices with filtering
     */
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(Long businessAccountId, String status, Pageable pageable) {
        log.debug("Getting invoices - businessAccountId: {}, status: {}", businessAccountId, status);
        
        Page<Invoice> invoices;
        
        if (businessAccountId != null && status != null) {
            List<Invoice> invoiceList = invoiceRepository.findByBusinessAccountIdAndStatus(businessAccountId, status);
            // Convert to page (simplified - in production would use proper pagination)
            invoices = invoiceRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (businessAccountId != null) {
            invoices = invoiceRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (status != null) {
            invoices = invoiceRepository.findByStatus(status, pageable);
        } else {
            invoices = invoiceRepository.findAll(pageable);
        }
        
        return invoices.map(this::mapToResponse);
    }
    
    /**
     * Get invoice by ID
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return mapToResponse(invoice);
    }
    
    /**
     * Get invoice by invoice number
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", invoiceNumber));
        return mapToResponse(invoice);
    }
    
    /**
     * Mark invoice as paid
     */
    @Transactional
    public InvoiceResponse markInvoiceAsPaid(Long id, BigDecimal paidAmount) {
        log.info("Marking invoice {} as paid with amount: {}", id, paidAmount);
        
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        
        // Validate current status
        if ("PAID".equals(invoice.getStatus())) {
            throw new BadRequestException("Invoice is already marked as paid");
        }
        
        if ("CANCELLED".equals(invoice.getStatus())) {
            throw new BadRequestException("Cannot mark cancelled invoice as paid");
        }
        
        // Validate paid amount
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Paid amount must be greater than zero");
        }
        
        if (paidAmount.compareTo(invoice.getTotalAmount()) > 0) {
            throw new BadRequestException("Paid amount cannot exceed total amount");
        }
        
        // Update invoice
        invoice.setPaidAmount(paidAmount);
        invoice.setPaidAt(LocalDateTime.now());
        
        // Update status based on payment
        if (paidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else {
            // Partial payment - keep as PENDING
            invoice.setStatus("PENDING");
        }
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice {} marked as paid", savedInvoice.getInvoiceNumber());
        
        // Send notification
        if (savedInvoice.getBusinessAccount() != null && savedInvoice.getBusinessAccount().getUser() != null) {
            sendInvoiceNotification(savedInvoice, "Invoice Payment Received", 
                    String.format("Payment of %s received for invoice %s", 
                            paidAmount, savedInvoice.getInvoiceNumber()));
        }
        
        return mapToResponse(savedInvoice);
    }
    
    /**
     * Get overdue invoices
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getOverdueInvoices(Long businessAccountId) {
        log.debug("Getting overdue invoices for businessAccountId: {}", businessAccountId);
        
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices;
        
        if (businessAccountId != null) {
            overdueInvoices = invoiceRepository.findOverdueInvoicesByBusinessAccount(businessAccountId, today);
        } else {
            overdueInvoices = invoiceRepository.findOverdueInvoices(today);
        }
        
        return overdueInvoices.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate PDF invoice
     */
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long invoiceId) {
        log.info("Generating PDF for invoice: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        
        InvoiceResponse invoiceResponse = mapToResponse(invoice);
        
        // Prepare template context
        Context context = new Context();
        context.setVariable("invoice", invoiceResponse);
        context.setVariable("appName", appName);
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("year", LocalDate.now().getYear());
        
        // Generate HTML from Thymeleaf template
        String htmlContent = templateEngine.process("invoices/invoice-pdf", context);
        
        // Convert HTML to PDF
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            byte[] pdfBytes = outputStream.toByteArray();
            log.info("PDF generated successfully for invoice: {}, size: {} bytes", invoiceId, pdfBytes.length);
            return pdfBytes;
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice: {}", invoiceId, e);
            throw new RuntimeException("Failed to generate PDF invoice", e);
        }
    }
    
    /**
     * Generate unique invoice number: INV-YYYYMMDD-XXXX
     */
    private String generateInvoiceNumber() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int sequence = invoiceCounter.getAndIncrement();
        // Reset counter if it gets too large (use modulo to keep it 4 digits)
        if (sequence > 9999) {
            invoiceCounter.set(1);
            sequence = 1;
        }
        return String.format("INV-%s-%04d", datePart, sequence);
    }
    
    /**
     * Send notification for invoice events
     */
    private void sendInvoiceNotification(Invoice invoice, String title, String message) {
        try {
            if (invoice.getBusinessAccount() == null || invoice.getBusinessAccount().getUser() == null) {
                return;
            }
            
            Long userId = invoice.getBusinessAccount().getUser().getId();
            
            Map<String, Object> data = new HashMap<>();
            data.put("invoiceId", invoice.getId());
            data.put("invoiceNumber", invoice.getInvoiceNumber());
            data.put("status", invoice.getStatus());
            data.put("totalAmount", invoice.getTotalAmount());
            data.put("orderId", invoice.getOrder() != null ? invoice.getOrder().getId() : null);
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(userId)
                    .notificationType(NotificationType.INVOICE_GENERATED)
                    .channel("in_app")
                    .title(title)
                    .message(message)
                    .data(data)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send invoice notification", e);
        }
    }
    
    /**
     * Map Invoice entity to InvoiceResponse
     */
    private InvoiceResponse mapToResponse(Invoice invoice) {
        List<InvoiceResponse.InvoiceItemResponse> items = new ArrayList<>();
        if (invoice.getInvoiceItems() != null) {
            items = invoice.getInvoiceItems().stream()
                    .map(item -> InvoiceResponse.InvoiceItemResponse.builder()
                            .id(item.getId())
                            .productId(null) // InvoiceItem doesn't store productId directly
                            .productName(item.getProductName())
                            .sku(null) // InvoiceItem doesn't store SKU
                            .quantity(item.getQty())
                            .unitPrice(item.getPrice())
                            .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())))
                            .build())
                    .collect(Collectors.toList());
        }
        
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrder() != null ? invoice.getOrder().getId() : null)
                .orderNumber(invoice.getOrder() != null ? invoice.getOrder().getOrderNumber() : null)
                .businessAccountId(invoice.getBusinessAccount() != null ? invoice.getBusinessAccount().getId() : null)
                .businessAccountName(invoice.getBusinessAccount() != null ? 
                        invoice.getBusinessAccount().getCompanyName() : null)
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .items(items)
                .build();
    }
}

