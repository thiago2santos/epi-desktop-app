# Matriz executavel de testes - UC-MAT-01 (Matriz funcao x EPI)

Spec: `docs/03-operacao/spec-uc-mat-01-matriz.md`. Nenhum teste existe em codigo ainda.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| MAT01-001 | INT/AUDIT | CA-01 | SESMT; funcao ativa; EPI com CA ativo | Incluir o EPI | Linha ativa, modo Individual, treinamento nao; `MATRIZ_INCLUIDA` |
| MAT01-002 | INT | CA-02, `MAT-002` | Linha ativa do par | Incluir de novo | `MAT-002`; uma linha so |
| MAT01-003 | INT | CA-03, `MAT-003` | EPI sem CA ativo | Incluir | `MAT-003` |
| MAT01-004 | INT | `MAT-004` | Funcao inativa | Incluir | `MAT-004` |
| MAT01-005 | INT/AUDIT | CA-04 | Linha ativa | Marcar Posto e treinamento | Mesma linha; `MATRIZ_ALTERADA` |
| MAT01-006 | UI | CA-05 | Linha ativa | Cancelar a inativacao | Linha segue ativa |
| MAT01-007 | INT/AUDIT | CA-05, CA-06 | Linha ativa | Confirmar inativacao e incluir de novo | Linha antiga inativa; linha nova ativa; `MATRIZ_INATIVADA` |
| MAT01-008 | RBAC | CA-07 | `ALMOXARIFE` | Incluir | `AUTH-004` |
| MAT01-009 | UI | Tela | Funcao sem linhas | Abrir | "Nenhum EPI na matriz desta funcao." |

## Gate para iniciar o codigo

`MAT01-001`, `MAT01-002`, `MAT01-003`, `MAT01-007`, `MAT01-008`.
