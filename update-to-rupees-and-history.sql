-- SQL Script to Convert Prices to Rupees and Add Historical Graph Data
-- Run this in MySQL after the application has started once

USE ims;

-- ============================================
-- PART 1: Convert Prices from Dollars to Rupees
-- Exchange Rate: 1 USD = 83 INR (approximate)
-- ============================================

-- Update Item Prices (multiply by 83)
UPDATE inventory_item SET item_price = item_price * 83;
UPDATE inventory_item SET item_fine_rate = item_fine_rate * 83;

-- Show updated prices
SELECT item_name, item_price as 'Price (₹)', item_fine_rate as 'Fine Rate (₹)' 
FROM inventory_item;

-- ============================================
-- PART 2: Add Historical Dashboard Snapshots
-- Creating data for last 15 days with realistic trends
-- ============================================

-- Clear existing snapshots (if any)
DELETE FROM dashboard_snapshot;

-- Insert historical data (15 days of history)
-- Nov 1, 2024
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-01 09:00:00', 0, 0, 250, 0, 'INITIAL');

-- Nov 2, 2024 - First items added
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-02 10:30:00', 0, 0, 245, 0, 'ITEM_ADDED');

-- Nov 3, 2024 - Items issued
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-03 11:15:00', 2, 0, 243, 2, 'ITEM_ISSUED');

-- Nov 4, 2024 - More items issued
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-04 14:20:00', 3, 0, 242, 3, 'ITEM_ISSUED');

-- Nov 5, 2024 - Items returned
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-05 09:45:00', 2, 1, 243, 3, 'ITEM_RETURNED');

-- Nov 6, 2024 - New inventory added
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-06 10:00:00', 2, 1, 248, 3, 'ITEM_ADDED');

-- Nov 7, 2024 - Items issued
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-07 13:30:00', 4, 1, 244, 5, 'ITEM_ISSUED');

-- Nov 8, 2024 - Items returned
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-08 11:00:00', 3, 2, 245, 5, 'ITEM_RETURNED');

-- Nov 9, 2024 - Weekend (no activity)
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-09 09:00:00', 3, 2, 245, 5, 'WEEKEND');

-- Nov 10, 2024 - Weekend (no activity)
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-10 09:00:00', 3, 2, 245, 5, 'WEEKEND');

-- Nov 11, 2024 - New week, items issued
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-11 10:15:00', 5, 2, 240, 7, 'ITEM_ISSUED');

-- Nov 12, 2024 - Items returned and new items added
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-12 14:00:00', 4, 3, 241, 7, 'ITEM_RETURNED');

-- Nov 13, 2024 - Current day (morning)
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-13 09:00:00', 3, 3, 238, 6, 'ITEM_ISSUED');

-- Nov 13, 2024 - Current day (afternoon) - matches current state
INSERT INTO dashboard_snapshot (timestamp, items_borrowed, items_returned, inventory_remaining, items_issued, event_type)
VALUES ('2024-11-13 15:30:00', 3, 0, 235, 3, 'ITEM_UPDATED');

-- ============================================
-- PART 3: Verify Changes
-- ============================================

-- Show price summary in Rupees
SELECT 
    'Price Conversion Summary' as Info,
    COUNT(*) as 'Total Items',
    MIN(item_price) as 'Min Price (₹)',
    MAX(item_price) as 'Max Price (₹)',
    AVG(item_price) as 'Avg Price (₹)'
FROM inventory_item;

-- Show historical data count
SELECT 
    'Historical Data Summary' as Info,
    COUNT(*) as 'Total Snapshots',
    MIN(timestamp) as 'From Date',
    MAX(timestamp) as 'To Date'
FROM dashboard_snapshot;

-- Show daily summary
SELECT 
    DATE(timestamp) as 'Date',
    items_borrowed as 'Borrowed',
    items_returned as 'Returned',
    inventory_remaining as 'Inventory',
    items_issued as 'Issued',
    event_type as 'Event'
FROM dashboard_snapshot
ORDER BY timestamp;

SELECT '✅ Conversion Complete! Prices are now in Rupees (₹) and historical graph data has been added.' as Status;
