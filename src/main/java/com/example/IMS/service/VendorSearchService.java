package com.example.IMS.service;

import com.example.IMS.dto.VendorSearchRequest;
import com.example.IMS.dto.VendorSearchResponse;

/**
 * Service interface for vendor search operations.
 * 
 * <p>This service orchestrates the vendor search workflow, including:
 * <ul>
 *   <li>Query processing and input validation</li>
 *   <li>Multi-criteria filtering (price, delivery time, stock availability)</li>
 *   <li>Vendor ranking using configurable strategies</li>
 *   <li>Badge assignment for highlighting vendor strengths</li>
 *   <li>Order history lookup for personalization</li>
 *   <li>Entity-to-DTO mapping for API responses</li>
 * </ul>
 * 
 * <p>The service ensures multi-tenant data isolation by scoping all queries
 * to the authenticated retailer's context. Only verified and active vendors
 * (BusinessProfile with ROLE_VENDOR, verificationStatus=VERIFIED, 
 * onboardingStage=ACTIVE) are included in search results.
 * 
 * <p>This interface supports future extensibility for AI-powered recommendations
 * through the modular ranking strategy pattern.
 * 
 * @see VendorSearchRequest
 * @see VendorSearchResponse
 * @see com.example.IMS.service.ranking.RankingStrategy
 */
public interface VendorSearchService {
    
    /**
     * Search for vendors based on multiple criteria with pagination support.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Validates the search request parameters (price range, pagination)</li>
     *   <li>Queries the repository with filters (query string, price, delivery, stock)</li>
     *   <li>Applies ranking algorithm when sortBy is "relevance"</li>
     *   <li>Assigns badges (BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY) to top vendors</li>
     *   <li>Checks order history to set previouslyOrdered flags</li>
     *   <li>Maps BusinessProfile entities to VendorCardDTO objects</li>
     *   <li>Returns paginated response with metadata</li>
     * </ol>
     * 
     * <p>The search respects multi-tenant boundaries by filtering results
     * to vendors within the same tenant context as the retailer user.
     * 
     * <p><strong>Security:</strong> This method assumes the caller has already
     * verified that the user has ROLE_RETAILER authority. The userId parameter
     * is used for tenant isolation and personalization features.
     * 
     * <p><strong>Performance:</strong> The implementation uses database indexes
     * and JOIN FETCH strategies to ensure response times under 2 seconds for
     * result sets up to 1000 vendors.
     * 
     * @param request the search request containing query string, filters, 
     *                sorting preferences, and pagination parameters. Must not be null.
     *                All filter fields are optional except page and size.
     * @param userId the authenticated retailer user ID for tenant isolation
     *               and order history lookup. Must not be null.
     * @return a VendorSearchResponse containing the list of matching vendor cards,
     *         pagination metadata (currentPage, totalPages, totalElements, pageSize),
     *         and sorted according to the specified criteria. Never returns null.
     * @throws IllegalArgumentException if the request contains invalid parameters
     *         (e.g., minPrice > maxPrice, negative values, invalid sortBy/sortDirection)
     * @throws org.springframework.security.access.AccessDeniedException if the user
     *         lacks ROLE_RETAILER authority (should be prevented by controller layer)
     */
    VendorSearchResponse searchVendors(VendorSearchRequest request, Long userId);
}
