# Matriz executavel de testes - UC-ENT-02 (Termo)

Spec: `docs/03-operacao/spec-uc-ent-02-termo.md`. O termo nao tem tela propria: os casos rodam no passo de ciencia do `UC-ENT-01`. Cobertos por `FornecimentoPolicyTest`, `FornecimentoManagementServiceIntegrationTest`, `FornecimentoRollbackTest` e `FornecimentoUxTest`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| ENT02-001 | INT | CA-01 | Ficha pronta; senha de recebimento certa | Confirmar | Termo `TERMO-NR6-01`, metodo `SENHA`, usuario operador; senha ausente do termo |
| ENT02-002 | INT | CA-02, `ENT-015` | Senha em branco ou diferente do hash | Confirmar | `ENT-015`; sem ficha, sem movimento, sem termo |
| ENT02-003 | UI | CA-03 | Trabalhador "Ana Lima" | Abrir o passo do termo | O texto contem "Ana Lima" |
| ENT02-004 | INT | CA-04 | Senha certa; falha forcada depois do termo | Confirmar | Sem termo orfao |
| ENT02-005 | UI | Tela | Campo de senha vazio | Olhar Proximo e Confirmar fornecimento | Os dois desabilitados; nao ha caixa de assinatura no balcao |
| ENT02-006 | INT | CA-05, `ENT-014` | Trabalhador sem hash | Confirmar | `ENT-014`; sem ficha, sem movimento, sem termo |
