package com.example.IMS.controller;

import com.example.IMS.model.Item;
import com.example.IMS.model.User;
import com.example.IMS.model.Vendor;
import com.example.IMS.repository.IItemRepository;
import com.example.IMS.repository.IVendorRepository;
import com.example.IMS.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/vendors")
public class VendorController {

    private static final Logger logger = LoggerFactory.getLogger(VendorController.class);

    @Autowired
    private IVendorRepository vendorRepository;

    @Autowired
    private IItemRepository itemRepository;

    @Autowired
    private EmailService emailService;

    // List all vendors - role-aware redirect
    @GetMapping
    public String listVendors(Model model) {
        if (hasRole("ROLE_RETAILER")) {
            logger.info("Retailer accessing /vendors - redirecting to /retailer/vendor-search");
            return "redirect:/retailer/vendor-search";
        }
        // Platform admin continues to legacy vendor master
        model.addAttribute("vendors", vendorRepository.findAll());
        return "vendor_list";
    }

    // Show form to add vendor - role-aware redirect
    @GetMapping("/add")
    public String showAddVendorForm(Model model) {
        if (hasRole("ROLE_RETAILER")) {
            logger.info("Retailer accessing /vendors/add - redirecting to /retailer/vendor-search");
            return "redirect:/retailer/vendor-search";
        }
        // Platform admin continues to legacy vendor form
        model.addAttribute("vendor", new Vendor());
        return "vendor_form";
    }
    
    /**
     * Check if current authenticated user has the specified role.
     */
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    // Save vendor
    @PostMapping("/save")
    public String saveVendor(@ModelAttribute Vendor vendor, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        vendorRepository.save(vendor);

        // E-19: welcome email to newly added vendor
        try {
            if (vendor.getEmail() != null && !vendor.getEmail().isBlank()) {
                emailService.sendVendorWelcomeEmail(vendor.getEmail(), vendor.getName());
            }
        } catch (Exception mailEx) {
            logger.warn("E-19 vendor welcome email failed for {}: {}", vendor.getEmail(), mailEx.getMessage());
        }

        return "redirect:/vendors";
    }

    
    // Delete vendor
    @GetMapping("/delete/{id}")
    public String deleteVendor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check if vendor has associated items
            List<Item> items = itemRepository.findAll();
            long itemCount = items.stream()
                    .filter(item -> item.getVendor() != null && item.getVendor().getId() == id)
                    .count();
            
            if (itemCount > 0) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete vendor. There are " + itemCount + " item(s) associated with this vendor. Please delete or reassign those items first.");
                return "redirect:/vendors";
            }
            
            vendorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Vendor deleted successfully!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Cannot delete vendor. This vendor has associated items. Please delete or reassign those items first.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred while deleting the vendor: " + e.getMessage());
        }
        return "redirect:/vendors";
    }
}
