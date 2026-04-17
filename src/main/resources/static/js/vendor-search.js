/**
 * Vendor Search JavaScript Client
 * Handles client-side functionality for the vendor search page
 * Requirements: 19.1-19.6
 */

// ============================================================================
// Global State Variables
// ============================================================================

let currentPage = 0;
let currentFilters = {};
let debounceTimer = null;

// ============================================================================
// CSRF Token Extraction
// ============================================================================

const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

// ============================================================================
// Initialization
// ============================================================================

/**
 * Initialize the vendor search page when DOM is ready
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('Vendor Search: Initializing...');
    
    // Initialize event listeners
    initializeEventListeners();
    
    // Perform initial search to load vendors
    performSearch();
    
    console.log('Vendor Search: Initialization complete');
});

/**
 * Initialize all event listeners for search and filter controls
 * Task 12.2: Implement event listeners
 * Requirements: 18.6, 19.1-19.6
 */
function initializeEventListeners() {
    console.log('Initializing event listeners...');
    
    // Search query input with 500ms debounce
    const searchQuery = document.getElementById('searchQuery');
    if (searchQuery) {
        searchQuery.addEventListener('input', function(e) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentPage = 0;
                performSearch();
            }, 500); // 500ms debounce
        });
    }
    
    // Filter change event listeners
    const filterInputs = ['minPrice', 'maxPrice', 'maxDeliveryDays', 'minQuantity', 
                         'maxDistanceKm', 'sortBy', 'sortDirection'];
    filterInputs.forEach(inputId => {
        const element = document.getElementById(inputId);
        if (element) {
            element.addEventListener('change', function() {
                currentPage = 0;
                performSearch();
            });
        }
    });
    
    // Clear filters button
    const clearFiltersBtn = document.getElementById('clearFilters');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', clearFilters);
    }
    
    console.log('Event listeners initialized');
}

/**
 * Perform vendor search with current filters and pagination
 * Task 12.3: Implement performSearch function
 * Requirements: 19.1-19.6
 */
function performSearch() {
    console.log('Performing search...');
    
    // Show loading indicator
    showLoading(true);
    
    // Build query parameters
    const params = buildQueryParams();
    
    // Make fetch API call to search endpoint
    fetch(`/api/retailer/vendors/search?${params}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Search results received:', data);
        renderResults(data);
        showLoading(false);
    })
    .catch(error => {
        console.error('Search error:', error);
        showError('Failed to load vendors. Please try again.');
        showLoading(false);
    });
}

/**
 * Build URL query parameters from form inputs
 * Task 12.4: Implement buildQueryParams function
 * Requirements: 19.1-19.6, 21.2
 */
function buildQueryParams() {
    const params = new URLSearchParams();
    
    // Search query
    const query = document.getElementById('searchQuery')?.value.trim();
    if (query) {
        params.append('query', query);
    }
    
    // Price filters
    const minPrice = document.getElementById('minPrice')?.value;
    if (minPrice) {
        params.append('minPrice', minPrice);
    }
    
    const maxPrice = document.getElementById('maxPrice')?.value;
    if (maxPrice) {
        params.append('maxPrice', maxPrice);
    }
    
    // Delivery filter
    const maxDeliveryDays = document.getElementById('maxDeliveryDays')?.value;
    if (maxDeliveryDays) {
        params.append('maxDeliveryDays', maxDeliveryDays);
    }
    
    // Stock filter
    const minQuantity = document.getElementById('minQuantity')?.value;
    if (minQuantity) {
        params.append('minQuantity', minQuantity);
    }
    
    // Distance filter
    const maxDistanceKm = document.getElementById('maxDistanceKm')?.value;
    if (maxDistanceKm) {
        params.append('maxDistanceKm', maxDistanceKm);
    }
    
    // Sort options
    const sortBy = document.getElementById('sortBy')?.value || 'relevance';
    params.append('sortBy', sortBy);
    
    const sortDirection = document.getElementById('sortDirection')?.value || 'desc';
    params.append('sortDirection', sortDirection);
    
    // Pagination
    params.append('page', currentPage);
    params.append('size', 20);
    
    return params.toString();
}

/**
 * Render search results in the vendor grid
 * Task 12.5: Implement renderResults function
 * Requirements: 16.7, 19.3-19.6
 */
function renderResults(data) {
    console.log('Rendering results...');
    
    const vendorGrid = document.getElementById('vendorGrid');
    const noResults = document.getElementById('noResults');
    const resultsCount = document.getElementById('resultsCount');
    
    // Clear previous results
    if (vendorGrid) {
        vendorGrid.innerHTML = '';
    }
    
    if (data.vendors && data.vendors.length > 0) {
        // Show results count
        const totalResults = document.getElementById('totalResults');
        if (totalResults) {
            totalResults.textContent = data.totalElements;
        }
        if (resultsCount) {
            resultsCount.style.display = 'block';
        }
        if (noResults) {
            noResults.style.display = 'none';
        }
        
        // Render vendor cards
        data.vendors.forEach(vendor => {
            if (vendorGrid) {
                vendorGrid.innerHTML += createVendorCard(vendor);
            }
        });
        
        // Render pagination
        renderPagination(data);
    } else {
        // Show no results message
        if (resultsCount) {
            resultsCount.style.display = 'none';
        }
        if (noResults) {
            noResults.style.display = 'block';
        }
        const paginationNav = document.getElementById('paginationNav');
        if (paginationNav) {
            paginationNav.style.display = 'none';
        }
    }
}

/**
 * Create HTML for a vendor card
 * Task 12.6: Implement createVendorCard function
 * Requirements: 17.1-17.11
 */
function createVendorCard(vendor) {
    const badgeHtml = vendor.badge !== 'NONE' ? 
        `<span class="badge bg-${getBadgeColor(vendor.badge)} position-absolute top-0 end-0 m-2">
            ${vendor.badge.replace(/_/g, ' ')}
        </span>` : '';
    
    const previousOrderHtml = vendor.previouslyOrdered ? 
        `<span class="badge bg-info text-dark">
            <i class="fas fa-history"></i> Previously Ordered
        </span>` : '';
    
    const verifiedIcon = vendor.verified ? 
        '<i class="fas fa-check-circle text-success" title="Verified"></i>' : '';
    
    return `
        <div class="col">
            <div class="card h-100 shadow-sm vendor-card" data-vendor-id="${vendor.vendorId}">
                ${badgeHtml}
                <div class="card-body">
                    <h5 class="card-title">
                        ${vendor.vendorName} ${verifiedIcon}
                    </h5>
                    
                    <div class="vendor-details">
                        <p class="mb-2">
                            <strong>Price:</strong> 
                            <span class="text-primary fs-5">₹${vendor.pricePerUnit.toFixed(2)}</span> per unit
                        </p>
                        
                        <p class="mb-2">
                            <i class="fas fa-box text-muted"></i>
                            <strong>Stock Available:</strong> ${vendor.availableQuantity} units
                        </p>
                        
                        <p class="mb-2">
                            <i class="fas fa-truck text-muted"></i>
                            <strong>Delivery Time:</strong> ${vendor.deliveryDays} days
                        </p>
                        
                        <p class="mb-2">
                            <i class="fas fa-chart-line text-muted"></i>
                            <strong>Reliability:</strong> ${(vendor.reliabilityScore * 100).toFixed(0)}%
                        </p>
                        
                        <p class="mb-2">
                            <i class="fas fa-star text-warning"></i>
                            <strong>Rating:</strong> ${vendor.rating.toFixed(1)} / 5.0
                        </p>
                        
                        <p class="mb-2">
                            <i class="fas fa-map-marker-alt text-muted"></i>
                            ${vendor.location}
                        </p>
                        
                        ${previousOrderHtml}
                    </div>
                </div>
                
                <div class="card-footer bg-white border-top-0">
                    <div class="d-grid gap-2">
                        <button class="btn btn-primary btn-sm" onclick="viewVendorDetails(${vendor.vendorId})">
                            <i class="fas fa-eye"></i> View Details
                        </button>
                        <button class="btn btn-outline-success btn-sm" onclick="placeOrder(${vendor.vendorId})">
                            <i class="fas fa-shopping-cart"></i> Order Now
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

/**
 * Render pagination controls
 * Task 12.7: Implement renderPagination function
 * Requirements: 20.1-20.7
 */
function renderPagination(data) {
    const paginationNav = document.getElementById('paginationNav');
    const paginationControls = document.getElementById('paginationControls');
    
    if (!paginationNav || !paginationControls) {
        return;
    }
    
    if (data.totalPages <= 1) {
        paginationNav.style.display = 'none';
        return;
    }
    
    paginationNav.style.display = 'block';
    paginationControls.innerHTML = '';
    
    // Previous button
    const prevDisabled = data.currentPage === 0;
    paginationControls.innerHTML += `
        <li class="page-item ${prevDisabled ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${data.currentPage - 1}); return false;">
                <i class="fas fa-chevron-left"></i> Previous
            </a>
        </li>
    `;
    
    // Page numbers (show max 10 pages)
    const startPage = Math.max(0, data.currentPage - 5);
    const endPage = Math.min(data.totalPages, startPage + 10);
    
    for (let i = startPage; i < endPage; i++) {
        const active = i === data.currentPage ? 'active' : '';
        paginationControls.innerHTML += `
            <li class="page-item ${active}">
                <a class="page-link" href="#" onclick="changePage(${i}); return false;">
                    ${i + 1}
                </a>
            </li>
        `;
    }
    
    // Next button
    const nextDisabled = data.currentPage >= data.totalPages - 1;
    paginationControls.innerHTML += `
        <li class="page-item ${nextDisabled ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${data.currentPage + 1}); return false;">
                Next <i class="fas fa-chevron-right"></i>
            </a>
        </li>
    `;
}

// ============================================================================
// Helper Functions
// Task 12.8: Implement helper functions
// Requirements: 18.7, 19.6, 20.5
// ============================================================================

/**
 * Change page and perform new search
 */
function changePage(page) {
    currentPage = page;
    performSearch();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Clear all filters and reset to default state
 */
function clearFilters() {
    console.log('Clearing filters...');
    
    document.getElementById('searchQuery').value = '';
    document.getElementById('minPrice').value = '';
    document.getElementById('maxPrice').value = '';
    document.getElementById('maxDeliveryDays').value = '';
    document.getElementById('minQuantity').value = '';
    document.getElementById('maxDistanceKm').value = '';
    document.getElementById('sortBy').value = 'relevance';
    document.getElementById('sortDirection').value = 'desc';
    
    currentPage = 0;
    performSearch();
}

/**
 * Show or hide loading indicator
 */
function showLoading(show) {
    const loadingIndicator = document.getElementById('loadingIndicator');
    const vendorGrid = document.getElementById('vendorGrid');
    
    if (loadingIndicator) {
        loadingIndicator.style.display = show ? 'block' : 'none';
    }
    if (vendorGrid) {
        vendorGrid.style.display = show ? 'none' : 'block';
    }
}

/**
 * Show error message in vendor grid
 */
function showError(message) {
    const vendorGrid = document.getElementById('vendorGrid');
    if (vendorGrid) {
        vendorGrid.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger" role="alert">
                    <i class="fas fa-exclamation-triangle"></i> ${message}
                </div>
            </div>
        `;
    }
}

/**
 * Get Bootstrap color class for badge type
 */
function getBadgeColor(badge) {
    switch(badge) {
        case 'BEST_PRICE': 
            return 'success';
        case 'FAST_DELIVERY': 
            return 'primary';
        case 'HIGH_RELIABILITY': 
            return 'warning';
        default: 
            return 'secondary';
    }
}

/**
 * Navigate to vendor details page
 */
function viewVendorDetails(vendorId) {
    window.location.href = `/retailer/vendor/${vendorId}`;
}

/**
 * Navigate to order creation page with vendor pre-selected
 */
function placeOrder(vendorId) {
    window.location.href = `/retailer/order/create?vendorId=${vendorId}`;
}
