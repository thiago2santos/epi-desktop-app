### UC-ENT-02 — Registrar aceite do termo de responsabilidade

- **Atores**: Almoxarife, SESMT, Admin, registrando o aceite do trabalhador no balcao.
- **Descricao**: Grava o termo `6.6.1` dentro da mesma confirmacao do fornecimento.
- **Pre-condicoes**: Ficha em revisao, ainda nao gravada.
- **Gatilho**: Passo Ciencia e termo.
- **Fluxo principal**:
  1. Sistema mostra o texto `TERMO-NR6-01` com o nome do trabalhador.
  2. Operador marca o aceite da assinatura manual.
  3. A confirmacao do `UC-ENT-01` grava o termo junto.
- **Fluxos alternativos/excecoes**:
  - `ENT-009` Aceite desmarcado. Nada e gravado.
- **Pos-condicoes**: Ficha aponta para a versao do texto, o metodo `ASSINATURA_MANUAL` e o operador.
- **Spec**: `docs/03-operacao/spec-uc-ent-02-termo.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-ent-02.md`
- **Status**: especificado; sem tela propria.
