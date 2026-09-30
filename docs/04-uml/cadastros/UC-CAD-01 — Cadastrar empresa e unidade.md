### UC-CAD-01 — Cadastrar empresa e unidade
- **Atores**: Admin, SESMT
- **Descricao**: Registra dados institucionais da empresa e unidade operacional.
- **Pre-condicoes**: Permissao de cadastro.
- **Gatilho**: Configuracao inicial da unidade.
- **Fluxo principal**:
  1. Operador acessa cadastro institucional.
  2. Informa razao social, CNPJ e unidade.
  3. Confirma gravacao.
- **Fluxos alternativos/excecoes**:
  - CNPJ duplicado/invalido: sistema recusa.
- **Pos-condicoes**: Empresa/unidade disponiveis para uso nos demais modulos.
- **Regras relacionadas**: CNPJ unico; escopo por unidade.
