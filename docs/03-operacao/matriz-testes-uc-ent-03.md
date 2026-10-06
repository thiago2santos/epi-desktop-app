# Matriz executavel de testes - UC-ENT-03 (Historico)

Spec: `docs/03-operacao/spec-uc-ent-03-historico.md`. Cobertos por `HistoricoPolicyTest`, `HistoricoManagementServiceIntegrationTest` e `HistoricoUxTest`. Pedido (`ENT03-002`) nao tem tabela: a consulta so le fornecimento, devolucao e estorno.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| ENT03-001 | INT | CA-01, CA-06 | Um item devolvido e um item estornado no periodo | Consultar | Quatro linhas; o fornecimento estornado segue visivel; consulta nao grava auditoria |
| ENT03-002 | INT | CA-02 | Pedido em aberto do trabalhador | Consultar | Pedido ausente |
| ENT03-003 | UI | CA-03 | Periodo sem fato | Consultar | "Nenhum fornecimento no periodo." |
| ENT03-004 | UI | CA-04 | Data final anterior a inicial | Consultar | Nao chama a busca |
| ENT03-005 | RBAC/UI | CA-05 | `CONSULTA` | Abrir um item | Sem acao de devolver ou estornar |
