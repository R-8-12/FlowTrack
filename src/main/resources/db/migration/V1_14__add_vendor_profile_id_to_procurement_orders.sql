-- V1_14: Add vendor_profile_id FK to procurement_orders
-- Bridges the legacy vendor_id with the new BusinessProfile-based vendor flow.
-- NULL allowed for backward compatibility with existing legacy orders.

ALTER TABLE procurement_orders
    ADD COLUMN vendor_profile_id BIGINT NULL,
    ADD CONSTRAINT fk_po_vendor_profile
        FOREIGN KEY (vendor_profile_id) REFERENCES business_profiles(id) ON DELETE SET NULL;

CREATE INDEX idx_po_vendor_profile_id ON procurement_orders (vendor_profile_id);
