### UC-CAD-02 — Cadastrar setor e funcao
- **Atores**: SESMT, Admin
- **Descricao**: Registra e mantem estrutura organizacional (setor e funcao) para vincular trabalhadores e matriz.
- **Pre-condicoes**: Unidade existente.
- **Gatilho**: Inclusao/ajuste de estrutura funcional.
- **Fluxo principal**:
  1. Operador cadastra setor com nome e status inicial.
  2. Operador cadastra funcao vinculada a setor ativo.
  3. Operador pode editar dados e alternar status (inativar/reativar) sem delete fisico.
  4. Para inativacao, sistema exige confirmacao explicita.
- **Fluxos alternativos/excecoes**:
  - `CAD-021` Nome de setor ja existente.
  - `CAD-024` Tentativa de inativar setor com funcao ativa vinculada.
  - `CAD-025` Nome de funcao ja existente no setor.
  - `CAD-026` Setor invalido/inativo para cadastro/edicao de funcao.
  - `CAD-028` Tentativa de inativar funcao com empregado ativo vinculado.
- **Pos-condicoes**: Setores/funcoes ativos disponiveis para cadastro de trabalhador e matriz.
- **Regras relacionadas**: unicidade case-insensitive; sem delete fisico; auditoria de criar/editar/inativar/reativar.
