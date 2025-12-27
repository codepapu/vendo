package com.vendo.b2b.service;

import com.vendo.b2b.dto.request.CreateBulkOrderRequest;
import com.vendo.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BulkOrderFileParserService {
    
    /**
     * Parse CSV or Excel file and convert to CreateBulkOrderRequest
     */
    public CreateBulkOrderRequest parseBulkOrderFile(MultipartFile file, Long businessAccountId, String name, String description) {
        log.info("Parsing bulk order file: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new BadRequestException("File name is required");
        }
        
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        String fileType = determineFileType(fileExtension);
        
        List<CreateBulkOrderRequest.BulkOrderItemRequest> items;
        
        try {
            if ("csv".equals(fileType)) {
                items = parseCSV(file.getInputStream());
            } else if ("excel".equals(fileType)) {
                items = parseExcel(file.getInputStream(), fileExtension);
            } else {
                throw new BadRequestException("Unsupported file type. Only CSV and Excel files are supported.");
            }
        } catch (Exception e) {
            log.error("Failed to parse file: {}", fileName, e);
            throw new BadRequestException("Failed to parse file: " + e.getMessage());
        }
        
        if (items.isEmpty()) {
            throw new BadRequestException("No valid items found in the file");
        }
        
        return CreateBulkOrderRequest.builder()
                .businessAccountId(businessAccountId)
                .name(name != null ? name : "Bulk Order from " + fileName)
                .description(description)
                .sourceFileName(fileName)
                .sourceFileType(fileExtension.toUpperCase())
                .items(items)
                .build();
    }
    
    /**
     * Parse CSV file
     * Expected format: Row Number, Product SKU, Product Name, Quantity, Unit Price (optional)
     * First row is header, data starts from row 2
     */
    private List<CreateBulkOrderRequest.BulkOrderItemRequest> parseCSV(InputStream inputStream) throws Exception {
        List<CreateBulkOrderRequest.BulkOrderItemRequest> items = new ArrayList<>();
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
            
            String line;
            int rowNumber = 1; // Start from 1 (header is row 1)
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    rowNumber++;
                    continue; // Skip header row
                }
                
                if (line.trim().isEmpty()) {
                    rowNumber++;
                    continue; // Skip empty lines
                }
                
                String[] columns = parseCSVLine(line);
                
                if (columns.length < 3) {
                    log.warn("Skipping row {}: insufficient columns (expected at least 3: SKU, Name, Quantity)", rowNumber);
                    rowNumber++;
                    continue;
                }
                
                try {
                    String productSku = columns.length > 0 ? columns[0].trim() : null;
                    String productName = columns.length > 1 ? columns[1].trim() : null;
                    Integer quantity = columns.length > 2 ? parseInteger(columns[2].trim()) : null;
                    BigDecimal unitPrice = columns.length > 3 && !columns[3].trim().isEmpty() 
                            ? parseBigDecimal(columns[3].trim()) : null;
                    
                    if (productSku == null || productSku.isEmpty()) {
                        log.warn("Skipping row {}: Product SKU is required", rowNumber);
                        rowNumber++;
                        continue;
                    }
                    
                    if (quantity == null || quantity <= 0) {
                        log.warn("Skipping row {}: Quantity must be greater than 0", rowNumber);
                        rowNumber++;
                        continue;
                    }
                    
                    CreateBulkOrderRequest.BulkOrderItemRequest item = CreateBulkOrderRequest.BulkOrderItemRequest.builder()
                            .rowNumber(rowNumber)
                            .productSku(productSku)
                            .productName(productName)
                            .quantity(quantity)
                            .unitPrice(unitPrice)
                            .build();
                    
                    items.add(item);
                } catch (Exception e) {
                    log.warn("Skipping row {} due to parsing error: {}", rowNumber, e.getMessage());
                }
                
                rowNumber++;
            }
        }
        
        return items;
    }
    
    /**
     * Parse CSV line handling quoted values
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * Parse Excel file (XLSX or XLS)
     * Expected format: Row 1 is header, data starts from row 2
     * Columns: Product SKU, Product Name, Quantity, Unit Price (optional)
     */
    private List<CreateBulkOrderRequest.BulkOrderItemRequest> parseExcel(InputStream inputStream, String fileExtension) throws Exception {
        List<CreateBulkOrderRequest.BulkOrderItemRequest> items = new ArrayList<>();
        
        Workbook workbook;
        if ("xlsx".equals(fileExtension)) {
            workbook = new XSSFWorkbook(inputStream);
        } else if ("xls".equals(fileExtension)) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new BadRequestException("Unsupported Excel format: " + fileExtension);
        }
        
        try {
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new BadRequestException("Excel file must have at least a header row and one data row");
            }
            
            // Skip header row (row 0), start from row 1
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                
                if (row == null) {
                    continue; // Skip empty rows
                }
                
                // Check if row is empty
                if (isRowEmpty(row)) {
                    continue;
                }
                
                try {
                    // Column 0: Product SKU
                    String productSku = getCellValueAsString(row.getCell(0));
                    if (productSku == null || productSku.trim().isEmpty()) {
                        log.warn("Skipping row {}: Product SKU is required", rowIndex + 1);
                        continue;
                    }
                    
                    // Column 1: Product Name
                    String productName = getCellValueAsString(row.getCell(1));
                    
                    // Column 2: Quantity
                    Integer quantity = getCellValueAsInteger(row.getCell(2));
                    if (quantity == null || quantity <= 0) {
                        log.warn("Skipping row {}: Quantity must be greater than 0", rowIndex + 1);
                        continue;
                    }
                    
                    // Column 3: Unit Price (optional)
                    BigDecimal unitPrice = getCellValueAsBigDecimal(row.getCell(3));
                    
                    CreateBulkOrderRequest.BulkOrderItemRequest item = CreateBulkOrderRequest.BulkOrderItemRequest.builder()
                            .rowNumber(rowIndex + 1) // Excel rows are 1-indexed
                            .productSku(productSku.trim())
                            .productName(productName != null ? productName.trim() : null)
                            .quantity(quantity)
                            .unitPrice(unitPrice)
                            .build();
                    
                    items.add(item);
                } catch (Exception e) {
                    log.warn("Skipping row {} due to parsing error: {}", rowIndex + 1, e.getMessage());
                }
            }
        } finally {
            workbook.close();
        }
        
        return items;
    }
    
    /**
     * Get cell value as String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format numeric value without decimal if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * Get cell value as Integer
     */
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    /**
     * Get cell value as BigDecimal
     */
    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) {
                        return null;
                    }
                    return new BigDecimal(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    /**
     * Check if row is empty
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Determine file type from extension
     */
    private String determineFileType(String extension) {
        switch (extension.toLowerCase()) {
            case "csv":
                return "csv";
            case "xlsx":
            case "xls":
                return "excel";
            default:
                return "unknown";
        }
    }
    
    /**
     * Parse integer from string
     */
    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parse BigDecimal from string
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

