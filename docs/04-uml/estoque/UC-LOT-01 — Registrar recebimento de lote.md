### UC-LOT-01 — Registrar recebimento de lote
- **Atores**: Almoxarife
- **Descricao**: Registra entrada de lote com dados de validade, quantidade e custo.
- **Pre-condicoes**: EPI e CA cadastrados.
- **Gatilho**: Recebimento de material.
- **Fluxo principal**:
  1. Almoxarife seleciona unidade e EPI.
  2. Informa lote, validade da peca, quantidade e custo unitario.
  3. Confirma recebimento.
- **Fluxos alternativos/excecoes**:
  - Quantidade menor ou igual a zero: sistema recusa.
  - Lote duplicado para EPI/unidade: sistema recusa.
- **Pos-condicoes**: Lote criado com saldo inicial disponivel.
- **Regras relacionadas**: saldo inicial positivo; unicidade de lote por escopo.
