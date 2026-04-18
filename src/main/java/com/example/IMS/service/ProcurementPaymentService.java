package com.example.IMS.service;

import com.example.IMS.dto.PaymentRequest;
import com.example.IMS.dto.PaymentResponse;
import com.example.IMS.dto.PaymentVerificationRequest;
import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.ProcurementPayment;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.model.User;
import com.example.IMS.model.enums.PaymentStatus;
import com.example.IMS.repository.ProcurementOrderRepository;
import com.example.IMS.repository.ProcurementPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Manages the payment lifecycle for procurement orders.
 *
 * <p>Integrates with the existing {@link PaymentService} (Razorpay order creation /
 * signature verification) and persists payment records in {@code procurement_payments}.
 *
 * <p>Fulfillment policy: a vendor can only mark an order SUPPLIED after the linked
 * payment record is in PAID status.
 */
@Service
@Transactional
public class ProcurementPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(ProcurementPaymentService.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProcurementPaymentRepository paymentRepository;

    @Autowired
    private ProcurementOrderRepository orderRepository;

    // ── Initiate payment ──────────────────────────────────────────────────────

    /**
     * Create a Razorpay order for a procurement order and persist a PENDING payment record.
     *
     * @param orderId   the procurement order ID
     * @param retailer  the authenticated retailer
     * @return Razorpay order details (orderId, amount, key) for the frontend checkout
     */
    public PaymentResponse initiatePayment(Long orderId, User retailer) {
        ProcurementOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Only ACCEPTED orders can be paid
        if (order.getStatus() != ProcurementOrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order must be ACCEPTED before payment. Current status: " + order.getStatus());
        }

        // Prevent duplicate payment initiation
        Optional<ProcurementPayment> existingPaid = paymentRepository.findPaidPaymentForOrder(orderId);
        if (existingPaid.isPresent()) {
            throw new IllegalStateException("Order " + orderId + " is already paid.");
        }

        double amount = order.getExpectedUnitPrice() * order.getRequestedQuantity();

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);
        paymentRequest.setReceipt("ORDER_" + orderId + "_" + System.currentTimeMillis());
        paymentRequest.setDescription("Payment for order #" + orderId + " — " + order.getItemName());
        paymentRequest.setCustomerName(retailer.getFirstName() + " " + retailer.getLastName());
        paymentRequest.setCustomerEmail(retailer.getEmail());

        PaymentResponse response = paymentService.createOrder(paymentRequest);

        if ("failed".equals(response.getStatus())) {
            logger.error("Razorpay order creation failed for orderId={}: {}", orderId, response.getMessage());
            throw new RuntimeException("Payment gateway error: " + response.getMessage());
        }

        // Persist PENDING payment record
        ProcurementPayment payment = new ProcurementPayment();
        payment.setProcurementOrder(order);
        payment.setRetailer(retailer);
        payment.setRazorpayOrderId(response.getOrderId());
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        logger.info("Payment initiated: orderId={}, razorpayOrderId={}, amount={}", orderId, response.getOrderId(), amount);
        return response;
    }

    // ── Verify and capture ────────────────────────────────────────────────────

    /**
     * Verify Razorpay signature and transition payment to PAID.
     * Also transitions the procurement order to SUPPLIED if payment is confirmed.
     *
     * @param verificationRequest contains razorpayOrderId, razorpayPaymentId, razorpaySignature
     * @return true if verification succeeded
     */
    public boolean verifyAndCapture(PaymentVerificationRequest verificationRequest) {
        boolean valid = paymentService.verifyPaymentSignature(verificationRequest);

        ProcurementPayment payment = paymentRepository
                .findByRazorpayOrderId(verificationRequest.getRazorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payment record for Razorpay order: " + verificationRequest.getRazorpayOrderId()));

        if (valid) {
            payment.setRazorpayPaymentId(verificationRequest.getRazorpayPaymentId());
            payment.setRazorpaySignature(verificationRequest.getRazorpaySignature());
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
            logger.info("Payment PAID: procurementOrderId={}, paymentId={}", payment.getProcurementOrder().getId(), verificationRequest.getRazorpayPaymentId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);
            logger.warn("Payment FAILED (bad signature): procurementOrderId={}", payment.getProcurementOrder().getId());
        }

        return valid;
    }

    // ── Webhook-driven update ─────────────────────────────────────────────────

    /**
     * Called by the webhook controller when Razorpay sends a payment.captured event.
     * Idempotent — safe to call multiple times for the same payment.
     */
    public void handlePaymentCaptured(String razorpayPaymentId, String razorpayOrderId) {
        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            if (payment.getStatus() != PaymentStatus.PAID) {
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);
                logger.info("Webhook: payment PAID via captured event — orderId={}", razorpayOrderId);
            }
        });
    }

    /**
     * Called by the webhook controller when Razorpay sends a payment.failed event.
     */
    public void handlePaymentFailed(String razorpayOrderId, String errorDescription) {
        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.AUTHORIZED) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(errorDescription);
                paymentRepository.save(payment);
                logger.info("Webhook: payment FAILED — orderId={}, reason={}", razorpayOrderId, errorDescription);
            }
        });
    }

    // ── Fulfillment guard ─────────────────────────────────────────────────────

    /**
     * Returns true if the procurement order has a confirmed PAID payment.
     * Vendors must check this before marking an order as SUPPLIED.
     */
    @Transactional(readOnly = true)
    public boolean isOrderPaid(Long orderId) {
        return paymentRepository.findPaidPaymentForOrder(orderId).isPresent();
    }
}
