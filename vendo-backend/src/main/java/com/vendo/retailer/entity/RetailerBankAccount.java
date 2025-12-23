package com.vendo.retailer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "retailer_bank_accounts", indexes = {
    @Index(name = "idx_retailer_bank_accounts_retailer_id", columnList = "retailer_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RetailerBankAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    @NotNull
    private Retailer retailer;
    
    @Column(name = "account_holder_name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String accountHolderName;
    
    @Column(name = "account_number", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String accountNumber;
    
    @Column(name = "bank_name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String bankName;
    
    @Column(name = "ifsc_code", length = 50)
    @Size(max = 50)
    private String ifscCode;
    
    @Column(name = "swift_code", length = 50)
    @Size(max = 50)
    private String swiftCode;
    
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
