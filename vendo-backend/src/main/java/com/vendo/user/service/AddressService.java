package com.vendo.user.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.user.dto.request.AddAddressRequest;
import com.vendo.user.dto.request.UpdateAddressRequest;
import com.vendo.user.dto.response.AddressResponse;
import com.vendo.user.entity.Address;
import com.vendo.user.entity.User;
import com.vendo.user.repository.AddressRepository;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {
    
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    
    private static final int MAX_ADDRESSES_PER_USER = 10;
    
    @Transactional
    public AddressResponse addAddress(Long userId, AddAddressRequest request) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check address limit
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount >= MAX_ADDRESSES_PER_USER) {
            throw new BadRequestException("Maximum number of addresses (" + MAX_ADDRESSES_PER_USER + ") reached");
        }
        
        // Validate address data
        validateAddressData(request.getAddressLine1(), request.getCity(), 
                          request.getState(), request.getPostalCode(), request.getCountry());
        
        // If this is set as default, unset other default addresses
        Boolean isDefault = request.getIsDefault() != null && request.getIsDefault();
        if (isDefault) {
            unsetDefaultAddresses(userId);
        }
        
        // Create new address
        Address address = Address.builder()
                .user(user)
                .addressType(request.getAddressType() != null ? request.getAddressType() : "SHIPPING")
                .contactName(request.getContactName())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(isDefault)
                .build();
        
        address = addressRepository.save(address);
        log.info("Address added for user ID: {}, address ID: {}", userId, address.getId());
        
        return buildAddressResponse(address);
    }
    
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Find address and verify ownership
        Address address = addressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        // Validate address data if provided
        if (request.getAddressLine1() != null || request.getCity() != null || 
            request.getState() != null || request.getPostalCode() != null || 
            request.getCountry() != null) {
            String addressLine1 = request.getAddressLine1() != null ? request.getAddressLine1() : address.getAddressLine1();
            String city = request.getCity() != null ? request.getCity() : address.getCity();
            String state = request.getState() != null ? request.getState() : address.getState();
            String postalCode = request.getPostalCode() != null ? request.getPostalCode() : address.getPostalCode();
            String country = request.getCountry() != null ? request.getCountry() : address.getCountry();
            
            validateAddressData(addressLine1, city, state, postalCode, country);
        }
        
        // Update fields
        if (request.getAddressType() != null) {
            address.setAddressType(request.getAddressType());
        }
        if (request.getContactName() != null) {
            address.setContactName(request.getContactName());
        }
        if (request.getPhone() != null) {
            address.setPhone(request.getPhone());
        }
        if (request.getAddressLine1() != null) {
            address.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            address.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        
        // Handle default address change
        if (request.getIsDefault() != null) {
            if (request.getIsDefault() && !address.getIsDefault()) {
                // Set this as default and unset others
                unsetDefaultAddresses(userId);
                address.setIsDefault(true);
            } else if (!request.getIsDefault() && address.getIsDefault()) {
                // Unset default - ensure at least one address remains if there are multiple
                List<Address> userAddresses = addressRepository.findByUserId(userId);
                if (userAddresses.size() > 1) {
                    address.setIsDefault(false);
                } else {
                    throw new BadRequestException("Cannot unset default address. At least one address must be set as default");
                }
            }
        }
        
        address = addressRepository.save(address);
        log.info("Address updated for user ID: {}, address ID: {}", userId, addressId);
        
        return buildAddressResponse(address);
    }
    
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Find address and verify ownership
        Address address = addressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        // Check if this is the only address
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        if (userAddresses.size() == 1) {
            throw new BadRequestException("Cannot delete the only address. At least one address is required");
        }
        
        // If deleting default address, set another one as default
        if (address.getIsDefault()) {
            List<Address> otherAddresses = userAddresses.stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .collect(Collectors.toList());
            if (!otherAddresses.isEmpty()) {
                Address newDefault = otherAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
        
        addressRepository.delete(address);
        log.info("Address deleted for user ID: {}, address ID: {}", userId, addressId);
    }
    
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long userId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        
        return addresses.stream()
                .map(this::buildAddressResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Find address and verify ownership
        Address address = addressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        // Unset other default addresses
        unsetDefaultAddresses(userId);
        
        // Set this address as default
        address.setIsDefault(true);
        address = addressRepository.save(address);
        
        log.info("Default address set for user ID: {}, address ID: {}", userId, addressId);
        
        return buildAddressResponse(address);
    }
    
    private void unsetDefaultAddresses(Long userId) {
        List<Address> defaultAddresses = addressRepository.findByUserId(userId).stream()
                .filter(Address::getIsDefault)
                .collect(Collectors.toList());
        
        for (Address addr : defaultAddresses) {
            addr.setIsDefault(false);
        }
        
        if (!defaultAddresses.isEmpty()) {
            addressRepository.saveAll(defaultAddresses);
        }
    }
    
    private void validateAddressData(String addressLine1, String city, String state, 
                                    String postalCode, String country) {
        if (addressLine1 == null || addressLine1.trim().isEmpty()) {
            throw new BadRequestException("Address line 1 is required");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new BadRequestException("City is required");
        }
        if (state == null || state.trim().isEmpty()) {
            throw new BadRequestException("State is required");
        }
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new BadRequestException("Postal code is required");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new BadRequestException("Country is required");
        }
        
        // Validate lengths
        if (addressLine1.length() > 255) {
            throw new BadRequestException("Address line 1 must be less than 255 characters");
        }
        if (city.length() > 100) {
            throw new BadRequestException("City must be less than 100 characters");
        }
        if (state.length() > 100) {
            throw new BadRequestException("State must be less than 100 characters");
        }
        if (postalCode.length() > 20) {
            throw new BadRequestException("Postal code must be less than 20 characters");
        }
        if (country.length() > 100) {
            throw new BadRequestException("Country must be less than 100 characters");
        }
    }
    
    private AddressResponse buildAddressResponse(Address address) {
        AddressResponse response = AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser() != null ? address.getUser().getId() : null)
                .addressType(address.getAddressType())
                .contactName(address.getContactName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
        
        // Compute and set formatted address
        String formatted = formatAddress(
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
        response.setFormattedAddress(formatted);
        
        return response;
    }
    
    private String formatAddress(String addressLine1, String addressLine2, 
                                 String city, String state, String postalCode, String country) {
        StringBuilder sb = new StringBuilder();
        
        if (addressLine1 != null && !addressLine1.trim().isEmpty()) {
            sb.append(addressLine1);
        }
        
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(addressLine2);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(city);
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(state);
        }
        
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(postalCode);
        }
        
        if (country != null && !country.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(country);
        }
        
        return sb.toString();
    }
}
