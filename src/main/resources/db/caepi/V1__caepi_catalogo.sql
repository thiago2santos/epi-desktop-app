-- Catálogo CAEPI isolado do banco operacional.
-- A publicação longa não participa da transação de auditoria.

CREATE TABLE caepi_carga (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    finished_at TEXT NOT NULL,
    mode TEXT NOT NULL,
    result TEXT NOT NULL,
    actor_user_id INTEGER,
    source_name TEXT NOT NULL,
    byte_size INTEGER NOT NULL,
    sha256 TEXT NOT NULL,
    record_count INTEGER NOT NULL DEFAULT 0,
    failure_reason TEXT,
    CHECK (result IN ('SUCESSO', 'FALHA')),
    CHECK (mode IN ('MANUAL', 'AUTOMATICA'))
);

CREATE TABLE caepi_ca (
    ca_number TEXT PRIMARY KEY,
    ca_status TEXT NOT NULL,
    valid_until TEXT,
    equipment TEXT,
    manufacturer TEXT,
    carga_id INTEGER NOT NULL,
    FOREIGN KEY (carga_id) REFERENCES caepi_carga (id)
);

CREATE TABLE caepi_variante (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ca_number TEXT NOT NULL,
    ca_status TEXT NOT NULL,
    valid_until TEXT,
    equipment TEXT,
    manufacturer TEXT NOT NULL DEFAULT '',
    manufacturer_norm TEXT NOT NULL DEFAULT '',
    carga_id INTEGER NOT NULL,
    FOREIGN KEY (carga_id) REFERENCES caepi_carga (id)
);

CREATE INDEX idx_caepi_variante_busca ON caepi_variante (carga_id, manufacturer_norm);
