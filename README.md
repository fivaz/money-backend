indexes added to DB:


CREATE EXTENSION IF NOT EXISTS pg_trgm;


CREATE INDEX trgm_description_idx ON transaction USING gin (description gin_trgm_ops);


CREATE INDEX trgm_budget_name_idx ON budget USING gin (name gin_trgm_ops);