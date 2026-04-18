package com.example.IMS.controller;

import com.example.IMS.model.BusinessProfile;
import com.example.IMS.model.User;
import com.example.IMS.repository.BusinessProfileRepository;
import com.example.IMS.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for role-scoped analytics data.
 *
 * <p>Returns Chart.js-compatible payloads for retailer and vendor dashboards.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    /**
     * GET /api/analytics/retailer
     *
     * <p>Returns retailer-specific metrics: total purchases, paid vs pending,
     * total spend, and connected vendor count.
     */
    @GetMapping("/retailer")
    @PreAuthorize("hasAuthority('ROLE_RETAILER')")
    public ResponseEntity<Map<String, Object>> retailerAnalytics() {
        User user = currentUser();
        AnalyticsService.RetailerAnalytics a = analyticsService.getRetailerAnalytics(user.getId());

        Map<String, Object> payload = Map.of(
            "totalPurchases",   a.totalPurchases,
            "paidOrders",       a.paidOrders,
            "pendingOrders",    a.pendingOrders,
            "totalSpend",       a.totalSpend,
            "connectedVendors", a.connectedVendors,
            // Chart.js bar data: paid vs pending
            "chartData", Map.of(
                "labels",   List.of("Paid Orders", "Pending Orders"),
                "datasets", List.of(Map.of(
                    "label", "Order Status",
                    "data",  List.of(a.paidOrders, a.pendingOrders),
                    "backgroundColor", List.of("#38a169", "#dd6b20")
                ))
            )
        );
        return ResponseEntity.ok(payload);
    }

    /**
     * GET /api/analytics/vendor
     *
     * <p>Returns vendor-specific metrics: connected retailers, paid orders, total revenue.
     */
    @GetMapping("/vendor")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public ResponseEntity<Map<String, Object>> vendorAnalytics() {
        User user = currentUser();
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(user.getId());

        if (profiles.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "connectedRetailers", 0,
                "paidOrders", 0,
                "totalRevenue", 0.0
            ));
        }

        Long profileId = profiles.get(0).getId();
        AnalyticsService.VendorAnalytics a = analyticsService.getVendorAnalytics(profileId, user.getId());

        Map<String, Object> payload = Map.of(
            "connectedRetailers", a.connectedRetailers,
            "paidOrders",         a.paidOrders,
            "totalRevenue",       a.totalRevenue,
            // Chart.js doughnut data
            "chartData", Map.of(
                "labels",   List.of("Connected Retailers", "Paid Orders"),
                "datasets", List.of(Map.of(
                    "data",            List.of(a.connectedRetailers, a.paidOrders),
                    "backgroundColor", List.of("#2b6cb0", "#38a169")
                ))
            )
        );
        return ResponseEntity.ok(payload);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
