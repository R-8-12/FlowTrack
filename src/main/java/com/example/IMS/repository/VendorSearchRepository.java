package com.example.IMS.repository;

import com.example.IMS.model.BusinessProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Repository for vendor search operations
 * Uses custom queries with JOIN FETCH to avoid N+1 problems
 * 
 * IMPORTANT: This repository queries only BusinessProfile entities with ROLE_VENDOR
 * It does NOT use the legacy Vendor entity (Requirement 25.1)
 */
@Repository
public interface VendorSearchRepository extends JpaRepository<BusinessProfile, Long> {
    
    /**
     * Search vendors with dynamic filtering
     * Uses JPQL with JOIN FETCH for performance
     * 
     * NOTE: The Item entity currently references the legacy Vendor entity via vendor_id_fk.
     * This query assumes that vendor_id_fk can be matched to BusinessProfile IDs.
     * A proper implementation would require either:
     * 1. Adding a business_profile_id column to Item table, OR
     * 2. Creating a VendorInventory junction table, OR
     * 3. Migrating legacy Vendor IDs to BusinessProfile IDs
     * 
     * For now, this query uses a LEFT JOIN with Item where i.vendor.id = bp.id
     * This will work if the vendor_id_fk in Item matches BusinessProfile IDs.
     * 
     * Note: Item.price is a double, so we use Double parameters instead of BigDecimal
     * 
     * @param query Product name or vendor name (case-insensitive)
     * @param minPrice Minimum price filter (nullable)
     * @param maxPrice Maximum price filter (nullable)
     * @param minQuantity Minimum stock quantity (nullable)
     * @param pageable Pagination parameters
     * @return Page of BusinessProfile entities
     */
    @Query(value = "SELECT DISTINCT bp FROM BusinessProfile bp " +
           "LEFT JOIN FETCH bp.user u " +
           "LEFT JOIN Item i ON i.vendor.id = bp.id " +
           "WHERE bp.verificationStatus = 'VERIFIED' " +
           "AND bp.onboardingStage = 'ACTIVE' " +
           "AND u.enabled = true " +
           "AND (:query IS NULL OR :query = '' OR " +
           "     LOWER(bp.legalBusinessName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
           "AND (:minQuantity IS NULL OR i.quantity >= :minQuantity)",
           countQuery = "SELECT COUNT(DISTINCT bp) FROM BusinessProfile bp " +
           "LEFT JOIN bp.user u " +
           "LEFT JOIN Item i ON i.vendor.id = bp.id " +
           "WHERE bp.verificationStatus = 'VERIFIED' " +
           "AND bp.onboardingStage = 'ACTIVE' " +
           "AND u.enabled = true " +
           "AND (:query IS NULL OR :query = '' OR " +
           "     LOWER(bp.legalBusinessName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
           "AND (:minQuantity IS NULL OR i.quantity >= :minQuantity)")
    Page<BusinessProfile> searchVendors(
        @Param("query") String query,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("minQuantity") Integer minQuantity,
        Pageable pageable
    );
    
    /**
     * Count total matching vendors (for pagination metadata)
     * 
     * This count query mirrors the search query logic but returns a count
     * instead of the full result set.
     * Note: Cannot use JOIN FETCH in count queries
     * Note: Item.price is a double, so we use Double parameters instead of BigDecimal
     * 
     * @param query Product name or vendor name (case-insensitive)
     * @param minPrice Minimum price filter (nullable)
     * @param maxPrice Maximum price filter (nullable)
     * @param minQuantity Minimum stock quantity (nullable)
     * @return Total count of matching vendors
     */
    @Query(value = "SELECT COUNT(DISTINCT bp) FROM BusinessProfile bp " +
           "LEFT JOIN bp.user u " +
           "LEFT JOIN Item i ON i.vendor.id = bp.id " +
           "WHERE bp.verificationStatus = 'VERIFIED' " +
           "AND bp.onboardingStage = 'ACTIVE' " +
           "AND u.enabled = true " +
           "AND (:query IS NULL OR :query = '' OR " +
           "     LOWER(bp.legalBusinessName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
           "AND (:minQuantity IS NULL OR i.quantity >= :minQuantity)",
           countQuery = "SELECT COUNT(DISTINCT bp) FROM BusinessProfile bp " +
           "LEFT JOIN bp.user u " +
           "LEFT JOIN Item i ON i.vendor.id = bp.id " +
           "WHERE bp.verificationStatus = 'VERIFIED' " +
           "AND bp.onboardingStage = 'ACTIVE' " +
           "AND u.enabled = true " +
           "AND (:query IS NULL OR :query = '' OR " +
           "     LOWER(bp.legalBusinessName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
           "AND (:minQuantity IS NULL OR i.quantity >= :minQuantity)")
    long countMatchingVendors(
        @Param("query") String query,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("minQuantity") Integer minQuantity
    );
}
