# Matriz executavel de testes - UC-LOT-03 (Reserva)

Spec: `docs/03-operacao/spec-uc-lot-03-reserva.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| LOT03-001 | INT/AUDIT | CA-01, CA-07 | Disponivel 5 | Reservar 2 | Fisica 5, reservada 2, disponivel 3; `LOTE_RESERVADO` |
| LOT03-002 | INT | CA-02, `LOT-008` | Disponivel 2 | Reservar 3 | `LOT-008`; saldos intactos |
| LOT03-003 | INT | CA-03, `LOT-009` | Lote vencido | Reservar 1 | `LOT-009` |
| LOT03-004 | INT/AUDIT | CA-04 | Reserva restante 2 | Liberar 1 | Restante 1; disponivel sobe 1; `LOTE_RESERVA_LIBERADA` |
| LOT03-005 | INT | CA-04, `LOT-010` | Restante 1 | Liberar 2 | `LOT-010` |
| LOT03-006 | UI | CA-05 | Reserva ativa | Cancelar a liberacao | Restante intacto |
| LOT03-007 | INT | CA-06 | Reserva de 4, demanda 10, disponivel 6 | Calcular compra | A comprar 4, nao 8 |
| LOT03-008 | RBAC | Regra 7 | `CONSULTA` | Reservar | `AUTH-004` |
