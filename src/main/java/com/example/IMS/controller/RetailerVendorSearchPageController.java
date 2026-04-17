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
 * Controller for serving the vendor search UI page.
 * 
 * <p>This controller is separate from the REST API controller and is responsible
 * for serving the Thymeleaf HTML page that provides the vendor search interface.
 * The actual search functionality is handled by the REST API endpoint.
 * 
 * <p>All endpoints require ROLE_RETAILER authority and are secured at the controller level.
 * 
 * @see RetailerVendorSearchController for the REST API endpoint
 */
@Controller
@RequestMapping("/retailer")
@PreAuthorize("hasAuthority('ROLE_RETAILER')")
public class RetailerVendorSearchPageController {
    
    /**
     * Display the vendor search page.
     * 
     * <p>This endpoint serves the Thymeleaf template that renders the vendor search UI
     * with filters, search bar, and results grid. The page uses JavaScript to make
     * asynchronous calls to the REST API endpoint for search functionality.
     * 
     * <p>The authenticated user is added to the model for display purposes (e.g., showing
     * the user's name in the navigation bar).
     * 
     * @param model the Spring MVC model for passing data to the view
     * @return the view name "retailer/vendor-search" which resolves to the Thymeleaf template
     */
    @GetMapping("/vendor-search")
    public String vendorSearchPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        return "retailer/vendor-search";
    }
    
    /**
     * Convenience alias: /retailer/vendors -> /retailer/vendor-search
     */
    @GetMapping("/vendors")
    public String vendorsAlias() {
        return "redirect:/retailer/vendor-search";
    }
    
    /**
     * Convenience alias: /retailer/suppliers -> /retailer/vendor-search
     */
    @GetMapping("/suppliers")
    public String suppliersAlias() {
        return "redirect:/retailer/vendor-search";
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
