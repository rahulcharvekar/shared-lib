

package com.shared.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Secure Pagination Request DTO with mandatory date range parameters
 * Ensures all paginated APIs have proper date filtering to prevent data exposure
 */
@Schema(description = "Secure pagination request with mandatory date range filtering")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurePaginationRequest {
    @Schema(description = "Opaque cursor for page navigation (cursor-based pagination)")
    private String pageToken;

    /**
     * Validate the request for robust pagination security.
     * Throws IllegalArgumentException if invalid.
     */
    public void validate() {
        // Validate date format and range using String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.LocalDate start, end;
        try {
            start = java.time.LocalDate.parse(startDate, formatter);
            end = java.time.LocalDate.parse(endDate, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        if (start.plusDays(31).isBefore(end)) {
            throw new IllegalArgumentException("Date range must not exceed 31 days");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
        if (sortDir != null && !("asc".equalsIgnoreCase(sortDir) || "desc".equalsIgnoreCase(sortDir))) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
        }
    }
    // Whitelist of allowed sortBy aliases and their DB columns
    public static final Map<String, String> ALLOWED_SORT_BY;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("createdAt", "created_at");
        map.put("receiptDate", "receipt_date");
        map.put("amount", "amount");
        map.put("id", "id");
        // Add more aliases if needed
        ALLOWED_SORT_BY = Collections.unmodifiableMap(map);
    }
    
    @NotNull(message = "Start date is mandatory for secure pagination")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Start date must be in YYYY-MM-DD format")
    @Schema(description = "Start date for filtering (YYYY-MM-DD) - MANDATORY", 
            example = "2024-01-01", required = true)
    private String startDate;
    
    @NotNull(message = "End date is mandatory for secure pagination")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "End date must be in YYYY-MM-DD format")
    @Schema(description = "End date for filtering (YYYY-MM-DD) - MANDATORY", 
            example = "2024-12-31", required = true)
    private String endDate;
    
    @Min(value = 0, message = "Page number must be 0 or greater")
    @Schema(description = "Page number (0-based, ignored for forward navigation)", example = "0", defaultValue = "0", hidden = true)
    private int page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Page size (max 100)", example = "20", defaultValue = "20")
    private int size = 20;
    
    @Schema(description = "Sort field", example = "receiptDate")
    private String sortBy = "receiptDate";
    
    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    @Schema(description = "Sort direction", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDir = "desc";
    

    @Schema(description = "Receipt status filter", example = "PENDING")
    private String status;

    public String getStatus() {
        return status;
    }

    public String getPageToken() {
        return pageToken;
    }

    public void setPageToken(String pageToken) {
        this.pageToken = pageToken;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Constructors
    public SecurePaginationRequest() {}
    
    public SecurePaginationRequest(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public SecurePaginationRequest(String startDate, String endDate, int page, int size) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.page = page;
        this.size = size;
    }
    
    // Getters and Setters
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    @Schema(hidden = true)
    public int getPage() {
        return page;
    }
    /**
     * Deprecated: page is ignored for forward navigation (cursor-based)
     */
    @Schema(hidden = true)
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    /**
     * Set sortBy only if allowed.
     */
    public void setSortBy(String sortBy) {
        if (sortBy != null && !ALLOWED_SORT_BY.containsKey(sortBy)) {
            throw new IllegalArgumentException("Unknown sortBy alias: " + sortBy);
        }
        this.sortBy = sortBy;
    }

    /**
     * Get the DB column for the current sortBy alias.
     */
    public String getSortByColumn() {
        return ALLOWED_SORT_BY.get(sortBy);
    }
    
    public String getSortDir() {
        return sortDir;
    }
    
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
    
    
    @Override
    public String toString() {
        return "SecurePaginationRequest{" +
                "startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", status='" + status + '\'' +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDir='" + sortDir + '\'' +
                '}';
    }
}
