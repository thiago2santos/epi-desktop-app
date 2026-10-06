# Matriz executavel de testes - UC-MAT-02 (Periodicidade)

Spec: `docs/03-operacao/spec-uc-mat-02-periodicidade.md`. Cobertos por `PeriodicidadePolicyTest`, `PeriodicidadeManagementServiceIntegrationTest` e `PeriodicidadeUxTest`. A formula da regra 6 usa a data informada; a ficha real chega no `UC-ENT-01`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| MAT02-001 | INT/AUDIT | CA-01 | EPI em uma funcao da matriz | Salvar 180 e aviso 15 | Gravado; `PERIODICIDADE_DEFINIDA` |
| MAT02-002 | INT | CA-02, `MAT-006` | Linha visivel | Aviso 180 e prazo 180 | `MAT-006`; valor anterior intacto |
| MAT02-003 | INT | `MAT-005` | Linha visivel | Dias 0 | `MAT-005` |
| MAT02-004 | UI | CA-03 | EPI fora da matriz | Abrir | EPI ausente da grade |
| MAT02-005 | INT | CA-04 | EPI na matriz sem periodicidade | Consultar cobertura | "Sem prazo" |
| MAT02-006 | INT | CA-05 | Prazo 180, aviso 15, fornecimento que conta ha 10 dias | Consultar | "Vigente" |
| MAT02-007 | INT | CA-05 | Mesmo prazo, fornecimento ha 170 dias | Consultar | "Troca em 10 dias" |
| MAT02-008 | INT | CA-05 | Fornecimento ha 180 dias | Consultar | "Prazo vencido" |
| MAT02-009 | INT | CA-05 | Sem fornecimento | Consultar | "Pendente" |
| MAT02-010 | INT | CA-06 | Linha modo Posto, sem fornecimento | Consultar | "Posto" |
| MAT02-011 | INT | CA-07 | Ficha com data D e prazo 180; depois prazo 90 | Consultar | Data da ficha segue D; situacao usa 90 |
| MAT02-012 | RBAC | Regra 8 | `ALMOXARIFE` | Salvar | `AUTH-004` |
| MAT02-013 | UI | Tela | Nenhuma linha de matriz | Abrir | "Nenhum EPI na matriz. Inclua o EPI na matriz do perfil primeiro." |

## Gate para iniciar o codigo

`MAT02-001`, `MAT02-002`, `MAT02-005`, `MAT02-006`, `MAT02-010`. Cobertura com fornecimento real (`MAT02-006` em diante no servico) espera o `UC-ENT-01`; ate la, o calculo puro da regra 6 cobre a formula.
