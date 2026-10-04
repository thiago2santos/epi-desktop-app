# Spec curta - UC-LOT-06 (Necessidade de compra)

## Identificacao

- ID: `UC-LOT-06`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-EST`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois de saldo e reserva existirem
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - comprar olhando so a fisica trata peca reservada como livre e peca vencida como estoque.
- Atores: `SESMT` e `Admin` informam a demanda e leem a conta. `Almoxarife` e `Consulta` leem.
- Impacto se nao resolver:
  - a reposicao erra para cima ou para baixo antes de existir cotacao.

## 2) Escopo

- Comportamento no escopo:
  - para cada EPI, tamanho e unidade, mostrar fisica, reservada, disponivel vigente e a demanda que o operador informa para o periodo;
  - calcular a comprar = max(0, demanda - disponivel vigente);
  - sugerir valor = a comprar vezes o ultimo custo unitario informado de recebimento daquele EPI na unidade;
  - marcar "custo incompleto" quando nao houver custo informado.
- Fora de escopo:
  - ordem de compra, cotacao, fornecedor e aprovacao financeira;
  - previsao automatica pela matriz ou pelo `UC-SOL-01` (quando esses existirem, a demanda pode passar a vir deles; a formula nao muda);
  - o relatorio de quanto ja foi consumido (`UC-REL-04`).

## 3) Regras de negocio

1. Disponivel vigente ignora lote vencido e desconta a reserva. A demanda nao soma a reserva de novo.
2. Demanda e inteira e maior ou igual a zero, por linha. Linha sem demanda nao entra na conta e a tela pede o numero se o operador calcular o recorte.
3. A comprar nunca e negativa. Estoque sobrando aparece como sobra, nao como compra negativa.
4. Ultimo custo e o custo unitario do recebimento mais recente daquele EPI e unidade em que o custo foi informado. Custo zero conta como informado.
5. Esta tela nao grava movimento de estoque.
6. Auditoria nao registra a consulta. Se no futuro a demanda digitada for salva como plano, o evento entra numa iteracao propria. Nesta spec a demanda e da sessao de calculo.

## 4) Catalogo de erros

`LOT-002` se a demanda for negativa ou fracionaria. `LOT-016` se o operador pedir o calculo do recorte sem demanda nas linhas visiveis. `AUTH-004` para quem nao ve Estoque.

## 5) Criterios de aceite

- `CA-01`: fisico 10, reservada 4, disponivel 6, demanda 10, a comprar 4.
- `CA-02`: lote vencido com fisica 10 nao entra no disponivel. Demanda 3, a comprar 3.
- `CA-03`: disponivel 8 e demanda 5 mostram a comprar 0 e sobra 3.
- `CA-04`: sem custo informado, a quantidade a comprar aparece e o valor fica "custo incompleto".
- `CA-05`: a tela nao cria recebimento, reserva nem baixa.
- `CA-06`: `Consulta` ve o resultado e nao edita a demanda. `SESMT` edita a demanda.

## 6) Tela

Destino no grupo Estoque: "Necessidade de compra".

- Filtro por unidade.
- Colunas: EPI, tamanho, Fisica, Reservada, Disponivel, Demanda, A comprar, Valor sugerido.
- Demanda editavel para `SESMT` e `Admin`.
- Vazio: "Nenhum lote nesta unidade para comparar com a demanda."
- Texto fixo no topo: "O que ja esta reservado nao entra de novo na compra. Peca vencida nao conta como disponivel."
- Sucesso do calculo e a propria grade, sem alerta.

## 7) Seguranca e auditoria

- Nao grava estoque.
- Risco: usar a fisica no lugar do disponivel e comprar peca que ja esta separada.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-lot-06.md`
