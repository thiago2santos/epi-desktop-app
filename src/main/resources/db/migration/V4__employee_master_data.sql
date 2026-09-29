CREATE TABLE IF NOT EXISTS department (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    department_id INTEGER NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (name, department_id),
    FOREIGN KEY (department_id) REFERENCES department (id)
);

CREATE TABLE IF NOT EXISTS employee (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_code TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    department_id INTEGER NOT NULL,
    job_role_id INTEGER NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department (id),
    FOREIGN KEY (job_role_id) REFERENCES job_role (id)
);

CREATE INDEX IF NOT EXISTS idx_employee_code ON employee (employee_code);
CREATE INDEX IF NOT EXISTS idx_employee_name ON employee (full_name);
CREATE INDEX IF NOT EXISTS idx_employee_active ON employee (active);

INSERT OR IGNORE INTO department (name, active) VALUES ('Operacao', 1);
INSERT OR IGNORE INTO department (name, active) VALUES ('SESMT', 1);

INSERT OR IGNORE INTO job_role (name, department_id, active)
SELECT 'Almoxarife', d.id, 1
FROM department d
WHERE d.name = 'Operacao';

INSERT OR IGNORE INTO job_role (name, department_id, active)
SELECT 'Tecnico de Seguranca', d.id, 1
FROM department d
WHERE d.name = 'SESMT';
