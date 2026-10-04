### UC-MAT-02 — Definir periodicidade de troca

- **Atores**: SESMT, Admin
- **Descricao**: Grava dias de troca e aviso antecipado por EPI, e define o texto da cobertura.
- **Pre-condicoes**: EPI em ao menos uma linha ativa da matriz.
- **Gatilho**: Parametrizar a troca.
- **Fluxo principal**:
  1. SESMT ve um EPI por linha, com quantas funcoes o usam.
  2. Informa dias e aviso.
  3. Salva.
- **Fluxos alternativos/excecoes**:
  - `MAT-005` Dias invalidos.
  - `MAT-006` Aviso invalido.
  - Sem valor salvo: cobertura "Sem prazo". O fornecimento segue.
- **Pos-condicoes**: Consulta do trabalhador mostra Vigente, Troca em N dias, Prazo vencido, Pendente, Sem prazo ou Posto.
- **Spec**: `docs/03-operacao/spec-uc-mat-02-periodicidade.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-mat-02.md`
- **Status**: especificado; implementacao pendente.
