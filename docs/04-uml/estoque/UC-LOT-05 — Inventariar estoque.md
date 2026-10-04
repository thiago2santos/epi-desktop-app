### UC-LOT-05 — Inventariar estoque

- **Atores**: Almoxarife conta; Admin confirma; SESMT e Consulta leem o encerrado
- **Descricao**: Compara a contagem da unidade com o fisico do diario e grava so a diferenca.
- **Pre-condicoes**: Unidade sem outro inventario aberto.
- **Gatilho**: Contagem da prateleira.
- **Fluxo principal**:
  1. Operador abre o inventario da unidade.
  2. Informa a quantidade contada de cada lote.
  3. Confirma o resumo.
  4. Sistema grava `AJUSTE_INVENTARIO` onde houve diferenca.
- **Fluxos alternativos/excecoes**:
  - `LOT-013` Linha sem contagem.
  - `LOT-014` Contagem abaixo do reservado.
  - `LOT-015` Inventario ja aberto ou ja encerrado.
- **Pos-condicoes**: Fisico reconciliado. Recebimento original intacto. Ajuste fora do consumo.
- **Spec**: `docs/03-operacao/spec-uc-lot-05-inventario.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-lot-05.md`
- **Status**: especificado; feature propria.
