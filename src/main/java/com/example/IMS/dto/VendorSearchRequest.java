package com.example.IMS.dto;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for vendor search with filters and pagination
 * Validates all input parameters before processing
 */
public class VendorSearchRequest {
    
    // Search query
    private String query;  // Product name or vendor name (optional)
    
    // Price filters
    @DecimalMin(value = "0.0", message = "minPrice must be non-negative")
    private BigDecimal minPrice;
    
    @DecimalMin(value = "0.0", message = "maxPrice must be non-negative")
    private BigDecimal maxPrice;
    
    // Delivery filter
    @Min(value = 1, message = "maxDeliveryDays must be at least 1")
    private Integer maxDeliveryDays;
    
    // Stock filter
    @Min(value = 1, message = "minQuantity must be at least 1")
    private Integer minQuantity;
    
    // Distance filter
    @DecimalMin(value = "0.0", message = "maxDistanceKm must be non-negative")
    private BigDecimal maxDistanceKm;
    
    // Verification filter (always true, but explicit)
    private Boolean verifiedOnly = true;
    
    // Sorting
    @Pattern(regexp = "price|delivery|rating|relevance", 
             message = "sortBy must be one of: price, delivery, rating, relevance")
    private String sortBy = "relevance";
    
    @Pattern(regexp = "asc|desc", 
             message = "sortDirection must be one of: asc, desc")
    private String sortDirection;  // Default depends on sortBy
    
    // Pagination
    @Min(value = 0, message = "page must be 0 or greater")
    private Integer page = 0;
    
    @Min(value = 1, message = "size must be at least 1")
    @Max(value = 50, message = "size cannot exceed 50")
    private Integer size = 20;
    
    // Constructors
    public VendorSearchRequest() {}
    
    // Getters and setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public BigDecimal getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }
    
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public Integer getMaxDeliveryDays() {
        return maxDeliveryDays;
    }
    
    public void setMaxDeliveryDays(Integer maxDeliveryDays) {
        this.maxDeliveryDays = maxDeliveryDays;
    }
    
    public Integer getMinQuantity() {
        return minQuantity;
    }
    
    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }
    
    public BigDecimal getMaxDistanceKm() {
        return maxDistanceKm;
    }
    
    public void setMaxDistanceKm(BigDecimal maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }
    
    public Boolean getVerifiedOnly() {
        return verifiedOnly;
    }
    
    public void setVerifiedOnly(Boolean verifiedOnly) {
        this.verifiedOnly = verifiedOnly;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    /**
     * Get effective sort direction based on sortBy
     * Default: asc for price/delivery, desc for rating/relevance
     */
    public String getEffectiveSortDirection() {
        if (sortDirection != null) {
            return sortDirection;
        }
        return ("price".equals(sortBy) || "delivery".equals(sortBy)) ? "asc" : "desc";
    }
    
    /**
     * Validate price range consistency
     * Throws IllegalArgumentException if minPrice > maxPrice
     */
    public void validatePriceRange() {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                "Invalid price range: minPrice cannot exceed maxPrice");
        }
    }
}
