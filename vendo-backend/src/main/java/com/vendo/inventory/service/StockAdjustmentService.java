package com.vendo.inventory.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.inventory.dto.request.StockAdjustmentRequest;
import com.vendo.inventory.dto.response.StockAdjustmentResponse;
import com.vendo.inventory.entity.Inventory;
import com.vendo.inventory.entity.StockAdjustment;
import com.vendo.inventory.entity.StockMovement;
import com.vendo.inventory.repository.InventoryRepository;
import com.vendo.inventory.repository.StockAdjustmentRepository;
import com.vendo.inventory.repository.StockMovementRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockAdjustmentService {
    
    private static final int DEFAULT_APPROVAL_THRESHOLD = 100;
    
    private final StockAdjustmentRepository adjustmentRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    
    /**
     * Adjust stock
     */
    @Transactional
    public StockAdjustmentResponse adjustStock(StockAdjustmentRequest request, Long userId) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", request.getInventoryId()));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        int previousQuantity = inventory.getQuantity();
        int newQuantity = previousQuantity + request.getQuantity();
        
        if (newQuantity < 0) {
            throw new BadRequestException("Adjustment would result in negative quantity");
        }
        
        // Determine if approval is required
        int threshold = request.getAdjustmentThreshold() != null ? 
                request.getAdjustmentThreshold() : DEFAULT_APPROVAL_THRESHOLD;
        boolean requiresApproval = Math.abs(request.getQuantity()) > threshold;
        
        // If approval not required, apply immediately
        String status = requiresApproval ? "PENDING" : "APPROVED";
        
        StockAdjustment adjustment = StockAdjustment.builder()
                .inventory(inventory)
                .quantity(request.getQuantity())
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reason(request.getReason())
                .status(status)
                .createdBy(user)
                .requiresApproval(requiresApproval)
                .adjustmentThreshold(threshold)
                .build();
        
        if (!requiresApproval) {
            // Apply adjustment immediately
            inventory.setQuantity(newQuantity);
            inventoryRepository.save(inventory);
            
            // Create stock movement
            createStockMovement(inventory, request.getQuantity(), request.getReason(), userId);
            
            adjustment.setApprovedBy(user);
            adjustment.setApprovedAt(LocalDateTime.now());
        }
        
        StockAdjustment savedAdjustment = adjustmentRepository.save(adjustment);
        log.info("Stock adjustment created: ID={}, Quantity={}, RequiresApproval={}", 
                savedAdjustment.getId(), request.getQuantity(), requiresApproval);
        
        return mapToResponse(savedAdjustment);
    }
    
    /**
     * Approve adjustment
     */
    @Transactional
    public StockAdjustmentResponse approveAdjustment(Long adjustmentId, Long approverId, String notes) {
        StockAdjustment adjustment = adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new ResourceNotFoundException("StockAdjustment", "id", adjustmentId));
        
        if (!"PENDING".equals(adjustment.getStatus())) {
            throw new BadRequestException("Only pending adjustments can be approved");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverId));
        
        // Apply adjustment
        Inventory inventory = adjustment.getInventory();
        inventory.setQuantity(adjustment.getNewQuantity());
        inventoryRepository.save(inventory);
        
        // Create stock movement
        createStockMovement(inventory, adjustment.getQuantity(), adjustment.getReason(), approverId);
        
        // Update adjustment
        adjustment.setStatus("APPROVED");
        adjustment.setApprovedBy(approver);
        adjustment.setApprovalNotes(notes);
        adjustment.setApprovedAt(LocalDateTime.now());
        
        StockAdjustment savedAdjustment = adjustmentRepository.save(adjustment);
        log.info("Stock adjustment approved: ID={}", savedAdjustment.getId());
        
        return mapToResponse(savedAdjustment);
    }
    
    /**
     * Reject adjustment
     */
    @Transactional
    public StockAdjustmentResponse rejectAdjustment(Long adjustmentId, Long approverId, String reason) {
        StockAdjustment adjustment = adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new ResourceNotFoundException("StockAdjustment", "id", adjustmentId));
        
        if (!"PENDING".equals(adjustment.getStatus())) {
            throw new BadRequestException("Only pending adjustments can be rejected");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverId));
        
        adjustment.setStatus("REJECTED");
        adjustment.setApprovedBy(approver);
        adjustment.setApprovalNotes(reason);
        adjustment.setApprovedAt(LocalDateTime.now());
        
        StockAdjustment savedAdjustment = adjustmentRepository.save(adjustment);
        log.info("Stock adjustment rejected: ID={}, Reason={}", savedAdjustment.getId(), reason);
        
        return mapToResponse(savedAdjustment);
    }
    
    /**
     * Get adjustment history
     */
    @Transactional(readOnly = true)
    public List<StockAdjustmentResponse> getAdjustmentHistory(Long inventoryId) {
        List<StockAdjustment> adjustments = adjustmentRepository.findAdjustmentHistoryByInventoryId(inventoryId);
        return adjustments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<StockAdjustmentResponse> getAdjustmentsByStatus(String status, Pageable pageable) {
        Page<StockAdjustment> adjustments = adjustmentRepository.findByStatus(status, pageable);
        return adjustments.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<StockAdjustmentResponse> getPendingApprovals() {
        List<StockAdjustment> adjustments = adjustmentRepository.findPendingApprovals();
        return adjustments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private void createStockMovement(Inventory inventory, Integer quantity, String reason, Long userId) {
        String movementType = quantity > 0 ? "IN" : "OUT";
        
        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(movementType)
                .quantity(Math.abs(quantity))
                .referenceType("ADJUSTMENT")
                .notes(reason)
                .build();
        
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            movement.setCreatedBy(user);
        }
        
        stockMovementRepository.save(movement);
    }
    
    private StockAdjustmentResponse mapToResponse(StockAdjustment adjustment) {
        return StockAdjustmentResponse.builder()
                .id(adjustment.getId())
                .inventoryId(adjustment.getInventory().getId())
                .productId(adjustment.getInventory().getProduct().getId())
                .productName(adjustment.getInventory().getProduct().getName())
                .quantity(adjustment.getQuantity())
                .previousQuantity(adjustment.getPreviousQuantity())
                .newQuantity(adjustment.getNewQuantity())
                .reason(adjustment.getReason())
                .status(adjustment.getStatus())
                .createdById(adjustment.getCreatedBy().getId())
                .createdByName(adjustment.getCreatedBy().getFirstName() + " " + adjustment.getCreatedBy().getLastName())
                .approvedById(adjustment.getApprovedBy() != null ? adjustment.getApprovedBy().getId() : null)
                .approvedByName(adjustment.getApprovedBy() != null ? 
                        adjustment.getApprovedBy().getFirstName() + " " + adjustment.getApprovedBy().getLastName() : null)
                .approvalNotes(adjustment.getApprovalNotes())
                .requiresApproval(adjustment.getRequiresApproval())
                .createdAt(adjustment.getCreatedAt())
                .approvedAt(adjustment.getApprovedAt())
                .build();
    }
}

