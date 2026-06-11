-- V1_15: Product catalog table for vendor-listed products
CREATE TABLE IF NOT EXISTS vendor_products (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    vendor_profile_id       BIGINT          NOT NULL,
    name                    VARCHAR(200)    NOT NULL,
    category                VARCHAR(100)    NOT NULL,
    description             TEXT,
    price_per_unit          DECIMAL(12,2)   NOT NULL,
    available_quantity      INT             NOT NULL DEFAULT 0,
    minimum_order_quantity  INT             NOT NULL DEFAULT 1,
    delivery_days           INT             NOT NULL DEFAULT 3,
    unit                    VARCHAR(50)     NOT NULL DEFAULT 'pieces',
    sku                     VARCHAR(100),
    active                  TINYINT(1)      NOT NULL DEFAULT 1,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_vp_profile
        FOREIGN KEY (vendor_profile_id) REFERENCES business_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_vp_vendor_profile ON vendor_products (vendor_profile_id);
CREATE INDEX idx_vp_active         ON vendor_products (vendor_profile_id, active);
CREATE INDEX idx_vp_category       ON vendor_products (category);
