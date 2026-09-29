ALTER TABLE usuario ADD COLUMN credencial_troca_obrigatoria INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN tentativas_invalidas INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN bloqueado_ate TEXT;
ALTER TABLE usuario ADD COLUMN credencial_atualizada_em TEXT;
