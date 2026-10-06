# Matriz executavel de testes - UC-ENT-02 (Termo)

Spec: `docs/03-operacao/spec-uc-ent-02-termo.md`. O termo nao tem tela propria: os casos rodam no passo de ciencia do `UC-ENT-01`. Cobertos por `FornecimentoPolicyTest`, `FornecimentoManagementServiceIntegrationTest`, `FornecimentoRollbackTest` e `FornecimentoUxTest`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| ENT02-001 | INT | CA-01 | Ficha pronta e aceite marcado | Confirmar | Termo `TERMO-NR6-01`, metodo `ASSINATURA_MANUAL`, usuario operador |
| ENT02-002 | INT | CA-02, `ENT-009` | Aceite desmarcado | Confirmar | `ENT-009`; sem ficha, sem movimento, sem termo |
| ENT02-003 | UI | CA-03 | Trabalhador "Ana Lima" | Abrir o passo do termo | O texto contem "Ana Lima" |
| ENT02-004 | INT | CA-04 | Aceite marcado; falha forcada depois do termo | Confirmar | Sem termo orfao |
| ENT02-005 | UI | Tela | Aceite desmarcado | Olhar o botao confirmar | Botao desabilitado |
