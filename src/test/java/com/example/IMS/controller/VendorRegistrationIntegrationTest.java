package com.example.IMS.controller;

import com.example.IMS.dto.VendorRegistrationDto;
import com.example.IMS.model.VendorProfile;
import com.example.IMS.model.User;
import com.example.IMS.repository.IVendorProfileRepository;
import com.example.IMS.repository.IUserRepository;
import com.example.IMS.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for Vendor Registration Flow
 * 
 * Verifies:
 * 1. User record is created in 'users' table with ROLE_VENDOR
 * 2. VendorProfile record is created in 'vendor_profiles' table
 * 3. All business details are persisted correctly
 * 4. User-Profile linkage is established
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class VendorRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IVendorProfileRepository vendorProfileRepository;

    @Autowired
    private UserService userService;

    private VendorRegistrationDto validDto;

    @BeforeEach
    public void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
        vendorProfileRepository.deleteAll();

        // Create a valid vendor registration DTO
        validDto = new VendorRegistrationDto();
        validDto.setUsername("testvendor");
        validDto.setEmail("vendor@test.com");
        validDto.setPassword("Test@1234");
        validDto.setConfirmPassword("Test@1234");
        validDto.setFirstName("Jane");
        validDto.setLastName("Vendor");
        validDto.setCompanyName("Tech Supplies Co");
        validDto.setBusinessType("Wholesaler");
        validDto.setCompanyAddress("789 Industrial Ave, City");
        validDto.setPhoneNumber("8888888888");
        validDto.setGstNumber("29ABCDE1234F1Z5");
        validDto.setPanNumber("AAAPE5055K");
        validDto.setTradeLicenseNumber("TL123456");
        validDto.setProductCategories("Electronics, Accessories");
        validDto.setCompanyDescription("Wholesale supplier of tech products");
        validDto.setBankName("State Bank");
        validDto.setBankAccountNumber("1234567890");
        validDto.setBankIfscCode("SBIN0001234");
    }

    @Test
    public void testVendorRegistrationCreatesUserAndProfile() {
        // Create user with role via service
        User savedUser = userService.registerUserWithRole(
                new com.example.IMS.dto.UserRegistrationDto() {{
                    setUsername(validDto.getUsername());
                    setEmail(validDto.getEmail());
                    setPassword(validDto.getPassword());
                    setFirstName(validDto.getFirstName());
                    setLastName(validDto.getLastName());
                }},
                "ROLE_VENDOR"
        );

        // Create vendor profile
        userService.createVendorProfile(savedUser, validDto);

        // Verify user exists
        User foundUser = userRepository.findByUsername(validDto.getUsername()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(validDto.getEmail());
        assertThat(foundUser.getFirstName()).isEqualTo(validDto.getFirstName());
        assertThat(foundUser.getRoles()).hasSize(1);
        assertThat(foundUser.getRoles().stream()
                .map(r -> r.getName())
                .anyMatch(name -> name.equals("ROLE_VENDOR")))
                .isTrue();

        // Verify vendor profile exists
        VendorProfile profile = vendorProfileRepository.findByUserId(foundUser.getId()).orElse(null);
        assertThat(profile).isNotNull();
        assertThat(profile.getCompanyName()).isEqualTo(validDto.getCompanyName());
        assertThat(profile.getBusinessType()).isEqualTo(validDto.getBusinessType());
        assertThat(profile.getCompanyAddress()).isEqualTo(validDto.getCompanyAddress());
        assertThat(profile.getPhoneNumber()).isEqualTo(validDto.getPhoneNumber());
        assertThat(profile.getGstNumber()).isEqualTo(validDto.getGstNumber());
        assertThat(profile.getPanNumber()).isEqualTo(validDto.getPanNumber());
        assertThat(profile.getTradeLicenseNumber()).isEqualTo(validDto.getTradeLicenseNumber());
        assertThat(profile.getBankName()).isEqualTo(validDto.getBankName());
        assertThat(profile.getBankAccountNumber()).isEqualTo(validDto.getBankAccountNumber());
        assertThat(profile.getBankIfscCode()).isEqualTo(validDto.getBankIfscCode());
    }

    @Test
    public void testVendorProfileLinkageToUser() {
        // Create and link
        User savedUser = userService.registerUserWithRole(
                new com.example.IMS.dto.UserRegistrationDto() {{
                    setUsername(validDto.getUsername());
                    setEmail(validDto.getEmail());
                    setPassword(validDto.getPassword());
                    setFirstName(validDto.getFirstName());
                    setLastName(validDto.getLastName());
                }},
                "ROLE_VENDOR"
        );

        userService.createVendorProfile(savedUser, validDto);

        // Verify the bidirectional relationship
        VendorProfile profile = vendorProfileRepository.findByUserId(savedUser.getId()).orElse(null);
        assertThat(profile).isNotNull();
        assertThat(profile.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(profile.getUser().getUsername()).isEqualTo(validDto.getUsername());
    }

    @Test
    public void testVendorDuplicateEmailRejected() {
        // Create first vendor
        User firstVendor = userService.registerUserWithRole(
                new com.example.IMS.dto.UserRegistrationDto() {{
                    setUsername("vendor1");
                    setEmail("duplicate@vendor.com");
                    setPassword("Test@1234");
                    setFirstName("First");
                    setLastName("Vendor");
                }},
                "ROLE_VENDOR"
        );

        // Try to create second vendor with same email
        try {
            userService.registerUserWithRole(
                    new com.example.IMS.dto.UserRegistrationDto() {{
                        setUsername("vendor2");
                        setEmail("duplicate@vendor.com");
                        setPassword("Test@1234");
                        setFirstName("Second");
                        setLastName("Vendor");
                    }},
                    "ROLE_VENDOR"
            );
            assertThat(true).isFalse(); // Should not reach here
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Email already exists");
        }
    }

    @Test
    public void testVendorAllBankDetailsPersistedCorrectly() {
        // Create vendor
        User user = userService.registerUserWithRole(
                new com.example.IMS.dto.UserRegistrationDto() {{
                    setUsername(validDto.getUsername());
                    setEmail(validDto.getEmail());
                    setPassword(validDto.getPassword());
                    setFirstName(validDto.getFirstName());
                    setLastName(validDto.getLastName());
                }},
                "ROLE_VENDOR"
        );

        userService.createVendorProfile(user, validDto);

        // Verify all details including bank info
        VendorProfile profile = vendorProfileRepository.findByUserId(user.getId()).orElse(null);
        assertThat(profile).isNotNull();
        assertThat(profile.getBankAccountNumber()).isEqualTo(validDto.getBankAccountNumber());
        assertThat(profile.getBankIfscCode()).isEqualTo(validDto.getBankIfscCode());
        assertThat(profile.getBankName()).isEqualTo(validDto.getBankName());
        assertThat(profile.getProductCategories()).isEqualTo(validDto.getProductCategories());
        assertThat(profile.getCompanyDescription()).isEqualTo(validDto.getCompanyDescription());
    }

    @Test
    public void testVendorCanUpdateProfileData() {
        // Create initial profile
        User user = userService.registerUserWithRole(
                new com.example.IMS.dto.UserRegistrationDto() {{
                    setUsername(validDto.getUsername());
                    setEmail(validDto.getEmail());
                    setPassword(validDto.getPassword());
                    setFirstName(validDto.getFirstName());
                    setLastName(validDto.getLastName());
                }},
                "ROLE_VENDOR"
        );

        userService.createVendorProfile(user, validDto);

        // Update with new data
        VendorRegistrationDto updatedDto = new VendorRegistrationDto();
        updatedDto.setCompanyName("Updated Tech Supplies");
        updatedDto.setPhoneNumber("7777777777");
        updatedDto.setProductCategories("Electronics, Software, Services");
        updatedDto.setBankName("HDFC Bank");

        userService.createVendorProfile(user, updatedDto);

        // Verify update
        VendorProfile updated = vendorProfileRepository.findByUserId(user.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getCompanyName()).isEqualTo("Updated Tech Supplies");
        assertThat(updated.getPhoneNumber()).isEqualTo("7777777777");
        assertThat(updated.getBankName()).isEqualTo("HDFC Bank");
    }
}
