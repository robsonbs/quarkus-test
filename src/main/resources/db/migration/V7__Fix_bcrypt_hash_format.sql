-- Corrige as senhas demo para usar formato $2a$ compatível com Quarkus Elytron
-- Senha: 123 -> Hash gerado com BcryptUtil
UPDATE
  users
SET
  password = '$2a$10$hibzY57sqKAYX6Qqo8SPXetkj/.c.eyRUw99WwW8zGZOYQAIOxbB6'
WHERE
  email IN ('admin@example.com', 'user@example.com');
  