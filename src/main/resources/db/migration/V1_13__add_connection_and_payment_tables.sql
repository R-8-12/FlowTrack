-- V1_13: Phase 3 + Phase 4 — Retailer-Vendor Connection & Procurement Payment tables
-- Author: FlowTrack Platform
-- Date: 2026-04-18

-- ─────────────────────────────────────────────────────────────────────────────
-- Table: retailer_vendor_connections
-- Tracks explicit connection requests between retailers and vendor profiles.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS retailer_vendor_connections (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    retailer_user_id    BIGINT          NOT NULL,
    vendor_profile_id   BIGINT          NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'REQUESTED',
    retailer_message    VARCHAR(500),
    vendor_response_note VARCHAR(500),
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,

    PRIMARY KEY (id),

    -- Enforce one connection record per retailer-vendor pair
    CONSTRAINT uq_retailer_vendor UNIQUE (retailer_user_id, vendor_profile_id),

    CONSTRAINT fk_rvc_retailer
        FOREIGN KEY (retailer_user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_rvc_vendor_profile
        FOREIGN KEY (vendor_profile_id) REFERENCES business_profiles(id) ON DELETE CASCADE,

    CONSTRAINT chk_rvc_status
        CHECK (status IN ('REQUESTED', 'CONNECTED', 'REJECTED', 'BLOCKED'))
);

-- Indexes for common query patterns
CREATE INDEX idx_rvc_retailer_id     ON retailer_vendor_connections (retailer_user_id);
CREATE INDEX idx_rvc_vendor_id       ON retailer_vendor_connections (vendor_profile_id);
CREATE INDEX idx_rvc_status          ON retailer_vendor_connections (status);
CREATE INDEX idx_rvc_retailer_status ON retailer_vendor_connections (retailer_user_id, status);
CREATE INDEX idx_rvc_vendor_status   ON retailer_vendor_connections (vendor_profile_id, status);


-- ─────────────────────────────────────────────────────────────────────────────
-- Table: procurement_payments
-- Tracks Razorpay payment records linked to procurement orders.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS procurement_payments (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    procurement_order_id    BIGINT          NOT NULL,
    retailer_user_id        BIGINT          NOT NULL,
    razorpay_order_id       VARCHAR(100),
    razorpay_payment_id     VARCHAR(100),
    razorpay_signature      VARCHAR(256),
    amount                  DOUBLE          NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    failure_reason          VARCHAR(500),
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_pp_order
        FOREIGN KEY (procurement_order_id) REFERENCES procurement_orders(id) ON DELETE CASCADE,

    CONSTRAINT fk_pp_retailer
        FOREIGN KEY (retailer_user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT chk_pp_status
        CHECK (status IN ('PENDING', 'AUTHORIZED', 'PAID', 'FAILED', 'REFUNDED'))
);

-- Indexes for webhook lookups and analytics queries
CREATE INDEX idx_pp_order_id          ON procurement_payments (procurement_order_id);
CREATE INDEX idx_pp_retailer_id       ON procurement_payments (retailer_user_id);
CREATE INDEX idx_pp_razorpay_order_id ON procurement_payments (razorpay_order_id);
CREATE INDEX idx_pp_status            ON procurement_payments (status);
CREATE INDEX idx_pp_retailer_status   ON procurement_payments (retailer_user_id, status);
