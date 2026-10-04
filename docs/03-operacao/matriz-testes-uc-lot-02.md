# Matriz executavel de testes - UC-LOT-02 (Consulta de saldo)

Spec: `docs/03-operacao/spec-uc-lot-02-consulta-saldo.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| LOT02-001 | INT | CA-01 | Recebimento de 10 | Consultar | Fisica 10, reservada 0, disponivel 10, situacao Vigente |
| LOT02-002 | INT | CA-02 | Lote vencido com fisica 4 | Consultar | Disponivel 0, situacao Vencido |
| LOT02-003 | INT | CA-03 | Reserva de 3 sobre fisica 10 | Consultar | Reservada 3, disponivel 7, fisica 10 |
| LOT02-004 | INT | CA-04 | Filtro que nao acha | Consultar | Lista vazia, sem erro |
| LOT02-005 | INT | CA-05 | Dois lotes, validades diferentes | Consultar sem filtro | Validade mais proxima primeiro |
| LOT02-006 | RBAC/UI | CA-06 | Papel `CONSULTA` | Abrir a tela | Lista visivel, sem receber, reservar ou baixar |
