### UC-CAD-01 — Cadastrar unidade

- **Atores**: Admin, SESMT
- **Descricao**: Mantem as unidades da empresa ja existente. A empresa Access permanece a do seed; este caso de uso nao cria outra sociedade.
- **Pre-condicoes**: Empresa semeada. Operador com permissao de cadastro.
- **Gatilho**: Nova planta, correcao de nome ou inativacao de unidade.
- **Fluxo principal**:
  1. Operador abre o cadastro de unidade.
  2. Informa nome e CNPJ. O CNPJ pode vir mascarado; o sistema guarda 14 digitos.
  3. Confirma a gravacao.
  4. Pode editar o nome, inativar ou reativar, sem apagar o registro.
- **Fluxos alternativos/excecoes**:
  - `CAD-041` Nome ou CNPJ ausente.
  - `CAD-042` CNPJ invalido.
  - `CAD-043` CNPJ ja cadastrado.
  - `CAD-044` Unidade alvo nao encontrada.
  - `CAD-045` Inativacao recusada porque ainda existe setor ativo.
- **Pos-condicoes**: Unidades ativas disponiveis no cadastro de setor.
- **Regras relacionadas**: uma empresa; CNPJ unico e com digito verificador; nome pode repetir; sem delete fisico; auditoria de criar, editar, inativar e reativar.
- **Fora deste caso de uso**: endereco, consulta de CNPJ na Receita, segunda empresa, importacao CSV.
- **Spec**: `docs/03-operacao/spec-uc-cad-01-unidade.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-cad-01.md`
- **Status**: especificado; implementacao pendente.
