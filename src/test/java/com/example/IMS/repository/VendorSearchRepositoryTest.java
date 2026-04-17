package com.example.IMS.repository;

import com.example.IMS.model.*;
import com.example.IMS.model.enums.BusinessType;
import com.example.IMS.model.enums.OnboardingStage;
import com.example.IMS.model.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VendorSearchRepository
 * Tests all filtering and pagination logic for vendor search
 * Requirements: 1.1-1.6, 2.1-2.5, 3.1-3.4, 4.1-4.3, 6.1-6.4
 */
@DataJpaTest
public class VendorSearchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VendorSearchRepository vendorSearchRepository;

    private User enabledUser;
    private User disabledUser;
    private BusinessProfile verifiedActiveVendor1;
    private BusinessProfile verifiedActiveVendor2;
    private BusinessProfile draftVendor;
    private BusinessProfile rejectedVendor;

    @BeforeEach
    public void setUp() {
        // Create roles
        Role vendorRole = new Role();
        vendorRole.setName("ROLE_VENDOR");
        entityManager.persist(vendorRole);

        // Create enabled user
        enabledUser = new User();
        enabledUser.setUsername("enabledvendor");
        enabledUser.setEmail("enabled@vendor.com");
        enabledUser.setPassword("password");
        enabledUser.setEnabled(true);
        enabledUser.addRole(vendorRole);
        entityManager.persist(enabledUser);

        // Create disabled user
        disabledUser = new User();
        disabledUser.setUsername("disabledvendor");
        disabledUser.setEmail("disabled@vendor.com");
        disabledUser.setPassword("password");
        disabledUser.setEnabled(false);
        disabledUser.addRole(vendorRole);
        entityManager.persist(disabledUser);

        // Create verified and active vendor 1
        verifiedActiveVendor1 = new BusinessProfile();
        verifiedActiveVendor1.setUser(enabledUser);
        verifiedActiveVendor1.setLegalBusinessName("Premium Electronics Ltd");
        verifiedActiveVendor1.setBusinessType(BusinessType.PRIVATE_LIMITED);
        verifiedActiveVendor1.setGstin("29ABCDE1234F1Z5");
        verifiedActiveVendor1.setPanNumber("ABCDE1234F");
        verifiedActiveVendor1.setRegisteredAddress("123 MG Road, Bangalore");
        verifiedActiveVendor1.setState("Karnataka");
        verifiedActiveVendor1.setPincode("560001");
        verifiedActiveVendor1.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedActiveVendor1.setOnboardingStage(OnboardingStage.ACTIVE);
        verifiedActiveVendor1.setDefaultDeliveryDays(5);
        verifiedActiveVendor1.setReliabilityScore(0.85);
        verifiedActiveVendor1.setRating(4.5);
        entityManager.persist(verifiedActiveVendor1);

        // Create verified and active vendor 2
        verifiedActiveVendor2 = new BusinessProfile();
        verifiedActiveVendor2.setUser(enabledUser);
        verifiedActiveVendor2.setLegalBusinessName("Budget Supplies Co");
        verifiedActiveVendor2.setBusinessType(BusinessType.PRIVATE_LIMITED);
        verifiedActiveVendor2.setGstin("29FGHIJ5678K1Z6");
        verifiedActiveVendor2.setPanNumber("FGHIJ5678K");
        verifiedActiveVendor2.setRegisteredAddress("456 Brigade Road, Bangalore");
        verifiedActiveVendor2.setState("Karnataka");
        verifiedActiveVendor2.setPincode("560002");
        verifiedActiveVendor2.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedActiveVendor2.setOnboardingStage(OnboardingStage.ACTIVE);
        verifiedActiveVendor2.setDefaultDeliveryDays(7);
        verifiedActiveVendor2.setReliabilityScore(0.75);
        verifiedActiveVendor2.setRating(4.0);
        entityManager.persist(verifiedActiveVendor2);

        // Create draft vendor (should not appear in search)
        draftVendor = new BusinessProfile();
        draftVendor.setUser(enabledUser);
        draftVendor.setLegalBusinessName("Draft Vendor Ltd");
        draftVendor.setBusinessType(BusinessType.PRIVATE_LIMITED);
        draftVendor.setGstin("29KLMNO9012P1Z7");
        draftVendor.setPanNumber("KLMNO9012P");
        draftVendor.setRegisteredAddress("789 Residency Road, Bangalore");
        draftVendor.setState("Karnataka");
        draftVendor.setPincode("560003");
        draftVendor.setVerificationStatus(VerificationStatus.DRAFT);
        draftVendor.setOnboardingStage(OnboardingStage.TIER1_COMPLETE);
        entityManager.persist(draftVendor);

        // Create rejected vendor (should not appear in search)
        rejectedVendor = new BusinessProfile();
        rejectedVendor.setUser(disabledUser);
        rejectedVendor.setLegalBusinessName("Rejected Vendor Ltd");
        rejectedVendor.setBusinessType(BusinessType.PRIVATE_LIMITED);
        rejectedVendor.setGstin("29RSTUV3456W1Z8");
        rejectedVendor.setPanNumber("RSTUV3456W");
        rejectedVendor.setRegisteredAddress("101 Church Street, Bangalore");
        rejectedVendor.setState("Karnataka");
        rejectedVendor.setPincode("560004");
        rejectedVendor.setVerificationStatus(VerificationStatus.REJECTED);
        rejectedVendor.setOnboardingStage(OnboardingStage.TIER2_COMPLETE);
        entityManager.persist(rejectedVendor);

        // Flush to get IDs
        entityManager.flush();

        // Create legacy vendors with IDs matching BusinessProfile IDs
        // This is a workaround for testing - in production, a proper migration would be needed
        // We need to insert vendors with specific IDs to match the BusinessProfile IDs
        Long bp1Id = verifiedActiveVendor1.getId();
        Long bp2Id = verifiedActiveVendor2.getId();
        
        // Use native SQL to insert vendors with specific IDs
        entityManager.getEntityManager().createNativeQuery(
            "INSERT INTO Vendor (vendor_id, vendor_name, vendor_email) VALUES (:id, :name, :email)")
            .setParameter("id", bp1Id)
            .setParameter("name", "Legacy Vendor for BP1")
            .setParameter("email", "legacy1@test.com")
            .executeUpdate();
            
        entityManager.getEntityManager().createNativeQuery(
            "INSERT INTO Vendor (vendor_id, vendor_name, vendor_email) VALUES (:id, :name, :email)")
            .setParameter("id", bp2Id)
            .setParameter("name", "Legacy Vendor for BP2")
            .setParameter("email", "legacy2@test.com")
            .executeUpdate();
        
        // Now create items that reference these vendor IDs
        entityManager.getEntityManager().createNativeQuery(
            "INSERT INTO Inventory_item (item_name, item_price, item_quantity, item_fine_rate, item_invoice_number, vendor_id_fk) " +
            "VALUES (:name, :price, :quantity, 0, 0, :vendorId)")
            .setParameter("name", "Dell Laptop")
            .setParameter("price", 50000.00)
            .setParameter("quantity", 10)
            .setParameter("vendorId", bp1Id)
            .executeUpdate();
            
        entityManager.getEntityManager().createNativeQuery(
            "INSERT INTO Inventory_item (item_name, item_price, item_quantity, item_fine_rate, item_invoice_number, vendor_id_fk) " +
            "VALUES (:name, :price, :quantity, 0, 0, :vendorId)")
            .setParameter("name", "Wireless Mouse")
            .setParameter("price", 500.00)
            .setParameter("quantity", 100)
            .setParameter("vendorId", bp1Id)
            .executeUpdate();
            
        entityManager.getEntityManager().createNativeQuery(
            "INSERT INTO Inventory_item (item_name, item_price, item_quantity, item_fine_rate, item_invoice_number, vendor_id_fk) " +
            "VALUES (:name, :price, :quantity, 0, 0, :vendorId)")
            .setParameter("name", "Mechanical Keyboard")
            .setParameter("price", 2500.00)
            .setParameter("quantity", 50)
            .setParameter("vendorId", bp2Id)
            .executeUpdate();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testSearchVendors_WithQueryParameter_ProductNameMatch() {
        // Test searching by product name
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            "laptop", null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Premium Electronics Ltd", result.getContent().get(0).getLegalBusinessName());
    }

    @Test
    public void testSearchVendors_WithQueryParameter_VendorNameMatch() {
        // Test searching by vendor name
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            "premium", null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Premium Electronics Ltd", result.getContent().get(0).getLegalBusinessName());
    }

    @Test
    public void testSearchVendors_WithQueryParameter_CaseInsensitive() {
        // Test case-insensitive search
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            "LAPTOP", null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testSearchVendors_WithEmptyQuery_ReturnsAllVerifiedActiveVendors() {
        // Test empty query returns all verified and active vendors
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            "", null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testSearchVendors_WithNullQuery_ReturnsAllVerifiedActiveVendors() {
        // Test null query returns all verified and active vendors
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testSearchVendors_WithMinPrice_FiltersCorrectly() {
        // Test minimum price filter
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, 1000.00, null, null, pageable
        );

        assertNotNull(result);
        // Should return vendors with items >= 1000 (laptop and keyboard)
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testSearchVendors_WithMaxPrice_FiltersCorrectly() {
        // Test maximum price filter
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, 1000.00, null, pageable
        );

        assertNotNull(result);
        // Should return vendors with items <= 1000 (mouse)
        assertEquals(1, result.getTotalElements());
        assertEquals("Premium Electronics Ltd", result.getContent().get(0).getLegalBusinessName());
    }

    @Test
    public void testSearchVendors_WithPriceRange_FiltersCorrectly() {
        // Test price range filter (both min and max)
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, 1000.00, 10000.00, null, pageable
        );

        assertNotNull(result);
        // Should return vendors with items between 1000 and 10000 (keyboard)
        assertEquals(1, result.getTotalElements());
        assertEquals("Budget Supplies Co", result.getContent().get(0).getLegalBusinessName());
    }

    @Test
    public void testSearchVendors_WithMinQuantity_FiltersCorrectly() {
        // Test minimum quantity filter
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, null, 50, pageable
        );

        assertNotNull(result);
        // Should return vendors with items quantity >= 50 (mouse and keyboard)
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testSearchVendors_OnlyVerifiedVendorsReturned() {
        // Test that only VERIFIED vendors are returned
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable
        );

        assertNotNull(result);
        result.getContent().forEach(vendor -> {
            assertEquals(VerificationStatus.VERIFIED, vendor.getVerificationStatus());
        });
    }

    @Test
    public void testSearchVendors_OnlyActiveVendorsReturned() {
        // Test that only ACTIVE vendors are returned
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable
        );

        assertNotNull(result);
        result.getContent().forEach(vendor -> {
            assertEquals(OnboardingStage.ACTIVE, vendor.getOnboardingStage());
        });
    }

    @Test
    public void testSearchVendors_DisabledUsersExcluded() {
        // Test that vendors with disabled users are excluded
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable
        );

        assertNotNull(result);
        result.getContent().forEach(vendor -> {
            assertTrue(vendor.getUser().isEnabled());
        });
    }

    @Test
    public void testSearchVendors_Pagination_PageSize() {
        // Test pagination with page size
        Pageable pageable = PageRequest.of(0, 1);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    public void testSearchVendors_Pagination_PageNumber() {
        // Test pagination with page number
        Pageable pageable1 = PageRequest.of(0, 1);
        Page<BusinessProfile> result1 = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable1
        );

        Pageable pageable2 = PageRequest.of(1, 1);
        Page<BusinessProfile> result2 = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable2
        );

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.getContent().size());
        assertEquals(1, result2.getContent().size());
        assertNotEquals(result1.getContent().get(0).getId(), result2.getContent().get(0).getId());
    }

    @Test
    public void testSearchVendors_Pagination_TotalElements() {
        // Test pagination total elements
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            null, null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testSearchVendors_CombinedFilters() {
        // Test combining multiple filters
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            "keyboard", 1000.00, 5000.00, 40, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Budget Supplies Co", result.getContent().get(0).getLegalBusinessName());
    }

    @Test
    public void testSearchVendors_NoMatchingResults() {
        // Test search with no matching results
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessProfile> result = vendorSearchRepository.searchVendors(
            "nonexistent", null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    public void testCountMatchingVendors_WithQuery() {
        // Test count with query parameter
        long count = vendorSearchRepository.countMatchingVendors(
            "laptop", null, null, null
        );

        assertEquals(1, count);
    }

    @Test
    public void testCountMatchingVendors_WithFilters() {
        // Test count with filters
        long count = vendorSearchRepository.countMatchingVendors(
            null, 1000.00, 10000.00, null
        );

        assertEquals(1, count);
    }

    @Test
    public void testCountMatchingVendors_NoFilters() {
        // Test count without filters
        long count = vendorSearchRepository.countMatchingVendors(
            null, null, null, null
        );

        assertEquals(2, count);
    }
}
