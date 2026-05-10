INSERT INTO marketplace_schema.service_categories (id,name)
VALUES ('11111111-1111-1111-1111-111111111111','Design') ON CONFLICT (name) DO NOTHING;
INSERT INTO marketplace_schema.service_categories (id,name)
VALUES ('22222222-2222-2222-2222-222222222222','Programming') ON CONFLICT (name) DO NOTHING;
INSERT INTO marketplace_schema.service_categories (id,name)
VALUES ('33333333-3333-3333-3333-333333333333','Writing') ON CONFLICT (name) DO NOTHING;
INSERT INTO marketplace_schema.service_categories (id,name)
VALUES ('44444444-4444-4444-4444-444444444444','Marketing') ON CONFLICT (name) DO NOTHING;
INSERT INTO marketplace_schema.service_categories (id,name)
VALUES ('55555555-5555-5555-5555-555555555555','Tutoring') ON CONFLICT (name) DO NOTHING;
