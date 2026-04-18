package com.example.IMS.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** Request body for a retailer initiating a connection request. */
public class ConnectionRequestDTO {

    @NotNull(message = "Vendor profile ID is required")
    private Long vendorProfileId;

    @Size(max = 500, message = "Message must be 500 characters or fewer")
    private String message;

    public Long getVendorProfileId() { return vendorProfileId; }
    public void setVendorProfileId(Long vendorProfileId) { this.vendorProfileId = vendorProfileId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
