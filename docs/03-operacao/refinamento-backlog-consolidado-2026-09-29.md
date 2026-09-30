# Refinamento Consolidado do Backlog - 2026-09-29

## Objetivo

Consolidar o estado real do backlog com base em:

- metodologia spec-driven adotada no projeto;
- implementacao efetiva no codigo;
- testes existentes;
- documentacao funcional e operacional.

## Metodo aplicado

Referencia oficial:

- `docs/99-governanca/metodologia-refinamento-spec-driven.md`

Passos usados neste consolidado:

1. inventario de UCs no UML;
2. cruzamento com servicos/usecases/repositories em `src/main/java`;
3. cruzamento com testes em `src/test/java`;
4. classificacao de status por UC;
5. definicao de proximo ataque por ondas.

## Escala de status

- `Concluido (baseline)`: implementado, com fluxo principal funcional e cobertura inicial de teste.
- `Parcial`: existe implementacao inicial, mas faltam regras/escopo/qualidade para considerar baseline fechado.
- `Nao iniciado`: sem implementacao funcional para o UC.

## Mapa de completude por caso de uso

### Administracao e seguranca

| UC | Status | Evidencia principal |
|---|---|---|
| UC-ADM-01 Cadastrar usuario | Concluido (baseline) | use cases de identidade + `UserAdministrationService` + testes de integracao |
| UC-ADM-02 Gerenciar papeis | Concluido (baseline) | `AssignRoleUseCase`, `RemoveRoleUseCase` |
| UC-ADM-03 Configurar parametros do sistema | Nao iniciado | sem modulo/tela dedicada |
| UC-ADM-04 Resetar credencial | Concluido (baseline) | `ResetCredentialUseCase` + auditoria |
| UC-TRV-01 Autenticar usuario | Concluido (baseline) | `AuthenticateUserUseCase` com resultado tipado |
| UC-TRV-02 Registrar auditoria sensivel | Concluido (baseline) | `AuditTrail` + `AuditService` + eventos em modulos |
| UC-TRV-03 Exportar relatorio PDF | Parcial | infra PDF presente, mas fluxo de relatorios ainda nao fechado |
| UC-AUD-01 Consultar auditoria (baseline interno) | Concluido (baseline) | `AuditQueryService` + `AuditTrailView` no modulo lateral |

### Cadastros mestres

| UC | Status | Evidencia principal |
|---|---|---|
| UC-CAD-01 Empresa e unidade | Nao iniciado | sem cadastro dedicado em UI/app |
| UC-CAD-02 Setor e funcao | Concluido (baseline) | use cases, repository JDBC, UI de abas, testes |
| UC-CAD-03 Empregado | Concluido (baseline) | regras de consistencia, auditoria, testes |
| UC-CAD-04 Cadastrar EPI | Concluido (baseline) | modulo `epi` + migration V5 + UI |
| UC-CAD-05 Vincular CA ao EPI | Concluido (baseline) | regras `CAD-03x`, evidencia oficial, UI e testes |
| UC-CAD-06 Inativar cadastro mestre | Parcial | cobertura para setor/funcao/empregado/epi/ca; faltam demais mestres |

### Lotes, matriz e periodicidade

| UC | Status | Evidencia principal |
|---|---|---|
| UC-LOT-01 Recebimento de lote | Nao iniciado | sem entidade/use case/repository do dominio lote |
| UC-LOT-02 Saldo e validade de lotes | Nao iniciado | sem listagem/consulta de lote |
| UC-MAT-01 Matriz funcao/GHE x EPI | Nao iniciado | sem modelo/servico/UI de matriz |
| UC-MAT-02 Periodicidade de reposicao | Nao iniciado | sem parametros/use case |

### Operacao de entrega e pos-entrega

| UC | Status | Evidencia principal |
|---|---|---|
| UC-ENT-01 Registrar entrega de EPI | Parcial | wizard de UI existe, sem core transacional completo |
| UC-ENT-02 Aceite do termo | Parcial | conceito em docs/modelagem; sem fechamento de fluxo persistido |
| UC-ENT-03 Historico por trabalhador | Parcial | base conceitual e regras; falta implementacao completa de consulta |
| UC-POS-01 Devolucao/descarte | Nao iniciado | sem fluxo operacional implementado |
| UC-POS-02 Estorno de entrega | Nao iniciado | sem fluxo de estorno formal no core |
| UC-POS-03 Pendencias de devolucao | Nao iniciado | sem painel/consulta de pendencias |

### Relatorios

| UC | Status | Evidencia principal |
|---|---|---|
| UC-REL-01 Ficha por trabalhador e periodo | Nao iniciado | sem caso de uso funcional fechado |
| UC-REL-02 Historico por EPI/CA/lote | Nao iniciado | sem modulo de relatorio implementado |
| UC-REL-03 Cobertura por trabalhador ativo | Nao iniciado | sem regras/motor de cobertura implementados |
| UC-REL-04 Consumo para budget | Nao iniciado | sem relatorio analitico de consumo |

## O que foi possivel refinar agora (deep dive interno)

1. Consolidacao do status real de todos os UCs mapeados no UML.
2. Cruzamento objetivo entre backlog e implementacao efetiva no codigo.
3. Classificacao de prontidao por UC (`concluido baseline`, `parcial`, `nao iniciado`).
4. Definicao de sequencia de execucao recomendada para fechamento de M1 e liberacao de M2.
5. Definicao de caminhos de verificacao para auditoria e cadastros criticos.

## Proximo ataque recomendado (ordem de execucao)

1. Fechar usabilidade pendente de `Cadastros` e `Auditoria`.
2. Implementar `UC-LOT-01` e `UC-LOT-02` (lotes).
3. Implementar `UC-MAT-01` e `UC-MAT-02` (matriz + periodicidade).
4. Fechar DoR de M2 e iniciar `UC-ENT-01` com transacao unica.
5. Avancar sequencia operacional (`ENT-02`, `ENT-03`, `POS-01`, `POS-02`, `POS-03`).
6. Consolidar relatorios (`REL-01` a `REL-04`) + exportacao PDF governada.

## Caminhos para verificar o que ja foi feito

### Cadastros e EPI/CA

- `src/main/java/br/com/easynr6/gestaoepi/modules/employee/`
- `src/main/java/br/com/easynr6/gestaoepi/modules/epi/`
- `src/main/java/br/com/easynr6/gestaoepi/ui/cadastros/`
- `src/main/resources/db/migration/V5__epi_catalog_master_data.sql`
- `src/test/java/br/com/easynr6/gestaoepi/modules/employee/`
- `src/test/java/br/com/easynr6/gestaoepi/modules/epi/`

### Identidade e credencial

- `src/main/java/br/com/easynr6/gestaoepi/identity/`
- `src/main/java/br/com/easynr6/gestaoepi/shared/auth/`
- `src/test/java/br/com/easynr6/gestaoepi/identity/`
- `src/test/java/br/com/easynr6/gestaoepi/shared/auth/`

### Auditoria

- `src/main/java/br/com/easynr6/gestaoepi/shared/audit/AuditTrail.java`
- `src/main/java/br/com/easynr6/gestaoepi/shared/audit/AuditService.java`
- `src/main/java/br/com/easynr6/gestaoepi/shared/audit/AuditQueryService.java`
- `src/main/java/br/com/easynr6/gestaoepi/ui/auditoria/AuditTrailView.java`

### Planejamento e refinamento

- `docs/03-operacao/backlog-implementacao-mvp.md`
- `docs/04-uml/casos-de-uso-uml.md`
- `docs/03-operacao/spec-uc-cad-04-05-nr6.md`
- `docs/03-operacao/matriz-testes-uc-cad-04-05.md`
- `docs/03-operacao/spec-uc-aud-01-consultar-auditoria.md`
- `docs/03-operacao/checklist-usabilidade-m1-cadastros-auditoria.md`

## Pendencias abertas para proxima sessao

1. Refinamento detalhado de `UC-LOT-01/02` com spec + matriz de testes.
2. Refinamento detalhado de `UC-MAT-01/02` com regras de negocio e criterios de aceite.
3. Fechamento da rodada de usabilidade pendente com registro de evidencias.
