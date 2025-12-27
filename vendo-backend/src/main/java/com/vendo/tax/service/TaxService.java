package com.vendo.tax.service;

import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.tax.config.TaxApiConfig;
import com.vendo.tax.dto.request.TaxRequest;
import com.vendo.tax.dto.response.TaxRateResponse;
import com.vendo.tax.dto.response.TaxResponse;
import com.vendo.tax.entity.TaxExemption;
import com.vendo.tax.entity.TaxRule;
import com.vendo.tax.repository.TaxExemptionRepository;
import com.vendo.tax.repository.TaxRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tax Service
 * Handles tax calculation based on location, product type, and business type
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaxService {
    
    private final TaxRuleRepository taxRuleRepository;
    private final TaxExemptionRepository taxExemptionRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final TaxApiService taxApiService;
    private final TaxApiConfig taxApiConfig;
    
    /**
     * Calculate tax for an order
     */
    @Transactional(readOnly = true)
    public TaxResponse calculateTax(TaxRequest request) {
        log.debug("Calculating tax for request: {}", request);
        
        BigDecimal subTotal = request.getSubTotal() != null ? request.getSubTotal() : BigDecimal.ZERO;
        
        // Try external tax API first if enabled and configured
        if (taxApiConfig.getEnabled() && taxApiService.isConfigured()) {
            TaxResponse apiResponse = taxApiService.calculateTaxFromApi(request);
            if (apiResponse != null) {
                log.debug("Tax calculated using external API");
                return apiResponse;
            }
            log.debug("External tax API returned null, falling back to internal calculation");
        }
        
        // Check for B2B exemptions
        TaxExemption exemption = checkTaxExemption(request);
        if (exemption != null && exemption.getExemptionPercentage().compareTo(BigDecimal.valueOf(100)) == 0) {
            return TaxResponse.builder()
                    .subTotal(subTotal)
                    .taxAmount(BigDecimal.ZERO)
                    .total(subTotal)
                    .currency("USD")
                    .breakdown(new ArrayList<>())
                    .isExempt(true)
                    .exemptionReason("B2B tax exemption applied")
                    .calculationMethod("EXEMPTION")
                    .build();
        }
        
        // Get applicable tax rules
        List<TaxRule> applicableRules = getApplicableTaxRules(request);
        
        if (applicableRules.isEmpty()) {
            log.warn("No tax rules found for request: {}", request);
            return TaxResponse.builder()
                    .subTotal(subTotal)
                    .taxAmount(BigDecimal.ZERO)
                    .total(subTotal)
                    .currency("USD")
                    .breakdown(new ArrayList<>())
                    .isExempt(false)
                    .calculationMethod("NONE")
                    .build();
        }
        
        // Calculate tax breakdown
        List<TaxResponse.TaxBreakdown> breakdown = new ArrayList<>();
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        String calculationMethod = applicableRules.get(0).getRuleType();
        
        // Apply tax rules in priority order
        for (TaxRule rule : applicableRules) {
            BigDecimal taxRate = rule.getTaxPercentage();
            
            // Apply exemption percentage if partial exemption exists
            if (exemption != null && exemption.getExemptionPercentage().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal exemptionRate = exemption.getExemptionPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                taxRate = taxRate.multiply(BigDecimal.ONE.subtract(exemptionRate));
            }
            
            BigDecimal taxAmount = subTotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalTaxAmount = totalTaxAmount.add(taxAmount);
            
            breakdown.add(TaxResponse.TaxBreakdown.builder()
                    .ruleName(rule.getRuleName())
                    .ruleType(rule.getRuleType())
                    .rate(taxRate)
                    .amount(taxAmount)
                    .description(String.format("%s tax at %.2f%%", rule.getRuleName(), taxRate))
                    .build());
        }
        
        BigDecimal total = subTotal.add(totalTaxAmount);
        
        return TaxResponse.builder()
                .subTotal(subTotal)
                .taxAmount(totalTaxAmount)
                .total(total)
                .currency("USD")
                .breakdown(breakdown)
                .isExempt(false)
                .calculationMethod(calculationMethod)
                .build();
    }
    
    /**
     * Get tax rates for a location
     */
    @Transactional(readOnly = true)
    public TaxRateResponse getTaxRates(String country, String state, String city, String productType, String businessType) {
        log.debug("Getting tax rates for location: {}/{}/{}", country, state, city);
        
        List<TaxRule> rules = new ArrayList<>();
        
        // Get location-based rules
        if (country != null) {
            List<TaxRule> locationRules = taxRuleRepository.findLocationBasedRules(country, state, city);
            rules.addAll(locationRules);
        }
        
        // Get product type rules
        if (productType != null) {
            List<TaxRule> productRules = taxRuleRepository.findByProductType(productType);
            rules.addAll(productRules);
        }
        
        // Get business type rules
        if (businessType != null) {
            List<TaxRule> businessRules = taxRuleRepository.findByBusinessType(businessType);
            rules.addAll(businessRules);
        }
        
        // Get default rules if no specific rules found
        if (rules.isEmpty()) {
            rules = taxRuleRepository.findDefaultRules();
        }
        
        // Remove duplicates and sort by priority
        rules = rules.stream()
                .distinct()
                .sorted(Comparator.comparing(TaxRule::getPriority))
                .collect(Collectors.toList());
        
        List<TaxRateResponse.TaxRate> rates = rules.stream()
                .map(rule -> TaxRateResponse.TaxRate.builder()
                        .ruleId(rule.getId())
                        .ruleName(rule.getRuleName())
                        .ruleType(rule.getRuleType())
                        .percentage(rule.getTaxPercentage())
                        .priority(rule.getPriority())
                        .description(String.format("%s: %.2f%%", rule.getRuleName(), rule.getTaxPercentage()))
                        .build())
                .collect(Collectors.toList());
        
        return TaxRateResponse.builder()
                .country(country)
                .state(state)
                .city(city)
                .productType(productType)
                .businessType(businessType)
                .rates(rates)
                .build();
    }
    
    /**
     * Get tax by location
     */
    @Transactional(readOnly = true)
    public TaxRateResponse getTaxByLocation(String country, String state, String city) {
        return getTaxRates(country, state, city, null, null);
    }
    
    /**
     * Apply tax rules based on request
     */
    @Transactional(readOnly = true)
    public List<TaxRule> applyTaxRules(TaxRequest request) {
        return getApplicableTaxRules(request);
    }
    
    /**
     * Get applicable tax rules for a request
     */
    private List<TaxRule> getApplicableTaxRules(TaxRequest request) {
        List<TaxRule> rules = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Get location-based rules
        if (request.getCountry() != null) {
            List<TaxRule> locationRules = taxRuleRepository.findLocationBasedRules(
                    request.getCountry(),
                    request.getState(),
                    request.getCity()
            );
            rules.addAll(locationRules);
        }
        
        // Get product type rules for each item
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (TaxRequest.TaxItemRequest item : request.getItems()) {
                if (item.getProductType() != null) {
                    List<TaxRule> productRules = taxRuleRepository.findByProductType(item.getProductType());
                    rules.addAll(productRules);
                }
            }
        }
        
        // Get business type rules
        if (request.getBusinessType() != null) {
            List<TaxRule> businessRules = taxRuleRepository.findByBusinessType(request.getBusinessType());
            // Filter by appliesToB2b if B2B
            if ("B2B".equals(request.getBusinessType()) || request.getBusinessAccountId() != null) {
                businessRules = businessRules.stream()
                        .filter(TaxRule::getAppliesToB2b)
                        .collect(Collectors.toList());
            }
            rules.addAll(businessRules);
        }
        
        // Get default rules if no specific rules found
        if (rules.isEmpty()) {
            rules = taxRuleRepository.findDefaultRules();
        }
        
        // Remove duplicates and sort by priority
        return rules.stream()
                .distinct()
                .sorted(Comparator.comparing(TaxRule::getPriority))
                .collect(Collectors.toList());
    }
    
    /**
     * Check for tax exemptions
     */
    private TaxExemption checkTaxExemption(TaxRequest request) {
        if (request.getBusinessAccountId() == null && request.getUserId() == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<TaxExemption> exemptions = taxExemptionRepository.findMatchingExemptions(
                request.getBusinessAccountId(),
                request.getUserId(),
                request.getCountry(),
                request.getState(),
                request.getItems() != null && !request.getItems().isEmpty() 
                    ? request.getItems().get(0).getProductType() 
                    : null,
                now
        );
        
        if (!exemptions.isEmpty()) {
            // Return the most applicable exemption (FULL exemption takes precedence)
            return exemptions.stream()
                    .filter(e -> "FULL".equals(e.getExemptionType()))
                    .findFirst()
                    .orElse(exemptions.get(0));
        }
        
        return null;
    }
}

