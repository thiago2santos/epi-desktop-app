# Spec Curta — UC-CAD-IMP-01 Importar cadastros via CSV (validacao previa)

## Identificacao

- ID: `UC-CAD-IMP-01`
- Feature de produto associada: `FE-CAD-01`
- Tipo: `UC` (orquestracao) + padrao reutilizavel `FEAT`
- Iniciativa/Epico: `INI-01` / `EP-CAD`
- Responsavel: Time Easy NR6
- Status: **registrado — refinamento pendente (DoR aberto)**

## 1) Contexto

- Problema real:
  - unidades novas precisam carregar **muitos** setores, funcoes e trabalhadores;
  - cadastro manual nao escala na **adocao**;
  - planilhas externas trazem **typos** e referencias a mestres inexistentes.
- Atores:
  - `Admin`, `SESMT` (carga autorizada; RBAC a confirmar).
- Impacto se nao resolver:
  - go-live lento, dados inconsistentes, desistencia de pilotos.

## 2) Escopo

### No escopo (MVP da feature)

- Hub ou entrada unica **“Importar cadastros (CSV)”** no modulo Cadastros.
- Tipos de arquivo suportados na v1 (minimo):
  - `setores.csv`
  - `funcoes.csv`
  - `trabalhadores.csv`
- Delimitador, encoding e cabecalho: **definir na Etapa 2 de refinamento** (proposta inicial: `;`, UTF-8, cabecalho na 1a linha — alinhado ao RelatorioCA quando possivel).
- Fluxo:
  1. Upload do arquivo.
  2. Parse em background (nao bloquear UI — mesmo principio de `UC-CAE-01`).
  3. Validacao de dominio **sem persistir**.
  4. Tela de **revisao** (staging) com tabela, cores e filtros conforme feature de negocio.
  5. Acao **Importar linhas validas** (ou equivalente) com commit **atomico por lote** (decisao §12).
  6. Auditoria terminal por tentativa.

### Fora de escopo (v1)

- Auto-criar setor/funcao **silenciosamente** a partir do CSV de trabalhadores sem passo de confirmacao.
- Importacao de EPI/CA/lotes/matriz.
- Agendamento/recorrencia (carga unica manual/autorizada).

## 3) Tela de revisao (requisitos UX normativos)

1. Exibir **todas as linhas** parseadas com numero de linha no arquivo.
2. Estado visual por linha:
   - **Valida**: fundo verde claro (`--enr6-status-ok-bg` ou token dedicado).
   - **Com pendencia**: fundo amarelo claro (`--enr6-status-warn-bg`).
3. Por celula com pendencia: indicador textual (codigo + mensagem curta) **no proprio campo** ou coluna “Pendencias”.
4. Filtros (minimo):
   - todas as linhas;
   - apenas validas;
   - apenas com pendencia.
5. Contadores: total / validas / com pendencia.
6. **Deep link condicional**:
   - se pendencia = `SETOR_NAO_ENCONTRADO` ou `FUNCAO_NAO_ENCONTRADA` (codigos provisorios), celula acionavel abre `UC-CAD-02` com nome **pre-preenchido**;
   - **somente** quando a resolucao exige acao humana no cadastro mestre;
   - ao voltar, usuario dispara **Revalidar** (linha ou arquivo).

## 4) Regras de negocio (rascunho — confirmar)

1. Validacao usa **mesmas regras** do cadastro unitario (`CAD-02x`, `CAD-03x`) onde aplicavel.
2. Matricula de trabalhador: unicidade global; normalizacao de espacos (trim).
3. Funcao deve pertencer ao setor informado (ou regra de resolucao por nome composto — decisao §12).
4. Linha valida no staging **nao garante** sucesso na publicacao se o estado mudar entre revisao e commit (conflito de concorrencia): tratar como falha de linha com auditoria.
5. Tentativa de importacao registra **exatamente um** evento terminal de auditoria (`SUCESSO`, `SUCESSO_PARCIAL`, `FALHA` — nomenclatura a fechar).
6. Usuario sem permissao: negar antes do parse (`CAD-IMP-006` provisorio).

## 5) Fluxos

### Fluxo principal

1. Operador autorizado seleciona tipo de cadastro e arquivo CSV.
2. Sistema parseia e valida; exibe staging.
3. Operador filtra/revisa; corrige mestres via deep link se necessario; revalida.
4. Operador confirma importacao das linhas validas.
5. Sistema persiste em transacao; audita; exibe resumo (importadas / ignoradas / falhas).

### Fluxos alternativos

- Arquivo ilegivel / cabecalho invalido: `CAD-IMP-001`, sem staging.
- Arquivo vazio: `CAD-IMP-002`.
- Nenhuma linha valida: `CAD-IMP-003`, bloquear commit ou permitir cancelar (decisao §12).
- Publicacao parcial falha mid-batch: rollback atomico (`CAD-IMP-004`).

## 6) Criterios de aceite (provisorios)

- `CA-CAD-IMP-01`: CSV de setores conforme layout importa setores validos apos confirmacao na staging.
- `CA-CAD-IMP-02`: CSV de trabalhadores com setor inexistente marca linha amarela e celula acionavel leva a cadastro de setor pre-preenchido.
- `CA-CAD-IMP-03`: Filtros exibem subconjuntos corretos sem perder contexto de contadores.
- `CA-CAD-IMP-04`: Linha verde persiste identico ao cadastro manual equivalente (mesmo modelo de dados).
- `CA-CAD-IMP-05`: Tentativa gera auditoria com ator, arquivo, contagens e resultado.
- `CA-CAD-IMP-06`: RBAC impede import para perfil nao autorizado.

## 7) Catalogo inicial de erros (provisorio)

| Codigo | Condicao | Mensagem operacional (rascunho) |
|--------|----------|----------------------------------|
| `CAD-IMP-001` | Formato/cabecalho invalido | O arquivo nao corresponde ao layout esperado para este tipo de cadastro. |
| `CAD-IMP-002` | Sem linhas de dados | Nenhum registro encontrado no arquivo. |
| `CAD-IMP-003` | Nenhuma linha valida | Nao ha registros prontos para importacao. Revise as pendencias. |
| `CAD-IMP-004` | Falha ao publicar lote | A importacao nao foi concluida. Nenhuma alteracao parcial foi aplicada. |
| `CAD-IMP-005` | Matricula duplicada (arquivo ou base) | Matricula ja existente ou repetida no arquivo. |
| `CAD-IMP-006` | Sem permissao | Seu perfil nao permite importacao em lote de cadastros. |
| `CAD-IMP-010` | Setor nao encontrado | Setor nao cadastrado — cadastre ou corrija o valor. |
| `CAD-IMP-011` | Funcao nao encontrada | Funcao nao cadastrada para o setor informado. |

## 8) Dependencias

- `UC-CAD-02`, `UC-CAD-03` estaveis (regras e entidades).
- Modelo de dados: `docs/01-negocio/modelagem-entrega-epi.md` (funcao/setor/trabalhador).
- Identidade UX: `docs/02-arquitetura/easy-nr6-ux-identity.md`.

## 9) Documentacao impactada (quando implementar)

- Prototipo mock: nova pagina `22-cadastros-import-csv.html` (futuro).
- Paridade Java: `docs/03-operacao/paridade-mock-java-backlog.md`.
- Layouts CSV versionados: `docs/03-operacao/layouts-csv-cadastros/` (criar na Etapa 2).

## 10) Decisoes pendentes antes do DoR

1. **Layouts CSV** finais (colunas, obrigatoriedade, exemplos downloadable).
2. Importar **somente linhas validas** com pendencias restantes vs **bloquear** ate zerar amarelas.
3. Resolucao de setor/funcao: match **case-insensitive**? alias? codigo externo?
4. Pacote unico (3 abas) vs tres uploads separados.
5. Politica de **atualizacao** vs **somente insert** (trabalhador existente por matricula).
6. Retencao do arquivo bruto e do snapshot de staging.
7. Revalidacao automatica ao voltar do deep link vs botao explicito.

## 11) Criterio de saida do refinamento

- decisoes §10 fechadas;
- matriz `matriz-testes-uc-cad-imp-01.md` executavel;
- layouts CSV publicados;
- pronto para slice vertical (parser + dominio + UI staging) sem alterar modulos nao dependentes.
