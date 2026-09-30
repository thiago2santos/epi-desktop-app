CREATE TABLE IF NOT EXISTS epi_catalog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    epi_code TEXT,
    description TEXT NOT NULL,
    annex_group TEXT NOT NULL,
    manufacturer_name TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (annex_group IN ('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'))
);

CREATE TABLE IF NOT EXISTS epi_ca_binding (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    epi_id INTEGER NOT NULL,
    ca_number TEXT NOT NULL,
    ca_status TEXT NOT NULL,
    valid_from TEXT,
    valid_until TEXT,
    official_check_at TEXT NOT NULL,
    official_check_note TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (epi_id) REFERENCES epi_catalog (id),
    CHECK (ca_status IN ('ACTIVE', 'SUSPENDED', 'CANCELED', 'EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_epi_catalog_description ON epi_catalog (description);
CREATE INDEX IF NOT EXISTS idx_epi_catalog_annex_group ON epi_catalog (annex_group);
CREATE INDEX IF NOT EXISTS idx_epi_catalog_active ON epi_catalog (active);
CREATE INDEX IF NOT EXISTS idx_epi_ca_binding_epi_id ON epi_ca_binding (epi_id);
CREATE INDEX IF NOT EXISTS idx_epi_ca_binding_ca_number ON epi_ca_binding (ca_number);
CREATE INDEX IF NOT EXISTS idx_epi_ca_binding_active ON epi_ca_binding (active);
