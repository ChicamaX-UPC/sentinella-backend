CREATE SCHEMA IF NOT EXISTS simulations;

CREATE TABLE IF NOT EXISTS simulations.simulation_scenarios (
    id               UUID PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    description      TEXT,
    simulation_type  VARCHAR(50) NOT NULL,
    parameters       JSONB NOT NULL,
    tailing_dam_id   UUID NOT NULL,
    created_by       UUID NOT NULL,
    is_public        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_simulation_scenarios_created_by
        FOREIGN KEY (created_by) REFERENCES iam.users (id)
);

CREATE INDEX IF NOT EXISTS idx_simulation_scenarios_dam_public
    ON simulations.simulation_scenarios (tailing_dam_id, is_public);

CREATE INDEX IF NOT EXISTS idx_simulation_scenarios_created_by
    ON simulations.simulation_scenarios (created_by);
