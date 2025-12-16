-- Flyway migration to ensure MyEntity sequence exists
CREATE SEQUENCE IF NOT EXISTS myentity_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE
  myentity
ALTER COLUMN
  id
SET
  DEFAULT nextval('myentity_seq');

ALTER SEQUENCE myentity_seq OWNED BY myentity.id;