### UC-LOT-04 — Baixa de prateleira

- **Atores**: Almoxarife, Admin
- **Descricao**: Tira da fisica o que venceu, se perdeu ou foi descartado sem fornecimento a trabalhador.
- **Pre-condicoes**: Lote com fisica maior que a reservada.
- **Gatilho**: Vencimento, perda ou descarte na prateleira.
- **Fluxo principal**:
  1. Operador escolhe motivo e quantidade.
  2. Confirma.
  3. Sistema grava `BAIXA_PRATELEIRA`.
- **Fluxos alternativos/excecoes**:
  - `LOT-011` Quantidade entra na reserva.
  - `LOT-012` Motivo ausente.
  - Cancelar a confirmacao nao grava.
- **Pos-condicoes**: A perda aparece a parte do consumo no budget.
- **Regras relacionadas**: nao substitui devolucao de peca ja fornecida.
- **Spec**: `docs/03-operacao/spec-uc-lot-04-baixa-prateleira.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-lot-04.md`
- **Status**: especificado; codigo apos o recebimento.
