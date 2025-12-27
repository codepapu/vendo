package com.vendo.orderreturn.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.orderreturn.dto.request.CreateReturnRequest;
import com.vendo.orderreturn.dto.response.ReturnResponse;
import com.vendo.orderreturn.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class ReturnController {
    
    private final ReturnService returnService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ReturnResponse>> createReturnRequest(
            @Valid @RequestBody CreateReturnRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ReturnResponse response = returnService.createReturnRequest(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnResponse>> getReturnById(@PathVariable Long id) {
        ReturnResponse response = returnService.getReturnById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ReturnResponse>> getReturnByCode(@PathVariable String code) {
        ReturnResponse response = returnService.getReturnByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> getReturnsByOrderId(@PathVariable Long orderId) {
        List<ReturnResponse> responses = returnService.getReturnsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> getReturnHistory(
            Authentication authentication,
            Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        List<ReturnResponse> responses = returnService.getReturnHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReturnResponse>> approveReturn(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        Long adminId = Long.parseLong(authentication.getName());
        ReturnResponse response = returnService.approveReturn(id, adminId, notes);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReturnResponse>> rejectReturn(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        Long adminId = Long.parseLong(authentication.getName());
        ReturnResponse response = returnService.rejectReturn(id, adminId, reason);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<ReturnResponse>> processRefund(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = Long.parseLong(authentication.getName());
        ReturnResponse response = returnService.processRefund(id, adminId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> getReturnsByStatus(@PathVariable String status) {
        List<ReturnResponse> responses = returnService.getReturnsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

