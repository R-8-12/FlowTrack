package com.example.IMS.controller;

import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.model.User;
import com.example.IMS.model.Vendor;
import com.example.IMS.service.ProcurementOrderService;
import com.example.IMS.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Collections;
import java.util.List;

@Controller
public class VendorOrderController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private ProcurementOrderService procurementOrderService;

    @GetMapping("/orders")
    public String listVendorOrders(Model model) {
        User user = currentUser();
        Vendor vendor = vendorService.getVendorByEmail(user.getEmail());

        if (vendor == null) {
            model.addAttribute("vendorLinked", false);
            model.addAttribute("orders", Collections.emptyList());
            return "orders/vendor-orders";
        }

        List<ProcurementOrder> orders = procurementOrderService.getVendorOrders(vendor);
        model.addAttribute("vendorLinked", true);
        model.addAttribute("orders", orders);
        return "orders/vendor-orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Long orderId,
                                    @RequestParam("status") String status,
                                    @RequestParam(value = "notes", required = false) String notes,
                                    RedirectAttributes redirectAttributes) {
        User user = currentUser();
        Vendor vendor = vendorService.getVendorByEmail(user.getEmail());

        if (vendor == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vendor profile link not found. Ensure vendor email matches your login email.");
            return "redirect:/orders";
        }

        try {
            ProcurementOrderStatus newStatus = ProcurementOrderStatus.valueOf(status.toUpperCase());
            procurementOrderService.updateOrderStatusForVendor(orderId, vendor, newStatus, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status value: " + status);
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
