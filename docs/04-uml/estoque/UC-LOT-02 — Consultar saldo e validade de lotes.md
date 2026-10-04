### UC-LOT-02 — Consultar saldo e validade de lotes

- **Atores**: Almoxarife, SESMT, Consulta, Admin
- **Descricao**: Mostra fisica, reservada, disponivel e a situacao do lote.
- **Pre-condicoes**: Diario de estoque existente. Pode estar vazio.
- **Gatilho**: Preparar fornecimento, reserva ou compra.
- **Fluxo principal**:
  1. Operador filtra por EPI, codigo, tamanho ou situacao.
  2. Sistema lista validade mais proxima primeiro.
  3. Lote vencido aparece com disponivel zero.
- **Fluxos alternativos/excecoes**:
  - Filtro sem resultado: lista vazia, sem erro.
- **Pos-condicoes**: Operador distingue peca livre, reservada, vencida e esgotada.
- **Regras relacionadas**: leitura derivada dos movimentos; situacao em texto.
- **Spec**: `docs/03-operacao/spec-uc-lot-02-consulta-saldo.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-lot-02.md`
- **Status**: especificado; implementacao pendente.
