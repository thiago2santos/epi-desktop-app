-- Devolucao do UC-POS-01. Nao entra no diario: a peca nao volta para a prateleira.

CREATE TABLE fornecimento_devolucao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL UNIQUE,
    motivo TEXT NOT NULL CHECK (
        motivo IN ('DESGASTE', 'DANO', 'DESCARTE', 'DESLIGAMENTO', 'EXTRAVIO', 'OUTRO')
    ),
    motivo_texto TEXT,
    devolvido_em TEXT NOT NULL,
    operator_user_id INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (item_id) REFERENCES fornecimento_item (id)
);

CREATE TRIGGER fornecimento_devolucao_no_update
BEFORE UPDATE ON fornecimento_devolucao
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_devolucao nao se edita');
END;

CREATE TRIGGER fornecimento_devolucao_no_delete
BEFORE DELETE ON fornecimento_devolucao
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_devolucao nao se apaga');
END;
