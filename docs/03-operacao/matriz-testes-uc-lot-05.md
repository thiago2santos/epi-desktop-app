# Matriz executavel de testes - UC-LOT-05 (Inventario)

Spec: `docs/03-operacao/spec-uc-lot-05-inventario.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| LOT05-001 | INT | CA-01 | Dois lotes na unidade | Abrir inventario | Linhas com fisico esperado e reservada; status Aberto |
| LOT05-002 | INT | CA-02 | Fisico 10 | Contar 10 e confirmar | Nenhum `AJUSTE_INVENTARIO` |
| LOT05-003 | INT/AUDIT | CA-03 | Fisico 10, reservada 2 | Contar 12 e confirmar | Fisica 12, reservada 2; evento `INVENTARIO_CONFIRMADO` |
| LOT05-004 | INT | CA-04, `LOT-014` | Reservada 4 | Contar 3 e confirmar | `LOT-014`; nenhum ajuste; inventario segue aberto |
| LOT05-005 | INT | CA-05, `LOT-013` | Duas linhas | Deixar uma em branco | `LOT-013` |
| LOT05-006 | UI | CA-06 | Resumo aberto | Cancelar | Fisico intacto; inventario aberto |
| LOT05-007 | INT | CA-07 | Ajuste de +2 e fornecimento de 1 | Abrir budget | Consumo 1; ajustes 2 |
| LOT05-008 | INT | Regra 1, `LOT-015` | Inventario ja aberto | Abrir outro na mesma unidade | Recusa; texto de inventario aberto |
