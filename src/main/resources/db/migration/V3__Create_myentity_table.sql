-- Flyway migration to satisfy legacy Quarkus sample entity
CREATE SEQUENCE IF NOT EXISTS myentity_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS myentity (
  id BIGINT PRIMARY KEY DEFAULT nextval('myentity_seq'),
  field VARCHAR(255)
);

ALTER SEQUENCE myentity_seq OWNED BY myentity.id;