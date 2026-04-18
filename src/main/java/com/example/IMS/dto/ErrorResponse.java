package com.example.IMS.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response DTO for API error handling
 * Used by global exception handler to provide consistent error responses
 */
public class ErrorResponse {
    
    private int status;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;
    
    /**
     * Default constructor
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * Constructor with status and message
     */
    public ErrorResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
    
    /**
     * Constructor with status, message, and field errors
     */
    public ErrorResponse(int status, String message, Map<String, String> fieldErrors) {
        this();
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }
    
    // Getters and setters
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Add a field error
     */
    public void addFieldError(String field, String error) {
        this.fieldErrors.put(field, error);
    }
}
