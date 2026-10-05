### UC-MAT-01 — Definir matriz funcao x EPI

- **Atores**: SESMT, Admin
- **Descricao**: Lista os EPIs exigidos por perfil, com CA esperado, modo Individual ou Posto, e flag de treinamento. O perfil e a funcao ou o GHE ativo.
- **Pre-condicoes**: Perfil ativo. EPI ativo com CA ativo.
- **Gatilho**: Implantacao ou revisao do que o perfil usa.
- **Fluxo principal**:
  1. SESMT escolhe a funcao, rotulada com o setor, ou o GHE ativo.
  2. Inclui o EPI. O CA esperado nasce do CA ativo.
  3. Ajusta modo e treinamento.
  4. Inativar pede confirmacao e encerra a linha sem apagar.
- **Fluxos alternativos/excecoes**:
  - `MAT-001` a `MAT-004`, `MAT-007` e `MAT-008`.
- **Pos-condicoes**: O perfil vigente tem a lista que o fornecimento e a cobertura leem.
- **Fora deste caso**: cadastro do GHE (`UC-CAD-07`), periodicidade, excecao no balcao.
- **Spec**: `docs/03-operacao/spec-uc-mat-01-matriz.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-mat-01.md`
- **Status**: implementado. Spec em `docs/03-operacao/spec-uc-mat-01-matriz.md`.
