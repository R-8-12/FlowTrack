package com.example.IMS.controller;

import com.example.IMS.model.Role;
import com.example.IMS.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for VendorRetailerNetworkController.
 * 
 * <p>Verifies Phase 2 implementation:
 * - /vendor/retailers endpoint is accessible to ROLE_VENDOR
 * - Endpoint returns proper view (no Whitelabel error)
 * - Unauthorized access is denied
 */
@WebMvcTest(VendorRetailerNetworkController.class)
@DisplayName("VendorRetailerNetworkController Mapping Tests")
class VendorRetailerNetworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private User vendorUser;

    @BeforeEach
    void setUp() {
        vendorUser = new User();
        vendorUser.setId(1L);
        vendorUser.setUsername("vendor@test.com");
        vendorUser.setFirstName("Test");
        vendorUser.setLastName("Vendor");
        Role vendorRole = new Role();
        vendorRole.setName("ROLE_VENDOR");
        Set<Role> roles = new HashSet<>();
        roles.add(vendorRole);
        vendorUser.setRoles(roles);
    }

    @Test
    @DisplayName("GET /vendor/retailers as VENDOR should return retailer-network view")
    @WithMockUser(username = "vendor@test.com", authorities = {"ROLE_VENDOR"})
    void testVendorAccessRetailerNetworkReturnsView() throws Exception {
        mockMvc.perform(get("/vendor/retailers"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/retailer-network"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    @DisplayName("GET /vendor/retailers as RETAILER should be forbidden")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerAccessRetailerNetworkForbidden() throws Exception {
        mockMvc.perform(get("/vendor/retailers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /vendor/retailers as PLATFORM_ADMIN should be forbidden")
    @WithMockUser(username = "admin@test.com", authorities = {"ROLE_PLATFORM_ADMIN"})
    void testAdminAccessRetailerNetworkForbidden() throws Exception {
        mockMvc.perform(get("/vendor/retailers"))
                .andExpect(status().isForbidden());
    }
}
