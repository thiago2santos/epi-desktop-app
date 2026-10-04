# Matriz executavel de testes - UC-REL-01 (Ficha)

Spec: `docs/03-operacao/spec-uc-rel-01-ficha.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| REL01-001 | INT | CA-01 | Fornecimento, devolucao e estorno no periodo | Gerar PDF | Os tres fatos, CA, lote e termo |
| REL01-002 | INT | CA-02 | Pedido em aberto | Gerar PDF | Pedido ausente |
| REL01-003 | INT | CA-03 | Periodo sem fato | Gerar PDF | Cabecalho e "Nenhum fornecimento no periodo." |
| REL01-004 | INT | CA-04 | Ficha com funcao Operador; trabalhador hoje e Analista | Gerar PDF | Funcao impressa Operador |
| REL01-005 | RBAC | CA-05 | `ALMOXARIFE` | Gerar | `AUTH-004`; sem arquivo |
| REL01-006 | INT | CA-06 | Mesmo recorte duas vezes, sem fato novo | Gerar de novo | Mesmas linhas; rodape com outro instante |
