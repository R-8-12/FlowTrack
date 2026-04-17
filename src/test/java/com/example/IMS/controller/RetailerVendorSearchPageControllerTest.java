package com.example.IMS.controller;

import com.example.IMS.model.Role;
import com.example.IMS.model.User;
import com.example.IMS.repository.IRoleRepository;
import com.example.IMS.repository.IUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for RetailerVendorSearchPageController (Task 8.4)
 * 
 * <p>This test class verifies that the vendor search page controller:
 * <ul>
 *   <li>Requires ROLE_RETAILER authentication</li>
 *   <li>Serves the correct Thymeleaf template</li>
 *   <li>Adds the authenticated user to the model</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("RetailerVendorSearchPageController - UI Page Tests (Task 8.4)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RetailerVendorSearchPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        retailerUser.setUsername("retailer" + suffix);
        retailerUser.setEmail("retailer" + suffix + "@test.com");
        retailerUser.setPassword(passwordEncoder.encode("password"));
        retailerUser.setEnabled(true);
        retailerUser.addRole(retailerRole);
        retailerUser = userRepository.save(retailerUser);

        // Create vendor user (for negative test)
        vendorUser = new User();
        vendorUser.setUsername("vendor" + suffix);
        vendorUser.setEmail("vendor" + suffix + "@test.com");
        vendorUser.setPassword(passwordEncoder.encode("password"));
        vendorUser.setEnabled(true);
        vendorUser.addRole(vendorRole);
        vendorUser = userRepository.save(vendorUser);
    }

    private UsernamePasswordAuthenticationToken authFor(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    @Order(1)
    @DisplayName("GET /retailer/vendor-search - unauthenticated → redirect to login")
    void vendorSearchPage_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/retailer/vendor-search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /retailer/vendor-search - ROLE_RETAILER → renders page with user in model")
    void vendorSearchPage_retailer_rendersPageWithUser() throws Exception {
        mockMvc.perform(get("/retailer/vendor-search")
                        .with(authentication(authFor(retailerUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("retailer/vendor-search"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", retailerUser));
    }

    @Test
    @Order(3)
    @DisplayName("GET /retailer/vendor-search - ROLE_VENDOR → 403 Forbidden")
    void vendorSearchPage_vendor_forbidden() throws Exception {
        mockMvc.perform(get("/retailer/vendor-search")
                        .with(authentication(authFor(vendorUser))))
                .andExpect(status().isForbidden());
    }
}
