CREATE SCHEMA IF NOT EXISTS profiles;

CREATE TABLE IF NOT EXISTS profiles.user_profiles (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    phone VARCHAR(30),
    job_title VARCHAR(100),
    plan_type VARCHAR(50),
    sensor_limit INT,
    subscription_id UUID,
    preferences JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
