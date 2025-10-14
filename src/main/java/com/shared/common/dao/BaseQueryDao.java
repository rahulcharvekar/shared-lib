package com.shared.common.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base DAO class for executing custom queries for read operations.
 * This centralizes all read query logic in the service layer for better control and debugging.
 */
@Component
public class BaseQueryDao {
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    /**
     * Execute a query and return a list of results
     */
    public <T> List<T> queryForList(String sql, Map<String, Object> params, RowMapper<T> mapper) {
        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapper.mapRow(rs, rowNum));
    }
    
    /**
     * Execute a query and return a single result
     */
    public <T> Optional<T> queryForObject(String sql, Map<String, Object> params, RowMapper<T> mapper) {
        List<T> results = queryForList(sql, params, mapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Execute a count query
     */
    public Long queryForCount(String sql, Map<String, Object> params) {
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }
    
    /**
     * Execute a query for pagination with total count
     */
    public <T> PageResult<T> queryForPage(String baseSql, String countSql, Map<String, Object> params, 
                                         int page, int size, RowMapper<T> mapper) {
        // Get total count
        Long totalCount = queryForCount(countSql, params);
        
        // Add pagination to the query
        String paginatedSql = baseSql + " LIMIT :limit OFFSET :offset";
        params.put("limit", size);
        params.put("offset", page * size);
        
        // Get page data
        List<T> content = queryForList(paginatedSql, params, mapper);
        
        return new PageResult<>(content, page, size, totalCount);
    }
    
    /**
     * Custom row mapper interface
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet rs, int rowNum) throws SQLException;
    }
    
    /**
     * Page result wrapper
     */
    public static class PageResult<T> {
        private final List<T> content;
        private final int page;
        private final int size;
        private final Long totalElements;
        private final int totalPages;
        
        public PageResult(List<T> content, int page, int size, Long totalElements) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil((double) totalElements / size);
        }
        
        // Getters
        public List<T> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public Long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean isFirst() { return page == 0; }
        public boolean isLast() { return page >= totalPages - 1; }
        public boolean hasNext() { return !isLast(); }
        public boolean hasPrevious() { return !isFirst(); }
    }
}
