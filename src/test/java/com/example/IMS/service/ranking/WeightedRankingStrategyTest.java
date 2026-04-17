package com.example.IMS.service.ranking;

import com.example.IMS.dto.VendorCardDTO;
import com.example.IMS.model.enums.Badge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeightedRankingStrategy
 * Tests the ranking algorithm with diverse vendor data and edge cases
 * Requirements: 8.1-8.7
 */
public class WeightedRankingStrategyTest {

    private WeightedRankingStrategy rankingStrategy;

    @BeforeEach
    public void setUp() {
        rankingStrategy = new WeightedRankingStrategy();
    }

    @Test
    public void testRankVendors_WithDiverseData_VerifyCorrectOrdering() {
        // Create vendors with diverse data
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // Vendor 1: Low price, slow delivery, medium reliability, low stock
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Vendor 1")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(10)
                .reliabilityScore(0.5)
                .availableQuantity(50)
                .build();
        
        // Vendor 2: High price, fast delivery, high reliability, high stock
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Vendor 2")
                .pricePerUnit(new BigDecimal("200.00"))
                .deliveryDays(2)
                .reliabilityScore(0.9)
                .availableQuantity(200)
                .build();
        
        // Vendor 3: Medium price, medium delivery, low reliability, medium stock
        VendorCardDTO vendor3 = new VendorCardDTO.Builder()
                .vendorId(3L)
                .vendorName("Vendor 3")
                .pricePerUnit(new BigDecimal("150.00"))
                .deliveryDays(5)
                .reliabilityScore(0.3)
                .availableQuantity(100)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        vendors.add(vendor3);
        
        // Rank vendors
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Verify list is not null and has same size
        assertNotNull(rankedVendors);
        assertEquals(3, rankedVendors.size());
        
        // Vendor 2 should rank highest (fast delivery, high reliability, high stock compensate for high price)
        // Vendor 1 should rank second (low price compensates for other factors)
        // Vendor 3 should rank lowest (medium on everything, low reliability)
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
        assertEquals(3L, rankedVendors.get(2).getVendorId());
    }

    @Test
    public void testRankVendors_WithEmptyList_ReturnsEmptyList() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        assertNotNull(rankedVendors);
        assertTrue(rankedVendors.isEmpty());
    }

    @Test
    public void testRankVendors_WithSingleVendor_ReturnsSameList() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        VendorCardDTO vendor = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Single Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(5)
                .reliabilityScore(0.8)
                .availableQuantity(100)
                .build();
        
        vendors.add(vendor);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        assertNotNull(rankedVendors);
        assertEquals(1, rankedVendors.size());
        assertEquals(1L, rankedVendors.get(0).getVendorId());
    }

    @Test
    public void testRankVendors_WithIdenticalValues_AllScoresShouldBeEqual() {
        // Create vendors with identical values for all metrics
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            VendorCardDTO vendor = new VendorCardDTO.Builder()
                    .vendorId((long) i)
                    .vendorName("Vendor " + i)
                    .pricePerUnit(new BigDecimal("100.00"))
                    .deliveryDays(5)
                    .reliabilityScore(0.7)
                    .availableQuantity(100)
                    .build();
            vendors.add(vendor);
        }
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // All vendors should have equal scores (0.5 for normalized metrics + 0.7 for reliability)
        // Since all scores are equal, order should be preserved
        assertNotNull(rankedVendors);
        assertEquals(3, rankedVendors.size());
        
        // Verify all vendors are present (order may vary since scores are equal)
        assertTrue(rankedVendors.stream().anyMatch(v -> v.getVendorId() == 1L));
        assertTrue(rankedVendors.stream().anyMatch(v -> v.getVendorId() == 2L));
        assertTrue(rankedVendors.stream().anyMatch(v -> v.getVendorId() == 3L));
    }

    @Test
    public void testRankVendors_LowerPriceRanksHigher_WhenOtherFactorsEqual() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // Vendor 1: Higher price
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Expensive Vendor")
                .pricePerUnit(new BigDecimal("200.00"))
                .deliveryDays(5)
                .reliabilityScore(0.8)
                .availableQuantity(100)
                .build();
        
        // Vendor 2: Lower price
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Cheap Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(5)
                .reliabilityScore(0.8)
                .availableQuantity(100)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Vendor 2 (lower price) should rank higher
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_FasterDeliveryRanksHigher_WhenOtherFactorsEqual() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // Vendor 1: Slower delivery
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Slow Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(10)
                .reliabilityScore(0.8)
                .availableQuantity(100)
                .build();
        
        // Vendor 2: Faster delivery
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Fast Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(2)
                .reliabilityScore(0.8)
                .availableQuantity(100)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Vendor 2 (faster delivery) should rank higher
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_HigherStockRanksHigher_WhenOtherFactorsEqual() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // Vendor 1: Lower stock
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Low Stock Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(5)
                .reliabilityScore(0.8)
                .availableQuantity(50)
                .build();
        
        // Vendor 2: Higher stock
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("High Stock Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(5)
                .reliabilityScore(0.8)
                .availableQuantity(200)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Vendor 2 (higher stock) should rank higher
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_HigherReliabilityRanksHigher_WhenOtherFactorsEqual() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // Vendor 1: Lower reliability
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Less Reliable Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(5)
                .reliabilityScore(0.5)
                .availableQuantity(100)
                .build();
        
        // Vendor 2: Higher reliability
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("More Reliable Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(5)
                .reliabilityScore(0.9)
                .availableQuantity(100)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Vendor 2 (higher reliability) should rank higher
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_WeightedScoringFormulaAccuracy() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // Create two vendors where we can manually calculate expected scores
        // Vendor 1: Min price (1.0 score), max delivery (0.0 score), min reliability (0.0), min stock (0.0)
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Vendor 1")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(10)
                .reliabilityScore(0.0)
                .availableQuantity(50)
                .build();
        
        // Vendor 2: Max price (0.0 score), min delivery (1.0 score), max reliability (1.0), max stock (1.0)
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Vendor 2")
                .pricePerUnit(new BigDecimal("200.00"))
                .deliveryDays(2)
                .reliabilityScore(1.0)
                .availableQuantity(200)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Expected scores:
        // Vendor 1: (0.35 * 1.0) + (0.25 * 0.0) + (0.25 * 0.0) + (0.15 * 0.0) = 0.35
        // Vendor 2: (0.35 * 0.0) + (0.25 * 1.0) + (0.25 * 1.0) + (0.15 * 1.0) = 0.65
        
        // Vendor 2 should rank higher (0.65 > 0.35)
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_NullVendorsList_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            rankingStrategy.rankVendors(null);
        });
    }

    @Test
    public void testRankVendors_EdgeCase_AllPricesEqual() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // All vendors have same price but different other metrics
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Vendor 1")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(10)
                .reliabilityScore(0.5)
                .availableQuantity(50)
                .build();
        
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Vendor 2")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(2)
                .reliabilityScore(0.9)
                .availableQuantity(200)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Vendor 2 should rank higher due to better delivery, reliability, and stock
        // (price scores should both be 0.5 since all prices are equal)
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_EdgeCase_AllDeliveryTimesEqual() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // All vendors have same delivery time but different other metrics
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Vendor 1")
                .pricePerUnit(new BigDecimal("200.00"))
                .deliveryDays(5)
                .reliabilityScore(0.5)
                .availableQuantity(50)
                .build();
        
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Vendor 2")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(5)
                .reliabilityScore(0.9)
                .availableQuantity(200)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Vendor 2 should rank higher due to better price, reliability, and stock
        // (delivery scores should both be 0.5 since all delivery times are equal)
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_EdgeCase_AllStockLevelsEqual() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // All vendors have same stock but different other metrics
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Vendor 1")
                .pricePerUnit(new BigDecimal("200.00"))
                .deliveryDays(10)
                .reliabilityScore(0.5)
                .availableQuantity(100)
                .build();
        
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Vendor 2")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(2)
                .reliabilityScore(0.9)
                .availableQuantity(100)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Vendor 2 should rank higher due to better price, delivery, and reliability
        // (stock scores should both be 0.5 since all stock levels are equal)
        assertEquals(2L, rankedVendors.get(0).getVendorId());
        assertEquals(1L, rankedVendors.get(1).getVendorId());
    }

    @Test
    public void testRankVendors_ComplexScenario_FiveVendors() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        // Create 5 vendors with varying characteristics
        vendors.add(new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Budget Vendor")
                .pricePerUnit(new BigDecimal("80.00"))
                .deliveryDays(15)
                .reliabilityScore(0.6)
                .availableQuantity(75)
                .build());
        
        vendors.add(new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Premium Vendor")
                .pricePerUnit(new BigDecimal("250.00"))
                .deliveryDays(1)
                .reliabilityScore(0.95)
                .availableQuantity(300)
                .build());
        
        vendors.add(new VendorCardDTO.Builder()
                .vendorId(3L)
                .vendorName("Balanced Vendor")
                .pricePerUnit(new BigDecimal("150.00"))
                .deliveryDays(7)
                .reliabilityScore(0.75)
                .availableQuantity(150)
                .build());
        
        vendors.add(new VendorCardDTO.Builder()
                .vendorId(4L)
                .vendorName("Fast Vendor")
                .pricePerUnit(new BigDecimal("180.00"))
                .deliveryDays(2)
                .reliabilityScore(0.8)
                .availableQuantity(120)
                .build());
        
        vendors.add(new VendorCardDTO.Builder()
                .vendorId(5L)
                .vendorName("Unreliable Vendor")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(10)
                .reliabilityScore(0.3)
                .availableQuantity(50)
                .build());
        
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Verify all vendors are present
        assertEquals(5, rankedVendors.size());
        
        // Premium Vendor (2) should rank high despite high price due to excellent other metrics
        // Budget Vendor (1) should rank reasonably due to low price
        // Unreliable Vendor (5) should rank low due to poor reliability
        
        // Verify Premium Vendor is in top 2
        assertTrue(rankedVendors.get(0).getVendorId() == 2L || rankedVendors.get(1).getVendorId() == 2L);
        
        // Verify Unreliable Vendor is in bottom 2
        assertTrue(rankedVendors.get(3).getVendorId() == 5L || rankedVendors.get(4).getVendorId() == 5L);
    }

    @Test
    public void testRankVendors_DoesNotModifyOriginalList() {
        List<VendorCardDTO> vendors = new ArrayList<>();
        
        VendorCardDTO vendor1 = new VendorCardDTO.Builder()
                .vendorId(1L)
                .vendorName("Vendor 1")
                .pricePerUnit(new BigDecimal("200.00"))
                .deliveryDays(10)
                .reliabilityScore(0.5)
                .availableQuantity(50)
                .build();
        
        VendorCardDTO vendor2 = new VendorCardDTO.Builder()
                .vendorId(2L)
                .vendorName("Vendor 2")
                .pricePerUnit(new BigDecimal("100.00"))
                .deliveryDays(2)
                .reliabilityScore(0.9)
                .availableQuantity(200)
                .build();
        
        vendors.add(vendor1);
        vendors.add(vendor2);
        
        // Store original order
        Long firstVendorId = vendors.get(0).getVendorId();
        Long secondVendorId = vendors.get(1).getVendorId();
        
        // Rank vendors
        List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
        
        // Verify original list order is unchanged
        assertEquals(firstVendorId, vendors.get(0).getVendorId());
        assertEquals(secondVendorId, vendors.get(1).getVendorId());
        
        // Verify ranked list is different
        assertNotEquals(vendors.get(0).getVendorId(), rankedVendors.get(0).getVendorId());
    }
}
