package com.example.IMS.service;

import com.example.IMS.dto.VendorCardDTO;
import com.example.IMS.dto.VendorSearchRequest;
import com.example.IMS.dto.VendorSearchResponse;
import com.example.IMS.model.BusinessProfile;
import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.model.User;
import com.example.IMS.model.enums.Badge;
import com.example.IMS.model.enums.OnboardingStage;
import com.example.IMS.model.enums.VerificationStatus;
import com.example.IMS.repository.ProcurementOrderRepository;
import com.example.IMS.repository.VendorSearchRepository;
import com.example.IMS.service.ranking.RankingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VendorSearchServiceImpl
 * Tests all service logic including search orchestration, badge assignment, and order history lookup
 * Requirements: 1.1-1.6, 7.1-7.8, 8.1-8.7, 9.1-9.6, 10.1-10.4, 12.1-12.11
 */
@ExtendWith(MockitoExtension.class)
public class VendorSearchServiceImplTest {

    @Mock
    private VendorSearchRepository vendorSearchRepository;

    @Mock
    private ProcurementOrderRepository procurementOrderRepository;

    @Mock
    private RankingStrategy rankingStrategy;

    @InjectMocks
    private VendorSearchServiceImpl vendorSearchService;

    private VendorSearchRequest validRequest;
    private BusinessProfile vendor1;
    private BusinessProfile vendor2;
    private BusinessProfile vendor3;
    private User user1;
    private Long retailerUserId;

    @BeforeEach
    public void setUp() {
        // Create valid search request
        validRequest = new VendorSearchRequest();
        validRequest.setQuery("laptop");
        validRequest.setMinPrice(BigDecimal.valueOf(1000.00));
        validRequest.setMaxPrice(BigDecimal.valueOf(50000.00));
        validRequest.setMinQuantity(10);
        validRequest.setSortBy("relevance");
        validRequest.setPage(0);
        validRequest.setSize(20);

        retailerUserId = 100L;

        // Create test users
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("vendor1");
        user1.setEnabled(true);

        // Create test vendors (BusinessProfiles)
        vendor1 = new BusinessProfile();
        vendor1.setId(1L);
        vendor1.setUser(user1);
        vendor1.setLegalBusinessName("Premium Electronics Ltd");
        vendor1.setVerificationStatus(VerificationStatus.VERIFIED);
        vendor1.setOnboardingStage(OnboardingStage.ACTIVE);
        vendor1.setDefaultDeliveryDays(5);
        vendor1.setReliabilityScore(0.85);
        vendor1.setRating(4.5);
        vendor1.setState("Karnataka");

        vendor2 = new BusinessProfile();
        vendor2.setId(2L);
        vendor2.setUser(user1);
        vendor2.setLegalBusinessName("Budget Supplies Co");
        vendor2.setVerificationStatus(VerificationStatus.VERIFIED);
        vendor2.setOnboardingStage(OnboardingStage.ACTIVE);
        vendor2.setDefaultDeliveryDays(7);
        vendor2.setReliabilityScore(0.75);
        vendor2.setRating(4.0);
        vendor2.setState("Maharashtra");

        vendor3 = new BusinessProfile();
        vendor3.setId(3L);
        vendor3.setUser(user1);
        vendor3.setLegalBusinessName("Fast Delivery Inc");
        vendor3.setVerificationStatus(VerificationStatus.VERIFIED);
        vendor3.setOnboardingStage(OnboardingStage.ACTIVE);
        vendor3.setDefaultDeliveryDays(3);
        vendor3.setReliabilityScore(0.90);
        vendor3.setRating(4.8);
        vendor3.setState("Tamil Nadu");
    }

    /**
     * Test searchVendors with valid request
     * Verifies response structure and pagination metadata
     */
    @Test
    public void testSearchVendors_WithValidRequest_ReturnsCorrectResponse() {
        // Arrange
        List<BusinessProfile> vendors = Arrays.asList(vendor1, vendor2);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 2);

        when(vendorSearchRepository.searchVendors(
            eq("laptop"),
            eq(1000.00),
            eq(50000.00),
            eq(10),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        // Mock ranking strategy to return same list
        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(validRequest, retailerUserId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getVendors().size());
        assertEquals(0, response.getCurrentPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getTotalElements());
        assertEquals(20, response.getPageSize());

        // Verify vendor cards are populated
        VendorCardDTO card1 = response.getVendors().get(0);
        assertNotNull(card1.getVendorId());
        assertNotNull(card1.getVendorName());
        assertNotNull(card1.getPricePerUnit());
        assertNotNull(card1.getDeliveryDays());
        assertNotNull(card1.getReliabilityScore());
        assertNotNull(card1.getRating());
        assertTrue(card1.getVerified());

        // Verify repository was called with correct parameters
        verify(vendorSearchRepository).searchVendors(
            eq("laptop"),
            eq(1000.00),
            eq(50000.00),
            eq(10),
            any(Pageable.class)
        );
    }

    /**
     * Test searchVendors with invalid price range
     * Should throw IllegalArgumentException
     */
    @Test
    public void testSearchVendors_WithInvalidPriceRange_ThrowsException() {
        // Arrange
        VendorSearchRequest invalidRequest = new VendorSearchRequest();
        invalidRequest.setMinPrice(BigDecimal.valueOf(50000.00));
        invalidRequest.setMaxPrice(BigDecimal.valueOf(1000.00));  // maxPrice < minPrice

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> vendorSearchService.searchVendors(invalidRequest, retailerUserId)
        );

        assertEquals("Invalid price range: minPrice cannot exceed maxPrice", exception.getMessage());

        // Verify repository was never called
        verify(vendorSearchRepository, never()).searchVendors(any(), any(), any(), any(), any());
    }

    /**
     * Test searchVendors with empty results
     * Verifies empty response structure
     */
    @Test
    public void testSearchVendors_WithEmptyResults_ReturnsEmptyResponse() {
        // Arrange
        Page<BusinessProfile> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(emptyPage);

        // Mock ranking strategy to return empty list
        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(validRequest, retailerUserId);

        // Assert
        assertNotNull(response);
        assertTrue(response.getVendors().isEmpty());
        assertEquals(0, response.getCurrentPage());
        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getTotalElements());
        assertEquals(20, response.getPageSize());

        // Verify ranking strategy was called (implementation calls it even for empty lists)
        verify(rankingStrategy).rankVendors(anyList());
    }

    /**
     * Test badge assignment with diverse vendors
     * Verifies correct badges are assigned to different vendors
     */
    @Test
    public void testSearchVendors_BadgeAssignment_WithDiverseVendors() {
        // Arrange
        List<BusinessProfile> vendors = Arrays.asList(vendor1, vendor2, vendor3);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 3);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        // Mock ranking strategy to return same list
        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(validRequest, retailerUserId);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getVendors().size());

        // Find vendors by name and check badges
        VendorCardDTO premiumElectronics = response.getVendors().stream()
            .filter(v -> "Premium Electronics Ltd".equals(v.getVendorName()))
            .findFirst()
            .orElse(null);
        assertNotNull(premiumElectronics);
        assertEquals(Badge.BEST_PRICE, premiumElectronics.getBadge());

        VendorCardDTO fastDelivery = response.getVendors().stream()
            .filter(v -> "Fast Delivery Inc".equals(v.getVendorName()))
            .findFirst()
            .orElse(null);
        assertNotNull(fastDelivery);
        // Fast Delivery Inc has fastest delivery (3 days) and highest reliability (0.90)
        // Since it can't have both badges, it should get FAST_DELIVERY
        assertTrue(fastDelivery.getBadge() == Badge.FAST_DELIVERY || fastDelivery.getBadge() == Badge.HIGH_RELIABILITY);

        // Verify at least one vendor has each badge type
        long bestPriceCount = response.getVendors().stream()
            .filter(v -> v.getBadge() == Badge.BEST_PRICE)
            .count();
        assertEquals(1, bestPriceCount);

        long fastDeliveryCount = response.getVendors().stream()
            .filter(v -> v.getBadge() == Badge.FAST_DELIVERY)
            .count();
        assertEquals(1, fastDeliveryCount);

        long highReliabilityCount = response.getVendors().stream()
            .filter(v -> v.getBadge() == Badge.HIGH_RELIABILITY)
            .count();
        assertEquals(1, highReliabilityCount);
    }

    /**
     * Test badge assignment with ties
     * Verifies first vendor gets badge when multiple vendors tie
     */
    @Test
    public void testSearchVendors_BadgeAssignment_WithTies() {
        // Arrange - Create vendors with identical prices
        vendor1.setDefaultDeliveryDays(5);
        vendor2.setDefaultDeliveryDays(5);  // Same delivery time as vendor1

        List<BusinessProfile> vendors = Arrays.asList(vendor1, vendor2);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 2);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(validRequest, retailerUserId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getVendors().size());

        // When there's a tie, first vendor in list should get the badge
        VendorCardDTO firstVendor = response.getVendors().get(0);
        assertNotEquals(Badge.NONE, firstVendor.getBadge());

        // Verify only one vendor has BEST_PRICE badge
        long bestPriceCount = response.getVendors().stream()
            .filter(v -> v.getBadge() == Badge.BEST_PRICE)
            .count();
        assertEquals(1, bestPriceCount);
    }

    /**
     * Test order history lookup
     * Verifies previouslyOrdered flag is set correctly
     */
    @Test
    public void testSearchVendors_OrderHistoryLookup_SetsPreviouslyOrderedFlag() {
        // Arrange
        List<BusinessProfile> vendors = Arrays.asList(vendor1, vendor2);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 2);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        // Create mock orders
        User retailer = new User();
        retailer.setId(retailerUserId);

        ProcurementOrder order1 = new ProcurementOrder();
        order1.setId(1L);
        order1.setRetailer(retailer);
        // Create a vendor entity with matching ID
        com.example.IMS.model.Vendor legacyVendor1 = new com.example.IMS.model.Vendor();
        legacyVendor1.setId(vendor1.getId());
        order1.setVendor(legacyVendor1);
        order1.setStatus(ProcurementOrderStatus.SUPPLIED);

        when(procurementOrderRepository.findAll()).thenReturn(Arrays.asList(order1));

        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(validRequest, retailerUserId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getVendors().size());

        // Vendor1 should have previouslyOrdered = true
        VendorCardDTO vendor1Card = response.getVendors().stream()
            .filter(v -> v.getVendorId().equals(vendor1.getId()))
            .findFirst()
            .orElse(null);
        assertNotNull(vendor1Card);
        assertTrue(vendor1Card.getPreviouslyOrdered());

        // Vendor2 should have previouslyOrdered = false
        VendorCardDTO vendor2Card = response.getVendors().stream()
            .filter(v -> v.getVendorId().equals(vendor2.getId()))
            .findFirst()
            .orElse(null);
        assertNotNull(vendor2Card);
        assertFalse(vendor2Card.getPreviouslyOrdered());
    }

    /**
     * Test entity to DTO mapping
     * Verifies all fields are populated correctly
     */
    @Test
    public void testSearchVendors_EntityToDtoMapping_AllFieldsPopulated() {
        // Arrange
        List<BusinessProfile> vendors = Arrays.asList(vendor1);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 1);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(validRequest, retailerUserId);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getVendors().size());

        VendorCardDTO card = response.getVendors().get(0);

        // Verify all fields are populated
        assertEquals(vendor1.getId(), card.getVendorId());
        assertEquals(vendor1.getLegalBusinessName(), card.getVendorName());
        assertNotNull(card.getPricePerUnit());
        assertTrue(card.getPricePerUnit().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(card.getAvailableQuantity());
        assertTrue(card.getAvailableQuantity() > 0);
        assertEquals(vendor1.getDefaultDeliveryDays(), card.getDeliveryDays());
        assertEquals(vendor1.getReliabilityScore(), card.getReliabilityScore());
        assertEquals(vendor1.getRating(), card.getRating());
        assertTrue(card.getVerified());
        assertNotNull(card.getLocation());
        assertTrue(card.getLocation().contains(vendor1.getState()));
        assertNotNull(card.getPreviouslyOrdered());
        assertNotNull(card.getBadge());
    }

    /**
     * Test buildPageable with sortBy="price"
     */
    @Test
    public void testSearchVendors_BuildPageable_WithSortByPrice() {
        // Arrange
        VendorSearchRequest request = new VendorSearchRequest();
        request.setSortBy("price");
        request.setSortDirection("asc");
        request.setPage(0);
        request.setSize(20);

        List<BusinessProfile> vendors = Arrays.asList(vendor1);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 1);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        vendorSearchService.searchVendors(request, retailerUserId);

        // Assert - Verify repository was called with correct Pageable
        verify(vendorSearchRepository).searchVendors(
            any(),
            any(),
            any(),
            any(),
            argThat(pageable -> 
                pageable.getPageNumber() == 0 &&
                pageable.getPageSize() == 20 &&
                pageable.getSort().isSorted()
            )
        );
    }

    /**
     * Test buildPageable with sortBy="delivery"
     */
    @Test
    public void testSearchVendors_BuildPageable_WithSortByDelivery() {
        // Arrange
        VendorSearchRequest request = new VendorSearchRequest();
        request.setSortBy("delivery");
        request.setSortDirection("asc");
        request.setPage(0);
        request.setSize(20);

        List<BusinessProfile> vendors = Arrays.asList(vendor1);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 1);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        vendorSearchService.searchVendors(request, retailerUserId);

        // Assert
        verify(vendorSearchRepository).searchVendors(
            any(),
            any(),
            any(),
            any(),
            argThat(pageable -> 
                pageable.getPageNumber() == 0 &&
                pageable.getPageSize() == 20 &&
                pageable.getSort().isSorted()
            )
        );
    }

    /**
     * Test buildPageable with sortBy="rating"
     */
    @Test
    public void testSearchVendors_BuildPageable_WithSortByRating() {
        // Arrange
        VendorSearchRequest request = new VendorSearchRequest();
        request.setSortBy("rating");
        request.setSortDirection("desc");
        request.setPage(0);
        request.setSize(20);

        List<BusinessProfile> vendors = Arrays.asList(vendor1);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 1);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        vendorSearchService.searchVendors(request, retailerUserId);

        // Assert
        verify(vendorSearchRepository).searchVendors(
            any(),
            any(),
            any(),
            any(),
            argThat(pageable -> 
                pageable.getPageNumber() == 0 &&
                pageable.getPageSize() == 20 &&
                pageable.getSort().isSorted()
            )
        );
    }

    /**
     * Test buildPageable with sortBy="relevance"
     * Should use unsorted Pageable and apply ranking strategy
     */
    @Test
    public void testSearchVendors_BuildPageable_WithSortByRelevance() {
        // Arrange
        VendorSearchRequest request = new VendorSearchRequest();
        request.setSortBy("relevance");
        request.setPage(0);
        request.setSize(20);

        List<BusinessProfile> vendors = Arrays.asList(vendor1, vendor2);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 2);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        // Mock ranking strategy to reverse the list
        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> {
            List<VendorCardDTO> input = invocation.getArgument(0);
            List<VendorCardDTO> reversed = new ArrayList<>(input);
            Collections.reverse(reversed);
            return reversed;
        });

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(request, retailerUserId);

        // Assert
        assertNotNull(response);
        
        // Verify ranking strategy was called
        verify(rankingStrategy).rankVendors(anyList());

        // Verify repository was called with unsorted Pageable
        verify(vendorSearchRepository).searchVendors(
            any(),
            any(),
            any(),
            any(),
            argThat(pageable -> 
                pageable.getPageNumber() == 0 &&
                pageable.getPageSize() == 20 &&
                pageable.getSort().isUnsorted()
            )
        );
    }

    /**
     * Test that ranking strategy is NOT called when sortBy is not "relevance"
     */
    @Test
    public void testSearchVendors_RankingStrategy_NotCalledForNonRelevanceSort() {
        // Arrange
        VendorSearchRequest request = new VendorSearchRequest();
        request.setSortBy("price");
        request.setPage(0);
        request.setSize(20);

        List<BusinessProfile> vendors = Arrays.asList(vendor1);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 1);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        vendorSearchService.searchVendors(request, retailerUserId);

        // Assert - Ranking strategy should NOT be called
        verify(rankingStrategy, never()).rankVendors(anyList());
    }

    /**
     * Test with null query parameter
     */
    @Test
    public void testSearchVendors_WithNullQuery_ReturnsAllVendors() {
        // Arrange
        VendorSearchRequest request = new VendorSearchRequest();
        request.setQuery(null);
        request.setPage(0);
        request.setSize(20);

        List<BusinessProfile> vendors = Arrays.asList(vendor1, vendor2);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 2);

        when(vendorSearchRepository.searchVendors(
            isNull(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(request, retailerUserId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getVendors().size());

        verify(vendorSearchRepository).searchVendors(
            isNull(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        );
    }

    /**
     * Test pagination with different page numbers
     */
    @Test
    public void testSearchVendors_Pagination_DifferentPageNumbers() {
        // Arrange
        VendorSearchRequest request = new VendorSearchRequest();
        request.setPage(2);  // Third page
        request.setSize(10);

        List<BusinessProfile> vendors = Arrays.asList(vendor1);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(2, 10), 21);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(request, retailerUserId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getCurrentPage());
        assertEquals(3, response.getTotalPages());  // 21 elements / 10 per page = 3 pages
        assertEquals(21, response.getTotalElements());
        assertEquals(10, response.getPageSize());
    }

    /**
     * Test that each vendor gets at most one badge
     */
    @Test
    public void testSearchVendors_BadgeAssignment_EachVendorGetsAtMostOneBadge() {
        // Arrange
        List<BusinessProfile> vendors = Arrays.asList(vendor1, vendor2, vendor3);
        Page<BusinessProfile> vendorPage = new PageImpl<>(vendors, PageRequest.of(0, 20), 3);

        when(vendorSearchRepository.searchVendors(
            any(),
            any(),
            any(),
            any(),
            any(Pageable.class)
        )).thenReturn(vendorPage);

        when(procurementOrderRepository.findAll()).thenReturn(Collections.emptyList());

        when(rankingStrategy.rankVendors(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VendorSearchResponse response = vendorSearchService.searchVendors(validRequest, retailerUserId);

        // Assert
        assertNotNull(response);
        
        // Verify each vendor has exactly one badge (including NONE)
        for (VendorCardDTO card : response.getVendors()) {
            assertNotNull(card.getBadge());
        }

        // Count non-NONE badges
        long nonNoneBadges = response.getVendors().stream()
            .filter(v -> v.getBadge() != Badge.NONE)
            .count();

        // Should have at most 3 non-NONE badges (one for each category)
        assertTrue(nonNoneBadges <= 3);
    }
}
