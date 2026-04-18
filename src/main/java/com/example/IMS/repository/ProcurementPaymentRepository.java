package com.example.IMS.repository;

import com.example.IMS.model.ProcurementPayment;
import com.example.IMS.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcurementPaymentRepository extends JpaRepository<ProcurementPayment, Long> {

    /** Find payment by Razorpay order ID (used during webhook processing). */
    Optional<ProcurementPayment> findByRazorpayOrderId(String razorpayOrderId);

    /** Find payment by Razorpay payment ID (used for verification). */
    Optional<ProcurementPayment> findByRazorpayPaymentId(String razorpayPaymentId);

    /** All payments for a procurement order. */
    List<ProcurementPayment> findByProcurementOrderId(Long procurementOrderId);

    /** Latest PAID payment for a procurement order (for fulfillment checks). */
    @Query("SELECT p FROM ProcurementPayment p WHERE p.procurementOrder.id = :orderId AND p.status = 'PAID' ORDER BY p.updatedAt DESC")
    Optional<ProcurementPayment> findPaidPaymentForOrder(@Param("orderId") Long orderId);

    /** All payments by a retailer, newest first. */
    @Query("SELECT p FROM ProcurementPayment p WHERE p.retailer.id = :retailerId ORDER BY p.createdAt DESC")
    List<ProcurementPayment> findByRetailerId(@Param("retailerId") Long retailerId);

    /** Payments by retailer filtered by status. */
    @Query("SELECT p FROM ProcurementPayment p WHERE p.retailer.id = :retailerId AND p.status = :status ORDER BY p.createdAt DESC")
    List<ProcurementPayment> findByRetailerIdAndStatus(
            @Param("retailerId") Long retailerId,
            @Param("status") PaymentStatus status);

    /** Total paid amount for a retailer (for analytics). */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM ProcurementPayment p WHERE p.retailer.id = :retailerId AND p.status = 'PAID'")
    double sumPaidAmountByRetailerId(@Param("retailerId") Long retailerId);

    /** Count of PAID payments for a vendor's orders (for vendor analytics). */
    @Query("SELECT COUNT(p) FROM ProcurementPayment p WHERE p.procurementOrder.vendor.id = :vendorId AND p.status = 'PAID'")
    long countPaidOrdersByVendorId(@Param("vendorId") Long vendorId);

    /** Total revenue for a vendor from PAID payments. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM ProcurementPayment p WHERE p.procurementOrder.vendor.id = :vendorId AND p.status = 'PAID'")
    double sumPaidAmountByVendorId(@Param("vendorId") Long vendorId);
}
