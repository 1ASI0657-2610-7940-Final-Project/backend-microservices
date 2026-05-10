CREATE SCHEMA IF NOT EXISTS engagement_schema;
CREATE TABLE IF NOT EXISTS engagement_schema.project_requests (
 id UUID PRIMARY KEY,
 service_id UUID NOT NULL,
 client_id UUID NOT NULL,
 freelancer_id UUID NOT NULL,
 message TEXT NOT NULL,
 proposed_price NUMERIC(12,2) NOT NULL,
 currency VARCHAR(10) NOT NULL,
 proposed_delivery_days INT NOT NULL,
 status VARCHAR(20) NOT NULL,
 created_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS engagement_schema.agreements (
 id UUID PRIMARY KEY,
 request_id UUID NOT NULL,
 final_price NUMERIC(12,2) NOT NULL,
 currency VARCHAR(10) NOT NULL,
 final_delivery_days INT NOT NULL,
 response_message TEXT,
 created_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS engagement_schema.projects (
 id UUID PRIMARY KEY,
 request_id UUID NOT NULL,
 agreement_id UUID NOT NULL,
 service_id UUID NOT NULL,
 client_id UUID NOT NULL,
 freelancer_id UUID NOT NULL,
 status VARCHAR(20) NOT NULL,
 final_price NUMERIC(12,2) NOT NULL,
 currency VARCHAR(10) NOT NULL,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS engagement_schema.project_status_history (
 id UUID PRIMARY KEY,
 project_id UUID NOT NULL,
 status VARCHAR(20) NOT NULL,
 comment TEXT,
 changed_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS engagement_schema.reviews (
 id UUID PRIMARY KEY,
 project_id UUID NOT NULL,
 reviewer_id UUID NOT NULL,
 reviewee_id UUID NOT NULL,
 rating INT NOT NULL,
 comment TEXT,
 created_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS engagement_schema.price_suggestions (
 id UUID PRIMARY KEY,
 service_type VARCHAR(120),
 complexity VARCHAR(50),
 urgency VARCHAR(50),
 freelancer_experience VARCHAR(50),
 suggested_min_price NUMERIC(12,2),
 suggested_max_price NUMERIC(12,2),
 currency VARCHAR(10),
 created_at TIMESTAMP
);
