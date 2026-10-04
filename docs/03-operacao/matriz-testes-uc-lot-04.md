# Matriz executavel de testes - UC-LOT-04 (Baixa de prateleira)

Spec: `docs/03-operacao/spec-uc-lot-04-baixa-prateleira.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| LOT04-001 | INT/AUDIT | CA-01, CA-06 | Fisica 10, reservada 2, lote vencido | Baixar 8 por vencimento | Fisica 2, reservada 2; `LOTE_BAIXA_PRATELEIRA` com motivo |
| LOT04-002 | INT | CA-02, `LOT-011` | Fisica 10, reservada 4 | Baixar 7 | `LOT-011`; saldos intactos |
| LOT04-003 | INT | CA-03, `LOT-012` | Lote com sobra | Baixar sem motivo | `LOT-012` |
| LOT04-004 | UI | CA-04 | Confirmacao aberta | Cancelar | Fisica intacta |
| LOT04-005 | INT | CA-05 | Baixa de 3 e fornecimento de 2 no periodo | Abrir consumo | Consumo 2; perdas 3 |
| LOT04-006 | RBAC | Regra 7 | `CONSULTA` | Baixar | `AUTH-004` |
