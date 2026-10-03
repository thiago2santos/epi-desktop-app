-- Uma empresa, várias unidades. O setor pertence à unidade.
-- O trabalhador herda a unidade pelo setor. O gestor vigente é outro trabalhador.
-- Troca de gestor fica na auditoria. O snapshot da solicitação entra com UC-SOL-01.

CREATE TABLE company (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    legal_name TEXT NOT NULL,
    cnpj_root TEXT NOT NULL UNIQUE
);

CREATE TABLE unit (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    cnpj TEXT NOT NULL UNIQUE,
    active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (company_id) REFERENCES company (id)
);

INSERT INTO company (legal_name, cnpj_root)
VALUES ('Access Gestão de Documentos Ltda.', '22755266');

INSERT INTO unit (company_id, name, cnpj)
SELECT c.id, v.name, v.cnpj
FROM company c
JOIN (
    SELECT 'São Paulo' AS name, '22755266000187' AS cnpj
    UNION ALL SELECT 'Itupeva', '22755266000268'
    UNION ALL SELECT 'Rio de Janeiro', '22755266000349'
    UNION ALL SELECT 'Curitiba', '22755266000420'
    UNION ALL SELECT 'Lagoa Santa', '22755266000691'
    UNION ALL SELECT 'Belo Horizonte', '22755266000772'
    UNION ALL SELECT 'Belo Horizonte', '22755266000853'
    UNION ALL SELECT 'São José do Rio Preto', '22755266000934'
    UNION ALL SELECT 'São José do Rio Preto', '22755266001078'
) v
WHERE c.cnpj_root = '22755266';

CREATE TABLE department_org (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    unit_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (unit_id, name),
    FOREIGN KEY (unit_id) REFERENCES unit (id)
);

INSERT INTO department_org (id, unit_id, name, active, created_at)
SELECT d.id, u.id, d.name, d.active, d.created_at
FROM department d
JOIN unit u ON u.cnpj = '22755266000268';

DROP TABLE department;

ALTER TABLE department_org RENAME TO department;

UPDATE sqlite_sequence
SET seq = (SELECT MAX(id) FROM department)
WHERE name = 'department_org';

UPDATE sqlite_sequence
SET name = 'department'
WHERE name = 'department_org';

CREATE INDEX idx_department_unit ON department (unit_id);

ALTER TABLE employee ADD COLUMN manager_id INTEGER REFERENCES employee (id);

CREATE INDEX idx_employee_manager ON employee (manager_id);
