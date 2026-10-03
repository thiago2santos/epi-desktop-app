# Protótipo HTML — persistência de sessão

## Status: **protótipo fechado** (22 telas + shell)

Simula o MVP documentado em HTML + **`Enr6MockStore`**, antes da implementação Java. Tudo que o operador faz no shell **persiste na aba** (`sessionStorage` + `window.__enr6Db` no pai do iframe).

## Cobertura por página

| Página | UC / FEAT | Store |
|--------|-----------|--------|
| shell + login | M0 RBAC | usuário `sessionStorage` |
| 01 Dashboard | FEAT | KPIs live |
| 02–05 | UC-ENT / pós-entrega | entregas, devolução, estorno, histórico |
| 06–09 | UC-CAD | cadastros + CA |
| 10–12 | UC-LOT / MAT | lotes, matriz, periodicidade |
| 13 | M3 hub | `runReport()` |
| 14–15 | M3 | cobertura, pendências |
| 16–17 | UC-SOL-01 | solicitação + fila |
| 18 | UC-AUD-01 | auditLog |
| 19–20 | UC-ADM | usuários, parâmetros |
| 21 | UC-CAE-01 | estado CAEPI + banner shell |

## Demo ponta a ponta sugerida

1. **shell.html** → Admin → Dashboard.
2. **Cadastros** (se necessário) → **Lotes** → **Matriz / Periodicidade**.
3. **Gestor** → solicitar EPI → **Fila** → aprovar → **Entrega** wizard.
4. **Histórico** → **Cobertura / Pendências** → **Relatórios** hub.
5. **Devolução** (Pedro inativo) ou **Estorno** → **Auditoria**.
6. **Parâmetros** / **Usuários** → **CAEPI** degradado (teste bloqueio EPI/CA).
7. **Reset** no rodapé → volta ao seed.

## Referências

- `docs/03-operacao/backlog-implementacao-mvp.md`
- Specs UC em `docs/03-operacao/` e UML em `docs/04-uml/`

## Convenções

- **Persistência:** `sessionStorage['enr6_mock_db_v1']` + `parent.__enr6Db`
- **Reset:** rodapé do shell → seed completo
- **Auditoria:** mutações críticas em `auditLog`
- **Java:** PDF, assinatura real e PostgreSQL ficam fora deste mock

## Entrega

- Zip de aprovação: `easy-nr6-mock-aprovacao.zip`
- README: [`README.md`](README.md)
