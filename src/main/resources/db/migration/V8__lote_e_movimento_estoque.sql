-- Identidade do lote e diário append-only. O saldo não fica em coluna.
-- UC-LOT-01 grava só RECEBIMENTO. Os outros tipos entram nos casos seguintes.

CREATE TABLE lote_epi (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    unit_id INTEGER NOT NULL,
    epi_id INTEGER NOT NULL,
    epi_ca_binding_id INTEGER NOT NULL,
    lot_code TEXT NOT NULL,
    manufacturer TEXT,
    size_label TEXT NOT NULL DEFAULT '',
    piece_valid_until TEXT NOT NULL,
    ca_checked_at TEXT NOT NULL,
    unit_cost_cents INTEGER,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES unit (id),
    FOREIGN KEY (epi_id) REFERENCES epi_catalog (id),
    FOREIGN KEY (epi_ca_binding_id) REFERENCES epi_ca_binding (id),
    UNIQUE (unit_id, epi_id, lot_code, size_label),
    CHECK (unit_cost_cents IS NULL OR unit_cost_cents >= 0)
);

CREATE TABLE estoque_movimento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lote_id INTEGER NOT NULL,
    movement_type TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lote_epi (id),
    CHECK (quantity > 0),
    CHECK (
        movement_type IN (
            'RECEBIMENTO',
            'RESERVA',
            'LIBERACAO_RESERVA',
            'BAIXA_FORNECIMENTO',
            'ESTORNO_FORNECIMENTO',
            'BAIXA_PRATELEIRA',
            'AJUSTE_INVENTARIO'
        )
    )
);

CREATE INDEX idx_lote_epi_unit ON lote_epi (unit_id);
CREATE INDEX idx_estoque_movimento_lote ON estoque_movimento (lote_id);

CREATE TRIGGER lote_epi_no_update
BEFORE UPDATE ON lote_epi
BEGIN
    SELECT RAISE(ABORT, 'lote_epi nao se edita');
END;

CREATE TRIGGER lote_epi_no_delete
BEFORE DELETE ON lote_epi
BEGIN
    SELECT RAISE(ABORT, 'lote_epi nao se apaga');
END;

CREATE TRIGGER estoque_movimento_no_update
BEFORE UPDATE ON estoque_movimento
BEGIN
    SELECT RAISE(ABORT, 'estoque_movimento e append-only');
END;

CREATE TRIGGER estoque_movimento_no_delete
BEFORE DELETE ON estoque_movimento
BEGIN
    SELECT RAISE(ABORT, 'estoque_movimento e append-only');
END;
