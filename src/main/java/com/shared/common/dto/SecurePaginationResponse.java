package com.shared.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Secure Pagination Response DTO with opaque pagination tokens
 * Prevents tampering with pagination parameters in URLs
 */
@Schema(description = "Secure pagination response with opaque navigation tokens")
public class SecurePaginationResponse<T> {
    
    @Schema(description = "List of data items for current page")
    private List<T> content;
    
    @Schema(description = "Current page number")
    private int currentPage;
    
    @Schema(description = "Page size")
    private int pageSize;
    
    @Schema(description = "Total number of elements across all pages")
    private long totalElements;
    
    @Schema(description = "Total number of pages")
    private int totalPages;
    
    @Schema(description = "Whether this is the first page")
    private boolean first;
    
    @Schema(description = "Whether this is the last page")
    private boolean last;
    
    @Schema(description = "Whether there is a next page")
    private boolean hasNext;
    
    @Schema(description = "Whether there is a previous page")
    private boolean hasPrevious;
    
    @Schema(description = "Opaque token for next page (tamper-proof)")
    private String nextPageToken;
    
    @Schema(description = "Opaque token for previous page (tamper-proof)")
    private String previousPageToken;
    
    @Schema(description = "Applied date range filter")
    private DateRange dateRange;
    
    @Schema(description = "Applied sorting")
    private SortInfo sortInfo;
    
    // Constructors
    public SecurePaginationResponse() {}
    
    public SecurePaginationResponse(List<T> content, int currentPage, int pageSize, 
                                  long totalElements, int totalPages) {
        this.content = content;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = currentPage == 0;
        this.last = currentPage == totalPages - 1;
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    // Getters and Setters
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public String getNextPageToken() {
        return nextPageToken;
    }
    
    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
    
    public String getPreviousPageToken() {
        return previousPageToken;
    }
    
    public void setPreviousPageToken(String previousPageToken) {
        this.previousPageToken = previousPageToken;
    }
    
    public DateRange getDateRange() {
        return dateRange;
    }
    
    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }
    
    public SortInfo getSortInfo() {
        return sortInfo;
    }
    
    public void setSortInfo(SortInfo sortInfo) {
        this.sortInfo = sortInfo;
    }
    
    // Inner classes for metadata
    @Schema(description = "Date range information")
    public static class DateRange {
        @Schema(description = "Start date")
        private String startDate;
        
        @Schema(description = "End date") 
        private String endDate;
        
        public DateRange(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
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
    }
    
    @Schema(description = "Sort information")
    public static class SortInfo {
        @Schema(description = "Sort field")
        private String sortBy;
        
        @Schema(description = "Sort direction")
        private String sortDir;
        
        public SortInfo(String sortBy, String sortDir) {
            this.sortBy = sortBy;
            this.sortDir = sortDir;
        }
        
        // Getters and Setters
        public String getSortBy() {
            return sortBy;
        }
        
        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }
        
        public String getSortDir() {
            return sortDir;
        }
        
        public void setSortDir(String sortDir) {
            this.sortDir = sortDir;
        }
    }
}
