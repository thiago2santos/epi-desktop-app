# Spec curta - UC-POS-03 (Pendencias de devolucao)

## Identificacao

- ID: `UC-POS-03`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: implementado na tela Pendencias (`PendenciasView`). A consulta nao grava auditoria. Item sem linha de matriz entra como individual. Posto, estorno, devolucao e trabalhador ativo ficam de fora.

## 1) Contexto

- Problema real:
  - no desligamento, a peca individual que ainda esta com o trabalhador precisa aparecer para o almoxarife recolher.
- Ator principal: `Almoxarife`. Tambem `SESMT` e `Admin`.
- Impacto se nao resolver:
  - o desligamento nao gera lista nenhuma.

## 2) Escopo

- Comportamento no escopo:
  - listar itens individuais de trabalhador inativo que ainda contam;
  - abrir o registro de devolucao da linha.
- Fora de escopo:
  - prazo vencido da periodicidade (isso e cobertura, `UC-MAT-02`);
  - item em modo Posto;
  - trabalhador ativo.

## 3) Regras de negocio

1. Entra na lista o item de fornecimento que conta, de linha de matriz `INDIVIDUAL`, cujo trabalhador esta inativo.
2. Item de linha `POSTO`, item estornado e item ja devolvido ficam de fora.
3. Fornecimento feito sem linha de matriz (excecao) trata-se como individual: desligamento tambem o lista.
4. Filtros: unidade, periodo do fornecimento, trabalhador. Filtro sem resultado e lista vazia, sem erro.
5. A lista nao grava e nao gera auditoria.
6. A acao "Registrar devolucao" abre o `UC-POS-01` com o item ja escolhido.
7. `Consulta` e `Gestor` nao abrem esta tela.

## 4) Criterios de aceite

- `CA-01`: trabalhador inativo com luva individual nao devolvida aparece.
- `CA-02`: creme em modo Posto do mesmo trabalhador nao aparece.
- `CA-03`: depois da devolucao, a linha sai da lista.
- `CA-04`: item estornado nao aparece.
- `CA-05`: trabalhador ativo nao aparece, mesmo com item individual.
- `CA-06`: lista vazia mostra "Nenhuma pendencia de devolucao."

## 5) Tela

Destino ja previsto: "Pendencias". Titulo sem codigo de caso de uso.

- Filtros no alto. Grade: trabalhador, matricula, EPI, data do fornecimento, quantidade.
- Uma acao primaria por linha: "Registrar devolucao".
- Vazio com a frase do `CA-06`, e sem botao de criar pendencia. A pendencia nasce do desligamento, nao de um formulario.

## 6) Seguranca e auditoria

- Login/senha: nao.
- Evento: nenhum na consulta.
- Risco: cobrar devolucao de descartavel de posto, ou esquecer a peca de quem saiu.

## 7) Documentacao impactada

- `docs/03-operacao/spec-uc-pos-01-devolucao.md`
- `docs/03-operacao/matriz-testes-uc-pos-03.md`
- `docs/04-uml/entrega/UC-POS-03 — Consultar pendencias.md`
