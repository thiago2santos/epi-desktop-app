# Spec curta - UC-REL-02 (Historico por EPI, CA e lote)

## Identificacao

- ID: `UC-REL-02`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-REL`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois do `UC-ENT-01` e do `UC-LOT-01`
- Exportacao: `docs/03-operacao/spec-uc-trv-03-pdf.md`

## 1) Contexto

- Problema real:
  - uma fiscalizacao ou um defeito de lote pergunta quem recebeu aquela peca, nao o contrario.
- Atores: `SESMT`, `Consulta`, `Admin`, `Almoxarife`.
- Impacto se nao resolver:
  - o lote existe e ninguem ve para onde a quantidade foi.

## 2) Escopo

- Comportamento no escopo:
  - listar, num periodo, os fatos de um EPI, de um CA ou de um lote;
  - incluir recebimento, fornecimento, devolucao, estorno, baixa de prateleira e ajuste de inventario;
  - oferecer o PDF do mesmo recorte pelo `UC-TRV-03`.
- Fora de escopo:
  - reserva (nao e saida da peca para o trabalhador nem perda);
  - pedido;
  - custo.

## 3) Regras de negocio

1. O operador escolhe ao menos um filtro dentre EPI, numero de CA ou codigo de lote, mais o periodo. Sem nenhum dos tres, a busca nao dispara: "Informe o EPI, o CA ou o lote."
2. Periodo invalido usa a mesma frase da ficha: "A data final precisa ser igual ou posterior a inicial."
3. Cada linha: data, tipo em texto (Recebimento, Fornecimento, Devolucao, Estorno, Baixa de prateleira, Ajuste de inventario), quantidade, lote, CA, e o trabalhador quando o fato tiver um. Unidade do fato vem do snapshot.
4. Fornecimento estornado continua na lista, com o estorno em outra linha.
5. Ordem: data mais recente primeiro.
6. Filtro sem linha: "Nenhum movimento no periodo." Nao e erro.
7. O PDF repete essas colunas, o filtro usado, o instante e o operador.
8. `Gestor` nao abre esta tela.

## 4) Catalogo de erros

Sem codigo `REL-` novo. As frases das regras 1 e 2. `AUTH-004`: "Voce nao tem permissao para consultar este historico."

## 5) Criterios de aceite

- `CA-01`: lote com recebimento de 10 e fornecimento de 2 lista as duas linhas, com o trabalhador so na segunda.
- `CA-02`: filtrar pelo CA do lote acha o fornecimento que gravou esse CA.
- `CA-03`: estorno aparece alem do fornecimento original.
- `CA-04`: reserva nao aparece.
- `CA-05`: baixa de prateleira aparece com o tipo "Baixa de prateleira".
- `CA-06`: filtro vazio de EPI, CA e lote nao consulta.
- `CA-07`: o PDF do recorte tem as mesmas linhas da tela.

## 6) Tela

Destino no grupo Relatorios: "Historico por EPI". Titulo sem codigo de caso de uso.

- Filtros abertos: periodo, EPI, CA, lote. Unidade a um clique.
- Grade com as colunas da regra 3. Tipo em texto.
- Botao "Gerar PDF" habilitado depois de uma consulta, inclusive a vazia.
- A geracao segue o `UC-TRV-03`.

## 7) Seguranca e auditoria

- Consulta nao audita. A exportacao audita `RELATORIO_EXPORTADO` com identificador `HISTORICO_EPI`.
- Risco: omitir o estorno e o lote parecer inteiro na mao dos trabalhadores.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/spec-uc-trv-03-pdf.md`
- `docs/03-operacao/matriz-testes-uc-rel-02.md`
- `docs/04-uml/casos-de-uso-uml.md`
