-- As linhas antigas nasceram em UTC. A partir daqui o instante é a hora civil da máquina.

DROP TRIGGER IF EXISTS auditoria_no_update;
DROP TRIGGER IF EXISTS auditoria_no_delete;

CREATE TABLE auditoria_nova (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    instante TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    usuario_id INTEGER,
    acao TEXT NOT NULL,
    entidade TEXT NOT NULL,
    entidade_id TEXT NOT NULL,
    detalhes TEXT,
    resultado TEXT NOT NULL DEFAULT 'SUCESSO',
    codigo TEXT,
    correlacao TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

INSERT INTO auditoria_nova (
    id, instante, usuario_id, acao, entidade, entidade_id, detalhes, resultado, codigo, correlacao
)
SELECT
    id,
    strftime('%Y-%m-%d %H:%M:%S', instante, 'localtime'),
    usuario_id,
    acao,
    entidade,
    entidade_id,
    detalhes,
    resultado,
    codigo,
    correlacao
FROM auditoria;

DROP TABLE auditoria;

ALTER TABLE auditoria_nova RENAME TO auditoria;

UPDATE sqlite_sequence
SET seq = (SELECT MAX(id) FROM auditoria)
WHERE name = 'auditoria_nova'
  AND EXISTS (SELECT 1 FROM auditoria);

UPDATE sqlite_sequence
SET name = 'auditoria'
WHERE name = 'auditoria_nova';

CREATE TRIGGER auditoria_no_update
BEFORE UPDATE ON auditoria
BEGIN
    SELECT RAISE(ABORT, 'auditoria append-only: update nao permitido');
END;

CREATE TRIGGER auditoria_no_delete
BEFORE DELETE ON auditoria
BEGIN
    SELECT RAISE(ABORT, 'auditoria append-only: delete nao permitido');
END;
