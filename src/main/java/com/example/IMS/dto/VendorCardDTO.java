package com.example.IMS.dto;

import com.example.IMS.model.enums.Badge;
import java.math.BigDecimal;

/**
 * DTO representing a vendor card in search results
 * Contains all information needed for display and decision-making
 * Uses Builder pattern for easier construction
 */
public class VendorCardDTO {
    
    private Long vendorId;                    // BusinessProfile ID
    private String vendorName;                // legalBusinessName
    private BigDecimal pricePerUnit;          // From Item.price
    private Integer availableQuantity;        // From Item.quantity
    private Integer deliveryDays;             // From vendor metadata
    private Double reliabilityScore;          // Computed from order history (0.0-1.0)
    private Double rating;                    // From review aggregation (0.0-5.0)
    private Boolean verified;                 // Always true (filtered)
    private String location;                  // "City, State" format
    private Boolean previouslyOrdered;        // Order history flag
    private Badge badge;                      // BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY, NONE
    
    // Constructors
    public VendorCardDTO() {}
    
    // Getters and setters
    public Long getVendorId() {
        return vendorId;
    }
    
    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
    
    public String getVendorName() {
        return vendorName;
    }
    
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    
    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }
    
    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    public Integer getDeliveryDays() {
        return deliveryDays;
    }
    
    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }
    
    public Double getReliabilityScore() {
        return reliabilityScore;
    }
    
    public void setReliabilityScore(Double reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Boolean getPreviouslyOrdered() {
        return previouslyOrdered;
    }
    
    public void setPreviouslyOrdered(Boolean previouslyOrdered) {
        this.previouslyOrdered = previouslyOrdered;
    }
    
    public Badge getBadge() {
        return badge;
    }
    
    public void setBadge(Badge badge) {
        this.badge = badge;
    }
    
    /**
     * Format location as "City, State"
     * @return formatted location string or default message
     */
    public String getFormattedLocation() {
        return location != null ? location : "Location not specified";
    }
    
    /**
     * Get reliability as percentage for display
     * @return reliability score as integer percentage (0-100)
     */
    public int getReliabilityPercentage() {
        return reliabilityScore != null ? (int)(reliabilityScore * 100) : 0;
    }
    
    /**
     * Builder pattern for easier construction of VendorCardDTO
     */
    public static class Builder {
        private VendorCardDTO dto = new VendorCardDTO();
        
        public Builder vendorId(Long vendorId) {
            dto.vendorId = vendorId;
            return this;
        }
        
        public Builder vendorName(String vendorName) {
            dto.vendorName = vendorName;
            return this;
        }
        
        public Builder pricePerUnit(BigDecimal pricePerUnit) {
            dto.pricePerUnit = pricePerUnit;
            return this;
        }
        
        public Builder availableQuantity(Integer availableQuantity) {
            dto.availableQuantity = availableQuantity;
            return this;
        }
        
        public Builder deliveryDays(Integer deliveryDays) {
            dto.deliveryDays = deliveryDays;
            return this;
        }
        
        public Builder reliabilityScore(Double reliabilityScore) {
            dto.reliabilityScore = reliabilityScore;
            return this;
        }
        
        public Builder rating(Double rating) {
            dto.rating = rating;
            return this;
        }
        
        public Builder verified(Boolean verified) {
            dto.verified = verified;
            return this;
        }
        
        public Builder location(String location) {
            dto.location = location;
            return this;
        }
        
        public Builder previouslyOrdered(Boolean previouslyOrdered) {
            dto.previouslyOrdered = previouslyOrdered;
            return this;
        }
        
        public Builder badge(Badge badge) {
            dto.badge = badge;
            return this;
        }
        
        public VendorCardDTO build() {
            return dto;
        }
    }
}
