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
 * Controller for vendor product catalog management.
 * 
 * <p>Handles product listing, creation, editing, and deletion for vendors.
 * All endpoints require ROLE_VENDOR authority.
 * 
 * <p>Phase 2 implementation: Placeholder views with navigation back to dashboard.
 * Full product catalog functionality to be implemented in future phases.
 */
@Controller
@RequestMapping("/products")
@PreAuthorize("hasAuthority('ROLE_VENDOR')")
public class ProductController {
    
    /**
     * Display product catalog listing page.
     * 
     * @param model the Spring MVC model
     * @return view name for product listing
     */
    @GetMapping
    public String listProducts(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Product Catalog");
        model.addAttribute("message", "Product catalog management coming soon. You'll be able to list, edit, and manage your product inventory here.");
        return "vendor/products";
    }
    
    /**
     * Display form to add a new product.
     * 
     * @param model the Spring MVC model
     * @return view name for product creation form
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Add New Product");
        model.addAttribute("message", "Product creation form coming soon. You'll be able to add products with details like name, description, price, SKU, and images.");
        return "vendor/product-add";
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
