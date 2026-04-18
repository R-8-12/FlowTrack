package com.example.IMS.controller;

import com.example.IMS.dto.VendorCardDTO;
import com.example.IMS.dto.VendorSearchResponse;
import com.example.IMS.model.Role;
import com.example.IMS.model.User;
import com.example.IMS.model.enums.Badge;
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
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RetailerVendorSearchController (Task 8.5).
 * 
 * <p>This test class verifies:
 * <ul>
 *   <li>Search endpoint with valid request returns 200 OK with correct JSON structure</li>
 *   <li>Search endpoint with invalid page number returns 400 Bad Request</li>
 *   <li>Search endpoint with invalid page size returns 400 Bad Request</li>
 *   <li>Search endpoint with invalid sortBy returns 400 Bad Request</li>
 *   <li>Search endpoint with ROLE_VENDOR user returns 403 Forbidden</li>
 *   <li>Search endpoint without authentication returns 401 Unauthorized</li>
 *   <li>Validation error handling returns field error messages</li>
 * </ul>
 * 
 * <p>Requirements: 13.1-13.5, 15.1-15.7, 21.1-21.7
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "verification.mode=MOCK",
        "verification.auto=false"
})
@DisplayName("RetailerVendorSearchController - Integration Tests (Task 8.5)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RetailerVendorSearchControllerIntegrationTest {

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

    private User retailerUser;
    private User vendorUser;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = String.valueOf(System.nanoTime()).substring(5, 13);

        // Create retailer role
        Role retailerRole = roleRepository.findByName("ROLE_RETAILER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_RETAILER")));

        // Create vendor role
        Role vendorRole = roleRepository.findByName("ROLE_VENDOR")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_VENDOR")));

        // Create retailer user
        retailerUser = new User();
        retailerUser.setUsername("integrationRetailer" + suffix);
        retailerUser.setEmail("integrationRetailer" + suffix + "@test.com");
        retailerUser.setPassword(passwordEncoder.encode("password"));
        retailerUser.setEnabled(true);
        retailerUser.addRole(retailerRole);
        retailerUser = userRepository.save(retailerUser);

        // Create vendor user (for negative test)
        vendorUser = new User();
        vendorUser.setUsername("integrationVendor" + suffix);
        vendorUser.setEmail("integrationVendor" + suffix + "@test.com");
        vendorUser.setPassword(passwordEncoder.encode("password"));
        vendorUser.setEnabled(true);
        vendorUser.addRole(vendorRole);
        vendorUser = userRepository.save(vendorUser);
    }

    private UsernamePasswordAuthenticationToken authFor(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    // ========= Valid Request Tests =========

    @Test
    @Order(1)
    @DisplayName("Search with valid request → 200 OK with correct JSON structure")
    void searchVendors_validRequest_returns200WithCorrectStructure() throws Exception {
        // Create mock vendor cards
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Test Vendor 1")
                .pricePerUnit(new BigDecimal("100.00"))
                .availableQuantity(50)
                .deliveryDays(5)
                .reliabilityScore(0.85)
                .rating(4.5)
                .verified(true)
                .location("Mumbai, Maharashtra")
                .previouslyOrdered(false)
                .badge(Badge.BEST_PRICE)
                .build();

        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Test Vendor 2")
                .pricePerUnit(new BigDecimal("120.00"))
                .availableQuantity(100)
                .deliveryDays(3)
                .reliabilityScore(0.90)
                .rating(4.8)
                .verified(true)
                .location("Delhi, Delhi")
                .previouslyOrdered(true)
                .badge(Badge.FAST_DELIVERY)
                .build();

        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Arrays.asList(vendor1, vendor2),
                0,  // currentPage
                1,  // totalPages
                2,  // totalElements
                20  // pageSize
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "laptop")
                        .param("page", "0")
                        .param("size", "20")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.vendors").isArray())
                .andExpect(jsonPath("$.vendors.length()").value(2))
                .andExpect(jsonPath("$.vendors[0].vendorId").value(1))
                .andExpect(jsonPath("$.vendors[0].vendorName").value("Test Vendor 1"))
                .andExpect(jsonPath("$.vendors[0].pricePerUnit").value(100.00))
                .andExpect(jsonPath("$.vendors[0].availableQuantity").value(50))
                .andExpect(jsonPath("$.vendors[0].deliveryDays").value(5))
                .andExpect(jsonPath("$.vendors[0].reliabilityScore").value(0.85))
                .andExpect(jsonPath("$.vendors[0].rating").value(4.5))
                .andExpect(jsonPath("$.vendors[0].verified").value(true))
                .andExpect(jsonPath("$.vendors[0].location").value("Mumbai, Maharashtra"))
                .andExpect(jsonPath("$.vendors[0].previouslyOrdered").value(false))
                .andExpect(jsonPath("$.vendors[0].badge").value("BEST_PRICE"))
                .andExpect(jsonPath("$.vendors[1].vendorId").value(2))
                .andExpect(jsonPath("$.vendors[1].previouslyOrdered").value(true))
                .andExpect(jsonPath("$.vendors[1].badge").value("FAST_DELIVERY"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    @Order(2)
    @DisplayName("Search with empty results → 200 OK with empty vendors array")
    void searchVendors_noResults_returns200WithEmptyArray() throws Exception {
        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.emptyList(),
                0,
                0,
                0,
                20
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "nonexistent")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendors").isArray())
                .andExpect(jsonPath("$.vendors.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @Order(3)
    @DisplayName("Search with filters → 200 OK with filtered results")
    void searchVendors_withFilters_returns200() throws Exception {
        VendorCardDTO vendor = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Filtered Vendor")
                .pricePerUnit(new BigDecimal("150.00"))
                .availableQuantity(75)
                .deliveryDays(4)
                .reliabilityScore(0.88)
                .rating(4.6)
                .verified(true)
                .location("Bangalore, Karnataka")
                .previouslyOrdered(false)
                .badge(Badge.HIGH_RELIABILITY)
                .build();

        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.singletonList(vendor),
                0,
                1,
                1,
                20
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "laptop")
                        .param("minPrice", "100")
                        .param("maxPrice", "200")
                        .param("maxDeliveryDays", "5")
                        .param("minQuantity", "50")
                        .param("sortBy", "price")
                        .param("sortDirection", "asc")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendors.length()").value(1))
                .andExpect(jsonPath("$.vendors[0].vendorName").value("Filtered Vendor"));
    }

    // ========= Security Tests =========

    @Test
    @Order(4)
    @DisplayName("Search with ROLE_VENDOR user → 403 Forbidden")
    void searchVendors_vendorUser_returns403() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "laptop")
                        .with(authentication(authFor(vendorUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    @DisplayName("Search without authentication → 401 Unauthorized (redirect to login)")
    void searchVendors_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("query", "laptop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ========= Validation Error Tests =========

    @Test
    @Order(6)
    @DisplayName("Search with invalid page number → 400 Bad Request with field error")
    void searchVendors_invalidPageNumber_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("page", "-1")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.page").value("page must be 0 or greater"));
    }

    @Test
    @Order(7)
    @DisplayName("Search with invalid page size (too small) → 400 Bad Request")
    void searchVendors_invalidPageSizeTooSmall_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("size", "0")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.size").value("size must be at least 1"));
    }

    @Test
    @Order(8)
    @DisplayName("Search with invalid page size (too large) → 400 Bad Request")
    void searchVendors_invalidPageSizeTooLarge_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("size", "100")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.size").value("size cannot exceed 50"));
    }

    @Test
    @Order(9)
    @DisplayName("Search with invalid sortBy → 400 Bad Request with field error")
    void searchVendors_invalidSortBy_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortBy", "invalid_sort")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sortBy").exists())
                .andExpect(jsonPath("$.sortBy").value("sortBy must be one of: price, delivery, rating, relevance"));
    }

    @Test
    @Order(10)
    @DisplayName("Search with invalid sortDirection → 400 Bad Request with field error")
    void searchVendors_invalidSortDirection_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortDirection", "sideways")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sortDirection").exists())
                .andExpect(jsonPath("$.sortDirection").value("sortDirection must be one of: asc, desc"));
    }

    @Test
    @Order(11)
    @DisplayName("Search with negative minPrice → 400 Bad Request")
    void searchVendors_negativeMinPrice_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("minPrice", "-100")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minPrice").exists())
                .andExpect(jsonPath("$.minPrice").value("minPrice must be non-negative"));
    }

    @Test
    @Order(12)
    @DisplayName("Search with negative maxPrice → 400 Bad Request")
    void searchVendors_negativeMaxPrice_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("maxPrice", "-50")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxPrice").exists())
                .andExpect(jsonPath("$.maxPrice").value("maxPrice must be non-negative"));
    }

    @Test
    @Order(13)
    @DisplayName("Search with invalid maxDeliveryDays (zero) → 400 Bad Request")
    void searchVendors_invalidMaxDeliveryDays_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("maxDeliveryDays", "0")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxDeliveryDays").exists())
                .andExpect(jsonPath("$.maxDeliveryDays").value("maxDeliveryDays must be at least 1"));
    }

    @Test
    @Order(14)
    @DisplayName("Search with invalid minQuantity (zero) → 400 Bad Request")
    void searchVendors_invalidMinQuantity_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("minQuantity", "0")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minQuantity").exists())
                .andExpect(jsonPath("$.minQuantity").value("minQuantity must be at least 1"));
    }

    @Test
    @Order(15)
    @DisplayName("Search with negative maxDistanceKm → 400 Bad Request")
    void searchVendors_negativeMaxDistanceKm_returns400() throws Exception {
        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("maxDistanceKm", "-10")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxDistanceKm").exists())
                .andExpect(jsonPath("$.maxDistanceKm").value("maxDistanceKm must be non-negative"));
    }

    // ========= Pagination Tests =========

    @Test
    @Order(16)
    @DisplayName("Search with pagination → 200 OK with correct page metadata")
    void searchVendors_withPagination_returnsCorrectMetadata() throws Exception {
        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.emptyList(),
                2,  // currentPage
                5,  // totalPages
                100, // totalElements
                20  // pageSize
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("page", "2")
                        .param("size", "20")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    // ========= Sort Options Tests =========

    @Test
    @Order(17)
    @DisplayName("Search with sortBy=price → 200 OK")
    void searchVendors_sortByPrice_returns200() throws Exception {
        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.emptyList(), 0, 0, 0, 20
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortBy", "price")
                        .param("sortDirection", "asc")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk());
    }

    @Test
    @Order(18)
    @DisplayName("Search with sortBy=delivery → 200 OK")
    void searchVendors_sortByDelivery_returns200() throws Exception {
        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.emptyList(), 0, 0, 0, 20
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortBy", "delivery")
                        .param("sortDirection", "asc")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk());
    }

    @Test
    @Order(19)
    @DisplayName("Search with sortBy=rating → 200 OK")
    void searchVendors_sortByRating_returns200() throws Exception {
        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.emptyList(), 0, 0, 0, 20
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortBy", "rating")
                        .param("sortDirection", "desc")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk());
    }

    @Test
    @Order(20)
    @DisplayName("Search with sortBy=relevance → 200 OK")
    void searchVendors_sortByRelevance_returns200() throws Exception {
        VendorSearchResponse mockResponse = new VendorSearchResponse(
                Collections.emptyList(), 0, 0, 0, 20
        );

        when(vendorSearchService.searchVendors(any(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/retailer/vendors/search")
                        .param("sortBy", "relevance")
                        .param("sortDirection", "desc")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk());
    }
}
