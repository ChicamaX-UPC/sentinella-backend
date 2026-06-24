ALTER TABLE payments.plans
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS stripe_product_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stripe_price_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stripe_setup_price_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS setup_price_cents BIGINT NOT NULL DEFAULT 0;

UPDATE payments.plans SET active = false WHERE code IN ('STARTER', 'PRO');

INSERT INTO payments.plans (
    id, code, name, price_cents, setup_price_cents, currency, sensor_limit, billing_period, active, created_at, updated_at
)
VALUES
    ('11111111-1111-1111-1111-111111111201', 'ECONOMY', 'Economy', 9000, 10000, 'USD', 5, 'MONTHLY', true, NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111202', 'PREMIUM', 'Premium', 14000, 24000, 'USD', 12, 'MONTHLY', true, NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111203', 'MAX', 'Max', 22000, 40000, 'USD', 20, 'MONTHLY', true, NOW(), NOW())
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    price_cents = EXCLUDED.price_cents,
    setup_price_cents = EXCLUDED.setup_price_cents,
    sensor_limit = EXCLUDED.sensor_limit,
    billing_period = EXCLUDED.billing_period,
    active = EXCLUDED.active,
    updated_at = NOW();

CREATE TABLE IF NOT EXISTS payments.billing_customers (
    user_id UUID PRIMARY KEY,
    stripe_customer_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
