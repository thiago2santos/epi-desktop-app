### UC-CAD-IMP-01 — Importar cadastros via CSV (validacao previa)

- **Atores**: Admin, SESMT
- **Descricao**: Permite carga em lote de cadastros organizacionais (setor, funcao, trabalhador) a partir de CSV padronizado, com etapa obrigatoria de revisao antes da persistencia.
- **Pre-condicoes**:
  - Operador autenticado com permissao de importacao em lote (RBAC — definir).
  - Layout CSV do tipo selecionado disponivel e documentado.
  - Para trabalhadores: regras de `UC-CAD-02` aplicaveis aos mestres referenciados.
- **Gatilho**: Necessidade de adocao/go-live com volume alto ou migracao de planilha.
- **Fluxo principal**:
  1. Operador acessa hub de importacao de cadastros.
  2. Seleciona tipo (setor, funcao ou trabalhador) e envia arquivo CSV.
  3. Sistema parseia e valida em background; apresenta tela de staging (tabela).
  4. Linhas validas aparecem com indicacao visual positiva; linhas com pendencia com indicacao de atencao e celulas destacadas.
  5. Operador usa filtros para focar validas ou pendencias.
  6. Operador confirma importacao das linhas validas.
  7. Sistema persiste conforme politica de lote; registra auditoria; exibe resumo.
- **Fluxos alternativos/excecoes**:
  - Arquivo invalido: `CAD-IMP-001` / `CAD-IMP-002`; sem persistencia.
  - Pendencia por mestre ausente: celula acionavel abre cadastro de setor/funcao pre-preenchido; operador retorna e revalida.
  - Nenhuma linha valida: `CAD-IMP-003`.
  - Falha na publicacao: `CAD-IMP-004`; base anterior preservada.
  - Sem permissao: `CAD-IMP-006`.
- **Pos-condicoes**:
  - Registros validos existem no cadastro correspondente.
  - Tentativa registrada em auditoria com contagens e resultado.
- **Regras relacionadas**: mesmas invariantes de `UC-CAD-02` e `UC-CAD-03`; sem bypass de validacao via CSV; deep link apenas quando acao humana for necessaria.

**Status**: UML registrado — detalhar fluxos na spec curta antes da implementacao.
