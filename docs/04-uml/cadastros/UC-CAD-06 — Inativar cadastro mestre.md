### UC-CAD-06 — Inativar cadastro mestre
- **Atores**: Admin, SESMT
- **Descricao**: Inativa trabalhador, EPI ou funcao sem apagar historico.
- **Pre-condicoes**: Cadastro existente.
- **Gatilho**: Mudanca organizacional ou desuso de item.
- **Fluxo principal**:
  1. Operador localiza registro.
  2. Marca status inativo.
  3. Confirma operacao.
- **Fluxos alternativos/excecoes**:
  - Registro com dependencia critica: sistema alerta e solicita confirmacao.
- **Pos-condicoes**: Registro inativo para novas operacoes.
- **Regras relacionadas**: historico preservado; auditoria.
