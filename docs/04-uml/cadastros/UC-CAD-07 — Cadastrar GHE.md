### UC-CAD-07 — Cadastrar GHE

- **Atores**: SESMT, Admin
- **Descricao**: Mantem o grupo de exposicao da unidade e as funcoes que compartilham a mesma lista de EPI.
- **Pre-condicoes**: Unidade existente. Funcao ativa no setor dessa unidade, para o vinculo.
- **Gatilho**: Varias funcoes passam a exigir o mesmo conjunto de EPI.
- **Fluxo principal**:
  1. Operador informa unidade, nome e status.
  2. Vincula funcoes ativas da mesma unidade.
  3. Edita o nome, desvincula ou inativa sem delete fisico.
- **Fluxos alternativos/excecoes**:
  - `CAD-051` Nome ou unidade ausente.
  - `CAD-052` Nome ja usado na unidade.
  - `CAD-053` GHE alvo nao encontrado.
  - `CAD-054` Funcao inativa, de outra unidade ou inexistente.
  - `CAD-055` Funcao ja pertence a outro GHE.
- **Pos-condicoes**: Funcao em GHE ativo tem como perfil vigente a lista desse GHE. Sem GHE ativo, o perfil e a propria funcao.
- **Fora deste caso**: lista de EPI (`UC-MAT-01`), periodicidade, risco e S-2240. O trabalhador nao recebe coluna de GHE.
- **Spec**: `docs/03-operacao/spec-uc-cad-07-ghe.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-cad-07.md`
- **Status**: implementado. Spec em `docs/03-operacao/spec-uc-cad-07-ghe.md`.
