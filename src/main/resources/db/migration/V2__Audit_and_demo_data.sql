-- Flyway migration: demo notes and audit log indices (idempotent)
INSERT INTO
  notes (title, content, user_id, created_at, updated_at)
SELECT
  'Como utilizar o sistema',
  '1. Faça login com sua conta. 2. Acesse notas e tarefas pelo menu superior.',
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
      notes
    WHERE
      title = 'Como utilizar o sistema'
      AND user_id = u.id
  );

INSERT INTO
  notes (title, content, user_id, created_at, updated_at)
SELECT
  'Política de auditoria',
  'Todas as ações são registradas automaticamente para fins de conformidade.',
  u.id,
  NOW(),
  NOW()
FROM
  users u
WHERE
  u.email = 'admin@example.com'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      notes
    WHERE
      title = 'Política de auditoria'
      AND user_id = u.id
  );

CREATE INDEX IF NOT EXISTS idx_audit_logs_occurred_at ON audit_logs (occurred_at DESC);