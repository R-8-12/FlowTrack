package com.example.IMS.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_orders")
public class ProcurementOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_user_id", nullable = false)
    private User retailer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "item_type_name", nullable = false, length = 100)
    private String itemTypeName;

    @Column(name = "requested_quantity", nullable = false)
    private int requestedQuantity;

    @Column(name = "expected_unit_price", nullable = false)
    private double expectedUnitPrice;

    @Column(name = "expected_fine_rate", nullable = false)
    private double expectedFineRate;

    @Column(name = "requested_invoice_number")
    private Long requestedInvoiceNumber;

    @Column(name = "retailer_notes", length = 1000)
    private String retailerNotes;

    @Column(name = "vendor_notes", length = 1000)
    private String vendorNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProcurementOrderStatus status = ProcurementOrderStatus.REQUESTED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "supplied_at")
    private LocalDateTime suppliedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRetailer() {
        return retailer;
    }

    public void setRetailer(User retailer) {
        this.retailer = retailer;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public double getExpectedUnitPrice() {
        return expectedUnitPrice;
    }

    public void setExpectedUnitPrice(double expectedUnitPrice) {
        this.expectedUnitPrice = expectedUnitPrice;
    }

    public double getExpectedFineRate() {
        return expectedFineRate;
    }

    public void setExpectedFineRate(double expectedFineRate) {
        this.expectedFineRate = expectedFineRate;
    }

    public Long getRequestedInvoiceNumber() {
        return requestedInvoiceNumber;
    }

    public void setRequestedInvoiceNumber(Long requestedInvoiceNumber) {
        this.requestedInvoiceNumber = requestedInvoiceNumber;
    }

    public String getRetailerNotes() {
        return retailerNotes;
    }

    public void setRetailerNotes(String retailerNotes) {
        this.retailerNotes = retailerNotes;
    }

    public String getVendorNotes() {
        return vendorNotes;
    }

    public void setVendorNotes(String vendorNotes) {
        this.vendorNotes = vendorNotes;
    }

    public ProcurementOrderStatus getStatus() {
        return status;
    }

    public void setStatus(ProcurementOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSuppliedAt() {
        return suppliedAt;
    }

    public void setSuppliedAt(LocalDateTime suppliedAt) {
        this.suppliedAt = suppliedAt;
    }
}
