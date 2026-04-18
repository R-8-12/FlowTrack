package com.example.IMS.controller;

import com.example.IMS.dto.PaymentResponse;
import com.example.IMS.dto.PaymentVerificationRequest;
import com.example.IMS.model.User;
import com.example.IMS.service.ProcurementPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for procurement order payment flow.
 *
 * <p>Retailer initiates payment for an ACCEPTED order, then verifies the
 * Razorpay signature after checkout completes.
 */
@RestController
@RequestMapping("/api/retailer/payments")
@PreAuthorize("hasAuthority('ROLE_RETAILER')")
public class ProcurementPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(ProcurementPaymentController.class);

    @Autowired
    private ProcurementPaymentService procurementPaymentService;

    /**
     * POST /api/retailer/payments/initiate?orderId=X
     *
     * <p>Creates a Razorpay order for the given procurement order and returns
     * the checkout parameters needed by the frontend.
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestParam Long orderId) {
        try {
            User retailer = currentUser();
            PaymentResponse response = procurementPaymentService.initiatePayment(orderId, retailer);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Payment initiation rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Payment initiation error for orderId={}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Payment gateway error"));
        }
    }

    /**
     * POST /api/retailer/payments/verify
     *
     * <p>Verifies the Razorpay signature after the user completes checkout.
     * On success the payment record transitions to PAID.
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            boolean success = procurementPaymentService.verifyAndCapture(request);
            if (success) {
                return ResponseEntity.ok(Map.of("status", "PAID", "message", "Payment verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", "Signature verification failed"));
            }
        } catch (Exception e) {
            logger.error("Payment verification error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Verification error"));
        }
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
