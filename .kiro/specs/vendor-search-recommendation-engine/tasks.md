# Implementation Plan: Vendor Search & Recommendation Engine

## Overview

This implementation plan breaks down the Vendor Search & Recommendation Engine feature into actionable coding tasks organized by phase. The feature enables retailers to search for verified vendors using multiple criteria (product name, price, delivery time, stock availability) with intelligent ranking and badge assignment.

**Technology Stack**: Java 11, Spring Boot 2.7.18, Spring Data JPA, Thymeleaf, Bootstrap 5, JavaScript ES6

**Key Components**:
- REST API endpoint: `GET /api/retailer/vendors/search`
- Service layer with weighted ranking algorithm (35% price + 25% delivery + 25% reliability + 15% stock)
- Repository layer with JPA queries and database indexes
- Thymeleaf UI with dynamic JavaScript rendering
- Badge system: BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY

## Tasks

- [x] 1. Create DTOs and Enums
  - Create `VendorSearchRequest` DTO with validation annotations (@Min, @Max, @Pattern, @DecimalMin)
  - Create `VendorSearchResponse` DTO with pagination metadata (vendors, currentPage, totalPages, totalElements, pageSize)
  - Create `VendorCardDTO` with Builder pattern for vendor display data
  - Create `Badge` enum with values: BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY, NONE (include displayName and colorCode fields)
  - Add validation method `validatePriceRange()` to VendorSearchRequest
  - Add helper methods `getEffectiveSortDirection()`, `hasNext()`, `hasPrevious()` to response DTOs
  - _Requirements: 1.1, 1.5, 1.6, 2.1-2.5, 3.1-3.3, 4.1-4.3, 7.1-7.8, 9.1-9.6, 11.1-11.5, 12.1-12.11, 15.1-15.7_

- [x] 2. Set up database schema and indexes
  - [x] 2.1 Add database indexes for search performance
    - Create index on `business_profiles(verification_status, onboarding_stage, user_id)` composite
    - Create index on `inventory_item(item_name)` for product name search
    - Create index on `inventory_item(item_price)` for price filtering
    - Create index on `inventory_item(item_quantity)` for stock filtering
    - Create index on `users(enabled)` for active user filtering
    - Create index on `procurement_orders(retailer_id, vendor_id)` for order history lookup
    - _Requirements: 14.1-14.5_
  
  - [x] 2.2 Add vendor metadata columns to BusinessProfile entity
    - Add `defaultDeliveryDays` (Integer, default 7) to BusinessProfile
    - Add `reliabilityScore` (Double, default 0.0, range 0.0-1.0) to BusinessProfile
    - Add `rating` (Double, default 0.0, range 0.0-5.0) to BusinessProfile
    - Add `totalOrders` (Integer, default 0) to BusinessProfile
    - Add `completedOrders` (Integer, default 0) to BusinessProfile
    - Update JPA entity annotations and generate migration script
    - _Requirements: 8.5, 12.6, 12.7_

- [x] 3. Implement repository layer
  - Create `VendorSearchRepository` interface extending JpaRepository<BusinessProfile, Long>
  - Implement `searchVendors()` method with @Query annotation using JPQL
  - Add JOIN FETCH for User entity to avoid N+1 queries
  - Add LEFT JOIN with Item entity for product name and price filtering
  - Add WHERE clauses for verificationStatus = VERIFIED, onboardingStage = ACTIVE, user.enabled = true
  - Add dynamic filtering for query string (LOWER LIKE for case-insensitive search)
  - Add dynamic filtering for minPrice, maxPrice, minQuantity parameters
  - Implement `countMatchingVendors()` method for pagination metadata
  - Add Pageable parameter support for pagination and sorting
  - _Requirements: 1.1-1.6, 2.1-2.5, 3.1-3.4, 4.1-4.3, 6.1-6.4, 14.1-14.5, 22.1-22.4_

- [x] 3.1 Write unit tests for repository layer
  - Test searchVendors with query parameter (product name and vendor name matching)
  - Test searchVendors with price range filters (minPrice, maxPrice, both)
  - Test searchVendors with delivery time filter
  - Test searchVendors with stock quantity filter
  - Test that only VERIFIED and ACTIVE vendors are returned
  - Test that disabled users are excluded
  - Test pagination (page size, page number, total elements)
  - Use @DataJpaTest annotation and in-memory H2 database
  - _Requirements: 1.1-1.6, 2.1-2.5, 3.1-3.4, 4.1-4.3, 6.1-6.4_

- [x] 4. Implement ranking strategy
  - [x] 4.1 Create RankingStrategy interface
    - Define `rankVendors(List<VendorCardDTO> vendors)` method returning sorted list
    - Add JavaDoc explaining strategy pattern for future ML integration
    - _Requirements: 8.1, 23.1-23.5_
  
  - [x] 4.2 Implement WeightedRankingStrategy
    - Define weight constants: PRICE_WEIGHT=0.35, DELIVERY_WEIGHT=0.25, RELIABILITY_WEIGHT=0.25, STOCK_WEIGHT=0.15
    - Implement `rankVendors()` method to compute relevance scores
    - Implement `normalizePrice()` method (lower price = higher score, inverted normalization)
    - Implement `normalizeDelivery()` method (faster delivery = higher score, inverted normalization)
    - Implement `normalizeStock()` method (higher stock = higher score)
    - Handle edge case where all vendors have identical values (return 0.5 score)
    - Sort vendors by computed score in descending order
    - Annotate with @Component for Spring dependency injection
    - _Requirements: 8.1-8.7_

- [x] 4.3 Write unit tests for ranking algorithm
  - Test rankVendors with diverse vendor data (verify correct ordering)
  - Test rankVendors with empty list (should return empty list)
  - Test rankVendors with identical values (all scores should be 0.5)
  - Test normalization methods with edge cases (min equals max)
  - Test that lower price vendors rank higher (when other factors equal)
  - Test that faster delivery vendors rank higher (when other factors equal)
  - Test weighted scoring formula accuracy
  - _Requirements: 8.1-8.7_

- [x] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement service layer
  - [x] 6.1 Create VendorSearchService interface
    - Define `searchVendors(VendorSearchRequest request, Long userId)` method
    - Add JavaDoc explaining service responsibilities
    - _Requirements: 1.1-1.6, 7.1-7.8, 8.1-8.7, 9.1-9.6, 10.1-10.4_
  
  - [x] 6.2 Implement VendorSearchServiceImpl
    - Inject VendorSearchRepository, ProcurementOrderRepository, RankingStrategy dependencies
    - Implement `searchVendors()` method orchestrating the search workflow
    - Call `request.validatePriceRange()` for input validation
    - Build Pageable with sorting configuration based on sortBy and sortDirection
    - Call repository.searchVendors() with filters and pagination
    - Map BusinessProfile entities to VendorCardDTO using Builder pattern
    - Apply ranking strategy when sortBy equals "relevance"
    - Call `assignBadges()` to assign BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY badges
    - Build and return VendorSearchResponse with pagination metadata
    - Add @Service and @Transactional(readOnly = true) annotations
    - Add SLF4J logging for search requests and results
    - _Requirements: 1.1-1.6, 7.1-7.8, 8.1-8.7, 9.1-9.6, 10.1-10.4, 11.1-11.5, 12.1-12.11, 24.1-24.5_
  
  - [x] 6.3 Implement badge assignment logic
    - Implement `assignBadges(List<VendorCardDTO> vendors)` private method
    - Find vendor with lowest pricePerUnit and assign Badge.BEST_PRICE
    - Find vendor with lowest deliveryDays and assign Badge.FAST_DELIVERY (if different from best price)
    - Find vendor with highest reliabilityScore and assign Badge.HIGH_RELIABILITY (if different from previous two)
    - Handle ties by assigning badge to first vendor in sorted list
    - Ensure each vendor gets at most one badge
    - _Requirements: 9.1-9.6_
  
  - [x] 6.4 Implement order history lookup
    - Implement `hasPreviousOrders(Long vendorId, Long retailerUserId)` private method
    - Query ProcurementOrderRepository for orders matching retailer and vendor IDs
    - Filter by order status: COMPLETED or DELIVERED
    - Return true if at least one matching order exists, false otherwise
    - _Requirements: 10.1-10.4_
  
  - [x] 6.5 Implement entity to DTO mapping
    - Implement `mapToVendorCard(BusinessProfile bp, Long retailerUserId)` private method
    - Extract vendorId from BusinessProfile.id
    - Extract vendorName from BusinessProfile.legalBusinessName
    - Extract pricePerUnit from associated Item entity (handle multiple items - use lowest price)
    - Extract availableQuantity from associated Item entity (handle multiple items - use sum or max)
    - Extract deliveryDays from BusinessProfile.defaultDeliveryDays
    - Extract reliabilityScore from BusinessProfile.reliabilityScore
    - Extract rating from BusinessProfile.rating
    - Set verified to true (always, since filtered)
    - Format location as "City, State" from registeredAddress and state fields
    - Call hasPreviousOrders() to set previouslyOrdered flag
    - Set badge to NONE initially (will be assigned later)
    - Use VendorCardDTO.Builder pattern
    - _Requirements: 12.1-12.11_
  
  - [x] 6.6 Implement buildPageable helper method
    - Implement `buildPageable(VendorSearchRequest request)` private method
    - Map sortBy "price" to Sort.by("price") with direction from request
    - Map sortBy "delivery" to Sort.by("deliveryDays") with direction from request
    - Map sortBy "rating" to Sort.by("rating") with direction from request
    - Map sortBy "relevance" to Sort.unsorted() (sorting done in-memory after ranking)
    - Use request.getEffectiveSortDirection() for default direction logic
    - Return PageRequest.of(page, size, sort)
    - _Requirements: 7.1-7.8_

- [x] 6.7 Write unit tests for service layer
  - Test searchVendors with valid request (verify response structure)
  - Test searchVendors with invalid price range (should throw IllegalArgumentException)
  - Test searchVendors with empty results (verify empty response)
  - Test badge assignment with diverse vendors (verify correct badges)
  - Test badge assignment with ties (verify first vendor gets badge)
  - Test order history lookup (mock ProcurementOrderRepository)
  - Test entity to DTO mapping (verify all fields populated correctly)
  - Test buildPageable with different sortBy values
  - Use @ExtendWith(MockitoExtension.class) and mock dependencies
  - _Requirements: 1.1-1.6, 7.1-7.8, 8.1-8.7, 9.1-9.6, 10.1-10.4, 12.1-12.11_

- [x] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Implement controller layer
  - [x] 8.1 Create RetailerVendorSearchController REST controller
    - Annotate with @RestController, @RequestMapping("/api/retailer/vendors"), @PreAuthorize("hasAuthority('ROLE_RETAILER')")
    - Inject VendorSearchService dependency
    - Add SLF4J logger
    - _Requirements: 13.1-13.5, 21.1-21.7_
  
  - [x] 8.2 Implement search endpoint
    - Create `searchVendors(@Valid @ModelAttribute VendorSearchRequest request)` method
    - Annotate with @GetMapping("/search")
    - Extract authenticated User from SecurityContextHolder.getContext().getAuthentication()
    - Call vendorSearchService.searchVendors(request, user.getId())
    - Return ResponseEntity.ok(response) on success
    - Log search request with user ID and query parameters
    - Log search completion with result count
    - _Requirements: 13.1-13.5, 21.1-21.7, 24.1-24.5_
  
  - [x] 8.3 Implement error handling
    - Add try-catch for IllegalArgumentException (return 400 Bad Request)
    - Add try-catch for generic Exception (return 500 Internal Server Error with logging)
    - Create @ExceptionHandler for MethodArgumentNotValidException (validation errors)
    - Return Map<String, String> with field errors for validation failures
    - Log all errors with appropriate levels (WARN for validation, ERROR for system errors)
    - _Requirements: 15.1-15.7, 21.3-21.6_
  
  - [x] 8.4 Create RetailerVendorSearchPageController for UI
    - Annotate with @Controller, @RequestMapping("/retailer"), @PreAuthorize("hasAuthority('ROLE_RETAILER')")
    - Create `vendorSearchPage(Model model)` method annotated with @GetMapping("/vendor-search")
    - Add authenticated User to model
    - Return "retailer/vendor-search" view name
    - _Requirements: 16.1-16.7_

- [x] 8.5 Write integration tests for controller
  - Test search endpoint with valid request (verify 200 OK and JSON structure)
  - Test search endpoint with invalid page number (verify 400 Bad Request)
  - Test search endpoint with invalid page size (verify 400 Bad Request)
  - Test search endpoint with invalid sortBy (verify 400 Bad Request)
  - Test search endpoint with ROLE_VENDOR user (verify 403 Forbidden)
  - Test search endpoint without authentication (verify 401 Unauthorized)
  - Test validation error handling (verify field error messages)
  - Use @WebMvcTest and MockMvc for testing
  - Use @WithMockUser for authentication testing
  - _Requirements: 13.1-13.5, 15.1-15.7, 21.1-21.7_

- [x] 9. Implement global exception handler
  - Create VendorSearchExceptionHandler class with @RestControllerAdvice
  - Create ErrorResponse DTO with status, message, fieldErrors, timestamp fields
  - Add @ExceptionHandler for MethodArgumentNotValidException (return 400 with field errors)
  - Add @ExceptionHandler for IllegalArgumentException (return 400 with message)
  - Add @ExceptionHandler for AccessDeniedException (return 403 with message)
  - Add @ExceptionHandler for EntityNotFoundException (return 404 with message)
  - Add @ExceptionHandler for generic Exception (return 500 with generic message, log stack trace)
  - Add SLF4J logging for all exceptions
  - _Requirements: 15.1-15.7, 21.3-21.6, 24.1-24.5_

- [x] 10. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Create Thymeleaf UI template
  - [x] 11.1 Create retailer/vendor-search.html template
    - Add HTML5 doctype and Thymeleaf namespace declarations
    - Add CSRF token meta tags using Thymeleaf expressions
    - Include Bootstrap 5 CSS and Font Awesome icons
    - Include custom vendor-search.css stylesheet
    - Add navigation bar fragment
    - _Requirements: 16.1-16.7, 19.2_
  
  - [x] 11.2 Create left sidebar with filter controls
    - Create card with "Filters" header
    - Add price range inputs (minPrice, maxPrice) with numeric validation
    - Add maxDeliveryDays input with numeric validation
    - Add minQuantity input with numeric validation
    - Add maxDistanceKm input with numeric validation
    - Add sortBy dropdown with options: Relevance, Price, Delivery Time, Rating
    - Add sortDirection dropdown with options: Ascending, Descending
    - Add "Clear Filters" button
    - Wrap all inputs in form with id="filterForm"
    - _Requirements: 16.2, 18.1-18.7_
  
  - [x] 11.3 Create main content area
    - Add search bar with input field (id="searchQuery") and search icon
    - Add loading indicator div (id="loadingIndicator") with spinner, initially hidden
    - Add results count div (id="resultsCount") with total results placeholder, initially hidden
    - Add vendor grid div (id="vendorGrid") with Bootstrap row and column classes
    - Add no results message div (id="noResults") with icon and text, initially hidden
    - Add pagination nav (id="paginationNav") with ul (id="paginationControls"), initially hidden
    - _Requirements: 16.1, 16.3-16.7, 20.1-20.7_
  
  - [x] 11.4 Include JavaScript files
    - Include Bootstrap 5 JS bundle
    - Include custom vendor-search.js script
    - _Requirements: 19.1-19.6_

- [x] 12. Implement JavaScript client
  - [x] 12.1 Create vendor-search.js with initialization
    - Declare global state variables: currentPage, currentFilters, debounceTimer
    - Extract CSRF token and header from meta tags
    - Add DOMContentLoaded event listener to call initializeEventListeners() and performSearch()
    - _Requirements: 19.1-19.6_
  
  - [x] 12.2 Implement event listeners
    - Add input event listener to searchQuery with 500ms debounce
    - Add change event listeners to all filter inputs (minPrice, maxPrice, maxDeliveryDays, minQuantity, maxDistanceKm, sortBy, sortDirection)
    - Add click event listener to clearFilters button
    - Reset currentPage to 0 on any filter change
    - _Requirements: 18.6, 19.1-19.6_
  
  - [x] 12.3 Implement performSearch function
    - Show loading indicator
    - Build query parameters from form inputs using buildQueryParams()
    - Make fetch GET request to /api/retailer/vendors/search with CSRF token in headers
    - Handle response: parse JSON and call renderResults(data)
    - Handle errors: log error and call showError() with user-friendly message
    - Hide loading indicator after completion
    - _Requirements: 19.1-19.6_
  
  - [x] 12.4 Implement buildQueryParams function
    - Create URLSearchParams object
    - Append query parameter if searchQuery input has value
    - Append minPrice, maxPrice, maxDeliveryDays, minQuantity, maxDistanceKm if inputs have values
    - Append sortBy and sortDirection from dropdowns
    - Append page (currentPage) and size (20) for pagination
    - Return params.toString()
    - _Requirements: 19.1-19.6, 21.2_
  
  - [x] 12.5 Implement renderResults function
    - Clear vendorGrid innerHTML
    - If vendors array is empty, show noResults div and hide resultsCount and paginationNav
    - If vendors array has items, update totalResults text, show resultsCount, hide noResults
    - Loop through vendors array and call createVendorCard() for each, append to vendorGrid
    - Call renderPagination(data) to update pagination controls
    - _Requirements: 16.7, 19.3-19.6_
  
  - [x] 12.6 Implement createVendorCard function
    - Create HTML string for vendor card with Bootstrap card classes
    - Add badge span if vendor.badge is not NONE (position absolute, top-right corner)
    - Add vendor name with verified icon if vendor.verified is true
    - Add price with ₹ symbol and 2 decimal places
    - Add stock available with quantity
    - Add delivery time with days
    - Add reliability score as percentage (multiply by 100)
    - Add rating with star icon
    - Add location
    - Add "Previously Ordered" badge if vendor.previouslyOrdered is true
    - Add "View Details" and "Order Now" buttons in card footer
    - Return HTML string
    - _Requirements: 17.1-17.11_
  
  - [x] 12.7 Implement renderPagination function
    - Hide paginationNav if totalPages <= 1
    - Clear paginationControls innerHTML
    - Add "Previous" button (disabled if currentPage is 0)
    - Calculate startPage and endPage for page number range (max 10 pages visible)
    - Loop from startPage to endPage, add page number buttons (mark current page as active)
    - Add "Next" button (disabled if currentPage >= totalPages - 1)
    - _Requirements: 20.1-20.7_
  
  - [x] 12.8 Implement helper functions
    - Implement changePage(page) to update currentPage and call performSearch()
    - Implement clearFilters() to reset all form inputs and call performSearch()
    - Implement showLoading(show) to toggle loading indicator visibility
    - Implement showError(message) to display error alert in vendorGrid
    - Implement getBadgeColor(badge) to return Bootstrap color class for badge type
    - Implement viewVendorDetails(vendorId) to navigate to vendor profile page
    - Implement placeOrder(vendorId) to navigate to order creation page
    - _Requirements: 18.7, 19.6, 20.5_

- [x] 13. Create custom CSS styling
  - Create static/css/vendor-search.css file
  - Add vendor-card hover effect (transform translateY and box-shadow transition)
  - Add badge positioning and sizing styles
  - Add vendor-details paragraph styling
  - Add search bar focus effect
  - Add pagination styling (colors, active state)
  - Add loading spinner sizing
  - Add responsive media queries for mobile (max-width: 768px)
  - Add badge color classes (bg-success, bg-primary, bg-warning)
  - _Requirements: 16.1-16.7, 17.1-17.11_

- [x] 14. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 15. Configure security
  - [x] 15.1 Update SecurityConfig for vendor search endpoints
    - Add antMatcher for /api/retailer/vendors/** requiring ROLE_RETAILER authority
    - Add antMatcher for /retailer/vendor-search requiring ROLE_RETAILER authority
    - Configure CSRF token repository with CookieCsrfTokenRepository.withHttpOnlyFalse()
    - _Requirements: 13.1-13.5, 19.2_
  
  - [x] 15.2 Verify multi-tenant data isolation
    - Review repository queries to ensure tenant scoping by user context
    - Review service layer to ensure userId is used for order history lookup
    - Add integration test to verify cross-tenant data is not accessible
    - _Requirements: 22.1-22.4_

- [x] 16. Add analytics and monitoring
  - Add logging for each search request with timestamp, userId, query, filters, result count
  - Add logging for search execution time using StopWatch or System.currentTimeMillis()
  - Configure Spring Boot Actuator for health checks and metrics endpoints
  - Add custom metrics for total searches, average response time, error rate (if Actuator metrics available)
  - Ensure all errors are logged with stack traces for debugging
  - _Requirements: 21.7, 24.1-24.5_

- [x] 17. Integration and wiring
  - [x] 17.1 Verify all components are wired correctly
    - Verify VendorSearchRepository is injected into VendorSearchService
    - Verify RankingStrategy is injected into VendorSearchService
    - Verify VendorSearchService is injected into RetailerVendorSearchController
    - Verify all @Component, @Service, @Repository, @RestController annotations are present
    - _Requirements: 23.1-23.5_
  
  - [x] 17.2 Test end-to-end flow manually
    - Start application and navigate to /retailer/vendor-search
    - Verify page loads with filters and search bar
    - Enter search query and verify results are displayed
    - Apply price filter and verify results update
    - Apply delivery time filter and verify results update
    - Apply stock filter and verify results update
    - Change sort option and verify results reorder
    - Navigate to page 2 and verify pagination works
    - Click "Clear Filters" and verify all filters reset
    - Verify badges are displayed correctly (BEST_PRICE, FAST_DELIVERY, HIGH_RELIABILITY)
    - Verify "Previously Ordered" indicator appears for vendors with order history
    - _Requirements: All requirements_
  
  - [x] 17.3 Verify backward compatibility
    - Verify legacy Vendor entity is not modified or queried
    - Verify only BusinessProfile entities with ROLE_VENDOR are used
    - Verify legacy Item records are not affected
    - _Requirements: 25.1-25.5_

- [x] 17.4 Write integration tests for end-to-end flow
  - Test complete search flow from controller to repository
  - Test search with multiple filters applied simultaneously
  - Test pagination across multiple pages
  - Test sorting by different criteria
  - Test badge assignment in real scenario with test data
  - Test order history lookup with real ProcurementOrder data
  - Use @SpringBootTest for full application context
  - _Requirements: All requirements_

- [x] 18. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Unit tests validate specific logic components
- Integration tests validate component interactions and security
- No property-based tests are included because this is an infrastructure/UI feature without complex transformation logic
- The design uses pseudocode in some sections, but the implementation language is Java (confirmed from pom.xml)
- Database indexes are critical for performance (Requirement 14.5: <2 seconds response time for 1000 vendors)
- Multi-tenant isolation is enforced through userId parameter in all service methods
- CSRF protection is required for all API requests from JavaScript client
