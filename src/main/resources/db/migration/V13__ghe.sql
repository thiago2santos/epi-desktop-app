-- GHE: grupo de exposicao da unidade. A funcao entra no grupo; o trabalhador nao ganha coluna.
CREATE TABLE ghe (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    unit_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (unit_id) REFERENCES unit (id)
);

CREATE UNIQUE INDEX uq_ghe_nome_unidade ON ghe (unit_id, UPPER(name));
CREATE INDEX idx_ghe_unit ON ghe (unit_id);

CREATE TABLE ghe_job_role (
    ghe_id INTEGER NOT NULL,
    job_role_id INTEGER NOT NULL,
    PRIMARY KEY (job_role_id),
    FOREIGN KEY (ghe_id) REFERENCES ghe (id),
    FOREIGN KEY (job_role_id) REFERENCES job_role (id)
);

CREATE INDEX idx_ghe_job_role_ghe ON ghe_job_role (ghe_id);
