package com.vendo.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    
    private Long id;
    private Long userId;
    private String addressType;
    private String contactName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private String formattedAddress;
    private LocalDateTime createdAt;
    
    /**
     * Returns the formatted address string.
     * The formatted address is computed and set by the service layer.
     * Format: addressLine1, addressLine2 (if present), city, state postalCode, country
     */
    public String getFormattedAddress() {
        // If formatted address is not set, compute it on the fly
        if (formattedAddress == null || formattedAddress.isEmpty()) {
            return formatAddress();
        }
        return formattedAddress;
    }
    
    /**
     * Formats the address as a single string
     * Format: addressLine1, addressLine2 (if present), city, state postalCode, country
     */
    private String formatAddress() {
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
