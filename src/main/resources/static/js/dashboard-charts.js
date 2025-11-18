// Dashboard Event-Based Charts
let charts = {};
let lastUpdateTime = null;

// Fetch dashboard history
function fetchDashboardHistory() {
    return fetch('/api/dashboard/history')
        .then(response => response.json())
        .catch(error => {
            console.error('Error fetching dashboard history:', error);
            return [];
        });
}

// Fetch current stats
function fetchCurrentStats() {
    return fetch('/api/dashboard/stats')
        .then(response => response.json())
        .catch(error => {
            console.error('Error fetching current stats:', error);
            return null;
        });
}

// Initialize charts with historical data
function initializeCharts() {
    fetchDashboardHistory().then(history => {
        console.log('Dashboard history:', history);
        
        if (history.length === 0) {
            // No history, create initial snapshot
            fetchCurrentStats().then(stats => {
                if (stats) {
                    createChartsWithSinglePoint(stats);
                }
            });
            return;
        }
        
        // Extract data from history - use date labels
        const labels = history.map(h => h.date || 'Today');
        
        const borrowedData = history.map(h => h.itemsBorrowed);
        const returnedData = history.map(h => h.itemsReturned);
        const inventoryData = history.map(h => h.inventoryRemaining);
        const issuedData = history.map(h => h.itemsIssued);
        
        // Store last update time
        if (history.length > 0) {
            lastUpdateTime = history[history.length - 1].timestamp;
        }
        
        // Create charts with historical data
        createChart('myAreaChart', 'Items Borrowed', labels, borrowedData, 'rgba(255, 206, 86, 0.2)', 'rgba(255, 206, 86, 1)', 'borrowed');
        createChart('myDuplicateChart', 'Items Returned', labels, returnedData, 'rgba(54, 162, 235, 0.2)', 'rgba(54, 162, 235, 1)', 'returned');
        createChart('myAreaChart2', 'Inventory Remaining', labels, inventoryData, 'rgba(255, 99, 132, 0.2)', 'rgba(255, 99, 132, 1)', 'inventory');
        createChart('myAreaChart3', 'Items Issued', labels, issuedData, 'rgba(153, 102, 255, 0.2)', 'rgba(153, 102, 255, 1)', 'issued');
    });
}

// Create chart with data
function createChart(canvasId, label, labels, data, bgColor, borderColor, chartKey) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;
    
    const currentValue = data.length > 0 ? data[data.length - 1] : 0;
    
    charts[chartKey] = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: label,
                data: data,
                backgroundColor: bgColor,
                borderColor: borderColor,
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            },
            plugins: {
                title: {
                    display: true,
                    text: 'Current: ' + currentValue
                }
            }
        }
    });
}

// Create charts with single data point
function createChartsWithSinglePoint(stats) {
    const label = new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    
    createChart('myAreaChart', 'Items Borrowed', [label], [stats.itemsBorrowed], 'rgba(255, 206, 86, 0.2)', 'rgba(255, 206, 86, 1)', 'borrowed');
    createChart('myDuplicateChart', 'Items Returned', [label], [stats.itemsReturned], 'rgba(54, 162, 235, 0.2)', 'rgba(54, 162, 235, 1)', 'returned');
    createChart('myAreaChart2', 'Inventory Remaining', [label], [stats.inventoryRemaining], 'rgba(255, 99, 132, 0.2)', 'rgba(255, 99, 132, 1)', 'inventory');
    createChart('myAreaChart3', 'Items Issued', [label], [stats.itemsIssued], 'rgba(153, 102, 255, 0.2)', 'rgba(153, 102, 255, 1)', 'issued');
}

// Check for new updates
function checkForUpdates() {
    fetchDashboardHistory().then(history => {
        if (history.length === 0) return;
        
        const latestTimestamp = history[history.length - 1].timestamp;
        
        // Check if there's a new update
        if (lastUpdateTime && latestTimestamp !== lastUpdateTime) {
            console.log('New update detected!');
            lastUpdateTime = latestTimestamp;
            
            // Get the new data point
            const latest = history[history.length - 1];
            const label = latest.date || new Date(latest.timestamp).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
            
            // Check if this date already exists in the chart
            const existingIndex = charts.borrowed.data.labels.indexOf(label);
            
            if (existingIndex >= 0) {
                // Update existing point for this date
                updateExistingPoint(charts.borrowed, existingIndex, latest.itemsBorrowed);
                updateExistingPoint(charts.returned, existingIndex, latest.itemsReturned);
                updateExistingPoint(charts.inventory, existingIndex, latest.inventoryRemaining);
                updateExistingPoint(charts.issued, existingIndex, latest.itemsIssued);
            } else {
                // Add new point for new date
                updateChart(charts.borrowed, label, latest.itemsBorrowed);
                updateChart(charts.returned, label, latest.itemsReturned);
                updateChart(charts.inventory, label, latest.inventoryRemaining);
                updateChart(charts.issued, label, latest.itemsIssued);
            }
        }
    });
}

// Update existing point in chart
function updateExistingPoint(chart, index, value) {
    if (!chart) return;
    
    chart.data.datasets[0].data[index] = value;
    chart.options.plugins.title.text = 'Current: ' + value;
    chart.update();
}

// Update chart with new data point
function updateChart(chart, label, value) {
    if (!chart) return;
    
    chart.data.labels.push(label);
    chart.data.datasets[0].data.push(value);
    
    // Keep only last 20 points
    if (chart.data.labels.length > 20) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
    }
    
    chart.options.plugins.title.text = 'Current: ' + value;
    chart.update();
}

// Initialize when page loads
window.addEventListener('load', function() {
    console.log('Page loaded, initializing charts...');
    
    setTimeout(function() {
        initializeCharts();
        
        // Check for updates every 3 seconds (only updates when data changes)
        setInterval(checkForUpdates, 3000);
    }, 500);
});
