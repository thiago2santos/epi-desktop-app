# Matriz executavel de testes - UC-POS-01 (Devolucao)

Spec: `docs/03-operacao/spec-uc-pos-01-devolucao.md`. Cobertos por `DevolucaoPolicyTest`, `DevolucaoManagementServiceIntegrationTest` e `DevolucaoUxTest`. `POS01-004` espera a tabela de estorno do `UC-POS-02`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| POS01-001 | INT/AUDIT | CA-01 | Item que conta; disponivel 4 | Devolver por desligamento | Devolucao gravada; disponivel segue 4; item sai da cobertura; `DEVOLUCAO_REGISTRADA` |
| POS01-002 | INT | CA-02, `POS-002` | Fornecimento em 10/03 | Data 09/03 | `POS-002`; sem registro |
| POS01-003 | INT | CA-03, `POS-001` | Item ja devolvido | Devolver de novo | `POS-001` |
| POS01-004 | INT | CA-04 | Item estornado | Devolver | `POS-001` |
| POS01-005 | UI | CA-05 | Dialogo aberto | Voltar | Nada gravado |
| POS01-006 | RBAC | CA-06 | `CONSULTA` | Confirmar | `AUTH-004` |
