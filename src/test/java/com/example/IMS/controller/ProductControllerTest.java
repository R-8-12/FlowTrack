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
 * Test suite for ProductController.
 * 
 * <p>Verifies Phase 2 implementation:
 * - /products endpoint is accessible to ROLE_VENDOR
 * - /products/add endpoint is accessible to ROLE_VENDOR
 * - Endpoints return proper views (no Whitelabel errors)
 * - Unauthorized access is denied
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Mapping Tests")
class ProductControllerTest {

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
    @DisplayName("GET /products as VENDOR should return products view")
    @WithMockUser(username = "vendor@test.com", authorities = {"ROLE_VENDOR"})
    void testVendorAccessProductsReturnsView() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/products"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    @DisplayName("GET /products/add as VENDOR should return product-add view")
    @WithMockUser(username = "vendor@test.com", authorities = {"ROLE_VENDOR"})
    void testVendorAccessProductsAddReturnsView() throws Exception {
        mockMvc.perform(get("/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/product-add"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    @DisplayName("GET /products as RETAILER should be forbidden")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerAccessProductsForbidden() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /products/add as RETAILER should be forbidden")
    @WithMockUser(username = "retailer@test.com", authorities = {"ROLE_RETAILER"})
    void testRetailerAccessProductsAddForbidden() throws Exception {
        mockMvc.perform(get("/products/add"))
                .andExpect(status().isForbidden());
    }
}
