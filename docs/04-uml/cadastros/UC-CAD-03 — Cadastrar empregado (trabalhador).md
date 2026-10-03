### UC-CAD-03 — Cadastrar empregado (trabalhador)
- **Atores**: SESMT, Admin
- **Descricao**: Inclui e mantem trabalhador apto a receber EPI (editar e alterar status sem delete fisico).
- **Pre-condicoes**: Funcao e setor cadastrados e ativos.
- **Gatilho**: Admissao ou regularizacao de cadastro.
- **Fluxo principal**:
  1. Operador informa matricula, nome, funcao e setor/departamento.
  2. Vincula o gestor responsavel vigente, quando aplicavel.
  3. Define status inicial (ativo/inativo).
  4. Sistema valida obrigatorios, unicidade da matricula, coerencia entre funcao e setor e vigencia do gestor.
  5. Operador confirma cadastro.
  6. Para inativacao, sistema exige confirmacao explicita em tela.
- **Fluxos alternativos/excecoes**:
  - `CAD-001` Matricula ja existente.
  - `CAD-002` Funcao invalida ou inativa.
  - `CAD-003` Inconsistencia entre funcao e setor.
  - `CAD-004` Campos obrigatorios ausentes.
  - `CAD-005` Operacao bloqueada por dependencia historica (quando aplicavel).
  - `CAD-006` Trabalhador alvo inexistente/nao selecionado para alteracao de status/edicao.
- **Pos-condicoes**: Trabalhador ativo/inativo disponivel para entrega e rastreavel por auditoria, com lotacao organizacional e gestor vigentes identificaveis.
- **Regras relacionadas**: matricula unica; consistencia funcao-setor; associacao trabalhador-gestor com vigencia/historico para escopo de solicitacao; sem delete fisico; auditoria em criar/editar/inativar/reativar.
- **Dependencia futura**: `UC-SOL-01` exige escopo de gestor autorizado e snapshot organizacional no envio; ver `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md`.
