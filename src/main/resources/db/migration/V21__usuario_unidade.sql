-- Unidade de vínculo do operador. Nula: o perfil corporativo enxerga todas.
-- Preenchida: a sessão só entra nessa planta.

ALTER TABLE usuario ADD COLUMN unidade_id INTEGER REFERENCES unit (id);
