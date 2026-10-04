# Matriz executavel de testes - UC-REL-02 (Historico por EPI)

Spec: `docs/03-operacao/spec-uc-rel-02-historico-epi.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| REL02-001 | INT | CA-01 | Recebimento 10 e fornecimento 2 do lote | Filtrar o lote | Duas linhas; trabalhador so no fornecimento |
| REL02-002 | INT | CA-02 | Fornecimento com o CA do lote | Filtrar o numero do CA | O fornecimento aparece |
| REL02-003 | INT | CA-03 | Item estornado | Filtrar o lote | Fornecimento e estorno |
| REL02-004 | INT | CA-04 | Reserva no lote | Filtrar o lote | Reserva ausente |
| REL02-005 | INT | CA-05 | Baixa de prateleira | Filtrar o lote | Tipo "Baixa de prateleira" |
| REL02-006 | UI | CA-06 | Periodo preenchido, sem EPI, CA ou lote | Consultar | Nao busca; frase pedindo um dos tres |
| REL02-007 | INT | CA-07 | Consulta com duas linhas | Gerar PDF | O PDF tem as duas linhas |
