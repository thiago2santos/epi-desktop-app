-- Ficha imutavel do UC-ENT-01. O saldo continua no diario; a baixa copia o organograma da hora.

CREATE TABLE fornecimento_ficha (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    operator_user_id INTEGER NOT NULL,
    employee_code TEXT NOT NULL,
    employee_name TEXT NOT NULL,
    unit_name TEXT NOT NULL,
    department_name TEXT NOT NULL,
    job_role_name TEXT NOT NULL,
    confirmed_at TEXT NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee (id)
);

CREATE INDEX idx_fornecimento_ficha_employee ON fornecimento_ficha (employee_id);

CREATE TRIGGER fornecimento_ficha_no_update
BEFORE UPDATE ON fornecimento_ficha
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_ficha nao se edita');
END;

CREATE TRIGGER fornecimento_ficha_no_delete
BEFORE DELETE ON fornecimento_ficha
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_ficha nao se apaga');
END;

CREATE TABLE fornecimento_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ficha_id INTEGER NOT NULL,
    lote_id INTEGER NOT NULL,
    epi_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    motivo TEXT NOT NULL CHECK (
        motivo IN (
            'PRIMEIRA_ENTREGA',
            'TROCA_PERIODICA',
            'DANO',
            'EXTRAVIO',
            'MUDANCA_FUNCAO',
            'OUTRO'
        )
    ),
    motivo_texto TEXT,
    ciencia INTEGER NOT NULL CHECK (ciencia IN (0, 1)),
    data_treinamento TEXT,
    excecao_texto TEXT,
    FOREIGN KEY (ficha_id) REFERENCES fornecimento_ficha (id),
    FOREIGN KEY (lote_id) REFERENCES lote_epi (id),
    UNIQUE (ficha_id, lote_id)
);

CREATE INDEX idx_fornecimento_item_epi ON fornecimento_item (epi_id);

CREATE TRIGGER fornecimento_item_no_update
BEFORE UPDATE ON fornecimento_item
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_item nao se edita');
END;

CREATE TRIGGER fornecimento_item_no_delete
BEFORE DELETE ON fornecimento_item
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_item nao se apaga');
END;

CREATE TABLE fornecimento_item_ca (
    item_id INTEGER NOT NULL,
    ca_number TEXT NOT NULL,
    PRIMARY KEY (item_id, ca_number),
    FOREIGN KEY (item_id) REFERENCES fornecimento_item (id)
);

CREATE TRIGGER fornecimento_item_ca_no_update
BEFORE UPDATE ON fornecimento_item_ca
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_item_ca nao se edita');
END;

CREATE TRIGGER fornecimento_item_ca_no_delete
BEFORE DELETE ON fornecimento_item_ca
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_item_ca nao se apaga');
END;

CREATE TABLE fornecimento_termo (
    ficha_id INTEGER PRIMARY KEY,
    versao TEXT NOT NULL,
    metodo TEXT NOT NULL,
    accepted_at TEXT NOT NULL,
    operator_user_id INTEGER NOT NULL,
    FOREIGN KEY (ficha_id) REFERENCES fornecimento_ficha (id)
);

CREATE TRIGGER fornecimento_termo_no_update
BEFORE UPDATE ON fornecimento_termo
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_termo nao se edita');
END;

CREATE TRIGGER fornecimento_termo_no_delete
BEFORE DELETE ON fornecimento_termo
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_termo nao se apaga');
END;

ALTER TABLE estoque_movimento ADD COLUMN unit_cost_cents INTEGER;
ALTER TABLE estoque_movimento ADD COLUMN unit_name TEXT;
ALTER TABLE estoque_movimento ADD COLUMN department_name TEXT;
ALTER TABLE estoque_movimento ADD COLUMN job_role_name TEXT;
ALTER TABLE estoque_movimento ADD COLUMN epi_id INTEGER;
