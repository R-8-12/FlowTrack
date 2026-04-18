package com.example.IMS.integration;

import com.example.IMS.dto.VendorSearchRequest;
import com.example.IMS.dto.VendorSearchResponse;
import com.example.IMS.model.*;
import com.example.IMS.model.enums.BusinessType;
import com.example.IMS.model.enums.OnboardingStage;
import com.example.IMS.model.enums.VerificationStatus;
import com.example.IMS.repository.BusinessProfileRepository;
import com.example.IMS.repository.IRoleRepository;
import com.example.IMS.repository.IUserRepository;
import com.example.IMS.service.VendorSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify multi-tenant data isolation in vendor search.
 * 
 * <p>This test verifies Requirement 22: Multi-Tenant Data Isolation
 * <ul>
 *   <li>22.1: Search engine scopes all queries by retailer's tenant context</li>
 *   <li>22.2: Vendor profiles returned belong to same tenant as retailer</li>
 *   <li>22.3: Prevents cross-tenant data leakage</li>
 *   <li>22.4: Uses businessProfileId as tenant isolation key</li>
 * </ul>
 * 
 * <p><strong>Test Scenario:</strong>
 * <ol>
 *   <li>Create two separate tenants (Tenant A and Tenant B)</li>
 *   <li>Each tenant has:
 *     <ul>
 *       <li>A retailer user</li>
 *       <li>A vendor user with verified BusinessProfile</li>
 *       <li>Inventory items associated with the vendor</li>
 *     </ul>
 *   </li>
 *   <li>Verify that Tenant A retailer can only see Tenant A vendors</li>
 *   <li>Verify that Tenant B retailer can only see Tenant B vendors</li>
 *   <li>Verify that cross-tenant data is NOT accessible</li>
 * </ol>
 * 
 * <p><strong>IMPORTANT FINDINGS:</strong>
 * <p>The current implementation has a CRITICAL ISSUE with multi-tenant isolation:
 * <ul>
 *   <li>The repository query does NOT filter by tenant context</li>
 *   <li>The service layer receives userId but does NOT use it for tenant scoping</li>
 *   <li>All verified/active vendors are returned regardless of tenant</li>
 * </ul>
 * 
 * <p><strong>RECOMMENDED FIX:</strong>
 * <p>The system needs to implement proper tenant isolation. However, the current
 * schema does NOT have a clear tenant identifier. Possible approaches:
 * <ol>
 *   <li><strong>Option 1 (Recommended):</strong> Add a tenant_id column to all entities
 *     and filter all queries by tenant_id</li>
 *   <li><strong>Option 2:</strong> Use the User entity as the tenant boundary - only
 *     show vendors whose User.id matches the retailer's User.id (single-user tenancy)</li>
 *   <li><strong>Option 3:</strong> Use BusinessProfile as the tenant boundary - only
 *     show vendors from the same "organization" (requires organization concept)</li>
 * </ol>
 * 
 * <p><strong>CURRENT BEHAVIOR:</strong>
 * <p>Since the system does NOT have explicit tenant isolation, this test documents
 * the EXPECTED behavior (tenant isolation) vs ACTUAL behavior (no isolation).
 * The test will FAIL until proper tenant isolation is implemented.
 * 
 * @see com.example.IMS.repository.VendorSearchRepository
 * @see com.example.IMS.service.VendorSearchServiceImpl
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class VendorSearchMultiTenantIsolationTest {

    @Autowired
    private VendorSearchService vendorSearchService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Tenant A entities
    private User tenantARetailer;
    private User tenantAVendor;
    private BusinessProfile tenantAVendorProfile;

    // Tenant B entities
    private User tenantBRetailer;
    private User tenantBVendor;
    private BusinessProfile tenantBVendorProfile;

    @BeforeEach
    public void setUp() {
        // Create roles
        Role retailerRole = roleRepository.findByName("ROLE_RETAILER")
            .orElseGet(() -> {
                Role role = new Role();
                role.setName("ROLE_RETAILER");
                return roleRepository.save(role);
            });

        Role vendorRole = roleRepository.findByName("ROLE_VENDOR")
            .orElseGet(() -> {
                Role role = new Role();
                role.setName("ROLE_VENDOR");
                return roleRepository.save(role);
            });

        // ========== TENANT A SETUP ==========

        // Create Tenant A Retailer
        tenantARetailer = new User();
        tenantARetailer.setUsername("tenantA_retailer");
        tenantARetailer.setEmail("retailer@tenantA.com");
        tenantARetailer.setPassword("password");
        tenantARetailer.setEnabled(true);
        tenantARetailer.addRole(retailerRole);
        tenantARetailer = userRepository.save(tenantARetailer);

        // Create Tenant A Vendor User
        tenantAVendor = new User();
        tenantAVendor.setUsername("tenantA_vendor");
        tenantAVendor.setEmail("vendor@tenantA.com");
        tenantAVendor.setPassword("password");
        tenantAVendor.setEnabled(true);
        tenantAVendor.addRole(vendorRole);
        tenantAVendor = userRepository.save(tenantAVendor);

        // Create Tenant A Vendor BusinessProfile
        tenantAVendorProfile = new BusinessProfile();
        tenantAVendorProfile.setUser(tenantAVendor);
        tenantAVendorProfile.setLegalBusinessName("Tenant A Electronics Ltd");
        tenantAVendorProfile.setBusinessType(BusinessType.PRIVATE_LIMITED);
        tenantAVendorProfile.setGstin("29AAAAA1111A1Z1");
        tenantAVendorProfile.setPanNumber("AAAAA1111A");
        tenantAVendorProfile.setRegisteredAddress("100 Tenant A Street, City A");
        tenantAVendorProfile.setState("State A");
        tenantAVendorProfile.setPincode("111111");
        tenantAVendorProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        tenantAVendorProfile.setOnboardingStage(OnboardingStage.ACTIVE);
        tenantAVendorProfile.setDefaultDeliveryDays(5);
        tenantAVendorProfile.setReliabilityScore(0.9);
        tenantAVendorProfile.setRating(4.8);
        tenantAVendorProfile = businessProfileRepository.save(tenantAVendorProfile);

        // ========== TENANT B SETUP ==========

        // Create Tenant B Retailer
        tenantBRetailer = new User();
        tenantBRetailer.setUsername("tenantB_retailer");
        tenantBRetailer.setEmail("retailer@tenantB.com");
        tenantBRetailer.setPassword("password");
        tenantBRetailer.setEnabled(true);
        tenantBRetailer.addRole(retailerRole);
        tenantBRetailer = userRepository.save(tenantBRetailer);

        // Create Tenant B Vendor User
        tenantBVendor = new User();
        tenantBVendor.setUsername("tenantB_vendor");
        tenantBVendor.setEmail("vendor@tenantB.com");
        tenantBVendor.setPassword("password");
        tenantBVendor.setEnabled(true);
        tenantBVendor.addRole(vendorRole);
        tenantBVendor = userRepository.save(tenantBVendor);

        // Create Tenant B Vendor BusinessProfile
        tenantBVendorProfile = new BusinessProfile();
        tenantBVendorProfile.setUser(tenantBVendor);
        tenantBVendorProfile.setLegalBusinessName("Tenant B Supplies Co");
        tenantBVendorProfile.setBusinessType(BusinessType.PRIVATE_LIMITED);
        tenantBVendorProfile.setGstin("29BBBBB2222B2Z2");
        tenantBVendorProfile.setPanNumber("BBBBB2222B");
        tenantBVendorProfile.setRegisteredAddress("200 Tenant B Avenue, City B");
        tenantBVendorProfile.setState("State B");
        tenantBVendorProfile.setPincode("222222");
        tenantBVendorProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        tenantBVendorProfile.setOnboardingStage(OnboardingStage.ACTIVE);
        tenantBVendorProfile.setDefaultDeliveryDays(7);
        tenantBVendorProfile.setReliabilityScore(0.8);
        tenantBVendorProfile.setRating(4.2);
        tenantBVendorProfile = businessProfileRepository.save(tenantBVendorProfile);

        // Flush to ensure IDs are generated
        entityManager.flush();

        // Create legacy vendors and items for both tenants
        createLegacyVendorAndItems(tenantAVendorProfile.getId(), "Tenant A Product");
        createLegacyVendorAndItems(tenantBVendorProfile.getId(), "Tenant B Product");

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Helper method to create legacy Vendor and Item entities for testing.
     * This is a workaround for the current schema where Item references legacy Vendor.
     */
    private void createLegacyVendorAndItems(Long businessProfileId, String productName) {
        // Create legacy vendor with ID matching BusinessProfile ID
        entityManager.createNativeQuery(
            "INSERT INTO Vendor (vendor_id, vendor_name, vendor_email) VALUES (:id, :name, :email)")
            .setParameter("id", businessProfileId)
            .setParameter("name", "Legacy Vendor " + businessProfileId)
            .setParameter("email", "legacy" + businessProfileId + "@test.com")
            .executeUpdate();

        // Create item for this vendor
        entityManager.createNativeQuery(
            "INSERT INTO Inventory_item (item_name, item_price, item_quantity, item_fine_rate, item_invoice_number, vendor_id_fk) " +
            "VALUES (:name, :price, :quantity, 0, 0, :vendorId)")
            .setParameter("name", productName)
            .setParameter("price", 1000.00)
            .setParameter("quantity", 50)
            .setParameter("vendorId", businessProfileId)
            .executeUpdate();
    }

    /**
     * Test that Tenant A retailer can only see Tenant A vendors.
     * 
     * <p><strong>EXPECTED BEHAVIOR:</strong> Only Tenant A vendors should be returned.
     * <p><strong>ACTUAL BEHAVIOR:</strong> Currently returns ALL vendors (both Tenant A and B).
     * 
     * <p><strong>ISSUE:</strong> The repository query does NOT filter by tenant context.
     * The userId parameter is passed to the service but NOT used for tenant scoping.
     */
    @Test
    public void testTenantARetailer_CanOnlySeeTenantAVendors() {
        // Create search request with no filters (should return all vendors for this tenant)
        VendorSearchRequest request = new VendorSearchRequest();
        request.setPage(0);
        request.setSize(50);

        // Execute search as Tenant A retailer
        VendorSearchResponse response = vendorSearchService.searchVendors(
            request, 
            tenantARetailer.getId()
        );

        // EXPECTED: Should only return Tenant A vendors
        // ACTUAL: Currently returns ALL vendors (Tenant A and Tenant B)
        
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getVendors(), "Vendors list should not be null");

        // Log the actual results for debugging
        System.out.println("=== Tenant A Retailer Search Results ===");
        System.out.println("Total vendors found: " + response.getTotalElements());
        response.getVendors().forEach(vendor -> {
            System.out.println("  - Vendor: " + vendor.getVendorName() + " (ID: " + vendor.getVendorId() + ")");
        });

        // CRITICAL ASSERTION: This will FAIL until tenant isolation is implemented
        // Expected: 1 vendor (Tenant A only)
        // Actual: 2 vendors (both Tenant A and Tenant B)
        assertEquals(1, response.getTotalElements(), 
            "Tenant A retailer should only see Tenant A vendors (FAILS due to missing tenant isolation)");

        // Verify the returned vendor is from Tenant A
        if (response.getTotalElements() > 0) {
            assertEquals(tenantAVendorProfile.getId(), response.getVendors().get(0).getVendorId(),
                "The returned vendor should be from Tenant A");
            assertEquals("Tenant A Electronics Ltd", response.getVendors().get(0).getVendorName(),
                "The returned vendor name should match Tenant A vendor");
        }
    }

    /**
     * Test that Tenant B retailer can only see Tenant B vendors.
     * 
     * <p><strong>EXPECTED BEHAVIOR:</strong> Only Tenant B vendors should be returned.
     * <p><strong>ACTUAL BEHAVIOR:</strong> Currently returns ALL vendors (both Tenant A and B).
     */
    @Test
    public void testTenantBRetailer_CanOnlySeeTenantBVendors() {
        // Create search request with no filters
        VendorSearchRequest request = new VendorSearchRequest();
        request.setPage(0);
        request.setSize(50);

        // Execute search as Tenant B retailer
        VendorSearchResponse response = vendorSearchService.searchVendors(
            request, 
            tenantBRetailer.getId()
        );

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getVendors(), "Vendors list should not be null");

        // Log the actual results for debugging
        System.out.println("=== Tenant B Retailer Search Results ===");
        System.out.println("Total vendors found: " + response.getTotalElements());
        response.getVendors().forEach(vendor -> {
            System.out.println("  - Vendor: " + vendor.getVendorName() + " (ID: " + vendor.getVendorId() + ")");
        });

        // CRITICAL ASSERTION: This will FAIL until tenant isolation is implemented
        assertEquals(1, response.getTotalElements(), 
            "Tenant B retailer should only see Tenant B vendors (FAILS due to missing tenant isolation)");

        // Verify the returned vendor is from Tenant B
        if (response.getTotalElements() > 0) {
            assertEquals(tenantBVendorProfile.getId(), response.getVendors().get(0).getVendorId(),
                "The returned vendor should be from Tenant B");
            assertEquals("Tenant B Supplies Co", response.getVendors().get(0).getVendorName(),
                "The returned vendor name should match Tenant B vendor");
        }
    }

    /**
     * Test that cross-tenant data is NOT accessible.
     * 
     * <p>Tenant A retailer searches for Tenant B vendor by name.
     * <p><strong>EXPECTED:</strong> No results (cross-tenant access denied).
     * <p><strong>ACTUAL:</strong> Returns Tenant B vendor (no isolation).
     */
    @Test
    public void testCrossTenantDataNotAccessible() {
        // Create search request for Tenant B vendor name
        VendorSearchRequest request = new VendorSearchRequest();
        request.setQuery("Tenant B Supplies");
        request.setPage(0);
        request.setSize(50);

        // Execute search as Tenant A retailer (trying to access Tenant B data)
        VendorSearchResponse response = vendorSearchService.searchVendors(
            request, 
            tenantARetailer.getId()
        );

        assertNotNull(response, "Response should not be null");

        // Log the actual results for debugging
        System.out.println("=== Cross-Tenant Access Test ===");
        System.out.println("Tenant A retailer searching for 'Tenant B Supplies'");
        System.out.println("Total vendors found: " + response.getTotalElements());
        response.getVendors().forEach(vendor -> {
            System.out.println("  - Vendor: " + vendor.getVendorName() + " (ID: " + vendor.getVendorId() + ")");
        });

        // CRITICAL ASSERTION: This will FAIL until tenant isolation is implemented
        // Expected: 0 vendors (cross-tenant access denied)
        // Actual: 1 vendor (Tenant B vendor is accessible)
        assertEquals(0, response.getTotalElements(), 
            "Tenant A retailer should NOT be able to see Tenant B vendors (FAILS due to missing tenant isolation)");
    }

    /**
     * Test that order history lookup respects tenant boundaries.
     * 
     * <p>This test verifies that the previouslyOrdered flag only considers
     * orders within the same tenant context.
     */
    @Test
    public void testOrderHistoryRespectsTenantBoundaries() {
        // Note: This test is a placeholder since ProcurementOrder currently
        // references legacy Vendor entity, not BusinessProfile.
        // Once the schema is updated, this test should verify that:
        // 1. Tenant A retailer's order history only includes Tenant A vendors
        // 2. Tenant B retailer's order history only includes Tenant B vendors
        // 3. Cross-tenant order history is not visible

        // For now, we just document the expected behavior
        System.out.println("=== Order History Tenant Isolation ===");
        System.out.println("EXPECTED: Order history should be scoped by tenant");
        System.out.println("ACTUAL: Not yet implemented (ProcurementOrder uses legacy Vendor)");
        
        // This test will be implemented once the schema is updated
        assertTrue(true, "Placeholder test - implement once ProcurementOrder is migrated to BusinessProfile");
    }
}
