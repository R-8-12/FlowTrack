package com.example.IMS.model.enums;

/**
 * Lifecycle states for a ProcurementPayment.
 *
 * <p>Transitions:
 * <pre>
 *   PENDING ──► AUTHORIZED ──► PAID
 *           └──► FAILED
 *   PAID    ──► REFUNDED
 * </pre>
 */
public enum PaymentStatus {
    /** Payment order created; awaiting user action in Razorpay checkout. */
    PENDING,
    /** Razorpay has authorised the payment; capture pending. */
    AUTHORIZED,
    /** Payment captured and confirmed via webhook or signature verification. */
    PAID,
    /** Payment attempt failed (declined, timeout, etc.). */
    FAILED,
    /** Payment was refunded after successful capture. */
    REFUNDED
}
