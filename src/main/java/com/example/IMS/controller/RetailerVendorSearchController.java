package com.example.IMS.controller;

import com.example.IMS.dto.VendorSearchRequest;
import com.example.IMS.dto.VendorSearchResponse;
import com.example.IMS.model.User;
import com.example.IMS.service.VendorSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for vendor search operations.
 * 
 * <p>This controller exposes the vendor search API endpoint for retailers to discover
 * and evaluate verified vendors based on multiple criteria including product availability,
 * pricing, delivery time, and vendor reliability.
 * 
 * <p>All endpoints require ROLE_RETAILER authority and are secured at the controller level.
 * The authenticated user context is used for tenant isolation and personalization features.
 * 
 * <p>API Endpoint: GET /api/retailer/vendors/search
 * 
 * @see VendorSearchService
 * @see VendorSearchRequest
 * @see VendorSearchResponse
 */
@RestController
@RequestMapping("/api/retailer/vendors")
@PreAuthorize("hasAuthority('ROLE_RETAILER')")
public class RetailerVendorSearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(RetailerVendorSearchController.class);
    
    @Autowired
    private VendorSearchService vendorSearchService;
    
    /**
     * Search for vendors with filters and pagination.
     * 
     * <p>This endpoint accepts multiple query parameters for filtering and sorting:
     * <ul>
     *   <li>query: Product name or vendor name (case-insensitive, optional)</li>
     *   <li>minPrice: Minimum price filter (optional)</li>
     *   <li>maxPrice: Maximum price filter (optional)</li>
     *   <li>minQuantity: Minimum stock quantity (optional)</li>
     *   <li>maxDeliveryDays: Maximum delivery days (optional)</li>
     *   <li>maxDistanceKm: Maximum distance in km (optional)</li>
     *   <li>verifiedOnly: Only verified vendors (default: true)</li>
     *   <li>sortBy: Sort field (price|delivery|rating|relevance, default: relevance)</li>
     *   <li>sortDirection: Sort direction (asc|desc, default: depends on sortBy)</li>
     *   <li>page: Page number (default: 0)</li>
     *   <li>size: Page size (default: 20, max: 50)</li>
     * </ul>
     * 
     * <p>Example request:
     * <pre>
     * GET /api/retailer/vendors/search?query=laptop&minPrice=10000&maxPrice=50000&page=0&size=20
     * </pre>
     * 
     * @param request the search request with filters and pagination parameters
     * @return ResponseEntity containing VendorSearchResponse with paginated results
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchVendors(
            @Valid @ModelAttribute VendorSearchRequest request) {
        
        try {
            // Get authenticated user
            User user = getCurrentUser();
            
            logger.info("Vendor search request from user: {} with query: '{}'", 
                       user.getId(), request.getQuery());
            
            // Execute search
            VendorSearchResponse response = vendorSearchService.searchVendors(request, user.getId());
            
            // Log analytics
            logger.info("Search completed: {} results for user {}", 
                       response.getTotalElements(), user.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Handle validation errors (e.g., invalid price range)
            logger.warn("Invalid search request: {}", e.getMessage());
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            // Handle unexpected system errors
            logger.error("Error processing vendor search request", e);
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "An unexpected error occurred while processing your request");
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Exception handler for validation errors.
     * 
     * <p>Handles MethodArgumentNotValidException and BindException thrown when @Valid validation fails
     * on request parameters. Returns a map of field names to error messages.
     * 
     * @param ex the validation exception
     * @return ResponseEntity with 400 Bad Request and field error details
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, org.springframework.validation.BindException.class})
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            Exception ex) {
        
        Map<String, String> errors = new HashMap<>();
        
        if (ex instanceof MethodArgumentNotValidException) {
            ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
        } else if (ex instanceof org.springframework.validation.BindException) {
            ((org.springframework.validation.BindException) ex).getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
        }
        
        logger.warn("Validation errors in search request: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
    
    /**
     * Get current authenticated user from security context.
     * 
     * @return the authenticated User object
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
