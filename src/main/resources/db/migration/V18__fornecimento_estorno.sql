-- Estorno do UC-POS-02. A ficha permanece. O movimento devolve a fisica, nao a reserva.

CREATE TABLE fornecimento_estorno (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL UNIQUE,
    motivo TEXT NOT NULL,
    operator_user_id INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (item_id) REFERENCES fornecimento_item (id)
);

CREATE TRIGGER fornecimento_estorno_no_update
BEFORE UPDATE ON fornecimento_estorno
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_estorno nao se edita');
END;

CREATE TRIGGER fornecimento_estorno_no_delete
BEFORE DELETE ON fornecimento_estorno
BEGIN
    SELECT RAISE(ABORT, 'fornecimento_estorno nao se apaga');
END;
