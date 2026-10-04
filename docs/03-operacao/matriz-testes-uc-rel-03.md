# Matriz executavel de testes - UC-REL-03 (Cobertura)

Spec: `docs/03-operacao/spec-uc-rel-03-cobertura.md`. A situacao do item e a do `UC-MAT-02`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| REL03-001 | INT | CA-01 | Luva Pendente e capacete Vigente | Abrir a unidade | Resumo "1 pendente"; detalhe com as duas frases |
| REL03-002 | INT | CA-02 | So um item Posto, sem fornecimento | Abrir | Resumo "OK"; Posto no detalhe |
| REL03-003 | INT | CA-03 | Funcao sem matriz | Abrir | "Sem matriz" |
| REL03-004 | INT | CA-04 | Unico item individual Sem prazo | Abrir | Resumo "Sem prazo" |
| REL03-005 | INT | CA-05 | Trabalhador inativo com item individual | Abrir | Ausente da lista |
| REL03-006 | INT | CA-06 | Mesma pessoa e o mesmo relogio do wizard | Comparar com o painel do fornecimento | A frase do item e a mesma |
| REL03-007 | INT | CA-07 | Consulta com um pendente | Gerar PDF | PDF com o resumo e o item |
