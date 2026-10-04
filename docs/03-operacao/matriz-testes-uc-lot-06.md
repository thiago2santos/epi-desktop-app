# Matriz executavel de testes - UC-LOT-06 (Necessidade de compra)

Spec: `docs/03-operacao/spec-uc-lot-06-necessidade-compra.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| LOT06-001 | INT | CA-01 | Fisica 10, reservada 4, custo 5 | Demanda 10 | Disponivel 6; a comprar 4; valor 20 |
| LOT06-002 | INT | CA-02 | So lote vencido, fisica 10 | Demanda 3 | Disponivel 0; a comprar 3 |
| LOT06-003 | INT | CA-03 | Disponivel 8 | Demanda 5 | A comprar 0; sobra 3 |
| LOT06-004 | INT | CA-04 | Recebimento sem custo | Demanda acima do disponivel | Quantidade a comprar visivel; valor "custo incompleto" |
| LOT06-005 | INT | CA-05 | Qualquer saldo | Calcular | Nenhum movimento novo |
| LOT06-006 | RBAC/UI | CA-06 | `CONSULTA` | Abrir | Ve a grade; demanda nao editavel |
| LOT06-007 | UI | Tela | Unidade sem lote | Abrir | "Nenhum lote nesta unidade para comparar com a demanda." |
