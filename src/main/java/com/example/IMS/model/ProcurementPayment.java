package com.example.IMS.model;

import com.example.IMS.model.enums.PaymentStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks a Razorpay payment linked to a ProcurementOrder.
 *
 * <p>One order can have at most one active payment record.
 * On failure the record is updated to FAILED and a new attempt creates a new record.
 *
 * <p>Status lifecycle:
 * <pre>
 *   PENDING ──► AUTHORIZED ──► PAID
 *           └──► FAILED
 *   PAID    ──► REFUNDED
 * </pre>
 */
@Entity
@Table(name = "procurement_payments")
public class ProcurementPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The procurement order this payment belongs to */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procurement_order_id", nullable = false)
    private ProcurementOrder procurementOrder;

    /** Retailer who initiated the payment */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "retailer_user_id", nullable = false)
    private User retailer;

    /** Razorpay order ID (rzp_order_xxx) — set when order is created */
    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    /** Razorpay payment ID (pay_xxx) — set after successful capture */
    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    /** Razorpay signature — stored for audit/replay prevention */
    @Column(name = "razorpay_signature", length = 256)
    private String razorpaySignature;

    /** Amount in INR (not paise) */
    @Column(name = "amount", nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Human-readable failure reason, populated on FAILED status */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

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

    public ProcurementOrder getProcurementOrder() { return procurementOrder; }
    public void setProcurementOrder(ProcurementOrder procurementOrder) { this.procurementOrder = procurementOrder; }

    public User getRetailer() { return retailer; }
    public void setRetailer(User retailer) { this.retailer = retailer; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
