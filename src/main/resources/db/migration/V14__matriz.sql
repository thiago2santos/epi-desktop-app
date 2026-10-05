-- Matriz do perfil: funcao fora de GHE ativo, ou o proprio GHE. Linha inativa nao bloqueia outra.
CREATE TABLE matriz_linha (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    perfil_tipo TEXT NOT NULL CHECK (perfil_tipo IN ('FUNCAO', 'GHE')),
    perfil_id INTEGER NOT NULL,
    epi_id INTEGER NOT NULL,
    ca_binding_id INTEGER NOT NULL,
    modo TEXT NOT NULL CHECK (modo IN ('INDIVIDUAL', 'POSTO')),
    exige_treinamento INTEGER NOT NULL DEFAULT 0 CHECK (exige_treinamento IN (0, 1)),
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    FOREIGN KEY (epi_id) REFERENCES epi_catalog (id),
    FOREIGN KEY (ca_binding_id) REFERENCES epi_ca_binding (id)
);

CREATE UNIQUE INDEX uq_matriz_perfil_epi_ativa
    ON matriz_linha (perfil_tipo, perfil_id, epi_id)
    WHERE active = 1;

CREATE INDEX idx_matriz_perfil ON matriz_linha (perfil_tipo, perfil_id);
