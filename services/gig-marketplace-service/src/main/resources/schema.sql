CREATE SCHEMA IF NOT EXISTS marketplace_schema;
CREATE TABLE IF NOT EXISTS marketplace_schema.service_categories (id UUID PRIMARY KEY, name VARCHAR(120) UNIQUE NOT NULL);
CREATE TABLE IF NOT EXISTS marketplace_schema.service_offerings (
  id UUID PRIMARY KEY,
  freelancer_id UUID NOT NULL,
  freelancer_display_name VARCHAR(160) NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  base_price NUMERIC(12,2) NOT NULL,
  currency VARCHAR(10) NOT NULL,
  delivery_days INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  category_id UUID NOT NULL REFERENCES marketplace_schema.service_categories(id),
  tags TEXT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS marketplace_schema.service_media (
  id UUID PRIMARY KEY,
  service_id UUID NOT NULL,
  public_url TEXT NOT NULL,
  media_type VARCHAR(20) NOT NULL,
  is_primary BOOLEAN NOT NULL,
  bucket VARCHAR(100) NOT NULL,
  path TEXT NOT NULL,
  content_type VARCHAR(120) NOT NULL,
  size_bytes BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS marketplace_schema.freelancer_reputation (
  freelancer_id UUID PRIMARY KEY,
  display_name VARCHAR(160) NOT NULL,
  average_rating DOUBLE PRECISION NOT NULL,
  reviews_count INT NOT NULL
);
