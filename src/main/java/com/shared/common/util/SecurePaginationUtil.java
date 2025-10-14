package com.shared.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.shared.common.dto.SecurePaginationRequest;
import com.shared.common.dto.SecurePaginationResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class for implementing secure pagination across all controllers
 */
public class SecurePaginationUtil {

    /**
     * Applies pageToken to the request by decoding and setting page, size, sortBy, sortDir.
     * Should be called before creating Pageable for cursor-based pagination.
     */
    public static void applyPageToken(SecurePaginationRequest request) {
        if (request.getPageToken() != null && !request.getPageToken().isEmpty()) {
            try {
                String tokenData = new String(java.util.Base64.getDecoder().decode(request.getPageToken()));
                // Format: startDate|endDate|page|size|sortBy|sortDir|timestamp
                String[] parts = tokenData.split("\\|");
                if (parts.length >= 6) {
                    // Only update page, size, sortBy, sortDir if present in token
                    int page = Integer.parseInt(parts[2]);
                    int size = Integer.parseInt(parts[3]);
                    String sortBy = parts[4];
                    String sortDir = parts[5];
                    request.setPage(page);
                    request.setSize(size);
                    request.setSortBy(sortBy);
                    request.setSortDir(sortDir);
                }
            } catch (Exception e) {
                // If token is invalid, ignore and fallback to defaults
            }
        }
    }
    
    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Validate and parse secure pagination request
     */
    public static ValidationResult validatePaginationRequest(SecurePaginationRequest request) {
        ValidationResult result = new ValidationResult();
        
        // Validate mandatory date parameters
        if (request.getStartDate() == null || request.getStartDate().trim().isEmpty()) {
            result.addError("Start date is mandatory for secure pagination");
        }
        
        if (request.getEndDate() == null || request.getEndDate().trim().isEmpty()) {
            result.addError("End date is mandatory for secure pagination");
        }
        
        // Parse and validate dates
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        try {
            if (request.getStartDate() != null && !request.getStartDate().trim().isEmpty()) {
                LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
                startDateTime = startDate.atStartOfDay();
            }
        } catch (DateTimeParseException e) {
            result.addError("Invalid start date format. Use YYYY-MM-DD format.");
        }
        
        try {
            if (request.getEndDate() != null && !request.getEndDate().trim().isEmpty()) {
                LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
                endDateTime = endDate.atTime(23, 59, 59);
            }
        } catch (DateTimeParseException e) {
            result.addError("Invalid end date format. Use YYYY-MM-DD format.");
        }
        
        // Validate date range logic
        if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
            result.addError("Start date cannot be after end date");
        }
        
        // Validate pagination parameters
        if (request.getPage() < 0) {
            result.addError("Page number must be 0 or greater");
        }
        
        if (request.getSize() < 1) {
            result.addError("Page size must be at least 1");
        }
        
        if (request.getSize() > MAX_PAGE_SIZE) {
            result.addError("Page size cannot exceed " + MAX_PAGE_SIZE);
        }
        
        // Validate sort direction
        if (request.getSortDir() != null && 
            !request.getSortDir().equalsIgnoreCase("asc") && 
            !request.getSortDir().equalsIgnoreCase("desc")) {
            result.addError("Sort direction must be 'asc' or 'desc'");
        }
        
        result.setStartDateTime(startDateTime);
        result.setEndDateTime(endDateTime);
        
        return result;
    }
    
    /**
     * Create secure sort from request with field validation
     */
    public static Sort createSecureSort(SecurePaginationRequest request, List<String> allowedSortFields) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        
        // Validate sortBy against allowed fields for security
        validateSortField(sortBy, allowedSortFields);
        
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(request.getSortDir())) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        return sort;
    }

    /**
     * Validate sort field against allowed fields
     */
    public static void validateSortField(String sortBy, List<String> allowedSortFields) {
        if (allowedSortFields != null && !allowedSortFields.isEmpty()) {
            if (sortBy != null && !allowedSortFields.contains(sortBy)) {
                throw new IllegalArgumentException("Invalid sort field: " + sortBy + ". Allowed fields: " + allowedSortFields);
            }
        }
    }

    /**
     * Create sort from field and direction with validation
     */
    public static Sort createSort(String sortBy, String sortDir, List<String> allowedSortFields) {
        validateSortField(sortBy, allowedSortFields);
        Sort sort = Sort.by(sortBy != null ? sortBy : "id");
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        return sort;
    }

    
    /**
     * Create secure pagination response with opaque tokens
     */
    public static <T> SecurePaginationResponse<T> createSecureResponse(
            Page<T> page, SecurePaginationRequest originalRequest) {
        
        SecurePaginationResponse<T> response = new SecurePaginationResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
        
        // Add date range metadata
        response.setDateRange(new SecurePaginationResponse.DateRange(
            originalRequest.getStartDate(), originalRequest.getEndDate()));
        
        // Add sort metadata
        response.setSortInfo(new SecurePaginationResponse.SortInfo(
            originalRequest.getSortBy(), originalRequest.getSortDir()));
        
        // Generate opaque pagination tokens
        if (page.hasNext()) {
            String nextToken = generateSecureToken(
                originalRequest.getStartDate(), originalRequest.getEndDate(),
                page.getNumber() + 1, page.getSize(),
                originalRequest.getSortBy(), originalRequest.getSortDir());
            response.setNextPageToken(nextToken);
        }
        
        if (page.hasPrevious()) {
            String prevToken = generateSecureToken(
                originalRequest.getStartDate(), originalRequest.getEndDate(),
                page.getNumber() - 1, page.getSize(),
                originalRequest.getSortBy(), originalRequest.getSortDir());
            response.setPreviousPageToken(prevToken);
        }
        
        return response;
    }
    
    /**
     * Generate secure opaque token for pagination (tamper-proof)
     * In production, this should use encryption or signed tokens
     */
    private static String generateSecureToken(String startDate, String endDate, int page, 
                                            int size, String sortBy, String sortDir) {
        String tokenData = String.format("%s|%s|%d|%d|%s|%s|%d", 
                                        startDate, endDate, page, size, 
                                        sortBy != null ? sortBy : "createdAt", 
                                        sortDir != null ? sortDir : "desc",
                                        System.currentTimeMillis());
        return java.util.Base64.getEncoder().encodeToString(tokenData.getBytes());
    }
    
    /**
     * Create error response for validation failures
     */
    public static Map<String, Object> createErrorResponse(ValidationResult validation) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", "Validation failed");
        error.put("details", validation.getErrors());
        error.put("timestamp", LocalDateTime.now());
        return error;
    }
    
    /**
     * Convert legacy parameters to SecurePaginationRequest
     */
    public static SecurePaginationRequest createFromLegacyParams(
            String startDate, String endDate, int page, int size, 
            String sortBy, String sortDir) {
        SecurePaginationRequest request = new SecurePaginationRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy != null ? sortBy : "createdAt");
        request.setSortDir(sortDir != null ? sortDir : "desc");
        return request;
    }
    
    /**
     * Validation result holder
     */
    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new java.util.ArrayList<>();
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        
        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }
        
        public void setStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
        }
        
        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }
        
        public void setEndDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
        }
    }
}
