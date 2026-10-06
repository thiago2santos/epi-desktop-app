# Matriz executavel de testes - UC-POS-03 (Pendencias)

Spec: `docs/03-operacao/spec-uc-pos-03-pendencias.md`. Cobertos por `PendenciaPolicyTest`, `PendenciaManagementServiceIntegrationTest` e `PendenciaUxTest`. A frase de vazio na tela e "Nenhuma pendência de devolução."

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| POS03-001 | INT | CA-01 | Trabalhador inativo; luva individual sem devolucao | Listar | A linha aparece |
| POS03-002 | INT | CA-02 | Creme em modo Posto do mesmo trabalhador | Listar | Creme ausente |
| POS03-003 | INT | CA-03 | Linha listada | Registrar a devolucao | A linha sai |
| POS03-004 | INT | CA-04 | Item estornado de trabalhador inativo | Listar | Item ausente |
| POS03-005 | INT | CA-05 | Trabalhador ativo com item individual | Listar | Trabalhador ausente |
| POS03-006 | UI | CA-06 | Nenhum desligamento pendente | Abrir | "Nenhuma pendencia de devolucao." |
