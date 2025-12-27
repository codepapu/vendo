package com.vendo.retailer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.ProductRetailerRequest;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import com.vendo.retailer.dto.response.ImportResponse;
import com.vendo.retailer.entity.ProductImportHistory;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.ProductImportHistoryRepository;
import com.vendo.retailer.repository.RetailerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportService {
    
    private final RetailerRepository retailerRepository;
    private final ProductRepository productRepository;
    private final RetailerProductService retailerProductService;
    private final ProductImportHistoryRepository importHistoryRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Import products from CSV/Excel file
     */
    @Transactional
    public ImportResponse importProducts(Long retailerId, MultipartFile file, Long userId) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        if (!"ACTIVE".equals(retailer.getStatus())) {
            throw new BadRequestException("Only ACTIVE retailers can import products");
        }
        
        String fileName = file.getOriginalFilename();
        String fileType = getFileType(fileName);
        
        if (!isValidFileType(fileType)) {
            throw new BadRequestException("Unsupported file type. Only CSV, XLS, and XLSX are supported");
        }
        
        // Create import history record
        ProductImportHistory importHistory = ProductImportHistory.builder()
                .retailer(retailer)
                .fileName(fileName)
                .fileType(fileType)
                .status("PROCESSING")
                .createdBy(userId)
                .build();
        importHistory = importHistoryRepository.save(importHistory);
        
        List<ImportResponse.ImportError> errors = new ArrayList<>();
        int successCount = 0;
        int totalRecords = 0;
        
        try {
            List<ProductImportRow> rows = parseFile(file, fileType);
            totalRecords = rows.size();
            
            for (int i = 0; i < rows.size(); i++) {
                ProductImportRow row = rows.get(i);
                try {
                    validateImportRow(row);
                    ProductRetailerRequest request = mapToProductRetailerRequest(row, retailerId);
                    retailerProductService.addProduct(retailerId, request);
                    successCount++;
                } catch (Exception e) {
                    errors.add(ImportResponse.ImportError.builder()
                            .rowNumber(i + 2) // +2 because row 1 is header, and we're 0-indexed
                            .message(e.getMessage())
                            .data(row.toString())
                            .build());
                    log.warn("Error importing row {}: {}", i + 2, e.getMessage());
                }
            }
            
            importHistory.setTotalRecords(totalRecords);
            importHistory.setSuccessCount(successCount);
            importHistory.setErrorCount(errors.size());
            importHistory.setStatus("COMPLETED");
            if (!errors.isEmpty()) {
                importHistory.setErrorDetails(objectMapper.writeValueAsString(errors));
            }
            
        } catch (Exception e) {
            log.error("Error processing import file: {}", e.getMessage(), e);
            importHistory.setStatus("FAILED");
            importHistory.setErrorDetails(e.getMessage());
        }
        
        importHistoryRepository.save(importHistory);
        
        return ImportResponse.builder()
                .importId(importHistory.getId())
                .retailerId(retailerId)
                .fileName(fileName)
                .fileType(fileType)
                .totalRecords(totalRecords)
                .successCount(successCount)
                .errorCount(errors.size())
                .status(importHistory.getStatus())
                .errors(errors)
                .createdAt(importHistory.getCreatedAt())
                .build();
    }
    
    /**
     * Validate import file before processing
     */
    @Transactional(readOnly = true)
    public ImportResponse validateImportFile(Long retailerId, MultipartFile file) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        String fileName = file.getOriginalFilename();
        String fileType = getFileType(fileName);
        
        if (!isValidFileType(fileType)) {
            throw new BadRequestException("Unsupported file type. Only CSV, XLS, and XLSX are supported");
        }
        
        List<ImportResponse.ImportError> errors = new ArrayList<>();
        int totalRecords = 0;
        
        try {
            List<ProductImportRow> rows = parseFile(file, fileType);
            totalRecords = rows.size();
            
            for (int i = 0; i < rows.size(); i++) {
                ProductImportRow row = rows.get(i);
                try {
                    validateImportRow(row);
                } catch (Exception e) {
                    errors.add(ImportResponse.ImportError.builder()
                            .rowNumber(i + 2)
                            .message(e.getMessage())
                            .data(row.toString())
                            .build());
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Error validating file: " + e.getMessage());
        }
        
        return ImportResponse.builder()
                .retailerId(retailerId)
                .fileName(fileName)
                .fileType(fileType)
                .totalRecords(totalRecords)
                .successCount(totalRecords - errors.size())
                .errorCount(errors.size())
                .status(errors.isEmpty() ? "VALID" : "INVALID")
                .errors(errors)
                .build();
    }
    
    /**
     * Get import template (CSV format)
     */
    public String getImportTemplate() {
        return "Product ID,Price,Compare At Price,Cost Price,Stock Quantity,Low Stock Threshold,Is Active\n" +
               "1,99.99,129.99,50.00,100,10,true\n" +
               "2,49.99,,30.00,50,5,true";
    }
    
    /**
     * Get import history for a retailer
     */
    @Transactional(readOnly = true)
    public Page<ImportResponse> getImportHistory(Long retailerId, Pageable pageable) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        Page<ProductImportHistory> history = importHistoryRepository.findByRetailerIdOrderByCreatedAtDesc(
                retailerId, pageable);
        
        return history.map(this::mapToImportResponse);
    }
    
    // Helper methods
    
    private String getFileType(String fileName) {
        if (fileName == null) {
            return "UNKNOWN";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".csv")) {
            return "CSV";
        } else if (lower.endsWith(".xlsx")) {
            return "XLSX";
        } else if (lower.endsWith(".xls")) {
            return "XLS";
        }
        return "UNKNOWN";
    }
    
    private boolean isValidFileType(String fileType) {
        return "CSV".equals(fileType) || "XLSX".equals(fileType) || "XLS".equals(fileType);
    }
    
    private List<ProductImportRow> parseFile(MultipartFile file, String fileType) throws Exception {
        List<ProductImportRow> rows = new ArrayList<>();
        
        if ("CSV".equals(fileType)) {
            rows = parseCSV(file);
        } else if ("XLSX".equals(fileType) || "XLS".equals(fileType)) {
            rows = parseExcel(file, fileType);
        }
        
        return rows;
    }
    
    private List<ProductImportRow> parseCSV(MultipartFile file) throws Exception {
        List<ProductImportRow> rows = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             CSVReader csvReader = new CSVReader(reader)) {
            
            List<String[]> records = csvReader.readAll();
            
            // Skip header row (index 0)
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                if (record.length < 7) continue; // Skip incomplete rows
                
                ProductImportRow row = new ProductImportRow();
                row.productId = parseLong(record[0]); // Product ID
                row.price = parseBigDecimal(record[1]); // Price
                row.compareAtPrice = parseBigDecimal(record[2]); // Compare At Price
                row.costPrice = parseBigDecimal(record[3]); // Cost Price
                row.stockQuantity = parseInt(record[4]); // Stock Quantity
                row.lowStockThreshold = parseInt(record[5]); // Low Stock Threshold
                row.isActive = parseBoolean(record[6]); // Is Active
                rows.add(row);
            }
        } catch (CsvException e) {
            throw new BadRequestException("Error parsing CSV file: " + e.getMessage());
        }
        
        return rows;
    }
    
    private List<ProductImportRow> parseExcel(MultipartFile file, String fileType) throws Exception {
        List<ProductImportRow> rows = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = "XLSX".equals(fileType) 
                     ? new XSSFWorkbook(inputStream) 
                     : new HSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                ProductImportRow importRow = new ProductImportRow();
                importRow.productId = parseLong(getCellValue(row.getCell(0)));
                importRow.price = parseBigDecimal(getCellValue(row.getCell(1)));
                importRow.compareAtPrice = parseBigDecimal(getCellValue(row.getCell(2)));
                importRow.costPrice = parseBigDecimal(getCellValue(row.getCell(3)));
                importRow.stockQuantity = parseInt(getCellValue(row.getCell(4)));
                importRow.lowStockThreshold = parseInt(getCellValue(row.getCell(5)));
                importRow.isActive = parseBoolean(getCellValue(row.getCell(6)));
                rows.add(importRow);
            }
        }
        
        return rows;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private void validateImportRow(ProductImportRow row) {
        if (row.productId == null) {
            throw new BadRequestException("Product ID is required");
        }
        
        Product product = productRepository.findById(row.productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", row.productId));
        
        if (row.price == null || row.price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }
        
        if (row.stockQuantity != null && row.stockQuantity < 0) {
            throw new BadRequestException("Stock quantity must be non-negative");
        }
    }
    
    private ProductRetailerRequest mapToProductRetailerRequest(ProductImportRow row, Long retailerId) {
        return ProductRetailerRequest.builder()
                .productId(row.productId)
                .retailerId(retailerId)
                .price(row.price)
                .compareAtPrice(row.compareAtPrice)
                .costPrice(row.costPrice)
                .stockQuantity(row.stockQuantity != null ? row.stockQuantity : 0)
                .lowStockThreshold(row.lowStockThreshold != null ? row.lowStockThreshold : 10)
                .isActive(row.isActive != null ? row.isActive : true)
                .build();
    }
    
    private ImportResponse mapToImportResponse(ProductImportHistory history) {
        List<ImportResponse.ImportError> errors = new ArrayList<>();
        try {
            if (history.getErrorDetails() != null && !history.getErrorDetails().isEmpty()) {
                errors = objectMapper.readValue(history.getErrorDetails(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ImportResponse.ImportError.class));
            }
        } catch (Exception e) {
            log.warn("Error parsing error details: {}", e.getMessage());
        }
        
        return ImportResponse.builder()
                .importId(history.getId())
                .retailerId(history.getRetailer().getId())
                .fileName(history.getFileName())
                .fileType(history.getFileType())
                .totalRecords(history.getTotalRecords())
                .successCount(history.getSuccessCount())
                .errorCount(history.getErrorCount())
                .status(history.getStatus())
                .errors(errors)
                .createdAt(history.getCreatedAt())
                .build();
    }
    
    // Helper parsing methods
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String lower = value.trim().toLowerCase();
        return "true".equals(lower) || "1".equals(lower) || "yes".equals(lower);
    }
    
    // Inner class for import row data
    private static class ProductImportRow {
        Long productId;
        BigDecimal price;
        BigDecimal compareAtPrice;
        BigDecimal costPrice;
        Integer stockQuantity;
        Integer lowStockThreshold;
        Boolean isActive;
        
        @Override
        public String toString() {
            return String.format("ProductId=%s, Price=%s, Stock=%s", productId, price, stockQuantity);
        }
    }
}

