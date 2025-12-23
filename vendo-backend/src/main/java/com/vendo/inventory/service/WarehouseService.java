package com.vendo.inventory.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.inventory.dto.request.WarehouseRequest;
import com.vendo.inventory.dto.response.WarehouseResponse;
import com.vendo.inventory.entity.Warehouse;
import com.vendo.inventory.repository.WarehouseRepository;
import com.vendo.user.entity.Address;
import com.vendo.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    private final AddressRepository addressRepository;
    
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> getAllWarehouses(Boolean isActive, Pageable pageable) {
        Page<Warehouse> warehouses;
        if (isActive != null) {
            warehouses = warehouseRepository.findByIsActive(isActive, pageable);
        } else {
            warehouses = warehouseRepository.findAll(pageable);
        }
        return warehouses.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        return mapToResponse(warehouse);
    }
    
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseByCode(String code) {
        Warehouse warehouse = warehouseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "code", code));
        return mapToResponse(warehouse);
    }
    
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllActiveWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAllActiveWarehouses();
        return warehouses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        // Validate code uniqueness
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Warehouse with code " + request.getCode() + " already exists");
        }
        
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .code(request.getCode())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        // Set address if provided
        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));
            warehouse.setAddress(address);
        }
        
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        log.info("Warehouse created with ID: {}", savedWarehouse.getId());
        
        return mapToResponse(savedWarehouse);
    }
    
    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        
        // Validate code uniqueness if changed
        if (request.getCode() != null && !request.getCode().equals(warehouse.getCode())) {
            if (warehouseRepository.existsByCode(request.getCode())) {
                throw new BadRequestException("Warehouse with code " + request.getCode() + " already exists");
            }
            warehouse.setCode(request.getCode());
        }
        
        if (request.getName() != null) {
            warehouse.setName(request.getName());
        }
        
        if (request.getIsActive() != null) {
            warehouse.setIsActive(request.getIsActive());
        }
        
        // Update address if provided
        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));
            warehouse.setAddress(address);
        }
        
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        log.info("Warehouse updated with ID: {}", updatedWarehouse.getId());
        
        return mapToResponse(updatedWarehouse);
    }
    
    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        
        // Check if warehouse has inventory
        if (warehouse.getInventory() != null && !warehouse.getInventory().isEmpty()) {
            throw new BadRequestException("Cannot delete warehouse with existing inventory. Please transfer or remove inventory first.");
        }
        
        warehouseRepository.delete(warehouse);
        log.info("Warehouse deleted with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> searchWarehouses(String keyword, Boolean isActive, Pageable pageable) {
        Page<Warehouse> warehouses = warehouseRepository.searchWarehouses(keyword, isActive, pageable);
        return warehouses.map(this::mapToResponse);
    }
    
    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        WarehouseResponse.WarehouseResponseBuilder builder = WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .code(warehouse.getCode())
                .isActive(warehouse.getIsActive())
                .createdAt(warehouse.getCreatedAt());
        
        if (warehouse.getAddress() != null) {
            builder.addressId(warehouse.getAddress().getId());
            
            Address address = warehouse.getAddress();
            WarehouseResponse.AddressInfo addressInfo = WarehouseResponse.AddressInfo.builder()
                    .id(address.getId())
                    .addressLine1(address.getAddressLine1())
                    .addressLine2(address.getAddressLine2())
                    .city(address.getCity())
                    .state(address.getState())
                    .postalCode(address.getPostalCode())
                    .country(address.getCountry())
                    .build();
            builder.address(addressInfo);
        }
        
        return builder.build();
    }
}

