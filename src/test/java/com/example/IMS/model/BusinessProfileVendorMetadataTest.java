package com.example.IMS.model;

import com.example.IMS.model.enums.BusinessType;
import com.example.IMS.model.enums.OnboardingStage;
import com.example.IMS.model.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessProfile vendor metadata fields
 * Tests the new vendor search and recommendation engine fields
 * Requirements: 8.5, 12.6, 12.7
 */
public class BusinessProfileVendorMetadataTest {

    private BusinessProfile businessProfile;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testvendor");
        user.setEmail("vendor@test.com");

        businessProfile = new BusinessProfile();
        businessProfile.setUser(user);
        businessProfile.setLegalBusinessName("Test Vendor Ltd");
        businessProfile.setBusinessType(BusinessType.PRIVATE_LIMITED);
        businessProfile.setGstin("29ABCDE1234F1Z5");
        businessProfile.setPanNumber("ABCDE1234F");
        businessProfile.setRegisteredAddress("123 Test Street");
        businessProfile.setState("Karnataka");
        businessProfile.setPincode("560001");
        businessProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        businessProfile.setOnboardingStage(OnboardingStage.ACTIVE);
    }

    @Test
    public void testDefaultDeliveryDays_DefaultValue() {
        // Verify default value is 7
        assertEquals(7, businessProfile.getDefaultDeliveryDays());
    }

    @Test
    public void testDefaultDeliveryDays_SetAndGet() {
        businessProfile.setDefaultDeliveryDays(5);
        assertEquals(5, businessProfile.getDefaultDeliveryDays());
    }

    @Test
    public void testReliabilityScore_DefaultValue() {
        // Verify default value is 0.0
        assertEquals(0.0, businessProfile.getReliabilityScore());
    }

    @Test
    public void testReliabilityScore_ValidRange() {
        businessProfile.setReliabilityScore(0.85);
        assertEquals(0.85, businessProfile.getReliabilityScore());

        businessProfile.setReliabilityScore(0.0);
        assertEquals(0.0, businessProfile.getReliabilityScore());

        businessProfile.setReliabilityScore(1.0);
        assertEquals(1.0, businessProfile.getReliabilityScore());
    }

    @Test
    public void testReliabilityScore_InvalidRange_TooLow() {
        assertThrows(IllegalArgumentException.class, () -> {
            businessProfile.setReliabilityScore(-0.1);
        });
    }

    @Test
    public void testReliabilityScore_InvalidRange_TooHigh() {
        assertThrows(IllegalArgumentException.class, () -> {
            businessProfile.setReliabilityScore(1.1);
        });
    }

    @Test
    public void testRating_DefaultValue() {
        // Verify default value is 0.0
        assertEquals(0.0, businessProfile.getRating());
    }

    @Test
    public void testRating_ValidRange() {
        businessProfile.setRating(4.5);
        assertEquals(4.5, businessProfile.getRating());

        businessProfile.setRating(0.0);
        assertEquals(0.0, businessProfile.getRating());

        businessProfile.setRating(5.0);
        assertEquals(5.0, businessProfile.getRating());
    }

    @Test
    public void testRating_InvalidRange_TooLow() {
        assertThrows(IllegalArgumentException.class, () -> {
            businessProfile.setRating(-0.1);
        });
    }

    @Test
    public void testRating_InvalidRange_TooHigh() {
        assertThrows(IllegalArgumentException.class, () -> {
            businessProfile.setRating(5.1);
        });
    }

    @Test
    public void testTotalOrders_DefaultValue() {
        // Verify default value is 0
        assertEquals(0, businessProfile.getTotalOrders());
    }

    @Test
    public void testTotalOrders_SetAndGet() {
        businessProfile.setTotalOrders(100);
        assertEquals(100, businessProfile.getTotalOrders());
    }

    @Test
    public void testCompletedOrders_DefaultValue() {
        // Verify default value is 0
        assertEquals(0, businessProfile.getCompletedOrders());
    }

    @Test
    public void testCompletedOrders_SetAndGet() {
        businessProfile.setCompletedOrders(85);
        assertEquals(85, businessProfile.getCompletedOrders());
    }

    @Test
    public void testVendorMetadata_AllFieldsTogether() {
        // Test setting all vendor metadata fields together
        businessProfile.setDefaultDeliveryDays(3);
        businessProfile.setReliabilityScore(0.92);
        businessProfile.setRating(4.7);
        businessProfile.setTotalOrders(150);
        businessProfile.setCompletedOrders(140);

        assertEquals(3, businessProfile.getDefaultDeliveryDays());
        assertEquals(0.92, businessProfile.getReliabilityScore());
        assertEquals(4.7, businessProfile.getRating());
        assertEquals(150, businessProfile.getTotalOrders());
        assertEquals(140, businessProfile.getCompletedOrders());
    }

    @Test
    public void testVendorMetadata_NullValues() {
        // Test that null values can be set (for optional fields)
        businessProfile.setDefaultDeliveryDays(null);
        businessProfile.setReliabilityScore(null);
        businessProfile.setRating(null);
        businessProfile.setTotalOrders(null);
        businessProfile.setCompletedOrders(null);

        assertNull(businessProfile.getDefaultDeliveryDays());
        assertNull(businessProfile.getReliabilityScore());
        assertNull(businessProfile.getRating());
        assertNull(businessProfile.getTotalOrders());
        assertNull(businessProfile.getCompletedOrders());
    }
}
