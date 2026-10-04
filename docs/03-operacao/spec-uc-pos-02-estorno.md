# Spec curta - UC-POS-02 (Estorno de fornecimento)

## Identificacao

- ID: `UC-POS-02`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois do `UC-ENT-01`
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - lancamento errado nao se apaga. O estorno e o registro novo que desfaz o efeito no saldo e tira o item da prova vigente.
- Ator principal: `SESMT`. Tambem `Admin`.
- Impacto se nao resolver:
  - o consumo do budget conta peca que nao saiu;
  - a ficha legal e editada por cima.

## 2) Escopo

- Comportamento no escopo:
  - estornar o item inteiro de uma ficha;
  - gravar `ESTORNO_FORNECIMENTO` com a mesma quantidade;
  - manter a ficha original visivel.
- Fora de escopo:
  - estorno parcial da quantidade;
  - recriar a reserva que a baixa tenha consumido;
  - devolucao de peca realmente entregue (`UC-POS-01`);
  - almoxarife corrigir a propria ficha.

## 3) Regras de negocio

1. So item que ainda conta: sem estorno e sem devolucao.
2. O motivo tem pelo menos 10 caracteres.
3. A quantidade do movimento e a quantidade inteira do item. Nao ha estorno de parte.
4. `ESTORNO_FORNECIMENTO` soma a fisica. Nao mexe na reservada e nao recria reserva. O disponivel sobe essa quantidade, se o lote nao estiver vencido. Lote vencido: a fisica sobe e o disponivel para fornecer continua zero, regra do diario.
5. O item deixa de contar na cobertura e no consumo vigente. No `UC-REL-04`, o estorno diminui o consumo do periodo em que o estorno foi gravado.
6. Se o item estava ligado a um pedido, a quantidade atendida daquele pedido diminui na mesma transacao. O pedido volta a `PARCIALMENTE_ATENDIDA` ou `APROVADA`, o que o saldo pendente exigir. Pedido `CANCELADA` ou `REJEITADA` nao e reaberto: o estorno do item fica registrado e o pedido permanece terminal.
7. Confirmacao obrigatoria. Cancelar nao grava. Nao ha update nem delete da ficha.
8. `Almoxarife`, `Consulta` e `Gestor` recebem `AUTH-004`.
9. Auditoria: `FORNECIMENTO_ESTORNADO`, entidade `FORNECIMENTO`, na mesma transacao do movimento. Falha desfaz os dois.

## 4) Catalogo de erros

| Codigo | Quando | Texto de tela |
|---|---|---|
| `POS-004` | Item ausente, ja estornado ou ja devolvido | Este fornecimento nao pode ser estornado. |
| `POS-005` | Motivo com menos de 10 caracteres | Descreva o motivo do estorno com pelo menos 10 caracteres. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para estornar fornecimento. |

## 5) Criterios de aceite

- `CA-01`: SESMT estorna item de 2 unidades. Fisica sobe 2. Reservada nao muda. A ficha original segue consultavel como "Estornado". Evento `FORNECIMENTO_ESTORNADO`.
- `CA-02`: motivo curto recusa com `POS-005` e nao grava movimento.
- `CA-03`: item ja devolvido recusa com `POS-004`.
- `CA-04`: segundo estorno do mesmo item recusa com `POS-004`.
- `CA-05`: lote vencido estornado aumenta a fisica e o disponivel para fornecer continua zero.
- `CA-06`: cancelar o dialogo nao grava.
- `CA-07`: `ALMOXARIFE` recebe `AUTH-004`.
- `CA-08`: item ligado a pedido atendido reduz a quantidade atendida do pedido na mesma transacao.

## 6) Tela

Destino ja previsto: "Estorno". Titulo sem codigo de caso de uso.

- Busca do trabalhador e itens que ainda contam.
- Campo de motivo e o botao "Estornar".
- Dialogo: "Estornar este fornecimento? A ficha permanece e o saldo da prateleira volta." Confirmar / Voltar.
- Vazio: "Nenhum fornecimento em aberto para estornar."
- Sucesso: "Fornecimento estornado." Faixa na propria tela.
- Codigo `POS-` nao aparece.

## 7) Seguranca e auditoria

- Login/senha: nao.
- Evento: `FORNECIMENTO_ESTORNADO`.
- Risco: apagar a ficha, ou devolver a reserva antiga e prender peca que ja esta livre.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/spec-uc-rel-04-consumo-budget.md`
- `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md`
- `docs/03-operacao/matriz-testes-uc-pos-02.md`
- `docs/04-uml/entrega/UC-POS-02 — Registrar estorno.md`
