-- Multi-Vendor Marketplace Schema Migration
-- This migration adds vendor support to the existing single-tenant marketplace

-- Create vendors table
CREATE TABLE IF NOT EXISTS vendors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    store_name VARCHAR(150) NOT NULL UNIQUE,
    store_slug VARCHAR(150) NOT NULL UNIQUE,
    business_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    commission_rate NUMERIC(5, 2) DEFAULT 10.00 CHECK (commission_rate >= 0 AND commission_rate <= 100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vendors_user_id ON vendors(user_id);
CREATE INDEX IF NOT EXISTS idx_vendors_status ON vendors(status);

-- Extend products table with vendor support (non-breaking - nullable vendor_id)
ALTER TABLE products ADD COLUMN IF NOT EXISTS vendor_id UUID REFERENCES vendors(id) ON DELETE SET NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS approval_status VARCHAR(50) DEFAULT 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_products_vendor_id ON products(vendor_id);

-- Extend order_items table for multi-vendor order splitting
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS vendor_id UUID REFERENCES vendors(id) ON DELETE SET NULL;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS fulfillment_status VARCHAR(50) DEFAULT 'PENDING';
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS vendor_payout_amount NUMERIC(10, 2);

CREATE INDEX IF NOT EXISTS idx_order_items_vendor_id ON order_items(vendor_id);
CREATE INDEX IF NOT EXISTS idx_order_items_fulfillment_status ON order_items(fulfillment_status);
