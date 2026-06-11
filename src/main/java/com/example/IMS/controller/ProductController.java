package com.example.IMS.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.IMS.model.BusinessProfile;
import com.example.IMS.model.User;
import com.example.IMS.model.VendorProduct;
import com.example.IMS.repository.BusinessProfileRepository;
import com.example.IMS.repository.VendorProductRepository;

@Controller
@RequestMapping("/products")
@PreAuthorize("hasAuthority('ROLE_VENDOR')")
public class ProductController {

    @Autowired
    private VendorProductRepository vendorProductRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @GetMapping
    public String listProducts(Model model) {
        User user = currentUser();
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);

        if (profiles.isEmpty()) {
            model.addAttribute("products", List.of());
            model.addAttribute("hasProfile", false);
            return "vendor/products";
        }

        List<VendorProduct> products = vendorProductRepository
                .findByVendorProfileIdAndActiveTrue(profiles.get(0).getId());

        model.addAttribute("hasProfile", true);
        model.addAttribute("products", products);
        model.addAttribute("vendorProfile", profiles.get(0));
        return "vendor/products";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("user", currentUser());
        model.addAttribute("product", new VendorProduct());
        return "vendor/product-add";
    }

    @PostMapping("/add")
    public String saveProduct(@Valid @ModelAttribute("product") VendorProduct product,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("user", currentUser());
            return "vendor/product-add";
        }

        User user = currentUser();
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(user.getId());

        if (profiles.isEmpty()) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "You need a business profile before listing products.");
            return "vendor/product-add";
        }

        product.setVendorProfile(profiles.get(0));
        vendorProductRepository.save(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "Product '" + product.getName() + "' listed successfully!");
        return "redirect:/products";
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
