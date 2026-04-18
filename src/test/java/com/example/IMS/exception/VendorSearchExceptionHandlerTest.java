package com.example.IMS.exception;

import com.example.IMS.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VendorSearchExceptionHandler
 * Tests all exception handling scenarios
 */
class VendorSearchExceptionHandlerTest {
    
    private VendorSearchExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new VendorSearchExceptionHandler();
    }
    
    @Test
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Invalid price range: minPrice cannot exceed maxPrice");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Invalid price range: minPrice cannot exceed maxPrice", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
    }
    
    @Test
    void testHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("User does not have ROLE_RETAILER authority");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Access denied"));
        assertTrue(errorResponse.getMessage().contains("User does not have ROLE_RETAILER authority"));
        assertNotNull(errorResponse.getTimestamp());
    }
    
    @Test
    void testHandleEntityNotFoundException() {
        // Arrange
        EntityNotFoundException ex = new EntityNotFoundException("Business profile not found: 123");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEntityNotFoundException(ex);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Business profile not found: 123", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
    }
    
    @Test
    void testHandleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected database error");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("An unexpected error occurred. Please try again later.", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
    }
    
    @Test
    void testHandleGenericException_WithNullPointerException() {
        // Arrange
        NullPointerException ex = new NullPointerException("Null value encountered");
        
        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        // Generic message should not expose internal details
        assertEquals("An unexpected error occurred. Please try again later.", errorResponse.getMessage());
    }
    
    @Test
    void testErrorResponse_AddFieldError() {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse(400, "Validation failed");
        
        // Act
        errorResponse.addFieldError("minPrice", "must be non-negative");
        errorResponse.addFieldError("page", "must be 0 or greater");
        
        // Assert
        assertEquals(2, errorResponse.getFieldErrors().size());
        assertEquals("must be non-negative", errorResponse.getFieldErrors().get("minPrice"));
        assertEquals("must be 0 or greater", errorResponse.getFieldErrors().get("page"));
    }
    
    @Test
    void testErrorResponse_DefaultConstructor() {
        // Act
        ErrorResponse errorResponse = new ErrorResponse();
        
        // Assert
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getFieldErrors());
        assertEquals(0, errorResponse.getFieldErrors().size());
    }
    
    @Test
    void testErrorResponse_ConstructorWithStatusAndMessage() {
        // Act
        ErrorResponse errorResponse = new ErrorResponse(404, "Resource not found");
        
        // Assert
        assertEquals(404, errorResponse.getStatus());
        assertEquals("Resource not found", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getFieldErrors());
    }
}
