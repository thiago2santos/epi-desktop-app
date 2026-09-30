### UC-CAD-04 — Cadastrar EPI
- **Atores**: SESMT
- **Descricao**: Cadastra e mantem item de EPI no catalogo com classificacao normativa.
- **Pre-condicoes**: Usuario SESMT autenticado.
- **Gatilho**: Novo item no programa de protecao.
- **Fluxo principal**:
  1. SESMT informa descricao, fabricante e classificacao do EPI conforme Anexo I da NR-6.
  2. Sistema valida obrigatorios e consistencia da classificacao.
  3. SESMT define status inicial (ativo/inativo) e confirma cadastro.
- **Fluxos alternativos/excecoes**:
  - `CAD-031` Campos obrigatorios de EPI ausentes.
  - `CAD-032` Classificacao fora do Anexo I.
  - `CAD-033` EPI duplicado no escopo.
- **Pos-condicoes**: EPI disponivel para vinculacao de CA.
- **Regras relacionadas**: classificacao normativa obrigatoria; sem delete fisico; auditoria.
