package com.example.IMS.service;

import com.example.IMS.dto.*;
import com.example.IMS.model.BusinessProfile;
import com.example.IMS.model.Item;
import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.model.enums.Badge;
import com.example.IMS.repository.VendorSearchRepository;
import com.example.IMS.repository.ProcurementOrderRepository;
import com.example.IMS.service.ranking.RankingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of vendor search service.
 * Handles search orchestration, ranking, and badge assignment.
 * 
 * <p>This service orchestrates the complete vendor search workflow:
 * <ol>
 *   <li>Validates search request parameters</li>
 *   <li>Builds pagination and sorting configuration</li>
 *   <li>Queries repository with filters</li>
 *   <li>Maps BusinessProfile entities to VendorCardDTO</li>
 *   <li>Applies ranking strategy for relevance sorting</li>
 *   <li>Assigns badges to highlight vendor strengths</li>
 *   <li>Returns paginated response with metadata</li>
 * </ol>
 * 
 * <p>All operations respect multi-tenant boundaries and only return
 * verified, active vendors.
 * 
 * @see VendorSearchService
 * @see VendorSearchRepository
 * @see RankingStrategy
 */
@Service
@Transactional(readOnly = true)
public class VendorSearchServiceImpl implements VendorSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(VendorSearchServiceImpl.class);
    
    @Autowired
    private VendorSearchRepository vendorSearchRepository;
    
    @Autowired
    private ProcurementOrderRepository procurementOrderRepository;
    
    @Autowired
    private RankingStrategy rankingStrategy;
    
    /**
     * Search for vendors based on criteria with pagination support.
     * 
     * @param request the search request containing query string, filters, 
     *                sorting preferences, and pagination parameters
     * @param userId the authenticated retailer user ID for tenant isolation
     *               and order history lookup
     * @return a VendorSearchResponse containing the list of matching vendor cards
     *         and pagination metadata
     * @throws IllegalArgumentException if the request contains invalid parameters
     */
    @Override
    public VendorSearchResponse searchVendors(VendorSearchRequest request, Long userId) {
        // Start timing for performance monitoring
        long startTime = System.currentTimeMillis();
        
        // Log search request with all parameters for analytics
        logger.info("Vendor search initiated - User: {}, Query: '{}', MinPrice: {}, MaxPrice: {}, " +
                   "MaxDelivery: {}, MinQuantity: {}, SortBy: {}, Page: {}, Size: {}",
                   userId, request.getQuery(), request.getMinPrice(), request.getMaxPrice(),
                   request.getMaxDeliveryDays(), request.getMinQuantity(), request.getSortBy(),
                   request.getPage(), request.getSize());
        
        try {
            // Validate request
            request.validatePriceRange();
            
            // Build pageable with sorting
            Pageable pageable = buildPageable(request);
            
            // Convert BigDecimal prices to Double for repository query
            Double minPrice = request.getMinPrice() != null ? request.getMinPrice().doubleValue() : null;
            Double maxPrice = request.getMaxPrice() != null ? request.getMaxPrice().doubleValue() : null;
            
            // Execute search query
            long queryStartTime = System.currentTimeMillis();
            Page<BusinessProfile> vendorPage = vendorSearchRepository.searchVendors(
                request.getQuery(),
                minPrice,
                maxPrice,
                request.getMinQuantity(),
                pageable
            );
            long queryDuration = System.currentTimeMillis() - queryStartTime;
            logger.debug("Database query completed in {} ms", queryDuration);
            
            // Convert to DTOs
            List<VendorCardDTO> vendorCards = vendorPage.getContent().stream()
                .map(bp -> mapToVendorCard(bp, userId))
                .collect(Collectors.toList());
            
            // Apply ranking if sortBy is "relevance"
            if ("relevance".equals(request.getSortBy())) {
                long rankingStartTime = System.currentTimeMillis();
                vendorCards = rankingStrategy.rankVendors(vendorCards);
                long rankingDuration = System.currentTimeMillis() - rankingStartTime;
                logger.debug("Ranking algorithm completed in {} ms", rankingDuration);
            }
            
            // Assign badges
            assignBadges(vendorCards);
            
            // Build response
            VendorSearchResponse response = new VendorSearchResponse(
                vendorCards,
                vendorPage.getNumber(),
                vendorPage.getTotalPages(),
                vendorPage.getTotalElements(),
                vendorPage.getSize()
            );
            
            // Calculate total execution time
            long totalDuration = System.currentTimeMillis() - startTime;
            
            // Log successful completion with metrics
            logger.info("Vendor search completed successfully - User: {}, Results: {}/{}, " +
                       "Page: {}/{}, ExecutionTime: {} ms",
                       userId, vendorCards.size(), vendorPage.getTotalElements(),
                       vendorPage.getNumber() + 1, vendorPage.getTotalPages(), totalDuration);
            
            // Log performance warning if search is slow
            if (totalDuration > 2000) {
                logger.warn("Slow vendor search detected - User: {}, ExecutionTime: {} ms (threshold: 2000 ms)",
                           userId, totalDuration);
            }
            
            return response;
            
        } catch (IllegalArgumentException e) {
            long totalDuration = System.currentTimeMillis() - startTime;
            logger.warn("Vendor search validation failed - User: {}, Error: '{}', ExecutionTime: {} ms",
                       userId, e.getMessage(), totalDuration);
            throw e;
        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - startTime;
            logger.error("Vendor search failed with unexpected error - User: {}, ExecutionTime: {} ms",
                        userId, totalDuration, e);
            throw new RuntimeException("Vendor search failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build Pageable with sorting configuration based on request parameters.
     * 
     * <p>Maps sortBy values to appropriate entity fields:
     * <ul>
     *   <li>"price" → sorts by Item.price (handled in repository)</li>
     *   <li>"delivery" → sorts by BusinessProfile.defaultDeliveryDays</li>
     *   <li>"rating" → sorts by BusinessProfile.rating</li>
     *   <li>"relevance" → no database sorting (done in-memory after ranking)</li>
     * </ul>
     * 
     * @param request the search request containing sortBy and sortDirection
     * @return Pageable object with pagination and sorting configuration
     */
    private Pageable buildPageable(VendorSearchRequest request) {
        Sort sort;
        String sortBy = request.getSortBy();
        String direction = request.getEffectiveSortDirection();
        
        switch (sortBy) {
            case "price":
                // Price sorting is handled in repository query via Item join
                sort = Sort.by(Sort.Direction.fromString(direction), "price");
                break;
            case "delivery":
                sort = Sort.by(Sort.Direction.fromString(direction), "defaultDeliveryDays");
                break;
            case "rating":
                sort = Sort.by(Sort.Direction.fromString(direction), "rating");
                break;
            case "relevance":
            default:
                // Relevance sorting done in-memory after ranking
                sort = Sort.unsorted();
                break;
        }
        
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
    
    /**
     * Map BusinessProfile entity to VendorCardDTO.
     * 
     * <p>Extracts vendor information from BusinessProfile and associated Item entities.
     * For vendors with multiple items, uses the lowest price and sum of quantities.
     * 
     * @param bp the BusinessProfile entity representing the vendor
     * @param retailerUserId the retailer user ID for order history lookup
     * @return VendorCardDTO populated with vendor information
     */
    private VendorCardDTO mapToVendorCard(BusinessProfile bp, Long retailerUserId) {
        // Note: In the current schema, Item references legacy Vendor entity via vendor_id_fk
        // This is a placeholder implementation that uses BusinessProfile metadata
        // A proper implementation would require:
        // 1. Adding business_profile_id to Item table, OR
        // 2. Creating a VendorInventory junction table, OR
        // 3. Migrating legacy Vendor IDs to BusinessProfile IDs
        
        // For now, use default values and BusinessProfile metadata
        // TODO: Replace with actual Item data once schema is updated
        
        return new VendorCardDTO.Builder()
            .vendorId(bp.getId())
            .vendorName(bp.getLegalBusinessName())
            .pricePerUnit(BigDecimal.valueOf(100.00))  // TODO: Get from Item entity
            .availableQuantity(50)  // TODO: Get from Item entity
            .deliveryDays(bp.getDefaultDeliveryDays() != null ? bp.getDefaultDeliveryDays() : 7)
            .reliabilityScore(bp.getReliabilityScore() != null ? bp.getReliabilityScore() : 0.0)
            .rating(bp.getRating() != null ? bp.getRating() : 0.0)
            .verified(true)  // Always true since filtered by repository
            .location(formatLocation(bp))
            .previouslyOrdered(hasPreviousOrders(bp.getId(), retailerUserId))
            .badge(Badge.NONE)  // Will be assigned later by assignBadges()
            .build();
    }
    
    /**
     * Format location as "City, State" from BusinessProfile address.
     * 
     * <p>Extracts city from registeredAddress and combines with state.
     * This is a simplified implementation that uses the state field directly.
     * 
     * @param bp the BusinessProfile entity
     * @return formatted location string in "City, State" format
     */
    private String formatLocation(BusinessProfile bp) {
        // Simplified implementation: just use state
        // TODO: Parse city from registeredAddress for more accurate location
        String state = bp.getState();
        
        if (state != null && !state.isEmpty()) {
            return "City, " + state;
        }
        
        return "Location not specified";
    }
    
    /**
     * Check if retailer has previous orders with vendor.
     * 
     * <p>Queries ProcurementOrder table for completed or supplied orders
     * matching the retailer user ID and vendor BusinessProfile ID.
     * 
     * <p>Note: Current ProcurementOrder entity references legacy Vendor entity.
     * This implementation assumes vendor_id in ProcurementOrder can be matched
     * to BusinessProfile IDs. A proper implementation would require schema updates.
     * 
     * @param vendorBusinessProfileId the BusinessProfile ID of the vendor
     * @param retailerUserId the User ID of the retailer
     * @return true if at least one completed order exists, false otherwise
     */
    private boolean hasPreviousOrders(Long vendorBusinessProfileId, Long retailerUserId) {
        // Note: Current ProcurementOrder entity references legacy Vendor entity
        // This is a placeholder implementation that returns false
        // TODO: Update once ProcurementOrder is migrated to use BusinessProfile
        
        // Query for orders matching retailer and vendor with completed status
        List<ProcurementOrder> orders = procurementOrderRepository.findAll().stream()
            .filter(order -> order.getRetailer() != null && order.getRetailer().getId().equals(retailerUserId))
            .filter(order -> order.getVendor() != null && order.getVendor().getId() == vendorBusinessProfileId)
            .filter(order -> order.getStatus() == ProcurementOrderStatus.SUPPLIED)
            .collect(Collectors.toList());
        
        return !orders.isEmpty();
    }
    
    /**
     * Assign badges to top vendors in each category.
     * 
     * <p>Assigns at most one badge per vendor based on the following criteria:
     * <ul>
     *   <li>BEST_PRICE: Vendor with lowest pricePerUnit</li>
     *   <li>FAST_DELIVERY: Vendor with lowest deliveryDays (if different from best price)</li>
     *   <li>HIGH_RELIABILITY: Vendor with highest reliabilityScore (if different from previous two)</li>
     * </ul>
     * 
     * <p>When multiple vendors tie for a badge criterion, the badge is assigned
     * to the first vendor in the sorted result set.
     * 
     * @param vendors the list of vendor cards to assign badges to (modified in place)
     */
    private void assignBadges(List<VendorCardDTO> vendors) {
        if (vendors.isEmpty()) {
            return;
        }
        
        // Find best price vendor
        VendorCardDTO bestPrice = vendors.stream()
            .min(Comparator.comparing(VendorCardDTO::getPricePerUnit))
            .orElse(null);
        if (bestPrice != null) {
            bestPrice.setBadge(Badge.BEST_PRICE);
        }
        
        // Find fastest delivery vendor (must be different from best price)
        VendorCardDTO fastestDelivery = vendors.stream()
            .filter(v -> !v.equals(bestPrice))
            .min(Comparator.comparing(VendorCardDTO::getDeliveryDays))
            .orElse(null);
        if (fastestDelivery != null) {
            fastestDelivery.setBadge(Badge.FAST_DELIVERY);
        }
        
        // Find highest reliability vendor (must be different from previous two)
        VendorCardDTO highestReliability = vendors.stream()
            .filter(v -> !v.equals(bestPrice) && !v.equals(fastestDelivery))
            .max(Comparator.comparing(VendorCardDTO::getReliabilityScore))
            .orElse(null);
        if (highestReliability != null) {
            highestReliability.setBadge(Badge.HIGH_RELIABILITY);
        }
    }
}
