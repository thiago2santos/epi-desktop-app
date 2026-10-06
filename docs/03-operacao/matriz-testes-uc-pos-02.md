# Matriz executavel de testes - UC-POS-02 (Estorno)

Spec: `docs/03-operacao/spec-uc-pos-02-estorno.md`. Cobertos por `EstornoPolicyTest`, `EstornoManagementServiceIntegrationTest` e `EstornoUxTest`. `POS02-008` espera o pedido do `UC-SOL-01`. `POS01-004` roda no mesmo teste de integracao.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| POS02-001 | INT/AUDIT | CA-01 | Item de 2; fisica 8; reservada 1 | Estornar com motivo longo | Fisica 10; reservada 1; ficha "Estornado"; `FORNECIMENTO_ESTORNADO` |
| POS02-002 | INT | CA-02, `POS-005` | Item que conta | Motivo "errei" | `POS-005`; fisica intacta |
| POS02-003 | INT | CA-03, `POS-004` | Item devolvido | Estornar | `POS-004` |
| POS02-004 | INT | CA-04 | Item ja estornado | Estornar de novo | `POS-004` |
| POS02-005 | INT | CA-05 | Lote vencido; fisica 3; item de 1 | Estornar | Fisica 4; disponivel para fornecer 0 |
| POS02-006 | UI | CA-06 | Dialogo aberto | Voltar | Nada gravado |
| POS02-007 | RBAC | CA-07 | `ALMOXARIFE` | Estornar | `AUTH-004` |
| POS02-008 | INT | CA-08 | Pedido atendido com quantidade 2 por este item | Estornar | Quantidade atendida do pedido cai 2; pedido deixa de estar `ATENDIDA` se ainda houver saldo |
