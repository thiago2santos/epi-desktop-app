### UC-ENT-01 — Registrar fornecimento de EPI

- **Atores**: Almoxarife, SESMT, Admin. Trabalhador no balcao, sem login.
- **Descricao**: Grava a ficha imutavel e a baixa do lote na mesma transacao.
- **Pre-condicoes**: Trabalhador ativo. Lote vigente com disponivel. Matriz da funcao, para o caminho sem excecao.
- **Gatilho**: Admissao, troca, dano, extravio ou mudanca de funcao.
- **Fluxo principal**:
  1. Operador localiza o trabalhador.
  2. Seleciona os EPIs da funcao, ve a cobertura na tabela, escolhe lote e quantidade.
  3. Registra a orientacao, o termo e a senha de recebimento.
  4. Confirma no dialogo. O sistema confere o hash.
  5. Sistema grava ficha, CAs, termo, `BAIXA_FORNECIMENTO` e auditoria.
- **Fluxos alternativos/excecoes**:
  - `ENT-001` a `ENT-015`.
  - `ENT-014` Trabalhador sem senha de recebimento.
  - `ENT-015` Senha em branco ou diferente do hash.
  - Fora da matriz: so SESMT ou Admin, com texto.
  - Cancelar o dialogo nao grava.
- **Pos-condicoes**: Disponivel diminui. Ficha nao se edita. Snapshot de setor e funcao fica no movimento.
- **Spec**: `docs/03-operacao/spec-uc-ent-01-fornecimento.md`
- **Testes**: `docs/03-operacao/matriz-testes-uc-ent-01.md`
- **Status**: implementado na tela Registrar fornecimento. Pedido e reserva entram quando `UC-SOL-01` e `UC-LOT-03` existirem.
