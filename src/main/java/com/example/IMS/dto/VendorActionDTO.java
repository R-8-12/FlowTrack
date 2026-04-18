package com.example.IMS.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** Request body for a vendor approving or rejecting a connection request. */
public class VendorActionDTO {

    @NotNull(message = "Connection ID is required")
    private Long connectionId;

    /** true = approve, false = reject */
    @NotNull(message = "Approve flag is required")
    private Boolean approve;

    @Size(max = 500, message = "Note must be 500 characters or fewer")
    private String note;

    public Long getConnectionId() { return connectionId; }
    public void setConnectionId(Long connectionId) { this.connectionId = connectionId; }

    public Boolean getApprove() { return approve; }
    public void setApprove(Boolean approve) { this.approve = approve; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
