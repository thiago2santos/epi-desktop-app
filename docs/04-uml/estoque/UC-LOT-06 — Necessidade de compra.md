### UC-LOT-06 — Necessidade de compra

- **Atores**: SESMT e Admin informam a demanda; Almoxarife e Consulta leem
- **Descricao**: Calcula quanto comprar descontando o disponivel vigente, que ja exclui reserva e peca vencida.
- **Pre-condicoes**: Saldos do diario. Demanda do periodo informada na tela.
- **Gatilho**: Planejar reposicao.
- **Fluxo principal**:
  1. Operador filtra a unidade e informa a demanda por EPI e tamanho.
  2. Sistema calcula a comprar = max(0, demanda - disponivel vigente).
  3. Sugere o valor pelo ultimo custo informado. Sem custo, marca incompleto.
- **Fluxos alternativos/excecoes**:
  - Demanda menor que o disponivel: a comprar zero e sobra visivel.
  - `LOT-016` Calculo sem demanda.
- **Pos-condicoes**: Nenhuma movimentacao de estoque.
- **Fora deste caso**: ordem de compra, cotacao e o consumo historico (`UC-REL-04`).
- **Spec**: `docs/03-operacao/spec-uc-lot-06-necessidade-compra.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-lot-06.md`
- **Status**: especificado; codigo apos saldo e reserva.
