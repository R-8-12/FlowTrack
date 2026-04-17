-- Migration: Add vendor metadata columns to business_profiles table
-- Purpose: Support vendor search and recommendation engine
-- Requirements: 8.5, 12.6, 12.7
-- Date: 2024

-- Add vendor metadata columns to business_profiles table
ALTER TABLE business_profiles
ADD COLUMN default_delivery_days INT DEFAULT 7 COMMENT 'Default delivery days for vendor orders',
ADD COLUMN reliability_score DOUBLE DEFAULT 0.0 COMMENT 'Reliability score based on order history (0.0-1.0)',
ADD COLUMN rating DOUBLE DEFAULT 0.0 COMMENT 'Average rating from retailer reviews (0.0-5.0)',
ADD COLUMN total_orders INT DEFAULT 0 COMMENT 'Total number of orders received by this vendor',
ADD COLUMN completed_orders INT DEFAULT 0 COMMENT 'Number of successfully completed orders';

-- Add check constraints to ensure data integrity
ALTER TABLE business_profiles
ADD CONSTRAINT chk_reliability_score_range CHECK (reliability_score >= 0.0 AND reliability_score <= 1.0),
ADD CONSTRAINT chk_rating_range CHECK (rating >= 0.0 AND rating <= 5.0),
ADD CONSTRAINT chk_total_orders_non_negative CHECK (total_orders >= 0),
ADD CONSTRAINT chk_completed_orders_non_negative CHECK (completed_orders >= 0),
ADD CONSTRAINT chk_completed_orders_lte_total CHECK (completed_orders <= total_orders);

-- Create indexes for vendor search performance
CREATE INDEX idx_bp_default_delivery_days ON business_profiles(default_delivery_days);
CREATE INDEX idx_bp_reliability_score ON business_profiles(reliability_score);
CREATE INDEX idx_bp_rating ON business_profiles(rating);

-- Update existing records to have default values (if any exist)
UPDATE business_profiles
SET default_delivery_days = 7,
    reliability_score = 0.0,
    rating = 0.0,
    total_orders = 0,
    completed_orders = 0
WHERE default_delivery_days IS NULL
   OR reliability_score IS NULL
   OR rating IS NULL
   OR total_orders IS NULL
   OR completed_orders IS NULL;
