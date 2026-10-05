# Spec curta - UC-LOT-01 (Recebimento de lote)

## Identificacao

- ID: `UC-LOT-01`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-EST`
- Responsavel: Time Easy NR6
- Status: implementado na tela Lotes e saldos (`LotManagementView`)
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - sem o recebimento, a entrega nao tem peca, CA da compra nem validade para baixar.
- Ator principal: `Almoxarife`. Tambem `Admin`.
- Impacto se nao resolver:
  - a ficha de fornecimento mente o lote;
  - o budget nao tem custo de origem.

## 2) Escopo

- Comportamento no escopo:
  - registrar a entrada de um lote fisico numa unidade;
  - gravar o movimento `RECEBIMENTO`;
  - fisica igual a quantidade recebida, reservada zero, disponivel igual a fisica se a peca estiver no prazo;
  - guardar codigo do lote de fabricacao, fabricante impresso na peca, tamanho, validade da peca, CA escolhido entre os vinculos ativos do EPI, data da consulta desse CA e custo unitario quando informado. O catalogo de EPI nao guarda fabricante: o mesmo tipo pode chegar de mais de um.
- Fora de escopo:
  - reserva, baixa de prateleira, inventario, compra e budget;
  - nota fiscal, cotacao e ordem de compra;
  - mais de um CA no mesmo lote fisico (o lote aponta para um vinculo `epi_ca`; CAs extras do conjugado ficam no fornecimento).

## 3) Regras de negocio

1. O lote pertence a uma unidade ativa e a um EPI que tenha ao menos um CA ativo. O operador escolhe qual CA esta impresso na peca.
2. Codigo do lote, validade e quantidade sao obrigatorios. Quantidade e inteira e maior que zero. Tamanho e opcional; vazio e um tamanho so.
3. Unicidade: mesmo codigo, mesmo EPI, mesmo tamanho e mesma unidade recusam. Outra unidade pode repetir o codigo.
4. Custo unitario, quando informado, e maior ou igual a zero. Em branco e permitido e fica nulo. Negativo recusa.
5. Peca ja vencida na data do recebimento pode entrar, com confirmacao. Disponivel nasce zero. O lote permanece para a baixa de prateleira.
6. CA vencido ou suspenso nao entra na lista. Se o vinculo ativo existir e a consulta do CA for antiga, a tela avisa e nao bloqueia. O snapshot da consulta fica no lote.
7. Nao ha edicao do recebimento. Correcao de quantidade futura e inventario ou estorno de um movimento, nunca update da linha.
8. `Almoxarife` e `Admin` recebem. `SESMT` e `Consulta` nao.
9. O evento de auditoria e `LOTE_RECEBIDO`, entidade `LOTE`.

## 4) Catalogo de erros

Usa `LOT-001`, `LOT-002`, `LOT-003`, `LOT-004`, `LOT-005`, `LOT-007` e `AUTH-004` do modelo.

## 5) Criterios de aceite

- `CA-01`: almoxarife recebe lote vigente e ve fisica, reservada zero e disponivel igual a quantidade.
- `CA-02`: CA, codigo, fabricante, validade e tamanho persistidos; custo nulo quando nao informado.
- `CA-03`: duplicidade na mesma unidade recusa com `LOT-003` e nao grava movimento.
- `CA-04`: peca vencida so grava depois da confirmacao, com disponivel zero.
- `CA-05`: cancelar a confirmacao de peca vencida nao grava.
- `CA-06`: papel sem permissao recebe `AUTH-004`.
- `CA-07`: auditoria com ator, acao `LOTE_RECEBIDO`, entidade `LOTE` e id.

## 6) Tela

Destino futuro no grupo Estoque: "Lotes e saldos", junto da consulta do `UC-LOT-02`.

- Formulario: unidade, EPI, CA (so vinculos ativos), codigo do lote, fabricante, tamanho, validade, quantidade, custo unitario.
- Ao escolher o CA, o fabricante recebe a razao social desse numero na base CAEPI e continua editavel. Sem o numero na base, o campo fica em branco.
- Salvar desabilitado ate unidade, EPI, CA, codigo, validade e quantidade.
- Lista: codigo, EPI, tamanho, validade, fisica, reservada, disponivel, situacao em texto.
- Vazio: "Nenhum lote recebido nesta unidade." e o botao "Registrar recebimento".
- Peca vencida: dialogo "Esta peca ja venceu. O lote entra sem quantidade disponivel." Confirmar / Cancelar.
- CA com consulta antiga: faixa de aviso, sem codigo de erro.
- Sucesso: "Lote recebido." Sem `Alert`.
- Erro amigavel, sem mostrar `LOT-003`.

## 7) Seguranca e auditoria

- Login/senha: nao.
- Evento: `LOTE_RECEBIDO`.
- Risco: gravar CA diferente do impresso na peca, ou tratar peca vencida como disponivel.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-lot-01.md`
- `docs/04-uml/estoque/UC-LOT-01 — Registrar recebimento de lote.md`
