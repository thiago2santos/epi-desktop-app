# Matriz de testes — UC-CAD-IMP-01 (stub)

Status: **nao executavel** — preencher apos fechamento do DoR em `spec-uc-cad-imp-01-importacao-csv-cadastros.md`.

## Cenarios minimos previstos

| ID | Tipo | Pre-condicao | Passos (resumo) | Resultado esperado |
|----|------|--------------|-----------------|-------------------|
| T-CAD-IMP-01 | integration | Layout setores valido | Upload → staging → confirmar | Setores criados; auditoria OK |
| T-CAD-IMP-02 | integration | Trabalhador referencia setor inexistente | Upload → linha amarela → link setor → cadastrar → revalidar → importar | Trabalhador persistido |
| T-CAD-IMP-03 | ui | Arquivo misto valido/invalido | Filtrar “com pendencia” / “validas” | Contadores e linhas coerentes |
| T-CAD-IMP-04 | unit | Matricula duplicada no arquivo | Validar staging | `CAD-IMP-005` na celula |
| T-CAD-IMP-05 | rbac | Perfil Consulta | Tentar importar | `CAD-IMP-006` |
| T-CAD-IMP-06 | integration | Falha simulada na persistencia | Confirmar import | Rollback; `CAD-IMP-004` |

## Evidencias

- A definir junto com layouts CSV e criterios `CA-CAD-IMP-*`.
