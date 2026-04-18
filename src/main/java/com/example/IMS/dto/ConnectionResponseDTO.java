package com.example.IMS.dto;

import com.example.IMS.model.RetailerVendorConnection;
import com.example.IMS.model.enums.ConnectionStatus;

import java.time.LocalDateTime;

/** Read-only view of a RetailerVendorConnection. */
public class ConnectionResponseDTO {

    private Long connectionId;
    private Long retailerUserId;
    private String retailerName;
    private Long vendorProfileId;
    private String vendorName;
    private ConnectionStatus status;
    private String retailerMessage;
    private String vendorResponseNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConnectionResponseDTO from(RetailerVendorConnection c) {
        ConnectionResponseDTO dto = new ConnectionResponseDTO();
        dto.connectionId       = c.getId();
        dto.retailerUserId     = c.getRetailer().getId();
        dto.retailerName       = c.getRetailer().getFirstName() + " " + c.getRetailer().getLastName();
        dto.vendorProfileId    = c.getVendorProfile().getId();
        dto.vendorName         = c.getVendorProfile().getLegalBusinessName();
        dto.status             = c.getStatus();
        dto.retailerMessage    = c.getRetailerMessage();
        dto.vendorResponseNote = c.getVendorResponseNote();
        dto.createdAt          = c.getCreatedAt();
        dto.updatedAt          = c.getUpdatedAt();
        return dto;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long getConnectionId() { return connectionId; }
    public Long getRetailerUserId() { return retailerUserId; }
    public String getRetailerName() { return retailerName; }
    public Long getVendorProfileId() { return vendorProfileId; }
    public String getVendorName() { return vendorName; }
    public ConnectionStatus getStatus() { return status; }
    public String getRetailerMessage() { return retailerMessage; }
    public String getVendorResponseNote() { return vendorResponseNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
