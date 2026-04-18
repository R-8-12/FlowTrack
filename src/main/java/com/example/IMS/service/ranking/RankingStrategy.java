package com.example.IMS.service.ranking;

import com.example.IMS.dto.VendorCardDTO;
import java.util.List;

/**
 * Strategy interface for vendor ranking algorithms.
 * 
 * <p>This interface defines the contract for ranking vendors based on various criteria.
 * It follows the Strategy design pattern to allow swapping between different ranking
 * implementations without modifying client code.</p>
 * 
 * <h3>Current Implementation:</h3>
 * <ul>
 *   <li>{@code WeightedRankingStrategy} - Default weighted scoring algorithm that combines
 *       price (35%), delivery time (25%), reliability (25%), and stock availability (15%)</li>
 * </ul>
 * 
 * <h3>Future ML Integration:</h3>
 * <p>This interface is designed to support future machine learning-based ranking strategies.
 * Potential ML implementations could include:</p>
 * <ul>
 *   <li>Collaborative filtering based on retailer purchase patterns</li>
 *   <li>Personalized recommendations using user behavior analysis</li>
 *   <li>Predictive scoring based on historical vendor performance</li>
 *   <li>Hybrid approaches combining rule-based and ML-based ranking</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre>
 * {@code
 * @Autowired
 * private RankingStrategy rankingStrategy;
 * 
 * List<VendorCardDTO> rankedVendors = rankingStrategy.rankVendors(vendors);
 * }
 * </pre>
 * 
 * <h3>Implementation Guidelines:</h3>
 * <ul>
 *   <li>Implementations should be stateless and thread-safe</li>
 *   <li>Ranking should be deterministic for the same input</li>
 *   <li>Handle edge cases (empty lists, identical scores)</li>
 *   <li>Return a new sorted list without modifying the input</li>
 * </ul>
 * 
 * @see com.example.IMS.dto.VendorCardDTO
 * @see com.example.IMS.service.VendorSearchService
 * 
 * @author IMS Development Team
 * @version 1.0
 * @since 2024
 */
public interface RankingStrategy {
    
    /**
     * Ranks a list of vendors by relevance score.
     * 
     * <p>This method takes an unordered list of vendor cards and returns a new list
     * sorted by relevance, with the most relevant vendors appearing first (highest score).</p>
     * 
     * <p>The ranking algorithm is implementation-specific. The default weighted strategy
     * computes a composite score based on normalized price, delivery time, reliability,
     * and stock availability metrics.</p>
     * 
     * <h4>Behavior:</h4>
     * <ul>
     *   <li>Returns a new sorted list (does not modify input)</li>
     *   <li>Handles empty lists gracefully (returns empty list)</li>
     *   <li>Handles single-vendor lists (returns as-is)</li>
     *   <li>Sorts in descending order of relevance (best first)</li>
     * </ul>
     * 
     * <h4>Thread Safety:</h4>
     * <p>Implementations must be thread-safe as this method may be called concurrently
     * by multiple search requests.</p>
     * 
     * @param vendors the list of vendor cards to rank; must not be null
     * @return a new list containing the same vendors sorted by relevance score in descending order
     * @throws NullPointerException if vendors is null (implementation-dependent)
     */
    List<VendorCardDTO> rankVendors(List<VendorCardDTO> vendors);
}
