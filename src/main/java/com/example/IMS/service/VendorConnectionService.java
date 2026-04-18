package com.example.IMS.service;

import com.example.IMS.dto.ConnectionRequestDTO;
import com.example.IMS.dto.ConnectionResponseDTO;
import com.example.IMS.dto.VendorActionDTO;

import java.util.List;

/**
 * Service interface for retailer-vendor connection lifecycle management.
 */
public interface VendorConnectionService {

    /** Retailer requests a connection to a vendor profile. */
    ConnectionResponseDTO requestConnection(Long retailerUserId, ConnectionRequestDTO request);

    /** Vendor approves or rejects a pending connection request. */
    ConnectionResponseDTO respondToConnection(Long vendorUserId, VendorActionDTO action);

    /** Block an existing CONNECTED connection (either party). */
    ConnectionResponseDTO blockConnection(Long actingUserId, Long connectionId);

    /** Unblock a BLOCKED connection (only the party who blocked it). */
    ConnectionResponseDTO unblockConnection(Long actingUserId, Long connectionId);

    /** Get connection status between a retailer and a vendor profile. */
    ConnectionResponseDTO getConnectionStatus(Long retailerUserId, Long vendorProfileId);

    /** All connections for a retailer (all statuses). */
    List<ConnectionResponseDTO> getRetailerConnections(Long retailerUserId);

    /** Pending connection requests for a vendor profile. */
    List<ConnectionResponseDTO> getPendingRequestsForVendor(Long vendorUserId);

    /** Connected retailers for a vendor profile. */
    List<ConnectionResponseDTO> getConnectedRetailersForVendor(Long vendorUserId);
}
