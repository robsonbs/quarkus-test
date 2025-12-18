-- Flyway migration: base schema and demo data
CREATE TABLE IF NOT EXISTS user_profiles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  profile_id BIGINT NOT NULL REFERENCES user_profiles(id),
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS notes (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  content TEXT,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS tasks (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  due_date DATE,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE
);

DO $$ BEGIN IF NOT EXISTS (
  SELECT
    1
  FROM
    pg_constraint
  WHERE
    conname = 'tasks_status_check'
) THEN
ALTER TABLE
  tasks
ADD
  CONSTRAINT tasks_status_check CHECK (
    status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')
  );

END IF;

END$$;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  action VARCHAR(255) NOT NULL,
  http_method VARCHAR(20) NOT NULL,
  resource_path VARCHAR(500) NOT NULL,
  client_ip VARCHAR(100),
  status_code INTEGER,
  entity_type VARCHAR(120),
  entity_id VARCHAR(120),
  user_agent VARCHAR(500),
  details TEXT,
  occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notes_user_id ON notes (user_id);

CREATE INDEX IF NOT EXISTS idx_tasks_owner_id ON tasks (owner_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_username ON audit_logs (username);

CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs (entity_type, entity_id);

-- Seed data
INSERT INTO
  user_profiles (name)
VALUES
  ('ADMIN'),
  ('USER') ON CONFLICT (name) DO NOTHING;

INSERT INTO
  users (name, email, password, profile_id, created_at)
SELECT
  'Admin User',
  'admin@example.com',
  '$2a$10$g.L8bcm3UaL5p/CVp/VwfeT0jsoLg2s2dF.iG.PSJPSAp1x/TzM/S',
  p.id,
  NOW()
FROM
  user_profiles p
WHERE
  p.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      users
    WHERE
      email = 'admin@example.com'
  );
ALTER TABLE
  tasks DROP CONSTRAINT IF EXISTS tasks_status_check;

ALTER TABLE
  tasks
ADD
  CONSTRAINT tasks_status_check CHECK (
    status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')
  );

INSERT INTO
  users (name, email, password, profile_id, created_at)
SELECT
  'Regular User',
  'user@example.com',
  '$2a$10$g.L8bcm3UaL5p/CVp/VwfeT0jsoLg2s2dF.iG.PSJPSAp1x/TzM/S',
  p.id,
  NOW()
FROM
  user_profiles p
WHERE
  p.name = 'USER'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      users
    WHERE
      email = 'user@example.com'
  );

INSERT INTO
  tasks (
    title,
    description,
    due_date,
    status,
    owner_id,
    created_at,
    updated_at
  )
SELECT
  'Entregar relatório trimestral',
  'Revisar métricas de uso e anexar gráficos atualizados.',
  CURRENT_DATE + INTERVAL '7 day',
  'IN_PROGRESS',
  u.id,
  NOW(),
  NOW()
FROM
  users u
WHERE
  u.email = 'user@example.com'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      tasks
    WHERE
      title = 'Entregar relatório trimestral'
  );

INSERT INTO
  tasks (
    title,
    description,
    due_date,
    status,
    owner_id,
    created_at,
    updated_at
  )
SELECT
  'Planejar revisão do sistema',
  'Listar melhorias desejadas e priorizar para o próximo sprint.',
  CURRENT_DATE + INTERVAL '14 day',
  'PENDING',
  u.id,
  NOW(),
  NOW()
FROM
  users u
WHERE
  u.email = 'user@example.com'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      tasks
    WHERE
      title = 'Planejar revisão do sistema'
  );