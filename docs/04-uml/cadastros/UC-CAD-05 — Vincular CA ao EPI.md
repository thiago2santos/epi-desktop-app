### UC-CAD-05 — Vincular CA ao EPI
- **Atores**: SESMT
- **Descricao**: Registra e mantem um ou mais CAs associados ao EPI com rastreabilidade de consulta oficial.
- **Pre-condicoes**: EPI existente.
- **Gatilho**: Inclusao/atualizacao de aprovacao do item.
- **Fluxo principal**:
  1. SESMT seleciona EPI.
  2. Informa numero do CA, situacao, vigencia e evidencia da consulta CAEPI.
  3. Sistema valida conflito de vigencia e consistencia para ativacao.
  4. SESMT confirma vinculacao.
- **Fluxos alternativos/excecoes**:
  - `CAD-034` Numero de CA invalido/ausente.
  - `CAD-035` Conflito de vigencia para o mesmo EPI.
  - `CAD-036` Situacao do CA impede ativacao.
  - `CAD-037` Evidencia de consulta oficial ausente.
- **Pos-condicoes**: EPI passa a ter CA(s) rastreavel(is) para uso operacional.
- **Regras relacionadas**: aderencia aos itens 6.4.1 e 6.9 da NR-6; CA obrigatorio para entrega; auditoria.
