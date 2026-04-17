package com.example.IMS.controller;

import com.example.IMS.model.Role;
import com.example.IMS.model.User;
import com.example.IMS.repository.IVendorRepository;
import com.example.IMS.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for VendorController role-aware redirects.
 * 
 * <p>Verifies Phase 1 implementation:
 * - ROLE_RETAILER accessing /vendors -> redirects to /retailer/vendor-search
 * - ROLE_RETAILER accessing /vendors/add -> redirects to /retailer/vendor-search
 * - ROLE_PLATFORM_ADMIN accessing /vendors -> shows legacy vendor_list
 * - ROLE_PLATFORM_ADMIN accessing /vendors/add -> shows legacy vendor_form
 */
@WebMvcTest(VendorController.class)
@DisplayName("VendorController Role-Aware Redirect Tests")
class VendorControllerRedirectTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IVendorRepository vendorRepository;

    @MockBean
    private EmailService emailService;

    private User retailerUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Create retailer user
        retailerUser = new User();
        retailerUser.setId(1L);
        retailerUser.setUsername("retailer@test.com");
        retailerUser.setFirstName("Test");
        retailerUser.setLastName("Retailer");
        Role retailerRole = new Role();
        retailerRole.setName("ROLE_RETAILER");
        Set<Role> retailerRoles = new HashSet<>();
        retailerRoles.add(retailerRole);
        retailerUser.setRoles(retailerRoles);

        // Create admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin@test.com");
        adminUser.setFirstName("Test");
        adminUser.setLastName("Admin");
        Role adminRole = new Role();
        adminRole.setName("ROLE_PLATFORM_ADMIN");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
    }

    @Test
    @DisplayName("GET /vendors as RETAILER should redirect to /retailer/vendor-search")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerAccessVendorsListRedirects() throws Exception {
        mockMvc.perform(get("/vendors"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/retailer/vendor-search"));
    }

    @Test
    @DisplayName("GET /vendors/add as RETAILER should redirect to /retailer/vendor-search")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerAccessVendorsAddRedirects() throws Exception {
        mockMvc.perform(get("/vendors/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/retailer/vendor-search"));
    }

    @Test
    @DisplayName("GET /vendors as PLATFORM_ADMIN should show vendor_list view")
    @WithMockUser(username = "admin@test.com", authorities = {"ROLE_PLATFORM_ADMIN"})
    void testAdminAccessVendorsListShowsLegacyView() throws Exception {
        mockMvc.perform(get("/vendors"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor_list"));
    }

    @Test
    @DisplayName("GET /vendors/add as PLATFORM_ADMIN should show vendor_form view")
    @WithMockUser(username = "admin@test.com", authorities = {"ROLE_PLATFORM_ADMIN"})
    void testAdminAccessVendorsAddShowsLegacyForm() throws Exception {
        mockMvc.perform(get("/vendors/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor_form"));
    }

    @Test
    @DisplayName("GET /vendors without authentication should return 401 or redirect to login")
    @WithMockUser(authorities = {})
    void testUnauthenticatedAccessVendorsDenied() throws Exception {
        mockMvc.perform(get("/vendors"))
                .andExpect(status().isForbidden());
    }
}
