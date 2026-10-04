### UC-LOT-01 — Registrar recebimento de lote

- **Atores**: Almoxarife, Admin
- **Descricao**: Registra a entrada fisica do lote e o primeiro movimento do diario de estoque.
- **Pre-condicoes**: Unidade ativa. EPI com CA ativo.
- **Gatilho**: Recebimento de material.
- **Fluxo principal**:
  1. Operador escolhe unidade, EPI e o CA impresso na peca.
  2. Informa codigo do lote, fabricante, tamanho, validade, quantidade e, se souber, o custo unitario.
  3. Confirma. Peca ja vencida pede confirmacao extra.
  4. Sistema grava `RECEBIMENTO`. Reservada nasce zero.
- **Fluxos alternativos/excecoes**:
  - `LOT-001` Obrigatorios ausentes.
  - `LOT-002` Quantidade invalida.
  - `LOT-003` Lote duplicado na unidade.
  - `LOT-004` EPI sem CA ativo.
  - `LOT-005` Unidade invalida ou inativa.
  - `LOT-007` Custo negativo.
- **Pos-condicoes**: Lote identificavel para consulta, reserva, baixa e fornecimento.
- **Regras relacionadas**: NR-6 6.5.1 (a), 6.9.2.1, 6.9.2.1.1, 6.9.3. Diario em `modelo-diario-estoque.md`.
- **Fora deste caso**: reserva, inventario, compra, budget, nota fiscal.
- **Spec**: `docs/03-operacao/spec-uc-lot-01-recebimento.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-lot-01.md`
- **Status**: implementado. Tela `LotManagementView`. Testes na matriz.
