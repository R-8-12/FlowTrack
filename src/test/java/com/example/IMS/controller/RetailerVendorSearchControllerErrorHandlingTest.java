package com.example.IMS.controller;

import com.example.IMS.dto.VendorSearchRequest;
import com.example.IMS.dto.VendorSearchResponse;
import com.example.IMS.model.Role;
import com.example.IMS.model.User;
import com.example.IMS.repository.IRoleRepository;
import com.example.IMS.repository.IUserRepository;
import com.example.IMS.service.VendorSearchService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for RetailerVendorSearchController error handling.
 * 
 * <p>Tests the error handling implementation for Task 8.3:
 * <ul>
 *   <li>IllegalArgumentException handling (400 Bad Request)</li>
 *   <li>Generic Exception handling (500 Internal Server Error)</li>
 *   <li>MethodArgumentNotValidException handling (validation errors)</li>
 * </ul>
 * 
 * <p>Requirements: 15.1-15.7, 21.3-21.6
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "verification.mode=MOCK",
        "verification.auto=false"
})
@DisplayName("RetailerVendorSearchController - Error Handling Tests (Task 8.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RetailerVendorSearchControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private VendorSearchService vendorSearchService;

    private User retailer;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = String.valueOf(System.nanoTime()).substring(5, 13);

        Role retailerRole = roleRepository.findByName("ROLE_RETAILER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_RETAILER")));

        retailer = new User();
        retailer.setUsername("errorTestRetailer" + suffix);
        retailer.setEmail("errorTest" + suffix + "@test.com");
        retailer.setPassword(passwordEncoder.encode("password"));
        retailer.setEnabled(true);
        retailer.addRole(retailerRole);
        retailer = userRepository.save(retailer);
    }

    private UsernamePasswordAuthenticationToken authFor(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    // ========= IllegalArgumentException Tests (400 Bad Request) =========

    @Test
    @Order(1)
    @DisplayName("Search with invalid price range → 400 Bad Request with error message")
    void searchVendors_invalidPriceRange_returns400() throws Exception {
        // Mock service to throw IllegalArgumentException
        when(vendorSearchService.searchVendors(any(VendorSearchRequest.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid price range: minPrice cannot exceed maxPrice"));

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("minPrice", "1000")
                        .param("maxPrice", "500")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid price range: minPrice cannot exceed maxPrice"));
    }

    @Test
    @Order(2)
    @DisplayName("Search with invalid delivery days → 400 Bad Request")
    void searchVendors_invalidDeliveryDays_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("maxDeliveryDays", "0")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxDeliveryDays").exists());
    }

    // ========= Generic Exception Tests (500 Internal Server Error) =========

    @Test
    @Order(3)
    @DisplayName("Search with unexpected system error → 500 Internal Server Error")
    void searchVendors_unexpectedError_returns500() throws Exception {
        // Mock service to throw generic exception
        when(vendorSearchService.searchVendors(any(VendorSearchRequest.class), anyLong()))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "laptop")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred while processing your request"));
    }

    @Test
    @Order(4)
    @DisplayName("Search with NullPointerException → 500 Internal Server Error")
    void searchVendors_nullPointerException_returns500() throws Exception {
        when(vendorSearchService.searchVendors(any(VendorSearchRequest.class), anyLong()))
                .thenThrow(new NullPointerException("Unexpected null value"));

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "test")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // ========= Validation Error Tests (MethodArgumentNotValidException) =========

    @Test
    @Order(5)
    @DisplayName("Search with negative page number → 400 Bad Request with field error")
    void searchVendors_negativePageNumber_returns400WithFieldError() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("page", "-1")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.page").exists());
    }

    @Test
    @Order(6)
    @DisplayName("Search with invalid page size (too large) → 400 Bad Request")
    void searchVendors_invalidPageSize_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("size", "100")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.size").exists());
    }

    @Test
    @Order(7)
    @DisplayName("Search with invalid sortBy value → 400 Bad Request")
    void searchVendors_invalidSortBy_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortBy", "invalid_sort")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sortBy").exists());
    }

    @Test
    @Order(8)
    @DisplayName("Search with invalid sortDirection → 400 Bad Request")
    void searchVendors_invalidSortDirection_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortDirection", "invalid_direction")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sortDirection").exists());
    }

    @Test
    @Order(9)
    @DisplayName("Search with negative minPrice → 400 Bad Request")
    void searchVendors_negativeMinPrice_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("minPrice", "-100")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minPrice").exists());
    }

    @Test
    @Order(10)
    @DisplayName("Search with negative minQuantity → 400 Bad Request")
    void searchVendors_negativeMinQuantity_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("minQuantity", "-10")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minQuantity").exists());
    }

    // ========= Successful Request Test (Baseline) =========

    @Test
    @Order(11)
    @DisplayName("Search with valid parameters → 200 OK")
    void searchVendors_validRequest_returns200() throws Exception {
        // Mock successful response
        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.emptyList(), 0, 0, 0, 20
        );
        when(vendorSearchService.searchVendors(any(VendorSearchRequest.class), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "laptop")
                        .param("minPrice", "1000")
                        .param("maxPrice", "5000")
                        .param("page", "0")
                        .param("size", "20")
                        .with(authentication(authFor(retailer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendors").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
}
