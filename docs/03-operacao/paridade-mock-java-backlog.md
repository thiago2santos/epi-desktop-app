# Paridade protótipo HTML × backlog Java (MVP)

Documento de alinhamento entre **documentação/backlog**, **protótipo navegável** (`docs/mock/`) e **código Java** (`src/main/java`). Atualizado após fechamento do mock de sessão (out/2026).

## Como usar

| Coluna | Significado |
|--------|-------------|
| **Backlog** | Item em `backlog-implementacao-mvp.md` (M0–M5) |
| **Mock** | Tela HTML + regras em `Enr6MockStore` — referência de **fluxo, copy e validações UX** |
| **Java** | Estado real no desktop (JavaFX + Spring + SQLite) |
| **Spec / testes** | Spec operacional ou matriz de testes em `docs/03-operacao/` |

**Legenda Java:** `✅` entregue baseline · `🟡` esqueleto/parcial · `⬜` não iniciado · `🔶` doc/spec pendente de DoR

**Legenda Mock:** `✅` sessão funcional · `—` N/A

---

## Mapa tela mock → alvo Java

| Mock | UC / FEAT | Alvo Java (módulo / classe) |
|------|-----------|-----------------------------|
| `shell.html` | M0 shell + RBAC | `MainShellView`, `EasyNr6DesktopApp`, `AuthService` |
| `01-dashboard` | FEAT home | `MainShellView.Modulo.DASHBOARD` (placeholder) → nova `DashboardView` |
| `02-entrega-wizard` | UC-ENT-01 | `FornecimentoWizardView` |
| `03-devolucao` | pós-entrega | novo módulo `operacao/devolucao` |
| `04-estorno` | pós-entrega | novo módulo `operacao/estorno` |
| `05-historico` | consulta | `AuditQueryService` / novo `IssuanceQueryService` |
| `06-trabalhadores` | UC-CAD-03 | `EmployeeManagementView` |
| `07-setores-funcoes` | UC-CAD-02 | `DepartmentManagementView`, `JobRoleManagementView` — no shell: `OrgStructureManagementView` |
| `08-cad-epi` | UC-CAD-04 | `EpiManagementView` |
| `09-cad-ca-epi` | UC-CAD-05 | `CaBindingManagementView` |
| `10-estoque-lotes` | UC-LOT-01/02 | `LotManagementView` no destino Lotes e saldos. Recebimento e consulta com filtro |
| `11-matriz` | UC-MAT-01 | `MainShellView.Modulo.REGRAS` → `MatrixManagementView` |
| `12-periodicidade` | UC-MAT ext. | mesma área Regras → `PeriodicityView` |
| `13-relatorios-hub` | M3 FEAT | `MainShellView.Modulo.RELATORIOS` → `ReportsHubView` |
| `14-cobertura` | M3 UC | derivado de matriz + entregas → `CoverageReportView` |
| `15-pendencias` | M3 FEAT | `PendenciesView` (query sobre entregas/fila) |
| `16-solicitar` | UC-SOL-01 | **não no shell Java** — papel `Gestor` ausente |
| `17-fila` | UC-SOL-01 | idem — fila central (M2 estendido / modo servidor) |
| `18-auditoria` | UC-AUD-01 | `AuditTrailView`, `AuditQueryService` |
| `19-admin-usuarios` | UC-ADM-01/02 | `UserAdministrationView`, `UserAdministrationService` |
| `20-admin-parametros` | UC-ADM-03 | parcial via identidade; falta `SystemParams` persistido |
| `21-caepi-import` | UC-CAE-01 | Java: importação manual com auditoria. Ciclo automático diário ainda pendente |
| `22-cadastros-import-csv` (previsto) | UC-CAD-IMP-01 / FE-CAD-01 | spec fechada; Java não iniciado |

Referência mock: [`docs/mock/ROADMAP-SESSION-MOCK.md`](../mock/ROADMAP-SESSION-MOCK.md), [`docs/mock/mock-store.js`](../mock/mock-store.js).

---

## Paridade por onda de backlog

### M0 — Fundação

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| Login + shell | ✅ shell + perfis | ✅ login + `MainShellView` | — | Mock: 5 perfis incl. **Gestor**; Java: `Papel` sem `GESTOR` |
| RBAC menu | ✅ | ✅ sidebar desabilita módulo | — | Operação, demanda, estoque, regras e relatórios ainda são tela de referência |
| Auditoria append-only | ✅ `auditLog` | ✅ `AuditTrail` + JDBC | `spec-uc-aud-01` | Falha da ação também entra na trilha, fora da transação |
| Credenciais | 🟡 simulado | ✅ Argon2, troca, bloqueio | — | Mock não valida senha real |
| Usuários e papéis | ✅ 19 | ✅ `UserAdministrationView` | — | Criar, editar, papel, reset, bloquear e reativar. Excluir fica só no serviço |
| Política multiusuário PG | — | 🔶 arquitetura doc | backlog M0 | Mock = demo local |

### M1 — Prontidão operacional

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| UC-CAD-01 Unidade | — | ✅ serviço e tela | `spec-uc-cad-01-unidade`, `matriz-testes-uc-cad-01` | Nome, CNPJ e status. Empresa continua a do seed |
| UC-CAD-02 Setor/função | ✅ | ✅ policies + UI | CAD-02x tests | |
| UC-CAD-07 GHE | — | ✅ serviço e tela | `spec-uc-cad-07-ghe`, matriz testes | Um GHE por função. Perfil vigente: GHE ativo ou a própria função |
| UC-CAD-03 Trabalhador | ✅ | ✅ | employee tests | |
| UC-CAD-04/05 EPI + CA | ✅ + CAEPI gate | ✅ serviço e tela | `spec-uc-cad-04-05`, matriz testes | Vínculo lê a última carga; print cobre número fora da base |
| UC-CAE-01 Import CAEPI | ✅ 21 AUTO sim + **manual real** (upload ZIP/txt) | ✅ manual | spec fechada + matriz | Índice e variantes publicados. Download diário automático pendente |
| UC-LOT-01/02 Recebimento e saldo | ✅ 10 | ✅ recebimento e consulta | spec + matriz | Diario `V8`. Filtro e saldo lido dos movimentos |
| UC-LOT-03 Reserva | — | ⬜ | spec + matriz | Solicitacao continua sem reservar |
| UC-LOT-04 Baixa de prateleira | — | ⬜ | spec + matriz | Perda, fora do consumo |
| UC-LOT-05 Inventario | — | ⬜ | spec + matriz | Ajuste da diferenca |
| UC-LOT-06 Necessidade de compra | — | ⬜ | spec + matriz | Demanda menos disponivel |
| UC-REL-04 Consumo para budget | — | ⬜ | spec + matriz | Fornecimento, perdas e ajustes separados |
| UC-MAT-01 matriz | ✅ 11 | ✅ serviço e tela | spec + matriz | Perfil = função ou GHE ativo. Mock ainda só tem função |
| UC-MAT-02 periodicidade | ✅ 12 | ✅ serviço e tela | spec + matriz | Dias e aviso por EPI. A data que conta sai da ficha |
| Domínios motivos/validação | 🟡 selects fixos | ⬜ | DoR M2 | Extrair enums do mock p/ spec UC-ENT |

### M2 — Core operacional

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| UC-ENT-01 fornecimento | ✅ 02 | ✅ serviço e tela | spec + matriz | Ficha, termo e `BAIXA_FORNECIMENTO` na mesma transacao |
| UC-ENT-02 termo | ✅ passo 4 do 02 | ✅ no fornecimento | spec + matriz | `ASSINATURA_MANUAL`, versao `TERMO-NR6-01` |
| Commit entrega+lote+audit | ✅ store | ⬜ | DoR §5 transação | Mock: `createIssuance` |
| Termo / ciência NR-6 | ✅ checkboxes | ⬜ | blueprint passo 4 | |
| Histórico trabalhador | ✅ 05 | ⬜ | — | |
| UC-POS-01 Devolucao | ✅ 03 | ✅ serviço e tela | spec + matriz | Nao devolve quantidade ao disponivel |
| UC-POS-02 Estorno | ✅ 04 | ✅ serviço e tela | spec + matriz | `ESTORNO_FORNECIMENTO`; ficha permanece. Pedido espera `UC-SOL-01` |
| UC-POS-03 Pendencias | — | ✅ serviço e tela | spec + matriz | Desligamento, item individual. Consulta nao grava auditoria |
| UC-ENT-03 Historico | ✅ 05 | ✅ serviço e tela | spec + matriz | Fornecimento, devolucao e estorno. Consulta nao grava auditoria |
| Estorno | ✅ 04 | ⬜ | — | |
| UC-SOL-01 gestor + fila | ✅ 16–17 | ⬜ | spec fechada + matriz | Papel `GESTOR` ainda ausente no enum |
| Exceção matriz na entrega | ✅ wizard | ⬜ | — | Mock: `matrixException` |

### M3 — Relatórios e evidência

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| Hub relatórios | ✅ 13 `runReport` | ⬜ placeholder | M3 backlog | PDF continua 🔶 Jasper |
| Cobertura | ✅ 14 | ⬜ | — | Portar `getCoverageReport` |
| Pendências | ✅ 15 | ⬜ | — | Portar `getPendencies` |
| UC-AUD-01 consulta | ✅ 18 | ✅ filtro livre | spec + checklist usabilidade | Período e exportação continuam abertos |

### M4 / M5

| Item | Mock | Java | Notas |
|------|------|------|-------|
| Cadastros “completos” | 🟡 list+form | 🟡 abas JavaFX | Usabilidade campo pendente checklist M1 |
| Admin parâmetros | ✅ 20 | ⬜ | Java: unidade/CAEPI schedule |
| Landing GTM | — | — | Fora do escopo mock |

---

## Regras de negócio: mock → domínio Java (checklist de port)

Ao implementar cada UC no Java, validar paridade com o mock (comportamento, não código 1:1):

| Regra | Mock (`mock-store.js`) | Java hoje |
|-------|------------------------|-----------|
| CAD-021 setor duplicado | ✅ | ✅ `DepartmentPolicy` |
| CAD-024 inativar setor c/ funções | ✅ | ✅ |
| CAD-028 função c/ empregados | ✅ | ✅ |
| EPI ativo exige CA ativo | ✅ | ✅ `SetEpiStatusUseCase` |
| CAEPI inválido bloqueia mutação EPI/CA | ✅ | ⬜ |
| Lote vencido bloqueia entrega | ✅ | ✅ `EntregaRules.podeEntregarLote` |
| Saldo insuficiente | ✅ | ✅ regra saldo |
| Fora da matriz sem exceção | ✅ | ⬜ |
| Devolução data ≥ entrega | ✅ | ✅ `devolucaoEmDataValida` |
| Estorno motivo ≥ 10 chars | ✅ | ✅ `EstornoPolicy` |
| Solicitação não baixa estoque | ✅ | ⬜ |
| Imutabilidade entrega | ✅ status REVERSED/RETURNED | ⬜ schema |

---

## Sequência Java recomendada (espelhando o mock validado)

Ordem para maximizar reutilização do protótipo como **critério de aceite UX**:

```text
Sprint J1 — M1 estoque + regras (desbloqueia DoR M2)
  • UC-LOT-01, UC-LOT-02, UC-CAD-07, UC-MAT-01, UC-MAT-02, UC-ENT-01, UC-ENT-02, UC-ENT-03, UC-POS-01, UC-POS-02 e UC-POS-03 implementados
  • UC-LOT-03 a 06 e UC-REL-04 já têm spec; código depois do recebimento e da baixa de fornecimento

Sprint J2 — depois da ficha
  • UC-ENT-01/02 implementados (`FornecimentoWizardView`, migracao V16)
  • UC-POS-01 implementado (`DevolucaoView`, migracao V17)
  • UC-POS-02 implementado (`EstornoView`, migracao V18)
  • UC-ENT-03 implementado (`HistoricoView`)
  • UC-POS-03 implementado (`PendenciasView`)

Sprint J3 — M3 evidência
  • Cobertura + pendências (14–15) + hub (13) + export PDF mínimo
  • Dashboard (01) com KPIs reais

Sprint J4 — Integrações e demanda
  • UC-CAE-01 (21) + bloqueio EPI/CA no serviço
  • UC-SOL-01 (16–17) + `Papel.GESTOR`
  • Parâmetros sistema (20) persistidos

Sprint J5 — Hardening M1 cadastros
  • Usabilidade checklist + AtlantaFX / identidade UX
  • Paridade mensagens CAD-03x tela a tela com mock
```

Cada sprint: **demo comparativa** — mesmo roteiro do [`LEIA-ME-APROVACAO.txt`](../mock/LEIA-ME-APROVACAO.txt) no mock e no Java.

---

## Atualização DoR M2 (com mock como evidência UX)

Itens do backlog que o **mock já demonstra** (aceite de product owner / key user sobre fluxo):

- [x] Wizard 5 passos com validações sequenciais (referência `02-entrega-wizard.html`).
- [x] Bloqueio lote vencido e saldo (comportamento esperado documentado).
- [x] Cobertura matriz × vigente calculável (`14` + `coverageForEmployee`).
- [ ] **Persistência JDBC** equivalente ao `createIssuance` (ainda pendente Java).
- [ ] Spec de UC-ENT-01 aceita pelo responsavel (texto em `spec-uc-ent-01-fornecimento.md`).

Itens DoR M1 ainda **não** espelhados no Java:

- [ ] Lotes cadastrados (migration + UI).
- [ ] Matriz + periodicidade ativas (migration + UI).
- [ ] UC-CAE-01 operacional (ou decisão consciente de adiar com risco documentado).

---

## Manutenção deste documento

1. Ao fechar uma feature Java, atualizar a coluna **Java** na tabela da onda correspondente.
2. Se o mock mudar (novo passo/regra), atualizar **Regras mock → Java** e o store.
3. Toda PR `UC`/`FEAT` deve citar: mock page(s), spec, e linha desta paridade.

**Última revisão:** UC-CAD-01 grava unidade pela tela. Os casos do índice operacional têm spec curta, inclusive ficha PDF, histórico por EPI, cobertura, exportação e unidade padrão. Java de estoque em diante ainda sem tabela.
