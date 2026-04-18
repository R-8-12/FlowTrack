package com.example.IMS.controller;

import com.example.IMS.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for vendor-retailer network management.
 * 
 * <p>Handles viewing connected retailers, connection requests, and network analytics.
 * All endpoints require ROLE_VENDOR authority.
 * 
 * <p>Phase 2 implementation: Placeholder view with navigation back to dashboard.
 * Full retailer network functionality to be implemented in Phase 3.
 */
@Controller
@RequestMapping("/vendor")
@PreAuthorize("hasAuthority('ROLE_VENDOR')")
public class VendorRetailerNetworkController {
    
    /**
     * Display retailer network page showing connected retailers and connection requests.
     * 
     * @param model the Spring MVC model
     * @return view name for retailer network
     */
    @GetMapping("/retailers")
    public String viewRetailerNetwork(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Retailer Network");
        model.addAttribute("message", "Retailer network management coming soon. You'll be able to view connected retailers, approve connection requests, and see network analytics.");
        return "vendor/retailer-network";
    }
    
    /**
     * Get current authenticated user from security context.
     * 
     * @return the authenticated User object
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
