package com.example.IMS.dto;

import java.util.List;

/**
 * Response DTO containing paginated vendor search results
 * Includes pagination metadata for navigation
 */
public class VendorSearchResponse {
    
    private List<VendorCardDTO> vendors;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    
    // Constructors
    public VendorSearchResponse() {}
    
    public VendorSearchResponse(List<VendorCardDTO> vendors, 
                               int currentPage, 
                               int totalPages, 
                               long totalElements, 
                               int pageSize) {
        this.vendors = vendors;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
    }
    
    // Getters and setters
    public List<VendorCardDTO> getVendors() {
        return vendors;
    }
    
    public void setVendors(List<VendorCardDTO> vendors) {
        this.vendors = vendors;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    /**
     * Check if there is a next page available
     * @return true if current page is not the last page
     */
    public boolean hasNext() {
        return currentPage < totalPages - 1;
    }
    
    /**
     * Check if there is a previous page available
     * @return true if current page is not the first page
     */
    public boolean hasPrevious() {
        return currentPage > 0;
    }
}
