-- Prazo de troca por EPI. Vale para toda funcao cuja matriz ativa exige esse EPI.
CREATE TABLE periodicidade_epi (
    epi_id INTEGER PRIMARY KEY,
    dias INTEGER NOT NULL CHECK (dias > 0),
    aviso_dias INTEGER NOT NULL CHECK (aviso_dias >= 0 AND aviso_dias < dias),
    FOREIGN KEY (epi_id) REFERENCES epi_catalog (id)
);
