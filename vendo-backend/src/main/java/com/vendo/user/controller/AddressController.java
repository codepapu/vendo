package com.vendo.user.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.user.dto.request.AddAddressRequest;
import com.vendo.user.dto.request.UpdateAddressRequest;
import com.vendo.user.dto.response.AddressResponse;
import com.vendo.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddAddressRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        AddressResponse address = addressService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", address));
    }
    
    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        AddressResponse address = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }
    
    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<AddressResponse> addresses = addressService.getAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }
    
    @PutMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        AddressResponse address = addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address set successfully", address));
    }
}
