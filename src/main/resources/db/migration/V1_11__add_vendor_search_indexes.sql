-- =====================================================
-- Migration: V1_11 - Add Vendor Search Performance Indexes
-- Purpose: Create database indexes to optimize vendor search queries
-- Requirements: 14.1-14.5
-- =====================================================

-- Index on business_profiles for vendor search filtering
-- Composite index for common search pattern: verification_status + onboarding_stage + user_id
CREATE INDEX idx_bp_search ON business_profiles(verification_status, onboarding_stage, user_id);

-- Index on inventory_item for product name search (case-insensitive LIKE queries)
CREATE INDEX idx_item_name ON inventory_item(item_name);

-- Index on inventory_item for price filtering
CREATE INDEX idx_item_price ON inventory_item(item_price);

-- Index on inventory_item for stock filtering
CREATE INDEX idx_item_quantity ON inventory_item(item_quantity);

-- Index on users for active user filtering
CREATE INDEX idx_user_enabled ON users(enabled);

-- Composite index on procurement_orders for order history lookup
-- Used to check if retailer has previous orders with a vendor
CREATE INDEX idx_order_retailer_vendor ON procurement_orders(retailer_user_id, vendor_id);
