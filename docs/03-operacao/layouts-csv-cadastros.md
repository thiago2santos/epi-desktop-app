# Layouts CSV de cadastro

Spec: `docs/03-operacao/spec-uc-cad-imp-01-importacao-csv-cadastros.md`.

Arquivo UTF-8, com ou sem BOM. Delimitador `;`. Cabecalho na primeira linha. Nome de coluna sem diferenciar maiusculas. Uma unidade escolhida na tela vale para o arquivo inteiro.

## setores.csv

| Coluna | Obrigatoria | Regra |
|---|---|---|
| nome | sim | Mesma regra do cadastro de setor. Nome repetido na unidade, no arquivo ou na base, e pendencia. |

## funcoes.csv

| Coluna | Obrigatoria | Regra |
|---|---|---|
| setor | sim | Nome do setor nesta unidade, comparacao sem diferenciar maiusculas, depois do trim. |
| funcao | sim | Mesma regra do cadastro de funcao. O par setor+funcao nao pode ja existir. |

## trabalhadores.csv

| Coluna | Obrigatoria | Regra |
|---|---|---|
| matricula | sim | Unica na base e no arquivo, depois do trim. |
| nome | sim | Mesma regra do cadastro de trabalhador. |
| setor | sim | Setor ativo desta unidade. |
| funcao | sim | Funcao ativa daquele setor. |
| gestor_matricula | nao | Em branco deixa sem gestor. Preenchida, precisa ser matricula de trabalhador ja ativo na base. |

Linha que falha qualquer regra fica amarela e nao entra no commit. Linha que passa fica pronta.
