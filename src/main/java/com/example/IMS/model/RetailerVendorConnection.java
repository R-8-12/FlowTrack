package com.example.IMS.model;

import com.example.IMS.model.enums.ConnectionStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an explicit connection between a retailer user and a vendor's BusinessProfile.
 *
 * <p>Lifecycle: REQUESTED → CONNECTED (vendor approves) or REJECTED (vendor rejects).
 * A CONNECTED connection can be BLOCKED by either party.
 *
 * <p>Uniqueness: one active connection record per (retailer_user_id, vendor_profile_id) pair.
 */
@Entity
@Table(
    name = "retailer_vendor_connections",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_retailer_vendor",
        columnNames = {"retailer_user_id", "vendor_profile_id"}
    )
)
public class RetailerVendorConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Retailer who initiated the connection request */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "retailer_user_id", nullable = false)
    private User retailer;

    /** Vendor's BusinessProfile being connected to */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_profile_id", nullable = false)
    private BusinessProfile vendorProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConnectionStatus status = ConnectionStatus.REQUESTED;

    @Column(name = "retailer_message", length = 500)
    private String retailerMessage;

    @Column(name = "vendor_response_note", length = 500)
    private String vendorResponseNote;

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

    public User getRetailer() { return retailer; }
    public void setRetailer(User retailer) { this.retailer = retailer; }

    public BusinessProfile getVendorProfile() { return vendorProfile; }
    public void setVendorProfile(BusinessProfile vendorProfile) { this.vendorProfile = vendorProfile; }

    public ConnectionStatus getStatus() { return status; }
    public void setStatus(ConnectionStatus status) { this.status = status; }

    public String getRetailerMessage() { return retailerMessage; }
    public void setRetailerMessage(String retailerMessage) { this.retailerMessage = retailerMessage; }

    public String getVendorResponseNote() { return vendorResponseNote; }
    public void setVendorResponseNote(String vendorResponseNote) { this.vendorResponseNote = vendorResponseNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
