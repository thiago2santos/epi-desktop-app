### UC-MAT-01 — Definir matriz funcao/GHE x EPI
- **Atores**: SESMT
- **Descricao**: Define quais EPIs sao obrigatorios por funcao/GHE.
- **Pre-condicoes**: Funcao/GHE e EPI cadastrados.
- **Gatilho**: Implantacao ou revisao de programa de EPI.
- **Fluxo principal**:
  1. SESMT seleciona funcao/GHE.
  2. Seleciona EPIs obrigatorios.
  3. Define modo de fornecimento e vigencia.
  4. Salva matriz.
- **Fluxos alternativos/excecoes**:
  - Duplicidade de regra ativa: sistema recusa.
- **Pos-condicoes**: Matriz ativa para orientar entrega.
- **Regras relacionadas**: unicidade de regra ativa por perfil + EPI.
