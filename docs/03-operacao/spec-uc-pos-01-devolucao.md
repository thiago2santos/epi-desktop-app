# Spec curta - UC-POS-01 (Devolucao ou descarte)

## Identificacao

- ID: `UC-POS-01`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: implementado na tela Devolucao / descarte (`DevolucaoView`, migracao `V17`). Item estornado recusa com `POS-001`.
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - peca que saiu para o trabalhador e voltou, estragou ou foi descartada precisa de registro. Isso nao e uma entrada de prateleira.
- Ator principal: `Almoxarife`. Tambem `SESMT` e `Admin`.
- Impacto se nao resolver:
  - a cobertura continua vigente depois do desligamento;
  - o saldo sobe com peca que ninguem inspecionou.

## 2) Escopo

- Comportamento no escopo:
  - registrar uma devolucao, descarte ou extravio de um item de fornecimento que ainda conta;
  - uma por item;
  - tirar o item da cobertura vigente.
- Fora de escopo:
  - devolver a quantidade ao disponivel (peca que volta a prateleira so entra de novo por `UC-LOT-01`, depois de inspecao);
  - estorno de lancamento errado (`UC-POS-02`);
  - baixa de peca que nunca foi fornecida (`UC-LOT-04`).

## 3) Regras de negocio

1. O item precisa contar: fornecimento sem estorno e sem devolucao anterior.
2. Motivo: `DESGASTE`, `DANO`, `DESCARTE`, `DESLIGAMENTO`, `EXTRAVIO` ou `OUTRO`. `OUTRO` exige texto.
3. A data e escolhida pelo operador, nao anterior a data do fornecimento e nao posterior a hoje. O relogio e injetavel.
4. Uma devolucao por item. A segunda recusa.
5. Nao grava `estoque_movimento`. Fisica, reservada e disponivel ficam como estavam.
6. O item deixa de contar na cobertura do `UC-MAT-02` e deixa de ser pendencia do `UC-POS-03`.
7. Confirmacao obrigatoria. Cancelar nao grava.
8. `Consulta` e `Gestor` nao registram.
9. Auditoria: `DEVOLUCAO_REGISTRADA`, entidade `DEVOLUCAO`, na mesma transacao do registro. Nao ha update da ficha original; nasce um registro novo apontando o item.

## 4) Catalogo de erros

| Codigo | Quando | Texto de tela |
|---|---|---|
| `POS-001` | Item ausente, ja estornado ou ja devolvido | Este item nao esta pendente de devolucao. |
| `POS-002` | Data anterior ao fornecimento ou futura | A data da devolucao precisa ser no dia do fornecimento ou depois, ate hoje. |
| `POS-003` | Motivo ausente, ou Outro sem texto | Escolha o motivo. Se for outro, descreva. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para registrar devolucao. |

## 5) Criterios de aceite

- `CA-01`: almoxarife devolve item vigente por desligamento. Nasce a devolucao. O disponivel do lote nao muda. A cobertura deixa de contar o item.
- `CA-02`: data anterior ao fornecimento recusa com `POS-002` e nao grava.
- `CA-03`: segunda devolucao do mesmo item recusa com `POS-001`.
- `CA-04`: item estornado recusa com `POS-001`.
- `CA-05`: cancelar o dialogo nao grava.
- `CA-06`: `CONSULTA` recebe `AUTH-004`.

## 6) Tela

Destino ja previsto: "Devolucao / descarte". Titulo sem codigo de caso de uso.

- Busca do trabalhador e lista dos itens que ainda contam: EPI, CA, lote, data do fornecimento, quantidade.
- Formulario da linha: data e motivo.
- Confirmar abre "Registrar esta devolucao? O saldo da prateleira nao muda." Confirmar / Voltar.
- Vazio: "Nenhum item pendente de devolucao para este trabalhador."
- Sucesso: "Devolucao registrada." Faixa na propria tela.
- Codigo `POS-` nao aparece.

## 7) Seguranca e auditoria

- Login/senha: nao.
- Evento: `DEVOLUCAO_REGISTRADA`.
- Risco: somar a peca devolvida de volta no disponivel e fornecer de novo sem recebimento.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-pos-01.md`
- `docs/03-operacao/spec-uc-pos-03-pendencias.md`
- `docs/04-uml/entrega/UC-POS-01 — Registrar devolucao.md`
