# Requirements Document

## Introduction

The Vendor Search & Recommendation Engine replaces the legacy manual vendor addition interface with a comprehensive search and discovery system. This feature enables retailers to search for verified vendors based on multiple criteria including product availability, pricing, delivery time, and vendor reliability. The system provides intelligent filtering, sorting, and recommendation capabilities to help retailers discover and connect with suitable vendors for bulk procurement.

This feature integrates with FlowTrack's existing multi-tenant architecture, RBAC system, and BusinessProfile verification workflow. It leverages the BusinessProfile entity (with ROLE_VENDOR) rather than the legacy Vendor entity, ensuring all searchable vendors are verified and active.

## Glossary

- **Search_Engine**: The backend service responsible for querying, filtering, and ranking vendors based on search criteria
- **Vendor_Card**: A UI component displaying summarized vendor information including name, price, availability, delivery time, reliability score, and badges
- **Retailer_User**: A user with ROLE_RETAILER authority who searches for vendors for bulk procurement
- **Verified_Vendor**: A BusinessProfile with role ROLE_VENDOR, verificationStatus = VERIFIED, and onboardingStage = ACTIVE
- **Vendor_Profile**: A BusinessProfile entity representing a vendor business with associated inventory and metadata
- **Inventory_Record**: Product stock information associated with a Vendor_Profile including product name, price, quantity, and delivery time
- **Reliability_Score**: A numeric metric (0.0 to 1.0) representing vendor performance based on order history, fulfillment rate, and ratings
- **Badge**: A visual indicator (BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY, or NONE) highlighting vendor strengths
- **Search_Request**: A data structure containing search query, filters, sorting preferences, and pagination parameters
- **Search_Response**: A paginated result set containing Vendor_Cards and metadata (total pages, total elements)
- **Ranking_Algorithm**: A weighted scoring function that combines price, delivery, reliability, and stock metrics to compute relevance
- **Filter_Criteria**: User-specified constraints including price range, delivery time, minimum quantity, and verification status
- **Sort_Option**: A user-selected ordering preference (price, delivery, rating, or relevance)
- **Pagination_Parameters**: Page number and page size for result set navigation
- **Distance_Filter**: An optional geographic constraint limiting search results by distance from retailer location
- **Previous_Order_Flag**: A boolean indicator showing whether the retailer has previously ordered from a vendor

## Requirements

### Requirement 1: Vendor Search Query Processing

**User Story:** As a retailer, I want to search for vendors by product name or vendor name, so that I can quickly find relevant suppliers for my procurement needs.

#### Acceptance Criteria

1. WHEN a Retailer_User submits a Search_Request with a query string, THE Search_Engine SHALL return all Verified_Vendors whose Vendor_Profile name or Inventory_Record product name contains the query string (case-insensitive)
2. WHEN a Retailer_User submits a Search_Request with an empty query string, THE Search_Engine SHALL return all Verified_Vendors
3. THE Search_Engine SHALL limit search results to Vendor_Profiles where verificationStatus equals VERIFIED AND onboardingStage equals ACTIVE
4. THE Search_Engine SHALL exclude Vendor_Profiles where the associated User has enabled equals false
5. WHEN a Search_Request contains Pagination_Parameters, THE Search_Engine SHALL return results according to the specified page number and page size
6. THE Search_Engine SHALL limit maximum page size to 50 records per page

### Requirement 2: Price Range Filtering

**User Story:** As a retailer, I want to filter vendors by price range, so that I can find suppliers within my budget constraints.

#### Acceptance Criteria

1. WHEN a Search_Request includes minPrice, THE Search_Engine SHALL exclude Vendor_Cards where pricePerUnit is less than minPrice
2. WHEN a Search_Request includes maxPrice, THE Search_Engine SHALL exclude Vendor_Cards where pricePerUnit is greater than maxPrice
3. WHEN a Search_Request includes both minPrice and maxPrice, THE Search_Engine SHALL return only Vendor_Cards where pricePerUnit is between minPrice and maxPrice (inclusive)
4. THE Search_Engine SHALL accept price values as BigDecimal with precision up to 2 decimal places
5. IF minPrice is greater than maxPrice, THEN THE Search_Engine SHALL return an error response with message "Invalid price range: minPrice cannot exceed maxPrice"

### Requirement 3: Delivery Time Filtering

**User Story:** As a retailer, I want to filter vendors by maximum delivery time, so that I can find suppliers who can meet my delivery deadlines.

#### Acceptance Criteria

1. WHEN a Search_Request includes maxDeliveryDays, THE Search_Engine SHALL exclude Vendor_Cards where deliveryDays is greater than maxDeliveryDays
2. THE Search_Engine SHALL accept maxDeliveryDays as a positive integer representing calendar days
3. IF maxDeliveryDays is less than 1, THEN THE Search_Engine SHALL return an error response with message "Invalid delivery time: maxDeliveryDays must be at least 1"
4. WHEN no maxDeliveryDays is specified, THE Search_Engine SHALL return all vendors regardless of delivery time

### Requirement 4: Stock Availability Filtering

**User Story:** As a retailer, I want to filter vendors by minimum available quantity, so that I can ensure suppliers have sufficient stock for my bulk orders.

#### Acceptance Criteria

1. WHEN a Search_Request includes minQuantity, THE Search_Engine SHALL exclude Vendor_Cards where availableQuantity is less than minQuantity
2. THE Search_Engine SHALL accept minQuantity as a positive integer
3. IF minQuantity is less than 1, THEN THE Search_Engine SHALL return an error response with message "Invalid quantity: minQuantity must be at least 1"
4. WHEN no minQuantity is specified, THE Search_Engine SHALL return all vendors regardless of stock level

### Requirement 5: Geographic Distance Filtering

**User Story:** As a retailer, I want to filter vendors by distance from my location, so that I can prioritize local suppliers and reduce shipping costs.

#### Acceptance Criteria

1. WHERE the retailer has a location specified in their Vendor_Profile, WHEN a Search_Request includes maxDistanceKm, THE Search_Engine SHALL exclude Vendor_Cards where the calculated distance exceeds maxDistanceKm
2. THE Search_Engine SHALL calculate distance using the Haversine formula based on latitude and longitude coordinates
3. THE Search_Engine SHALL accept maxDistanceKm as a positive decimal value
4. WHERE the retailer has no location specified, THE Search_Engine SHALL ignore the maxDistanceKm filter
5. WHERE a Vendor_Profile has no location specified, THE Search_Engine SHALL exclude that vendor when maxDistanceKm filter is active

### Requirement 6: Verification Status Filtering

**User Story:** As a retailer, I want to ensure I only see verified vendors, so that I can trust the suppliers I connect with.

#### Acceptance Criteria

1. THE Search_Engine SHALL always filter results to include only Vendor_Profiles where verificationStatus equals VERIFIED
2. THE Search_Engine SHALL always filter results to include only Vendor_Profiles where onboardingStage equals ACTIVE
3. WHEN a Search_Request includes verifiedOnly set to true, THE Search_Engine SHALL apply the verification filters (redundant but explicit)
4. THE Search_Engine SHALL exclude Vendor_Profiles with verificationStatus DRAFT, PENDING, or REJECTED

### Requirement 7: Search Result Sorting

**User Story:** As a retailer, I want to sort search results by different criteria, so that I can prioritize vendors based on what matters most to me.

#### Acceptance Criteria

1. WHEN a Search_Request includes sortBy equals "price", THE Search_Engine SHALL order results by pricePerUnit
2. WHEN a Search_Request includes sortBy equals "delivery", THE Search_Engine SHALL order results by deliveryDays
3. WHEN a Search_Request includes sortBy equals "rating", THE Search_Engine SHALL order results by rating
4. WHEN a Search_Request includes sortBy equals "relevance", THE Search_Engine SHALL order results by the computed Ranking_Algorithm score
5. WHEN a Search_Request includes sortDirection equals "asc", THE Search_Engine SHALL order results in ascending order
6. WHEN a Search_Request includes sortDirection equals "desc", THE Search_Engine SHALL order results in descending order
7. WHEN no sortBy is specified, THE Search_Engine SHALL default to sorting by "relevance"
8. WHEN no sortDirection is specified, THE Search_Engine SHALL default to "asc" for price and delivery, and "desc" for rating and relevance

### Requirement 8: Vendor Ranking Algorithm

**User Story:** As a retailer, I want search results ranked by relevance, so that the most suitable vendors appear first.

#### Acceptance Criteria

1. WHEN sortBy equals "relevance", THE Search_Engine SHALL compute a ranking score for each Vendor_Card using the Ranking_Algorithm
2. THE Ranking_Algorithm SHALL compute score as: (0.35 × normalizedPriceScore) + (0.25 × deliveryScore) + (0.25 × reliabilityScore) + (0.15 × stockScore)
3. THE Search_Engine SHALL normalize priceScore where lower prices receive higher scores (1.0 for lowest price in result set, 0.0 for highest)
4. THE Search_Engine SHALL normalize deliveryScore where shorter delivery times receive higher scores (1.0 for fastest delivery, 0.0 for slowest)
5. THE Search_Engine SHALL use reliabilityScore directly as a value between 0.0 and 1.0
6. THE Search_Engine SHALL normalize stockScore where higher available quantities receive higher scores (1.0 for highest stock, 0.0 for lowest)
7. THE Search_Engine SHALL handle edge cases where all vendors have identical values by assigning a score of 0.5 for that metric

### Requirement 9: Vendor Badge Assignment

**User Story:** As a retailer, I want to see badges highlighting vendor strengths, so that I can quickly identify the best options for my needs.

#### Acceptance Criteria

1. THE Search_Engine SHALL assign badge BEST_PRICE to the Vendor_Card with the lowest pricePerUnit in the result set
2. THE Search_Engine SHALL assign badge FAST_DELIVERY to the Vendor_Card with the lowest deliveryDays in the result set
3. THE Search_Engine SHALL assign badge HIGH_RELIABILITY to the Vendor_Card with the highest reliabilityScore in the result set
4. THE Search_Engine SHALL assign badge NONE to all other Vendor_Cards
5. WHEN multiple vendors tie for a badge criterion, THE Search_Engine SHALL assign the badge to the first vendor in the sorted result set
6. THE Search_Engine SHALL assign at most one badge per Vendor_Card

### Requirement 10: Previous Order History Indicator

**User Story:** As a retailer, I want to see which vendors I've ordered from before, so that I can prioritize familiar suppliers.

#### Acceptance Criteria

1. THE Search_Engine SHALL set previouslyOrdered to true for Vendor_Cards where the Retailer_User has at least one completed order with that Vendor_Profile
2. THE Search_Engine SHALL set previouslyOrdered to false for Vendor_Cards where the Retailer_User has no order history with that Vendor_Profile
3. THE Search_Engine SHALL determine order history by querying the orders table for records matching both the Retailer_User ID and Vendor_Profile ID
4. THE Search_Engine SHALL consider only orders with status COMPLETED or DELIVERED when determining previouslyOrdered flag

### Requirement 11: Search Response Pagination Metadata

**User Story:** As a retailer, I want to see pagination information with search results, so that I can navigate through multiple pages of vendors.

#### Acceptance Criteria

1. THE Search_Engine SHALL include totalPages in the Search_Response indicating the total number of pages available
2. THE Search_Engine SHALL include totalElements in the Search_Response indicating the total number of matching vendors
3. THE Search_Engine SHALL calculate totalPages as ceiling(totalElements / pageSize)
4. WHEN totalElements is zero, THE Search_Engine SHALL return totalPages as zero
5. THE Search_Engine SHALL include the current page number in the Search_Response

### Requirement 12: Vendor Card Data Structure

**User Story:** As a retailer, I want to see essential vendor information in search results, so that I can make informed decisions without viewing full profiles.

#### Acceptance Criteria

1. THE Search_Engine SHALL populate each Vendor_Card with vendorId from the Vendor_Profile ID
2. THE Search_Engine SHALL populate each Vendor_Card with vendorName from the Vendor_Profile legalBusinessName
3. THE Search_Engine SHALL populate each Vendor_Card with pricePerUnit from the Inventory_Record price
4. THE Search_Engine SHALL populate each Vendor_Card with availableQuantity from the Inventory_Record quantity
5. THE Search_Engine SHALL populate each Vendor_Card with deliveryDays from vendor-specific delivery metadata
6. THE Search_Engine SHALL populate each Vendor_Card with reliabilityScore computed from order history metrics
7. THE Search_Engine SHALL populate each Vendor_Card with rating from vendor review aggregation
8. THE Search_Engine SHALL populate each Vendor_Card with verified set to true (since only verified vendors are searchable)
9. THE Search_Engine SHALL populate each Vendor_Card with location from the Vendor_Profile registeredAddress and state
10. THE Search_Engine SHALL populate each Vendor_Card with previouslyOrdered flag as specified in Requirement 10
11. THE Search_Engine SHALL populate each Vendor_Card with badge as specified in Requirement 9

### Requirement 13: Search API Endpoint Security

**User Story:** As a platform administrator, I want to ensure only authenticated retailers can search for vendors, so that vendor data is protected.

#### Acceptance Criteria

1. THE Search_Engine SHALL require authentication for all search requests
2. THE Search_Engine SHALL verify the requesting user has authority ROLE_RETAILER
3. IF the requesting user does not have ROLE_RETAILER authority, THEN THE Search_Engine SHALL return HTTP 403 Forbidden
4. THE Search_Engine SHALL extract the Retailer_User ID from the authenticated security context
5. THE Search_Engine SHALL use the Retailer_User ID for personalization features (previouslyOrdered flag, distance calculation)

### Requirement 14: Search Performance Optimization

**User Story:** As a platform administrator, I want search queries to execute efficiently, so that retailers experience fast response times.

#### Acceptance Criteria

1. THE Search_Engine SHALL use database indexes on Vendor_Profile verificationStatus, onboardingStage, and User enabled fields
2. THE Search_Engine SHALL use database indexes on Inventory_Record product name and price fields
3. THE Search_Engine SHALL use JOIN FETCH to avoid N+1 query problems when loading Vendor_Profile and Inventory_Record associations
4. THE Search_Engine SHALL execute search queries using JPA Criteria API or native SQL with pagination support
5. THE Search_Engine SHALL return search results within 2 seconds for result sets up to 1000 matching vendors

### Requirement 15: Search Input Validation

**User Story:** As a platform administrator, I want invalid search requests rejected with clear error messages, so that clients can correct their requests.

#### Acceptance Criteria

1. IF a Search_Request contains page number less than 0, THEN THE Search_Engine SHALL return HTTP 400 Bad Request with message "Invalid page number: must be 0 or greater"
2. IF a Search_Request contains page size less than 1, THEN THE Search_Engine SHALL return HTTP 400 Bad Request with message "Invalid page size: must be at least 1"
3. IF a Search_Request contains page size greater than 50, THEN THE Search_Engine SHALL return HTTP 400 Bad Request with message "Invalid page size: maximum is 50"
4. IF a Search_Request contains invalid sortBy value, THEN THE Search_Engine SHALL return HTTP 400 Bad Request with message "Invalid sortBy: must be one of [price, delivery, rating, relevance]"
5. IF a Search_Request contains invalid sortDirection value, THEN THE Search_Engine SHALL return HTTP 400 Bad Request with message "Invalid sortDirection: must be one of [asc, desc]"
6. THE Search_Engine SHALL validate all numeric fields (minPrice, maxPrice, minQuantity, maxDeliveryDays, maxDistanceKm) are non-negative
7. IF any numeric field is negative, THEN THE Search_Engine SHALL return HTTP 400 Bad Request with a descriptive error message

### Requirement 16: Search UI Layout and Components

**User Story:** As a retailer, I want an intuitive search interface with filters and results, so that I can easily find and evaluate vendors.

#### Acceptance Criteria

1. THE Search_UI SHALL display a search bar accepting product name or vendor name queries
2. THE Search_UI SHALL display a left sidebar containing filter controls for price range, delivery time, minimum stock, and sort options
3. THE Search_UI SHALL display a main content area containing a grid of Vendor_Cards
4. THE Search_UI SHALL display pagination controls at the bottom of the results grid
5. WHEN a Retailer_User modifies any filter or search query, THE Search_UI SHALL submit a new Search_Request to the Search_Engine
6. THE Search_UI SHALL display a loading indicator while waiting for Search_Response
7. THE Search_UI SHALL display "No vendors found" message when Search_Response contains zero results

### Requirement 17: Vendor Card UI Display

**User Story:** As a retailer, I want vendor cards to display key information clearly, so that I can quickly compare options.

#### Acceptance Criteria

1. THE Search_UI SHALL display vendorName prominently at the top of each Vendor_Card
2. THE Search_UI SHALL display pricePerUnit with currency symbol (₹) and 2 decimal places
3. THE Search_UI SHALL display availableQuantity with label "Stock Available"
4. THE Search_UI SHALL display deliveryDays with label "Delivery Time" and unit "days"
5. THE Search_UI SHALL display reliabilityScore as a percentage (multiply by 100) with label "Reliability"
6. THE Search_UI SHALL display rating as stars or numeric value out of 5
7. THE Search_UI SHALL display a verified badge icon when verified equals true
8. THE Search_UI SHALL display the badge (BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY) as a colored label when badge is not NONE
9. THE Search_UI SHALL display location as "City, State" format
10. THE Search_UI SHALL display a "Previously Ordered" indicator when previouslyOrdered equals true
11. THE Search_UI SHALL display a "View Details" button or "Order" button for each Vendor_Card

### Requirement 18: Search Filter Interaction

**User Story:** As a retailer, I want filter controls to be responsive and intuitive, so that I can refine my search efficiently.

#### Acceptance Criteria

1. THE Search_UI SHALL provide text input fields for minPrice and maxPrice with numeric validation
2. THE Search_UI SHALL provide a slider or numeric input for maxDeliveryDays
3. THE Search_UI SHALL provide a numeric input for minQuantity
4. THE Search_UI SHALL provide a dropdown for sortBy with options: Relevance, Price, Delivery Time, Rating
5. THE Search_UI SHALL provide a dropdown or toggle for sortDirection with options: Ascending, Descending
6. WHEN a Retailer_User changes any filter value, THE Search_UI SHALL debounce the input for 500 milliseconds before submitting the Search_Request
7. THE Search_UI SHALL display a "Clear Filters" button that resets all filters to default values

### Requirement 19: Search Result Client-Side Rendering

**User Story:** As a retailer, I want search results to load dynamically without page refresh, so that I have a smooth browsing experience.

#### Acceptance Criteria

1. THE Search_UI SHALL use JavaScript fetch API or Axios to call the Search_Engine API endpoint
2. THE Search_UI SHALL include CSRF token in all API requests
3. WHEN the Search_Engine returns a Search_Response, THE Search_UI SHALL dynamically render Vendor_Cards in the results grid
4. THE Search_UI SHALL clear previous results before rendering new results
5. THE Search_UI SHALL update pagination controls based on totalPages and current page from Search_Response
6. IF the Search_Engine returns an error response, THEN THE Search_UI SHALL display an error message to the Retailer_User

### Requirement 20: Pagination Navigation

**User Story:** As a retailer, I want to navigate through multiple pages of search results, so that I can explore all available vendors.

#### Acceptance Criteria

1. THE Search_UI SHALL display page numbers as clickable links or buttons
2. THE Search_UI SHALL display "Previous" and "Next" buttons for page navigation
3. THE Search_UI SHALL disable "Previous" button when on the first page
4. THE Search_UI SHALL disable "Next" button when on the last page
5. WHEN a Retailer_User clicks a page number, THE Search_UI SHALL submit a Search_Request with the selected page number
6. THE Search_UI SHALL highlight the current page number
7. THE Search_UI SHALL display a maximum of 10 page numbers at a time with ellipsis for additional pages

### Requirement 21: Search API Endpoint Definition

**User Story:** As a frontend developer, I want a well-defined API endpoint for vendor search, so that I can integrate the search functionality.

#### Acceptance Criteria

1. THE Search_Engine SHALL expose endpoint GET /api/retailer/vendors/search
2. THE Search_Engine SHALL accept query parameters: query, minPrice, maxPrice, minQuantity, maxDeliveryDays, maxDistanceKm, verifiedOnly, sortBy, sortDirection, page, size
3. THE Search_Engine SHALL return HTTP 200 OK with Search_Response JSON when the request is valid
4. THE Search_Engine SHALL return HTTP 400 Bad Request with error details when validation fails
5. THE Search_Engine SHALL return HTTP 403 Forbidden when the user lacks ROLE_RETAILER authority
6. THE Search_Engine SHALL return HTTP 500 Internal Server Error when an unexpected error occurs
7. THE Search_Engine SHALL log all search requests with timestamp, user ID, and query parameters for analytics

### Requirement 22: Multi-Tenant Data Isolation

**User Story:** As a platform administrator, I want vendor search to respect multi-tenant boundaries, so that data from different tenants is not mixed.

#### Acceptance Criteria

1. THE Search_Engine SHALL scope all queries by the Retailer_User's tenant context
2. THE Search_Engine SHALL ensure Vendor_Profiles returned belong to the same tenant as the Retailer_User
3. THE Search_Engine SHALL prevent cross-tenant data leakage by validating tenant ID on all database queries
4. THE Search_Engine SHALL use the businessProfileId as the tenant isolation key for all operations

### Requirement 23: Search Extensibility for AI Recommendations

**User Story:** As a product manager, I want the search architecture to support future AI-powered recommendations, so that we can enhance the feature without major refactoring.

#### Acceptance Criteria

1. THE Search_Engine SHALL implement a modular service layer separating query logic, filtering, ranking, and response mapping
2. THE Search_Engine SHALL define a Ranking_Algorithm interface that can be swapped with alternative implementations
3. THE Search_Engine SHALL log search queries and user interactions to support future machine learning model training
4. THE Search_Engine SHALL expose a configuration parameter for selecting ranking strategy (default, ML-based, or hybrid)
5. WHERE an ML-based ranking strategy is configured, THE Search_Engine SHALL delegate ranking to an external recommendation service

### Requirement 24: Search Analytics and Monitoring

**User Story:** As a platform administrator, I want to monitor search usage and performance, so that I can optimize the feature and identify issues.

#### Acceptance Criteria

1. THE Search_Engine SHALL log each search request with timestamp, user ID, query, filters, and result count
2. THE Search_Engine SHALL log search execution time for performance monitoring
3. THE Search_Engine SHALL expose metrics for total searches, average response time, and error rate
4. THE Search_Engine SHALL integrate with Spring Boot Actuator for health checks and metrics endpoints
5. THE Search_Engine SHALL log errors with stack traces for debugging

### Requirement 25: Backward Compatibility with Legacy Vendor Entity

**User Story:** As a platform administrator, I want the new search system to coexist with the legacy Vendor entity, so that existing functionality is not disrupted during migration.

#### Acceptance Criteria

1. THE Search_Engine SHALL query only BusinessProfile entities with ROLE_VENDOR, not the legacy Vendor table
2. THE Search_Engine SHALL not modify or delete records in the legacy Vendor table
3. THE Search_Engine SHALL not depend on the legacy Vendor entity for any search functionality
4. WHERE legacy Item records reference the old Vendor entity, THE Search_Engine SHALL ignore those associations
5. THE Search_Engine SHALL support a future migration path where legacy Vendor records can be linked to BusinessProfile entities
