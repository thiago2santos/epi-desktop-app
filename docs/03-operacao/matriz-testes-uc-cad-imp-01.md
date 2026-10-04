# Matriz executavel de testes - UC-CAD-IMP-01 (Importar cadastros CSV)

Spec: `docs/03-operacao/spec-uc-cad-imp-01-importacao-csv-cadastros.md`. Layout: `docs/03-operacao/layouts-csv-cadastros.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| IMP01-001 | INT/AUDIT | CA-CAD-IMP-01 | Unidade ativa; `setores.csv` com dois nomes novos | Revisar e confirmar | Dois setores; auditoria com contagem 2 |
| IMP01-002 | INT | CA-CAD-IMP-02 | `trabalhadores.csv` com setor inexistente | Abrir a revisao e o vinculo | Linha "Com pendencia"; o cadastro de setor abre com o nome |
| IMP01-003 | INT | CA-CAD-IMP-02 | Setor criado; operador clica Revalidar | Revalidar e importar a linha pronta | Trabalhador gravado igual ao cadastro manual |
| IMP01-004 | UI | CA-CAD-IMP-03 | Arquivo misto | Filtrar Com pendencia | So as amarelas; contadores do total seguem o arquivo inteiro |
| IMP01-005 | INT | `CAD-IMP-005` | Matricula repetida no arquivo | Validar | As duas linhas com pendencia; nenhuma grava antes do commit |
| IMP01-006 | INT | Decisao 5 | Matricula ja na base | Validar | Pendencia; o trabalhador existente nao muda |
| IMP01-007 | UI | `CAD-IMP-003` | Nenhuma linha pronta | Olhar o botao | Botao desabilitado |
| IMP01-008 | INT | `CAD-IMP-004` | Falha forcada no commit | Confirmar | Nenhum setor novo; mensagem de rollback |
| IMP01-009 | RBAC | `CAD-IMP-006` | `CONSULTA` | Abrir a importacao | `CAD-IMP-006` |
| IMP01-010 | UI | Decisao 7 | Voltou do cadastro de setor | Nao clicar Revalidar | A linha segue com pendencia |
