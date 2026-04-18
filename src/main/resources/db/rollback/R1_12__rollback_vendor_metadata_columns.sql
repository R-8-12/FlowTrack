-- Rollback: Remove vendor metadata columns from business_profiles table
-- Purpose: Rollback migration V1_12__add_vendor_metadata_columns.sql
-- Date: 2024

-- Drop indexes first
DROP INDEX IF EXISTS idx_bp_rating ON business_profiles;
DROP INDEX IF EXISTS idx_bp_reliability_score ON business_profiles;
DROP INDEX IF EXISTS idx_bp_default_delivery_days ON business_profiles;

-- Drop check constraints
ALTER TABLE business_profiles
DROP CONSTRAINT IF EXISTS chk_completed_orders_lte_total,
DROP CONSTRAINT IF EXISTS chk_completed_orders_non_negative,
DROP CONSTRAINT IF EXISTS chk_total_orders_non_negative,
DROP CONSTRAINT IF EXISTS chk_rating_range,
DROP CONSTRAINT IF EXISTS chk_reliability_score_range;

-- Drop vendor metadata columns
ALTER TABLE business_profiles
DROP COLUMN IF EXISTS completed_orders,
DROP COLUMN IF EXISTS total_orders,
DROP COLUMN IF EXISTS rating,
DROP COLUMN IF EXISTS reliability_score,
DROP COLUMN IF EXISTS default_delivery_days;
