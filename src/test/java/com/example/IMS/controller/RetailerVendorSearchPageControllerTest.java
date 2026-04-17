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
 * Test suite for RetailerVendorSearchPageController.
 * 
 * <p>Verifies Phase 1 implementation:
 * - /retailer/vendor-search is accessible to ROLE_RETAILER
 * - Convenience aliases /retailer/vendors and /retailer/suppliers redirect properly
 * - Unauthorized access is denied
 */
@WebMvcTest(RetailerVendorSearchPageController.class)
@DisplayName("RetailerVendorSearchPageController Tests")
class RetailerVendorSearchPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private User retailerUser;

    @BeforeEach
    void setUp() {
        retailerUser = new User();
        retailerUser.setId(1L);
        retailerUser.setUsername("retailer@test.com");
        retailerUser.setFirstName("Test");
        retailerUser.setLastName("Retailer");
        Role retailerRole = new Role();
        retailerRole.setName("ROLE_RETAILER");
        Set<Role> roles = new HashSet<>();
        roles.add(retailerRole);
        retailerUser.setRoles(roles);
    }

    @Test
    @DisplayName("GET /retailer/vendor-search as RETAILER should return vendor-search view")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerAccessVendorSearchReturnsView() throws Exception {
        mockMvc.perform(get("/retailer/vendor-search"))
                .andExpect(status().isOk())
                .andExpect(view().name("retailer/vendor-search"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("GET /retailer/vendors as RETAILER should redirect to /retailer/vendor-search")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerVendorsAliasRedirects() throws Exception {
        mockMvc.perform(get("/retailer/vendors"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/retailer/vendor-search"));
    }

    @Test
    @DisplayName("GET /retailer/suppliers as RETAILER should redirect to /retailer/vendor-search")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerSuppliersAliasRedirects() throws Exception {
        mockMvc.perform(get("/retailer/suppliers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/retailer/vendor-search"));
    }

    @Test
    @DisplayName("GET /retailer/vendor-search as VENDOR should be forbidden")
    @WithMockUser(username = "vendor@test.com", authorities = {"ROLE_VENDOR"})
    void testVendorAccessVendorSearchForbidden() throws Exception {
        mockMvc.perform(get("/retailer/vendor-search"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /retailer/vendor-search as PLATFORM_ADMIN should be forbidden")
    @WithMockUser(username = "admin@test.com", authorities = {"ROLE_PLATFORM_ADMIN"})
    void testAdminAccessVendorSearchForbidden() throws Exception {
        mockMvc.perform(get("/retailer/vendor-search"))
                .andExpect(status().isForbidden());
    }
}
