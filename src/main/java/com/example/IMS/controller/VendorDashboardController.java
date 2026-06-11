package com.example.IMS.controller;

import com.example.IMS.model.BusinessProfile;
import com.example.IMS.model.User;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.repository.BusinessProfileRepository;
import com.example.IMS.repository.ProcurementOrderRepository;
import com.example.IMS.repository.RetailerVendorConnectionRepository;
import com.example.IMS.repository.VendorProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/vendor")
public class VendorDashboardController {

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private VendorProductRepository vendorProductRepository;

    @Autowired
    private ProcurementOrderRepository procurementOrderRepository;

    @Autowired
    private RetailerVendorConnectionRepository connectionRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = currentUser();
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("profiles", profiles);
        model.addAttribute("hasProfile", !profiles.isEmpty());

        if (!profiles.isEmpty()) {
            BusinessProfile primary = profiles.get(0);
            model.addAttribute("primaryProfile", primary);
            model.addAttribute("profileStatus", primary.getVerificationStatus());
            model.addAttribute("isVerified",
                    primary.getVerificationStatus() != null &&
                    primary.getVerificationStatus().name().equals("VERIFIED"));

            Long profileId = primary.getId();

            // Live stat card values
            long productCount     = vendorProductRepository.countByVendorProfileIdAndActiveTrue(profileId);
            long activeOrders     = procurementOrderRepository
                    .countByVendorProfileIdAndStatus(profileId, ProcurementOrderStatus.REQUESTED)
                    + procurementOrderRepository.countByVendorProfileIdAndStatus(profileId, ProcurementOrderStatus.ACCEPTED);
            long retailerCount    = connectionRepository.countConnectedRetailersByVendorProfileId(profileId);

            model.addAttribute("productCount",  productCount);
            model.addAttribute("activeOrders",  activeOrders);
            model.addAttribute("retailerCount", retailerCount);
        } else {
            model.addAttribute("productCount",  0L);
            model.addAttribute("activeOrders",  0L);
            model.addAttribute("retailerCount", 0L);
        }

        return "vendor/dashboard";
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
