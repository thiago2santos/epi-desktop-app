### UC-ENT-02 — Registrar aceite do termo de responsabilidade

- **Atores**: Almoxarife, SESMT, Admin, registrando o aceite do trabalhador no balcao.
- **Descricao**: Grava o termo `6.6.1` dentro da mesma confirmacao do fornecimento. O trabalhador autentica com a senha de recebimento.
- **Pre-condicoes**: Ficha em revisao, ainda nao gravada. Trabalhador com hash no `UC-CAD-03`.
- **Gatilho**: Passo Ciencia e termo.
- **Fluxo principal**:
  1. Sistema mostra o texto `TERMO-NR6-01` com o nome do trabalhador.
  2. Trabalhador digita a senha de recebimento.
  3. A confirmacao do `UC-ENT-01` confere o hash e grava o termo com metodo `SENHA`.
- **Fluxos alternativos/excecoes**:
  - `ENT-014` Sem senha no cadastro. Nada e gravado.
  - `ENT-015` Senha em branco ou diferente. Nada e gravado.
- **Pos-condicoes**: Ficha aponta para a versao do texto, o metodo `SENHA` e o operador. A senha em claro nao fica na ficha.
- **Spec**: `docs/03-operacao/spec-uc-ent-02-termo.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-ent-02.md`
- **Status**: implementado no passo Ciencia e termo. Metodo `SENHA`. Sem tela propria.
