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
| `02-entrega-wizard` | UC-ENT-01 | `EntregaWizardView` + futuro `IssuanceService` |
| `03-devolucao` | pós-entrega | novo módulo `operacao/devolucao` |
| `04-estorno` | pós-entrega | novo módulo `operacao/estorno` |
| `05-historico` | consulta | `AuditQueryService` / novo `IssuanceQueryService` |
| `06-trabalhadores` | UC-CAD-03 | `EmployeeManagementView` |
| `07-setores-funcoes` | UC-CAD-02 | `DepartmentManagementView`, `JobRoleManagementView` |
| `08-cad-epi` | UC-CAD-04 | `EpiManagementView` |
| `09-cad-ca-epi` | UC-CAD-05 | `EpiCaBindingManagementView` |
| `10-estoque-lotes` | UC-LOT-01/02 | `MainShellView.Modulo.ESTOQUE` (placeholder) → `LotManagementView` |
| `11-matriz` | UC-MAT-01 | `MainShellView.Modulo.REGRAS` → `MatrixManagementView` |
| `12-periodicidade` | UC-MAT ext. | mesma área Regras → `PeriodicityView` |
| `13-relatorios-hub` | M3 FEAT | `MainShellView.Modulo.RELATORIOS` → `ReportsHubView` |
| `14-cobertura` | M3 UC | derivado de matriz + entregas → `CoverageReportView` |
| `15-pendencias` | M3 FEAT | `PendenciesView` (query sobre entregas/fila) |
| `16-solicitar` | UC-SOL-01 | **não no shell Java** — papel `Gestor` ausente |
| `17-fila` | UC-SOL-01 | idem — fila central (M2 estendido / modo servidor) |
| `18-auditoria` | UC-AUD-01 | `AuditTrailView`, `AuditQueryService` |
| `19-admin-usuarios` | UC-ADM-01/02 | `UserAdministrationView`, `identity/*` |
| `20-admin-parametros` | UC-ADM-03 | parcial via identidade; falta `SystemParams` persistido |
| `21-caepi-import` | UC-CAE-01 | **mock** AUTO + manual + IndexedDB; Java **não iniciado** |
| `22-cadastros-import-csv` (previsto) | UC-CAD-IMP-01 / FE-CAD-01 | **não iniciado** — spec registrada; staging verde/amarelo + deep link CAD-02 |

Referência mock: [`docs/mock/ROADMAP-SESSION-MOCK.md`](../mock/ROADMAP-SESSION-MOCK.md), [`docs/mock/mock-store.js`](../mock/mock-store.js).

---

## Paridade por onda de backlog

### M0 — Fundação

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| Login + shell | ✅ shell + perfis | ✅ login + `MainShellView` | — | Mock: 5 perfis incl. **Gestor**; Java: `Papel` sem `GESTOR` |
| RBAC menu | ✅ | 🟡 sidebar desabilita módulo | — | Java: DASHBOARD/ESTOQUE/REGRAS/RELATORIOS ainda placeholder |
| Auditoria append-only | ✅ `auditLog` | ✅ `AuditTrail` + JDBC | `spec-uc-aud-01` | |
| Credenciais | 🟡 simulado | ✅ Argon2, troca, bloqueio | — | Mock não valida senha real |
| Política multiusuário PG | — | 🔶 arquitetura doc | backlog M0 | Mock = demo local |

### M1 — Prontidão operacional

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| UC-CAD-02 Setor/função | ✅ | ✅ policies + UI | CAD-02x tests | Alinhar mensagens CAD com mock |
| UC-CAD-03 Trabalhador | ✅ | ✅ | employee tests | |
| UC-CAD-04/05 EPI + CA | ✅ + CAEPI gate | ✅ `CaPolicy`, UI | `spec-uc-cad-04-05`, matriz testes | Java: evidência CAEPI; mock: CAD-037 texto fraco |
| UC-CAE-01 Import CAEPI | ✅ 21 AUTO sim + **manual real** (upload ZIP/txt) | ⬜ | `spec-uc-cae-01`, matriz | Java: `CaepiImportService` background + manual same pipeline |
| UC-LOT lote/saldo | ✅ 10 | ⬜ sem tabela/migration | 🔶 spec a escrever | **Copiar regras** de `createLot` / `createIssuance` do mock |
| UC-MAT-01 matriz | ✅ 11 | ⬜ | 🔶 spec a escrever | Mock: assignment, treinamento, CA esperado |
| UC-MAT periodicidade | ✅ 12 | ⬜ | 🔶 | Mock: `warnDays` → cobertura |
| Domínios motivos/validação | 🟡 selects fixos | ⬜ | DoR M2 | Extrair enums do mock p/ spec UC-ENT |

### M2 — Core operacional

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| UC-ENT-01 wizard 5 passos | ✅ 02 | 🟡 `EntregaWizardView` simulação | 🔶 UC-ENT spec | Java: passos diferentes; **sem persistência**; `EntregaRules` já espelha lote/devolução |
| Commit entrega+lote+audit | ✅ store | ⬜ | DoR §5 transação | Mock: `createIssuance` |
| Termo / ciência NR-6 | ✅ checkboxes | ⬜ | blueprint passo 4 | |
| Histórico trabalhador | ✅ 05 | ⬜ | — | |
| Devolução | ✅ 03 | ⬜ | `EntregaRules.devolucaoEmDataValida` | |
| Estorno | ✅ 04 | ⬜ | — | |
| UC-SOL-01 gestor + fila | ✅ 16–17 | ⬜ | `spec-uc-sol-01`, matriz | Depende `Papel.GESTOR` + modelo fila |
| Exceção matriz na entrega | ✅ wizard | ⬜ | — | Mock: `matrixException` |

### M3 — Relatórios e evidência

| Item | Mock | Java | Spec / testes | Notas |
|------|------|------|---------------|-------|
| Hub relatórios | ✅ 13 `runReport` | ⬜ placeholder | M3 backlog | PDF continua 🔶 Jasper |
| Cobertura | ✅ 14 | ⬜ | — | Portar `getCoverageReport` |
| Pendências | ✅ 15 | ⬜ | — | Portar `getPendencies` |
| UC-AUD-01 consulta | ✅ 18 | ✅ baseline | spec + checklist usabilidade | Filtros período/export 🔶 |

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
| Estorno motivo ≥ 10 chars | ✅ | ⬜ |
| Solicitação não baixa estoque | ✅ | ⬜ |
| Imutabilidade entrega | ✅ status REVERSED/RETURNED | ⬜ schema |

---

## Sequência Java recomendada (espelhando o mock validado)

Ordem para maximizar reutilização do protótipo como **critério de aceite UX**:

```text
Sprint J1 — M1 estoque + regras (desbloqueia DoR M2)
  • Migration `lote` + módulo UC-LOT (UI ≈ mock 10)
  • Migration `matriz` + `periodicidade` (UI ≈ mock 11–12)
  • Spec curta UC-LOT + UC-MAT + cenários (copiar fluxos do mock)

Sprint J2 — M2 entrega real
  • Migration `fornecimento` (issuance) + baixa lote transacional
  • Refatorar `EntregaWizardView` = passos mock 02 (trabalhador → matriz → lote → termo → revisão)
  • Histórico (05), devolução (03), estorno (04)

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
- [ ] Spec formal UC-ENT-01 assinada (extrair do mock + blueprint).

Itens DoR M1 ainda **não** espelhados no Java:

- [ ] Lotes cadastrados (migration + UI).
- [ ] Matriz + periodicidade ativas (migration + UI).
- [ ] UC-CAE-01 operacional (ou decisão consciente de adiar com risco documentado).

---

## Manutenção deste documento

1. Ao fechar uma feature Java, atualizar a coluna **Java** na tabela da onda correspondente.
2. Se o mock mudar (novo passo/regra), atualizar **Regras mock → Java** e o store.
3. Toda PR `UC`/`FEAT` deve citar: mock page(s), spec, e linha desta paridade.

**Última revisão:** protótipo HTML sessão completa; Java em M1 parcial (cadastros + wizard simulado + auditoria + admin usuários).
