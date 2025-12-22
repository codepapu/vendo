package com.vendo.admin.controller;

import com.vendo.admin.dto.request.UpdateUserRequest;
import com.vendo.admin.dto.response.AdminDashboardResponse;
import com.vendo.admin.dto.response.UserManagementResponse;
import com.vendo.admin.service.AdminService;
import com.vendo.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final AdminService adminService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
    
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfiguration() {
        Map<String, Object> config = adminService.getSystemConfiguration();
        return ResponseEntity.ok(ApiResponse.success(config));
    }
    
    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Void>> updateSystemConfiguration(
            @RequestBody Map<String, Object> config) {
        adminService.updateSystemConfiguration(config);
        return ResponseEntity.ok(ApiResponse.success("System configuration updated"));
    }
    
    // User Management APIs
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserManagementResponse> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserManagementResponse>> getUserById(@PathVariable Long id) {
        UserManagementResponse user = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserManagementResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserManagementResponse user = adminService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
    
    @PutMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserManagementResponse>> updateUserRoles(
            @PathVariable Long id,
            @RequestBody List<String> roles) {
        UserManagementResponse user = adminService.updateUserRoles(id, roles);
        return ResponseEntity.ok(ApiResponse.success("User roles updated successfully", user));
    }
    
    // Content Moderation APIs
    @GetMapping("/content/pending")
    public ResponseEntity<ApiResponse<Page<Object>>> getPendingContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object> pendingContent = adminService.getPendingContent(pageable);
        return ResponseEntity.ok(ApiResponse.success(pendingContent));
    }
    
    @PutMapping("/content/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveContent(
            @PathVariable Long id,
            @RequestParam String contentType) {
        adminService.approveContent(id, contentType);
        return ResponseEntity.ok(ApiResponse.success("Content approved successfully"));
    }
    
    @PutMapping("/content/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectContent(
            @PathVariable Long id,
            @RequestParam String contentType,
            @RequestParam(required = false) String reason) {
        adminService.rejectContent(id, contentType, reason);
        return ResponseEntity.ok(ApiResponse.success("Content rejected successfully"));
    }
    
    @PutMapping("/comments/{id}/moderate")
    public ResponseEntity<ApiResponse<Void>> moderateComment(
            @PathVariable Long id,
            @RequestParam String action) {
        adminService.moderateComment(id, action);
        return ResponseEntity.ok(ApiResponse.success("Comment moderated successfully"));
    }
}
