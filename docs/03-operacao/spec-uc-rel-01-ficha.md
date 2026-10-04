# Spec curta - UC-REL-01 (Ficha por trabalhador e periodo)

## Identificacao

- ID: `UC-REL-01`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-REL`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois do `UC-ENT-01`
- Norma: NR-6 `6.5.1.1` (o registro eletronico precisa sair em relatorio)
- Exportacao: `docs/03-operacao/spec-uc-trv-03-pdf.md`

## 1) Contexto

- Problema real:
  - a tela de historico mostra o periodo. A fiscalizacao pede o mesmo recorte em arquivo.
- Atores: `SESMT`, `Consulta`, `Admin`. `Almoxarife` consulta o historico na operacao e nao emite esta ficha.
- Impacto se nao resolver:
  - o item `6.5.1.1` fica so na tela.

## 2) Escopo

- Comportamento no escopo:
  - montar a ficha de um trabalhador num periodo;
  - incluir fornecimento, devolucao, estorno e o termo aceito em cada fornecimento;
  - gravar o PDF pelo `UC-TRV-03`.
- Fora de escopo:
  - pedido de solicitacao;
  - custo e budget;
  - alterar a ficha legal;
  - carimbo ou juntar PDFs (`PDFBox` fica para depois).

## 3) Regras de negocio

1. Um trabalhador, localizado por matricula ou nome, ativo ou inativo. O cabecalho usa o snapshot de cada fato: nome, matricula, unidade, setor e funcao gravados na epoca. A empresa e a razao social e o CNPJ da unidade daquele snapshot.
2. O periodo e obrigatorio. Data final anterior a inicial recusa na tela: "A data final precisa ser igual ou posterior a inicial."
3. Entram os mesmos fatos do `UC-ENT-03`: fornecimento, devolucao e estorno. Pedido nao entra.
4. Cada fornecimento lista data e hora, EPI, CA, lote, quantidade, motivo, situacao (Vigente, Estornado, Devolvido) e o metodo de ciencia. O texto do termo e a versao `TERMO-NR6-01` aparecem uma vez por ficha de fornecimento, nao repetidos em cada linha se varios itens compartilham o mesmo termo.
5. Devolucao e estorno aparecem como linhas proprias, com data e motivo da devolucao. O motivo interno do estorno entra na ficha: e correcao da prova. Nao entra custo.
6. Ordem: data crescente, para leitura de processo.
7. Periodo sem fato gera o PDF mesmo assim, com o cabecalho e a frase "Nenhum fornecimento no periodo."
8. Dois PDFs do mesmo recorte, sem fato novo no meio, tem as mesmas linhas. O rodape traz o instante da geracao e o nome do operador, para distinguir as copias.
9. `Almoxarife` e `Gestor` recebem `AUTH-004`.

## 4) Catalogo de erros

Periodo invalido usa a frase da regra 2, sem codigo novo. Trabalhador ausente: "Trabalhador nao encontrado." `AUTH-004`: "Voce nao tem permissao para emitir esta ficha." Falha do arquivo segue o `UC-TRV-03`.

## 5) Criterios de aceite

- `CA-01`: periodo com fornecimento, devolucao e estorno gera PDF com os tres fatos, CA, lote e termo.
- `CA-02`: pedido em aberto nao aparece no PDF.
- `CA-03`: periodo vazio gera PDF com a frase de vazio.
- `CA-04`: a funcao impressa e a da ficha, mesmo que o trabalhador tenha mudado de funcao depois.
- `CA-05`: `ALMOXARIFE` recebe `AUTH-004` e nenhum arquivo nasce.
- `CA-06`: repetir o recorte sem fato novo repete as linhas.

## 6) Tela

Destino no grupo Relatorios: "Ficha do trabalhador". Titulo sem codigo de caso de uso.

- Busca, periodo e o cartao do trabalhador antes de gerar.
- Um botao primario: "Gerar PDF".
- A previa na tela e a lista do `UC-ENT-03` no mesmo recorte, para o operador ver o que vai sair.
- Vazio de busca: "Nenhum trabalhador com essa matricula ou nome."
- Geracao fora da thread da janela, com a fase "Gerando a ficha...".

## 7) Seguranca e auditoria

- A emissao audita pelo `UC-TRV-03`: `RELATORIO_EXPORTADO`, entidade `RELATORIO`, com o identificador `FICHA_TRABALHADOR`, o trabalhador e o periodo.
- Risco: imprimir o organograma de hoje no lugar do snapshot e a prova mudar de funcao.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-ent-03-historico.md`
- `docs/03-operacao/spec-uc-trv-03-pdf.md`
- `docs/03-operacao/matriz-testes-uc-rel-01.md`
- `docs/04-uml/casos-de-uso-uml.md`
