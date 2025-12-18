-- Atualiza as senhas demo para o valor documentado (123) com hash Bcrypt
UPDATE
  users
SET
  password = '$2b$10$A2a/fdsaKR.V1k1pkrvj6OkeYWqYGlNZyJHnLFh09po29RdIHXnQO'
WHERE
  email IN ('admin@example.com', 'user@example.com');