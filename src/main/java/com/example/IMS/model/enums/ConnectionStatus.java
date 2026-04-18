package com.example.IMS.model.enums;

/**
 * Lifecycle states for a RetailerVendorConnection.
 *
 * <p>Transitions:
 * <pre>
 *   REQUESTED ──► CONNECTED  (vendor approves)
 *   REQUESTED ──► REJECTED   (vendor rejects)
 *   CONNECTED ──► BLOCKED    (either party blocks)
 *   BLOCKED   ──► CONNECTED  (unblock, by the party who blocked)
 * </pre>
 */
public enum ConnectionStatus {
    /** Retailer has sent a connection request; awaiting vendor action. */
    REQUESTED,
    /** Vendor approved the request; both parties are connected. */
    CONNECTED,
    /** Vendor declined the request. */
    REJECTED,
    /** Connection was blocked by one of the parties. */
    BLOCKED
}
