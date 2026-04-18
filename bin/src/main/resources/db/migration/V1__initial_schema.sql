-- V1__initial_schema.sql
-- Gas Agency DSC Collector — Multi-Tenant Schema

-- Agencies (tenants)
CREATE TABLE agencies (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    owner_name          VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    phone               VARCHAR(15),
    city                VARCHAR(100),
    address             TEXT,
    role                VARCHAR(20) NOT NULL DEFAULT 'AGENCY',
    plan_type           VARCHAR(20) NOT NULL DEFAULT 'FREE',
    elevenlabs_agent_id VARCHAR(100),
    twilio_phone_number VARCHAR(20),
    transfer_number     VARCHAR(20),
    agent_name          VARCHAR(100) DEFAULT 'Raju',
    monthly_call_limit  INT DEFAULT 100,
    calls_used_this_month INT DEFAULT 0,
    setup_completed     BOOLEAN DEFAULT FALSE,
    active              BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agencies_email ON agencies(email);

-- Customers (per agency)
CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    agency_id       BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(15) NOT NULL,
    address         TEXT,
    consumer_number VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_agency ON customers(agency_id);
CREATE INDEX idx_customers_phone ON customers(phone);

-- Campaigns (per agency)
CREATE TABLE campaigns (
    id              BIGSERIAL PRIMARY KEY,
    agency_id       BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    delivery_date   DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_customers INT DEFAULT 0,
    completed_calls INT DEFAULT 0,
    successful_calls INT DEFAULT 0,
    failed_calls    INT DEFAULT 0,
    transferred_calls INT DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP
);

CREATE INDEX idx_campaigns_agency ON campaigns(agency_id);
CREATE INDEX idx_campaigns_status ON campaigns(status);

-- Calls (per agency)
CREATE TABLE calls (
    id                  BIGSERIAL PRIMARY KEY,
    agency_id           BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    campaign_id         BIGINT NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    customer_id         BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    dsc_number          VARCHAR(10),
    elevenlabs_call_id  VARCHAR(100),
    twilio_call_sid     VARCHAR(100),
    duration_seconds    INT,
    attempt_count       INT NOT NULL DEFAULT 0,
    transcript          TEXT,
    notes               TEXT,
    transfer_reason     TEXT,
    cost_credits        DOUBLE PRECISION DEFAULT 0,
    called_at           TIMESTAMP,
    completed_at        TIMESTAMP,
    next_retry_at       TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_calls_agency ON calls(agency_id);
CREATE INDEX idx_calls_campaign ON calls(campaign_id);
CREATE INDEX idx_calls_customer ON calls(customer_id);
CREATE INDEX idx_calls_status ON calls(status);
