CREATE TABLE IF NOT EXISTS papel (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL UNIQUE,
    descricao TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS usuario_papel (
    usuario_id INTEGER NOT NULL,
    papel_id INTEGER NOT NULL,
    PRIMARY KEY (usuario_id, papel_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (papel_id) REFERENCES papel (id)
);

INSERT OR IGNORE INTO papel (codigo, descricao) VALUES ('ADMIN', 'Administrador');
INSERT OR IGNORE INTO papel (codigo, descricao) VALUES ('SESMT', 'Seguranca do trabalho');
INSERT OR IGNORE INTO papel (codigo, descricao) VALUES ('ALMOXARIFE', 'Operador de almoxarifado');
INSERT OR IGNORE INTO papel (codigo, descricao) VALUES ('CONSULTA', 'Consulta de dados e relatorios');

CREATE TRIGGER IF NOT EXISTS auditoria_no_update
BEFORE UPDATE ON auditoria
BEGIN
    SELECT RAISE(ABORT, 'auditoria append-only: update nao permitido');
END;

CREATE TRIGGER IF NOT EXISTS auditoria_no_delete
BEFORE DELETE ON auditoria
BEGIN
    SELECT RAISE(ABORT, 'auditoria append-only: delete nao permitido');
END;
