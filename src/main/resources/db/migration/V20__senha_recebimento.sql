-- Senha de recebimento do trabalhador. Nao e login.
-- O que se guarda e o hash Argon2. A senha em claro nao entra na ficha.

ALTER TABLE employee ADD COLUMN senha_recebimento_hash TEXT;
