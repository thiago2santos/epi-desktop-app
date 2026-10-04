### UC-MAT-01 — Definir matriz funcao x EPI

- **Atores**: SESMT, Admin
- **Descricao**: Lista os EPIs exigidos por funcao, com CA esperado, modo Individual ou Posto, e flag de treinamento.
- **Pre-condicoes**: Funcao ativa. EPI ativo com CA ativo.
- **Gatilho**: Implantacao ou revisao do que a funcao usa.
- **Fluxo principal**:
  1. SESMT escolhe a funcao, rotulada com o setor.
  2. Inclui o EPI. O CA esperado nasce do CA ativo.
  3. Ajusta modo e treinamento.
  4. Inativar pede confirmacao e encerra a linha sem apagar.
- **Fluxos alternativos/excecoes**:
  - `MAT-001` a `MAT-004` e `MAT-007`.
- **Pos-condicoes**: A funcao tem a lista que o fornecimento e a cobertura leem.
- **Fora deste caso**: GHE como entidade, periodicidade, excecao no balcao.
- **Spec**: `docs/03-operacao/spec-uc-mat-01-matriz.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-mat-01.md`
- **Status**: especificado; implementacao pendente.
