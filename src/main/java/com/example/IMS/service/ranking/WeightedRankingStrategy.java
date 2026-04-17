package com.example.IMS.service.ranking;

import com.example.IMS.dto.VendorCardDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default ranking strategy using weighted scoring algorithm.
 * 
 * <p>This implementation computes a relevance score for each vendor by combining
 * normalized metrics across four dimensions:</p>
 * <ul>
 *   <li><strong>Price (35%)</strong>: Lower prices receive higher scores</li>
 *   <li><strong>Delivery Time (25%)</strong>: Faster delivery receives higher scores</li>
 *   <li><strong>Reliability (25%)</strong>: Higher reliability scores are better</li>
 *   <li><strong>Stock Availability (15%)</strong>: Higher stock levels are better</li>
 * </ul>
 * 
 * <h3>Normalization Strategy:</h3>
 * <p>Each metric is normalized to a 0.0-1.0 scale based on the min/max values
 * in the current result set. For metrics where lower is better (price, delivery),
 * the normalization is inverted so that better values receive higher scores.</p>
 * 
 * <h3>Edge Case Handling:</h3>
 * <p>When all vendors have identical values for a metric (min equals max),
 * a neutral score of 0.5 is assigned to avoid division by zero and ensure
 * fair ranking based on other metrics.</p>
 * 
 * <h3>Thread Safety:</h3>
 * <p>This implementation is stateless and thread-safe. All computations are
 * performed on method parameters without modifying shared state.</p>
 * 
 * @see RankingStrategy
 * @see VendorCardDTO
 * 
 * @author IMS Development Team
 * @version 1.0
 * @since 2024
 */
@Component
public class WeightedRankingStrategy implements RankingStrategy {
    
    /**
     * Weight for price component in relevance score (35%)
     */
    private static final double PRICE_WEIGHT = 0.35;
    
    /**
     * Weight for delivery time component in relevance score (25%)
     */
    private static final double DELIVERY_WEIGHT = 0.25;
    
    /**
     * Weight for reliability component in relevance score (25%)
     */
    private static final double RELIABILITY_WEIGHT = 0.25;
    
    /**
     * Weight for stock availability component in relevance score (15%)
     */
    private static final double STOCK_WEIGHT = 0.15;
    
    /**
     * Ranks vendors by computing weighted relevance scores and sorting in descending order.
     * 
     * <p>The ranking algorithm:</p>
     * <ol>
     *   <li>Finds min/max values for price, delivery, and stock across all vendors</li>
     *   <li>Normalizes each metric to 0.0-1.0 scale</li>
     *   <li>Computes weighted score: 0.35*price + 0.25*delivery + 0.25*reliability + 0.15*stock</li>
     *   <li>Sorts vendors by score in descending order (highest score first)</li>
     * </ol>
     * 
     * <p><strong>Empty List Handling:</strong> Returns the input list unchanged if empty.</p>
     * 
     * <p><strong>Single Vendor:</strong> Returns the input list unchanged (no ranking needed).</p>
     * 
     * @param vendors the list of vendor cards to rank; must not be null
     * @return a new list containing the same vendors sorted by relevance score in descending order
     * @throws NullPointerException if vendors is null
     */
    @Override
    public List<VendorCardDTO> rankVendors(List<VendorCardDTO> vendors) {
        if (vendors == null) {
            throw new NullPointerException("Vendors list cannot be null");
        }
        
        if (vendors.isEmpty() || vendors.size() == 1) {
            return vendors;
        }
        
        // Find min/max values for normalization
        BigDecimal minPrice = vendors.stream()
            .map(VendorCardDTO::getPricePerUnit)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        BigDecimal maxPrice = vendors.stream()
            .map(VendorCardDTO::getPricePerUnit)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        int minDelivery = vendors.stream()
            .mapToInt(VendorCardDTO::getDeliveryDays)
            .min()
            .orElse(0);
        
        int maxDelivery = vendors.stream()
            .mapToInt(VendorCardDTO::getDeliveryDays)
            .max()
            .orElse(0);
        
        int minStock = vendors.stream()
            .mapToInt(VendorCardDTO::getAvailableQuantity)
            .min()
            .orElse(0);
        
        int maxStock = vendors.stream()
            .mapToInt(VendorCardDTO::getAvailableQuantity)
            .max()
            .orElse(0);
        
        // Compute scores and sort in descending order (highest score first)
        return vendors.stream()
            .sorted(Comparator.comparingDouble(v -> 
                -computeRelevanceScore(v, minPrice, maxPrice, minDelivery, maxDelivery, minStock, maxStock)))
            .collect(Collectors.toList());
    }
    
    /**
     * Computes the weighted relevance score for a single vendor.
     * 
     * <p>Score formula:</p>
     * <pre>
     * score = (PRICE_WEIGHT × normalizedPriceScore) +
     *         (DELIVERY_WEIGHT × normalizedDeliveryScore) +
     *         (RELIABILITY_WEIGHT × reliabilityScore) +
     *         (STOCK_WEIGHT × normalizedStockScore)
     * </pre>
     * 
     * @param vendor the vendor to score
     * @param minPrice minimum price in result set
     * @param maxPrice maximum price in result set
     * @param minDelivery minimum delivery days in result set
     * @param maxDelivery maximum delivery days in result set
     * @param minStock minimum stock quantity in result set
     * @param maxStock maximum stock quantity in result set
     * @return relevance score between 0.0 and 1.0
     */
    private double computeRelevanceScore(
            VendorCardDTO vendor,
            BigDecimal minPrice, BigDecimal maxPrice,
            int minDelivery, int maxDelivery,
            int minStock, int maxStock) {
        
        double priceScore = normalizePrice(vendor.getPricePerUnit(), minPrice, maxPrice);
        double deliveryScore = normalizeDelivery(vendor.getDeliveryDays(), minDelivery, maxDelivery);
        double reliabilityScore = vendor.getReliabilityScore();
        double stockScore = normalizeStock(vendor.getAvailableQuantity(), minStock, maxStock);
        
        return (PRICE_WEIGHT * priceScore) +
               (DELIVERY_WEIGHT * deliveryScore) +
               (RELIABILITY_WEIGHT * reliabilityScore) +
               (STOCK_WEIGHT * stockScore);
    }
    
    /**
     * Normalizes price to 0.0-1.0 scale with inversion (lower price = higher score).
     * 
     * <p>Normalization formula:</p>
     * <pre>
     * normalized = (price - min) / (max - min)
     * inverted = 1.0 - normalized
     * </pre>
     * 
     * <p><strong>Edge Case:</strong> If all prices are equal (min == max),
     * returns 0.5 to avoid division by zero.</p>
     * 
     * @param price the vendor's price per unit
     * @param min minimum price in result set
     * @param max maximum price in result set
     * @return normalized inverted score between 0.0 and 1.0
     */
    private double normalizePrice(BigDecimal price, BigDecimal min, BigDecimal max) {
        if (max.equals(min)) {
            return 0.5;  // All prices equal - neutral score
        }
        
        // Normalize to 0.0-1.0 range
        double normalized = price.subtract(min)
            .divide(max.subtract(min), 4, RoundingMode.HALF_UP)
            .doubleValue();
        
        // Invert so lower price = higher score
        return 1.0 - normalized;
    }
    
    /**
     * Normalizes delivery time to 0.0-1.0 scale with inversion (faster = higher score).
     * 
     * <p>Normalization formula:</p>
     * <pre>
     * normalized = (delivery - min) / (max - min)
     * inverted = 1.0 - normalized
     * </pre>
     * 
     * <p><strong>Edge Case:</strong> If all delivery times are equal (min == max),
     * returns 0.5 to avoid division by zero.</p>
     * 
     * @param delivery the vendor's delivery time in days
     * @param min minimum delivery days in result set
     * @param max maximum delivery days in result set
     * @return normalized inverted score between 0.0 and 1.0
     */
    private double normalizeDelivery(int delivery, int min, int max) {
        if (max == min) {
            return 0.5;  // All delivery times equal - neutral score
        }
        
        // Normalize to 0.0-1.0 range
        double normalized = (double)(delivery - min) / (max - min);
        
        // Invert so faster delivery = higher score
        return 1.0 - normalized;
    }
    
    /**
     * Normalizes stock quantity to 0.0-1.0 scale (higher stock = higher score).
     * 
     * <p>Normalization formula:</p>
     * <pre>
     * normalized = (stock - min) / (max - min)
     * </pre>
     * 
     * <p><strong>Edge Case:</strong> If all stock quantities are equal (min == max),
     * returns 0.5 to avoid division by zero.</p>
     * 
     * @param stock the vendor's available stock quantity
     * @param min minimum stock quantity in result set
     * @param max maximum stock quantity in result set
     * @return normalized score between 0.0 and 1.0
     */
    private double normalizeStock(int stock, int min, int max) {
        if (max == min) {
            return 0.5;  // All stock levels equal - neutral score
        }
        
        // Normalize to 0.0-1.0 range (higher stock = higher score)
        return (double)(stock - min) / (max - min);
    }
}
