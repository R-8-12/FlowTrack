package com.example.IMS.controller;

import com.example.IMS.model.BusinessProfile;
import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.model.User;
import com.example.IMS.repository.BusinessProfileRepository;
import com.example.IMS.repository.ProcurementOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/orders")
@PreAuthorize("hasAuthority('ROLE_VENDOR')")
public class VendorOrderController {

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private ProcurementOrderRepository procurementOrderRepository;

    /**
     * List all procurement orders for this vendor, looked up via their BusinessProfile.
     * Falls back gracefully when no business profile exists yet.
     */
    @GetMapping
    public String listVendorOrders(Model model) {
        User user = currentUser();
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(user.getId());

        if (profiles.isEmpty()) {
            model.addAttribute("vendorLinked", false);
            model.addAttribute("orders", Collections.emptyList());
            model.addAttribute("user", user);
            return "orders/vendor-orders";
        }

        // Use the legacy vendor_id FK via the first profile's linked Vendor record.
        // ProcurementOrder.vendor references the legacy Vendor entity; we fetch by
        // matching the vendor email to the user's email as the bridge until full migration.
        // For now surface all orders where the retailer has connected to this vendor profile.
        List<ProcurementOrder> orders = procurementOrderRepository
                .findByVendorProfileId(profiles.get(0).getId());

        model.addAttribute("vendorLinked", true);
        model.addAttribute("orders", orders);
        model.addAttribute("user", user);
        model.addAttribute("vendorProfile", profiles.get(0));
        return "orders/vendor-orders";
    }

    /**
     * Accept or reject an incoming order, or mark it as supplied.
     */
    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Long orderId,
                                    @RequestParam("status") String status,
                                    @RequestParam(value = "notes", required = false) String notes,
                                    RedirectAttributes redirectAttributes) {
        User user = currentUser();
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(user.getId());

        if (profiles.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No business profile found. Please complete onboarding first.");
            return "redirect:/orders";
        }

        try {
            ProcurementOrder order = procurementOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Security: only the owning vendor profile can update this order
            if (order.getVendorProfile() == null ||
                    !order.getVendorProfile().getId().equals(profiles.get(0).getId())) {
                throw new RuntimeException("You are not authorised to update this order");
            }

            if (order.getStatus() == ProcurementOrderStatus.REJECTED ||
                    order.getStatus() == ProcurementOrderStatus.SUPPLIED) {
                throw new RuntimeException("This order is already closed");
            }

            ProcurementOrderStatus newStatus = ProcurementOrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            if (notes != null && !notes.isBlank()) {
                order.setVendorNotes(notes.trim());
            }
            if (newStatus == ProcurementOrderStatus.SUPPLIED) {
                order.setSuppliedAt(LocalDateTime.now());
            }
            procurementOrderRepository.save(order);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus + ".");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status: " + status);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/orders";
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
