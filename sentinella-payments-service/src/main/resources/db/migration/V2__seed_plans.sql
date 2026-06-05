INSERT INTO payments.plans (id, code, name, price_cents, currency, sensor_limit, billing_period, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111101', 'STARTER', 'Starter', 9900, 'USD', 10, 'MONTHLY', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111102', 'PRO', 'Professional', 29900, 'USD', 50, 'MONTHLY', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;
