package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.ApprovePurchaseOrderRequest;
import com.vendo.b2b.dto.request.CreatePurchaseOrderRequest;
import com.vendo.b2b.dto.response.PurchaseOrderResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.notification.dto.request.SendNotificationRequest;
import com.vendo.notification.enums.NotificationType;
import com.vendo.notification.service.NotificationService;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.PurchaseOrder;
import com.vendo.order.repository.OrderRepository;
import com.vendo.order.repository.PurchaseOrderRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderRepository orderRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    /**
     * Create a new purchase order
     */
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(Long userId, CreatePurchaseOrderRequest request) {
        log.info("Creating purchase order for user: {}, order: {}", userId, request.getOrderId());
        
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));
        
        // Validate business account exists and is approved
        BusinessAccount businessAccount = businessAccountRepository.findById(request.getBusinessAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", request.getBusinessAccountId()));
        
        if (!"APPROVED".equals(businessAccount.getStatus())) {
            throw new BadRequestException("Business account must be APPROVED to create purchase orders");
        }
        
        // Validate order belongs to business account
        if (order.getBusinessAccount() == null || !order.getBusinessAccount().getId().equals(request.getBusinessAccountId())) {
            throw new BadRequestException("Order does not belong to the specified business account");
        }
        
        // Check if purchase order already exists for this order
        if (purchaseOrderRepository.findByOrderIdAndStatus(request.getOrderId(), "PENDING").isPresent()) {
            throw new BadRequestException("A pending purchase order already exists for this order");
        }
        
        // Get requesting user
        User requestedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Generate unique PO number
        String poNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Create purchase order
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .poNumber(poNumber)
                .businessAccount(businessAccount)
                .order(order)
                .status("PENDING")
                .requestedBy(requestedBy)
                .approvalNotes(request.getNotes())
                .build();
        
        PurchaseOrder savedPO = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order created with ID: {} and PO number: {}", savedPO.getId(), savedPO.getPoNumber());
        
        // Send notification to admins about new PO (in a real system, would notify specific admins)
        sendPurchaseOrderNotification(savedPO, "Purchase Order Created", 
                String.format("New purchase order %s has been created for order %s", 
                        savedPO.getPoNumber(), order.getOrderNumber()));
        
        return mapToResponse(savedPO);
    }
    
    /**
     * Approve a purchase order
     */
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long poId, Long approverUserId, ApprovePurchaseOrderRequest request) {
        log.info("Approving purchase order: {} by user: {}", poId, approverUserId);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", poId));
        
        // Validate current status
        if (!"PENDING".equals(purchaseOrder.getStatus())) {
            throw new BadRequestException("Only PENDING purchase orders can be approved");
        }
        
        // Validate approver user exists
        User approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverUserId));
        
        // Update purchase order
        purchaseOrder.setStatus("APPROVED");
        purchaseOrder.setApprovedBy(approver);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        if (request.getApprovalNotes() != null) {
            purchaseOrder.setApprovalNotes(request.getApprovalNotes());
        }
        
        PurchaseOrder savedPO = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order {} approved", savedPO.getPoNumber());
        
        // Send notification to requester
        if (purchaseOrder.getRequestedBy() != null) {
            sendPurchaseOrderNotification(savedPO, "Purchase Order Approved", 
                    String.format("Your purchase order %s has been approved", savedPO.getPoNumber()));
        }
        
        return mapToResponse(savedPO);
    }
    
    /**
     * Reject a purchase order
     */
    @Transactional
    public PurchaseOrderResponse rejectPurchaseOrder(Long poId, Long approverUserId, ApprovePurchaseOrderRequest request) {
        log.info("Rejecting purchase order: {} by user: {}", poId, approverUserId);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", poId));
        
        // Validate current status
        if (!"PENDING".equals(purchaseOrder.getStatus())) {
            throw new BadRequestException("Only PENDING purchase orders can be rejected");
        }
        
        // Validate approver user exists
        User approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverUserId));
        
        // Update purchase order
        purchaseOrder.setStatus("REJECTED");
        purchaseOrder.setApprovedBy(approver);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        if (request.getApprovalNotes() != null) {
            purchaseOrder.setApprovalNotes(request.getApprovalNotes());
        }
        
        PurchaseOrder savedPO = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order {} rejected", savedPO.getPoNumber());
        
        // Send notification to requester
        if (purchaseOrder.getRequestedBy() != null) {
            sendPurchaseOrderNotification(savedPO, "Purchase Order Rejected", 
                    String.format("Your purchase order %s has been rejected", savedPO.getPoNumber()));
        }
        
        return mapToResponse(savedPO);
    }
    
    /**
     * Get purchase orders with filtering
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> getPurchaseOrders(Long businessAccountId, String status, Pageable pageable) {
        log.debug("Getting purchase orders - businessAccountId: {}, status: {}", businessAccountId, status);
        
        Page<PurchaseOrder> purchaseOrders;
        
        if (businessAccountId != null && status != null) {
            List<PurchaseOrder> poList = purchaseOrderRepository.findByBusinessAccountIdAndStatus(businessAccountId, status);
            // Convert to page (simplified - in production would use proper pagination)
            purchaseOrders = purchaseOrderRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (businessAccountId != null) {
            purchaseOrders = purchaseOrderRepository.findByBusinessAccountId(businessAccountId, pageable);
        } else if (status != null) {
            purchaseOrders = purchaseOrderRepository.findByStatus(status, pageable);
        } else {
            purchaseOrders = purchaseOrderRepository.findAll(pageable);
        }
        
        return purchaseOrders.map(this::mapToResponse);
    }
    
    /**
     * Get purchase order by ID
     */
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
        return mapToResponse(purchaseOrder);
    }
    
    /**
     * Send notification for purchase order status changes
     */
    private void sendPurchaseOrderNotification(PurchaseOrder po, String title, String message) {
        try {
            Long userId = po.getRequestedBy() != null ? po.getRequestedBy().getId() : null;
            if (userId == null) {
                return;
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("purchaseOrderId", po.getId());
            data.put("poNumber", po.getPoNumber());
            data.put("status", po.getStatus());
            data.put("orderId", po.getOrder() != null ? po.getOrder().getId() : null);
            
            SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                    .userId(userId)
                    .notificationType(NotificationType.ORDER_CONFIRMED) // Using existing type
                    .channel("in_app")
                    .title(title)
                    .message(message)
                    .data(data)
                    .build();
            
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send purchase order notification", e);
        }
    }
    
    /**
     * Map PurchaseOrder entity to PurchaseOrderResponse
     */
    private PurchaseOrderResponse mapToResponse(PurchaseOrder po) {
        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .businessAccountId(po.getBusinessAccount() != null ? po.getBusinessAccount().getId() : null)
                .businessAccountName(po.getBusinessAccount() != null ? po.getBusinessAccount().getCompanyName() : null)
                .orderId(po.getOrder() != null ? po.getOrder().getId() : null)
                .orderNumber(po.getOrder() != null ? po.getOrder().getOrderNumber() : null)
                .status(po.getStatus())
                .requestedBy(po.getRequestedBy() != null ? po.getRequestedBy().getId() : null)
                .requestedByName(po.getRequestedBy() != null ? 
                        (po.getRequestedBy().getFirstName() + " " + po.getRequestedBy().getLastName()).trim() : null)
                .approvedBy(po.getApprovedBy() != null ? po.getApprovedBy().getId() : null)
                .approvedByName(po.getApprovedBy() != null ? 
                        (po.getApprovedBy().getFirstName() + " " + po.getApprovedBy().getLastName()).trim() : null)
                .approvalNotes(po.getApprovalNotes())
                .requestedAt(po.getRequestedAt())
                .approvedAt(po.getApprovedAt())
                .createdAt(po.getCreatedAt())
                .build();
    }
}

