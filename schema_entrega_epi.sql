PRAGMA foreign_keys = ON;

CREATE TABLE empresa (
  id INTEGER PRIMARY KEY,
  razao_social TEXT NOT NULL,
  cnpj TEXT NOT NULL UNIQUE
);

CREATE TABLE funcao (
  id INTEGER PRIMARY KEY,
  nome TEXT NOT NULL,
  setor TEXT NOT NULL
);

CREATE TABLE trabalhador (
  id INTEGER PRIMARY KEY,
  empresa_id INTEGER NOT NULL REFERENCES empresa(id),
  nome_completo TEXT NOT NULL,
  matricula TEXT NOT NULL UNIQUE,
  cpf TEXT,
  funcao_id INTEGER NOT NULL REFERENCES funcao(id),
  ativo INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0, 1))
);

CREATE TABLE epi (
  id INTEGER PRIMARY KEY,
  descricao TEXT NOT NULL,
  tipo_anexo_nr6 TEXT,
  exige_treinamento INTEGER NOT NULL DEFAULT 0 CHECK (exige_treinamento IN (0, 1))
);

CREATE TABLE epi_ca (
  id INTEGER PRIMARY KEY,
  epi_id INTEGER NOT NULL REFERENCES epi(id),
  numero_ca TEXT NOT NULL,
  fabricante TEXT NOT NULL,
  data_validade_ca TEXT,
  ativo INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0, 1)),
  UNIQUE (epi_id, numero_ca)
);

CREATE TABLE lote_epi (
  id INTEGER PRIMARY KEY,
  epi_id INTEGER NOT NULL REFERENCES epi(id),
  codigo_lote TEXT NOT NULL,
  fabricante TEXT NOT NULL,
  data_fabricacao TEXT,
  data_validade_peca TEXT NOT NULL,
  tamanho TEXT,
  quantidade_recebida NUMERIC NOT NULL CHECK (quantidade_recebida > 0),
  saldo_atual NUMERIC NOT NULL CHECK (saldo_atual >= 0),
  custo_unitario NUMERIC,
  data_recebimento TEXT NOT NULL,
  UNIQUE (epi_id, codigo_lote)
);

CREATE TABLE matriz_funcao_epi (
  id INTEGER PRIMARY KEY,
  funcao_id INTEGER NOT NULL REFERENCES funcao(id),
  epi_id INTEGER NOT NULL REFERENCES epi(id),
  periodicidade_dias INTEGER,
  obrigatorio INTEGER NOT NULL DEFAULT 1 CHECK (obrigatorio IN (0, 1)),
  modo_fornecimento TEXT NOT NULL CHECK (modo_fornecimento IN ('INDIVIDUAL', 'POSTO_DISPONIBILIZACAO')),
  ativo INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0, 1)),
  UNIQUE (funcao_id, epi_id)
);

CREATE TABLE entrega_epi (
  id INTEGER PRIMARY KEY,
  trabalhador_id INTEGER NOT NULL REFERENCES trabalhador(id),
  funcao_id INTEGER NOT NULL REFERENCES funcao(id),
  data_entrega TEXT NOT NULL,
  usuario_operador TEXT NOT NULL,
  canal_registro TEXT NOT NULL CHECK (canal_registro IN ('FISICO_DIGITALIZADO', 'SISTEMA_ELETRONICO', 'BIOMETRIA')),
  observacao TEXT,
  criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE entrega_epi_item (
  id INTEGER PRIMARY KEY,
  entrega_id INTEGER NOT NULL REFERENCES entrega_epi(id),
  epi_id INTEGER NOT NULL REFERENCES epi(id),
  lote_id INTEGER NOT NULL REFERENCES lote_epi(id),
  quantidade NUMERIC NOT NULL CHECK (quantidade > 0),
  motivo_entrega TEXT NOT NULL CHECK (
    motivo_entrega IN (
      'PRIMEIRA_ENTREGA',
      'SUBSTITUICAO_DESGASTE',
      'SUBSTITUICAO_DANO',
      'SUBSTITUICAO_EXTRAVIO',
      'MUDANCA_FUNCAO',
      'OUTRO'
    )
  ),
  entrega_fora_matriz INTEGER NOT NULL DEFAULT 0 CHECK (entrega_fora_matriz IN (0, 1)),
  justificativa_excecao TEXT,
  metodo_validacao_trabalhador TEXT NOT NULL CHECK (
    metodo_validacao_trabalhador IN ('ASSINATURA_MANUAL', 'ASSINATURA_DIGITAL', 'BIOMETRIA')
  ),
  validacao_referencia TEXT NOT NULL,
  criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE entrega_item_ca (
  id INTEGER PRIMARY KEY,
  entrega_item_id INTEGER NOT NULL REFERENCES entrega_epi_item(id),
  numero_ca TEXT NOT NULL,
  UNIQUE (entrega_item_id, numero_ca)
);

CREATE TABLE devolucao_epi_item (
  id INTEGER PRIMARY KEY,
  entrega_item_id INTEGER NOT NULL UNIQUE REFERENCES entrega_epi_item(id),
  data_devolucao TEXT NOT NULL,
  motivo_devolucao TEXT NOT NULL CHECK (
    motivo_devolucao IN ('DESGASTE', 'DANO', 'EXTRAVIO', 'DESLIGAMENTO', 'OUTRO')
  ),
  usuario_responsavel TEXT NOT NULL,
  observacao TEXT,
  criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE termo_responsabilidade_aceite (
  id INTEGER PRIMARY KEY,
  entrega_id INTEGER NOT NULL UNIQUE REFERENCES entrega_epi(id),
  texto_termo TEXT NOT NULL,
  aceito_em TEXT NOT NULL,
  metodo_aceite TEXT NOT NULL CHECK (
    metodo_aceite IN ('ASSINATURA_MANUAL', 'ASSINATURA_DIGITAL', 'BIOMETRIA')
  ),
  evidencia_aceite TEXT NOT NULL
);

CREATE TABLE entrega_estorno (
  id INTEGER PRIMARY KEY,
  entrega_item_id INTEGER NOT NULL UNIQUE REFERENCES entrega_epi_item(id),
  data_estorno TEXT NOT NULL,
  motivo_estorno TEXT NOT NULL,
  usuario_responsavel TEXT NOT NULL,
  criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auditoria (
  id INTEGER PRIMARY KEY,
  instante TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario TEXT NOT NULL,
  acao TEXT NOT NULL,
  entidade TEXT NOT NULL,
  entidade_id INTEGER NOT NULL,
  detalhes_json TEXT
);

CREATE INDEX idx_trabalhador_funcao ON trabalhador(funcao_id);
CREATE INDEX idx_matriz_funcao_ativo ON matriz_funcao_epi(funcao_id, ativo);
CREATE INDEX idx_entrega_trabalhador_data ON entrega_epi(trabalhador_id, data_entrega);
CREATE INDEX idx_entrega_item_entrega ON entrega_epi_item(entrega_id);
CREATE INDEX idx_entrega_item_epi ON entrega_epi_item(epi_id);
CREATE INDEX idx_lote_validade ON lote_epi(data_validade_peca);

CREATE TRIGGER trg_entrega_item_excecao
BEFORE INSERT ON entrega_epi_item
FOR EACH ROW
WHEN NEW.entrega_fora_matriz = 1 AND (NEW.justificativa_excecao IS NULL OR trim(NEW.justificativa_excecao) = '')
BEGIN
  SELECT RAISE(ABORT, 'Entrega fora da matriz exige justificativa de excecao');
END;

CREATE TRIGGER trg_devolucao_data_consistente
BEFORE INSERT ON devolucao_epi_item
FOR EACH ROW
WHEN (
  SELECT date(NEW.data_devolucao) < date(e.data_entrega)
  FROM entrega_epi_item i
  JOIN entrega_epi e ON e.id = i.entrega_id
  WHERE i.id = NEW.entrega_item_id
)
BEGIN
  SELECT RAISE(ABORT, 'Data de devolucao nao pode ser anterior a entrega');
END;

CREATE TRIGGER trg_entrega_item_lote_valido
BEFORE INSERT ON entrega_epi_item
FOR EACH ROW
WHEN (
  SELECT date(l.data_validade_peca) < date(e.data_entrega)
  FROM lote_epi l
  JOIN entrega_epi e ON e.id = NEW.entrega_id
  WHERE l.id = NEW.lote_id
)
BEGIN
  SELECT RAISE(ABORT, 'Nao e permitido entregar EPI com lote vencido na data da entrega');
END;

CREATE TRIGGER trg_entrega_item_sem_update
BEFORE UPDATE ON entrega_epi_item
FOR EACH ROW
BEGIN
  SELECT RAISE(ABORT, 'Entrega de EPI e imutavel: use estorno');
END;

CREATE TRIGGER trg_entrega_item_sem_delete
BEFORE DELETE ON entrega_epi_item
FOR EACH ROW
BEGIN
  SELECT RAISE(ABORT, 'Entrega de EPI e imutavel: use estorno');
END;

CREATE TRIGGER trg_devolucao_sem_update
BEFORE UPDATE ON devolucao_epi_item
FOR EACH ROW
BEGIN
  SELECT RAISE(ABORT, 'Devolucao de EPI e imutavel: registre novo evento');
END;

CREATE TRIGGER trg_devolucao_sem_delete
BEFORE DELETE ON devolucao_epi_item
FOR EACH ROW
BEGIN
  SELECT RAISE(ABORT, 'Devolucao de EPI e imutavel: registre novo evento');
END;
