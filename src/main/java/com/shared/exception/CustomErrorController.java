package com.shared.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Error Controller to handle /error endpoint
 * Provides consistent error responses without requiring authentication
 */
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            errorDetails.put("status", statusCode);
            errorDetails.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
            
            // Provide custom messages for common errors
            if (statusCode == 401) {
                errorDetails.put("message", "Authentication required. Please provide a valid JWT token.");
            } else if (statusCode == 403) {
                errorDetails.put("message", "Access denied. You don't have permission to access this resource.");
            } else if (statusCode == 404) {
                errorDetails.put("message", "Resource not found.");
            } else if (message != null) {
                errorDetails.put("message", message.toString());
            } else {
                errorDetails.put("message", "An error occurred");
            }
            
            return ResponseEntity.status(statusCode).body(errorDetails);
        }
        
        // Default error response
        errorDetails.put("status", 500);
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", "An unexpected error occurred");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }
}
