# Spec curta - UC-ENT-03 (Historico por trabalhador)

## Identificacao

- ID: `UC-ENT-03`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois do `UC-ENT-01`
- Norma: NR-6 `6.5.1.1` (a consulta e esta tela; o arquivo e o `UC-REL-01`)

## 1) Contexto

- Problema real:
  - o balcao e a fiscalizacao precisam ver o que o trabalhador recebeu, devolveu e o que foi estornado, sem misturar com pedido.
- Ator principal: `SESMT`. Tambem `Almoxarife`, `Consulta` e `Admin`.
- Impacto se nao resolver:
  - a ficha existe e ninguem consegue mostra-la.

## 2) Escopo

- Comportamento no escopo:
  - localizar um trabalhador por matricula ou nome;
  - listar, num periodo, os itens de fornecimento, as devolucoes e os estornos;
  - mostrar data, EPI, CA, lote, quantidade, motivo e a situacao em texto.
- Fora de escopo:
  - pedido de solicitacao (o gestor ve o resumo dele no `UC-SOL-01`);
  - custo e budget (`UC-REL-04`);
  - exportar PDF (e o `UC-REL-01`, pelo `UC-TRV-03`);
  - editar a ficha.

## 3) Regras de negocio

1. A busca acha trabalhador ativo ou inativo. O cartao mostra nome, matricula, setor, funcao e se esta ativo.
2. O periodo e obrigatorio. Data final anterior a inicial e recusada na propria tela, sem codigo `ENT-`.
3. Cada linha e um fato: Fornecimento, Devolucao ou Estorno. Pedido nao entra.
4. Fornecimento estornado continua na lista, com a situacao "Estornado", e o estorno aparece em outra linha. Devolucao aparece como "Devolvido" no item e como linha propria.
5. Campos: data, EPI, CA, lote, quantidade, motivo, situacao. Sem custo, sem texto livre do estorno nesta lista (o motivo do estorno fica na tela de estorno e na auditoria).
6. Ordem: data mais recente primeiro.
7. Periodo sem fato: lista vazia e a frase "Nenhum fornecimento no periodo."
8. `Consulta` so le. `Almoxarife` pode abrir a devolucao de um item que ainda conta. `Gestor` nao abre esta tela.
9. Abrir a lista nao gera auditoria. O dado pessoal fica restrito a quem ja consulta fornecimento.

## 4) Criterios de aceite

- `CA-01`: periodo com um fornecimento, uma devolucao e um estorno mostra tres linhas, situacoes distintas.
- `CA-02`: pedido em aberto do mesmo trabalhador nao aparece.
- `CA-03`: periodo vazio mostra a frase de vazio, sem erro.
- `CA-04`: data final anterior a inicial nao consulta.
- `CA-05`: `CONSULTA` ve a lista e nao ve acao de devolver ou estornar.
- `CA-06`: item estornado permanece visivel como "Estornado".

## 5) Tela

Destino ja previsto no grupo Operacao, ao lado do fornecimento: "Historico". Titulo sem codigo de caso de uso.

- Busca, periodo e o cartao do trabalhador.
- Grade com as colunas da regra 5. Situacao em texto.
- Vazio de busca: "Nenhum trabalhador com essa matricula ou nome."
- Sucesso da busca e a propria grade. Sem `Alert`.

## 6) Seguranca e auditoria

- Login/senha: nao.
- Evento: nenhum na consulta.
- Risco: misturar pedido com fornecimento e a ficha legal mentir o que foi entregue.

## 7) Documentacao impactada

- `docs/03-operacao/matriz-testes-uc-ent-03.md`
- `docs/03-operacao/spec-uc-pos-01-devolucao.md`
- `docs/03-operacao/spec-uc-pos-02-estorno.md`
- `docs/04-uml/entrega/UC-ENT-03 — Consultar historico.md`
