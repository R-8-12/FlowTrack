package com.example.IMS.model.enums;

/**
 * Badge types for highlighting vendor strengths in search results
 * Each badge has a display name and color code for UI rendering
 */
public enum Badge {
    BEST_PRICE("Best Price", "#28a745"),
    FAST_DELIVERY("Fast Delivery", "#007bff"),
    HIGH_RELIABILITY("High Reliability", "#ffc107"),
    NONE("", "");
    
    private final String displayName;
    private final String colorCode;
    
    Badge(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColorCode() {
        return colorCode;
    }
}
