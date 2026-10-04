# Matriz executavel de testes - UC-REL-04 (Consumo para budget)

Spec: `docs/03-operacao/spec-uc-rel-04-consumo-budget.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| REL04-001 | INT | CA-01 | Duas baixas do mesmo EPI, custos 10 e 10, qtd 1 e 2 | Consultar o periodo | Item quantidade 3, valor 30; total geral igual |
| REL04-002 | INT | CA-02 | Baixa de 2 e estorno de 2 no periodo | Consultar | Consumo 0 |
| REL04-003 | INT | CA-03 | Baixa de prateleira de 4 e fornecimento de 1 | Consultar | Consumo 1; perdas 4 |
| REL04-004 | INT | CA-04 | Ajuste +5 e fornecimento de 1 | Consultar | Consumo 1; ajustes 5 |
| REL04-005 | INT | CA-05 | Reserva de 3 sem baixa | Consultar | Consumo, perdas e ajustes em 0 |
| REL04-006 | INT | CA-06 | Baixa sem custo, qtd 2 | Consultar | Quantidade 2; valor da linha "custo incompleto" |
| REL04-007 | INT | CA-07 | Baixa com funcao Operador; trabalhador depois virou Analista | Filtrar Operador | A baixa aparece |
| REL04-008 | UI | CA-08 | Periodo sem movimento | Consultar | Zeros e "Nenhum consumo no periodo." |
| REL04-009 | RBAC | Regra 6 | `ALMOXARIFE` | Abrir | `AUTH-004` |
