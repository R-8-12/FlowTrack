package com.example.IMS.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * A product listed by a vendor, visible to connected retailers via vendor search.
 */
@Entity
@Table(name = "vendor_products")
public class VendorProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_profile_id", nullable = false)
    private BusinessProfile vendorProfile;

    @NotBlank(message = "Product name is required")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Category is required")
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Price must be positive")
    @Column(name = "price_per_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Quantity must be positive")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "minimum_order_quantity")
    private Integer minimumOrderQuantity = 1;

    @NotNull(message = "Delivery days is required")
    @Min(value = 1, message = "Delivery days must be at least 1")
    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;

    @Column(name = "unit", length = 50)
    private String unit = "pieces";

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public BusinessProfile getVendorProfile() { return vendorProfile; }
    public void setVendorProfile(BusinessProfile vendorProfile) { this.vendorProfile = vendorProfile; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getMinimumOrderQuantity() { return minimumOrderQuantity; }
    public void setMinimumOrderQuantity(Integer minimumOrderQuantity) { this.minimumOrderQuantity = minimumOrderQuantity; }

    public Integer getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(Integer deliveryDays) { this.deliveryDays = deliveryDays; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
