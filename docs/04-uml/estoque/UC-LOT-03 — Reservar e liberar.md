### UC-LOT-03 — Reservar e liberar

- **Atores**: Almoxarife, Admin
- **Descricao**: Separa quantidade vigente de um lote e devolve a reserva se o atendimento nao acontece.
- **Pre-condicoes**: Lote vigente com disponivel positivo.
- **Gatilho**: Separacao para um atendimento.
- **Fluxo principal**:
  1. Operador informa quantidade e, se quiser, uma referencia.
  2. Sistema grava `RESERVA` dentro do disponivel.
  3. Liberar, no todo ou em parte, grava `LIBERACAO_RESERVA` depois da confirmacao.
- **Fluxos alternativos/excecoes**:
  - `LOT-008` Acima do disponivel.
  - `LOT-009` Lote vencido.
  - `LOT-010` Liberacao acima do restante.
- **Pos-condicoes**: A compra ve essa quantidade como ja coberta. Nao ha fornecimento.
- **Regras relacionadas**: `UC-SOL-01` nao cria esta reserva. A entrega futura pode consumi-la.
- **Spec**: `docs/03-operacao/spec-uc-lot-03-reserva.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-lot-03.md`
- **Status**: especificado; codigo apos o recebimento.
