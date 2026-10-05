# Matriz Executavel de Testes - UC-CAD-04/05 (EPI + CA)

## Objetivo

Detalhar cenarios de teste executaveis para validar as regras do cadastro de EPI e vinculo de CA, com rastreabilidade normativa e cobertura de RBAC/auditoria.

## Vinculo com a especificacao oficial do caso de uso

- Documento base: `docs/03-operacao/spec-uc-cad-04-05-nr6.md`
- Cada cenario abaixo referencia explicitamente:
  - secao de regra de negocio (secao 4);
  - criterio de aceite (secao 8);
  - codigo de erro de dominio quando aplicavel (secao 5).
- Objetivo: garantir rastreabilidade completa `spec -> teste -> evidencia`.

## Legenda

- Tipo de teste:
  - `UNIT`: policy/validator
  - `INT`: integracao (use case + repository + DB)
  - `RBAC`: autorizacao por papel
  - `AUDIT`: trilha de auditoria
  - `UI`: validacao funcional de tela/fluxo

## Matriz de cenarios

| ID | Tipo | Referencia na spec | Regra alvo | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|---|
| CAD45-001 | INT | Regras sec.4(1), CA-01, CA-05 | Fluxo feliz EPI | Usuario `SESMT` autenticado | Cadastrar EPI com `description`, `annex_group=A`, `active=true` | EPI criado, status ativo, auditoria `EPI_CREATED` |
| CAD45-002 | UNIT/INT | Regras sec.4(1), Erro `CAD-031` | Obrigatorios EPI | Contexto pronto para cadastro | Tentar criar EPI com `description` vazio | Operacao bloqueada com `CAD-031` |
| CAD45-003 | UNIT/INT | Regras sec.4(1), CA-01, Erro `CAD-032` | Grupo Anexo I | Contexto pronto para cadastro | Tentar criar EPI com grupo fora de `A..I` | Operacao bloqueada com `CAD-032` |
| CAD45-004 | INT | Regras sec.4, Erro `CAD-033` | Duplicidade EPI | Ja existe EPI no escopo definido | Cadastrar novo EPI com mesma chave de unicidade | Operacao bloqueada com `CAD-033` |
| CAD45-005 | INT/AUDIT | CA-05 | Edicao EPI | EPI existente | Atualizar descricao/grupo com dados validos | EPI atualizado e evento `EPI_UPDATED` registrado |
| CAD45-006 | INT/AUDIT | Regras sec.4(7), CA-05 | Inativacao/Reativacao EPI | EPI existente | Inativar e depois reativar EPI | Status alterado corretamente e eventos `EPI_DEACTIVATED` / `EPI_REACTIVATED` |
| CAD45-007 | INT | Regras sec.4(2)(5), CA-02, CA-05 | Fluxo feliz CA | EPI existente e ativo | Vincular CA com `ca_number`, `ca_status=ACTIVE`, datas coerentes, `official_check_at`, `official_check_note` | Vinculo criado e auditoria `EPI_CA_BOUND` |
| CAD45-008 | UNIT/INT | Regras sec.4(2), Erro `CAD-034` | CA ausente/invalido | EPI selecionado | Tentar vincular CA com numero vazio/nulo | Operacao bloqueada com `CAD-034` |
| CAD45-009 | UNIT/INT | Regras sec.4(5), CA-02, Erro `CAD-037` | Evidencia oficial ausente | EPI selecionado | Tentar vincular CA sem `official_check_at` ou sem `official_check_note` | Operacao bloqueada com `CAD-037` |
| CAD45-010 | UNIT/INT | Regras sec.4(3), CA-03, Erro `CAD-035` | Conflito de vigencia | Ja existe vinculo ativo do mesmo CA no EPI com periodo sobreposto | Criar/ativar novo vinculo com sobreposicao temporal | Operacao bloqueada com `CAD-035` |
| CAD45-011 | UNIT/INT | Regras sec.4(4), Erro `CAD-036` | Status impeditivo | EPI selecionado | Tentar ativar vinculo de CA com status `SUSPENDED`, `CANCELED` ou `EXPIRED` | Operacao bloqueada com `CAD-036` |
| CAD45-012 | INT/AUDIT | Regras sec.4(3)(4)(5), CA-05 | Atualizacao de vinculo CA | Vinculo de CA existente | Alterar datas/status/evidencia sem violar regras | Vinculo atualizado e evento `EPI_CA_UPDATED` |
| CAD45-013 | INT/AUDIT | Regras sec.4(7), CA-05 | Inativar/Reativar vinculo CA | Vinculo de CA existente | Inativar e reativar vinculo | Status atualizado e eventos `EPI_CA_DEACTIVATED` / `EPI_CA_REACTIVATED` |
| CAD45-014 | UNIT/INT | Regras sec.4(2) | Normalizacao numero CA | EPI selecionado | Vincular CA com espacos/formato variado | Numero persistido normalizado; comparacao de duplicidade consistente |
| CAD45-015 | UNIT/INT | Regras sec.4(3), CA-03 | Integridade temporal | EPI selecionado | Informar `valid_until < valid_from` | Operacao bloqueada por regra temporal |
| CAD45-016 | INT | Erro `CAD-039` | Alvo inexistente | ID invalido | Editar/inativar EPI ou vinculo inexistente | Operacao bloqueada com `CAD-039` |
| CAD45-017 | RBAC | CA-06 | Permissao `ADMIN` | Usuario `ADMIN` autenticado | Criar/editar/inativar EPI e CA | Operacoes permitidas |
| CAD45-018 | RBAC | CA-06 | Permissao `SESMT` | Usuario `SESMT` autenticado | Criar/editar/inativar EPI e CA | Operacoes permitidas |
| CAD45-019 | RBAC | CA-06 | Bloqueio `ALMOXARIFE` | Usuario `ALMOXARIFE` autenticado | Tentar mutar EPI/CA | Operacao negada (`AUTH-004` ou equivalente) |
| CAD45-020 | RBAC | CA-06 | Bloqueio `CONSULTA` | Usuario `CONSULTA` autenticado | Tentar mutar EPI/CA | Operacao negada (`AUTH-004` ou equivalente) |
| CAD45-021 | AUDIT | CA-05 | Auditoria em criacao | Usuario autorizado + operacao de criacao | Criar EPI e vincular CA | Registros de auditoria com `actor`, `acao`, `entidade`, `entidade_id`, `detalhes` |
| CAD45-022 | AUDIT | CA-05 | Auditoria em status | Usuario autorizado + operacao sensivel | Inativar/reativar EPI e vinculo de CA | Eventos de auditoria gravados sem omissao |
| CAD45-023 | UI | CA-01, CA-05 | Fluxo de formulario EPI | Aplicacao aberta em `Cadastros > EPI` | Preencher formulario valido e salvar | Feedback verde (5s), item aparece na tabela |
| CAD45-024 | UI | Erros sec.5 (`CAD-03x`) | Feedback de erro EPI | Aplicacao aberta em `Cadastros > EPI` | Submeter formulario invalido | Feedback vermelho com codigo `CAD-03x` correspondente |
| CAD45-025 | UI | CA-02, CA-05 | Fluxo de formulario CA | Aplicacao aberta em `Cadastros > CA por EPI` | Selecionar EPI, vincular CA valido | Feedback verde e vinculo aparece na tabela |
| CAD45-026 | UI | Regras sec.4(7), CA-05 | Confirmacao de inativacao | Registro existente selecionado | Acionar inativacao de EPI/CA | Dialogo de confirmacao; sem confirmar nao inativa |

## Cenarios criticos (gate para iniciar implementacao completa)

Obrigatorios antes de considerar o UC pronto para desenvolvimento avancado:

- `CAD45-001`, `CAD45-002`, `CAD45-003`
- `CAD45-007`, `CAD45-009`, `CAD45-010`, `CAD45-011`
- `CAD45-017`, `CAD45-018`, `CAD45-019`
- `CAD45-021`, `CAD45-022`

## Evidencias esperadas por execucao

Para cada cenario executado, registrar:

- resultado esperado;
- resultado observado;
- evidencias (print/log/consulta SQL quando aplicavel);
- status final (`aprovado` ou `pendente ajuste`).
