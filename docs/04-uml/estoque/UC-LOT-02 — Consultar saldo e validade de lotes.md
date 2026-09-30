### UC-LOT-02 — Consultar saldo e validade de lotes
- **Atores**: Almoxarife, SESMT, Consulta
- **Descricao**: Exibe disponibilidade de lotes para entrega.
- **Pre-condicoes**: Lotes cadastrados.
- **Gatilho**: Preparacao para entrega ou controle de estoque.
- **Fluxo principal**:
  1. Operador filtra por EPI/unidade.
  2. Sistema exibe saldo, validade e status.
- **Fluxos alternativos/excecoes**:
  - Sem lote disponivel: sistema retorna lista vazia.
- **Pos-condicoes**: Operador identifica lote apto para entrega.
- **Regras relacionadas**: bloqueio de lote vencido na entrega.
