# Modelo do diario de estoque

Fonte unica das quantidades de EPI. Os casos `UC-LOT-01` a `UC-LOT-06`, o `UC-ENT-01` e o `UC-REL-04` leem e gravam daqui. O estorno do fornecimento grava movimento (`UC-POS-02`). A devolucao nao grava movimento (`UC-POS-01`).

Norma de referencia: NR-6 consolidada em 2025 (Portaria MTE n 57/2025, redacao da Portaria MTP n 2.175/2022).

## 1) O que a NR-6 exige no lote

- `6.5.1 (a)` e `6.9.2.1`: so se adquire EPI aprovado, comercializado com CA valido. O recebimento guarda o CA da data da compra.
- `6.9.2.1.1`: depois de comprado, o fornecimento observa armazenamento e o prazo de validade do fabricante ou importador. CA do certificado e validade da peca sao datas diferentes.
- `6.9.3`: a peca identifica nome comercial do fabricante ou importador, lote de fabricacao e numero do CA.
- `6.5.1.2.1`: sem a embalagem original no local de fornecimento, ficam visiveis identificacao, fabricante ou importador, lote, validade e CA.
- `6.5.1 (c)`, `(d)` e `6.5.1.1`: fornecer em perfeito estado e registrar o fornecimento, com relatorio, e a entrega. Reserva, compra e inventario nao sao fornecimento.

A norma nao define reserva, contagem de inventario nem formula de compra. Isso e regra de almoxarifado deste produto.

## 2) Diario append-only

Cada mudanca de quantidade e uma linha em `estoque_movimento`. Nenhuma tela edita ou apaga linha. Correcao e movimento novo.

O lote guarda a identidade da peca. O saldo nao e a fonte da verdade. As tres leituras sao a soma dos movimentos.

| Leitura | Conta |
|---|---|
| Fisica | O que entrou e ainda esta na unidade |
| Reservada | O que ja tem dono e ainda nao foi entregue |
| Disponivel | Fisica menos reservada. Lote com validade anterior a hoje fica com disponivel zero para fornecimento e para reserva |

| Tipo | Fisica | Reservada | Papel |
|---|---|---|---|
| `RECEBIMENTO` | soma | — | `UC-LOT-01` |
| `RESERVA` | — | soma | `UC-LOT-03` |
| `LIBERACAO_RESERVA` | — | diminui | `UC-LOT-03` |
| `BAIXA_FORNECIMENTO` | diminui | diminui a reserva ligada, se houver | contrato do `UC-ENT-01` |
| `ESTORNO_FORNECIMENTO` | soma | nao recria reserva | contrato do estorno de M2 |
| `BAIXA_PRATELEIRA` | diminui | nao mexe na reserva | `UC-LOT-04` |
| `AJUSTE_INVENTARIO` | soma ou diminui | nao mexe na reserva | `UC-LOT-05` |

Quantidade do movimento e inteira e maior que zero. O tipo define o sinal.

## 3) O que entra em cada pergunta do gestor

- **Consumo** da area de seguranca: `BAIXA_FORNECIMENTO` menos `ESTORNO_FORNECIMENTO` no periodo. E o que o trabalhador recebeu.
- **Perda**: `BAIXA_PRATELEIRA` (vencimento, perda, descarte do que nao foi entregue). Nao mistura com consumo.
- **Ajuste**: `AJUSTE_INVENTARIO`. Explica a diferenca da contagem. Nao e consumo nem compra.
- **Compra**: demanda ainda sem reserva, menos o disponivel vigente. O que ja esta reservado ja esta coberto pelo fisico. Somar de novo compraria a mesma peca duas vezes.

Cada `BAIXA_FORNECIMENTO` guarda, na propria linha, quantidade, custo unitario daquele recebimento, EPI, unidade, setor e funcao. O relatorio de budget nao depende do organograma de hoje.

Custo unitario zero ou ausente no recebimento nao bloqueia o lote. O `UC-REL-04` marca a linha como custo incompleto.

## 4) Inventario e outra feature

`UC-LOT-05` abre uma contagem da unidade, mostra o fisico esperado (inclui o que esta reservado), recebe a quantidade achada e, so na confirmacao, grava um ajuste por lote com diferenca. O recebimento original permanece. Contagem menor que a quantidade reservada daquele lote e recusada: primeiro se libera a reserva.

## 5) Contrato da entrega

`UC-ENT-01` grava `BAIXA_FORNECIMENTO` na mesma transacao da ficha. Se a baixa estiver ligada a uma reserva, a reserva cai junto. Peca vencida e disponivel insuficiente bloqueiam a entrega. Spec: `docs/03-operacao/spec-uc-ent-01-fornecimento.md`. O termo entra na mesma transacao (`UC-ENT-02`).

`UC-POS-02` grava `ESTORNO_FORNECIMENTO` e nao apaga a baixa. A fisica sobe. A reservada nao volta. `UC-POS-01` registra devolucao ou descarte e nao grava movimento: peca devolvida so retorna a prateleira por um recebimento novo.

`UC-SOL-01` continua sem criar reserva. A reserva explicita e o `UC-LOT-03`. Uma iteracao futura pode ligar o pedido aprovado a essa reserva.

## 6) Padrao de tela

As telas de estoque seguem `docs/02-arquitetura/usabilidade/heuristicas-nielsen.md` e o padrao ja usado em unidade e EPI.

- Tres numeros visiveis, com rotulo: Fisica, Reservada, Disponivel. Situacao em texto: Vigente, Vencido, Esgotado. Cor nao substitui o texto.
- Lista vazia diz o que falta e oferece a acao primaria quando o papel pode criar.
- Salvar comeca desabilitado e so liga com os obrigatorios.
- Baixa, liberar reserva, confirmar inventario e receber peca ja vencida pedem confirmacao. Cancelar nao grava.
- Sucesso e faixa temporaria na propria tela. `Alert` fica para a confirmacao destrutiva.
- A frase e do almoxarife. O codigo `LOT-` fica no log e na auditoria.
- Filtro por unidade, EPI, codigo do lote ou situacao. O operador nao precisa lembrar o id.
- Trabalho de gravacao nao trava a janela se um dia houver importacao. O recebimento manual e imediato.

## 7) Catalogo de erros `LOT-0xx`

A tela mostra o texto. O codigo vai para a auditoria.

| Codigo | Quando | Texto de tela |
|---|---|---|
| `LOT-001` | Unidade, EPI, CA, codigo do lote, validade ou quantidade ausente | Informe unidade, EPI, CA, lote, validade e quantidade. |
| `LOT-002` | Quantidade nao inteira ou menor que um | A quantidade precisa ser um numero inteiro maior que zero. |
| `LOT-003` | Mesmo codigo de lote, EPI, tamanho e unidade | Ja existe este lote para este EPI nesta unidade. |
| `LOT-004` | EPI sem vinculo de CA ativo | Este EPI nao tem CA ativo para receber. |
| `LOT-005` | Unidade inexistente ou inativa | Escolha uma unidade ativa. |
| `LOT-006` | Lote ou reserva alvo inexistente | Selecione um lote da lista. |
| `LOT-007` | Custo informado negativo | O custo unitario nao pode ser negativo. |
| `LOT-008` | Reserva acima do disponivel vigente | Nao ha quantidade disponivel para reservar. |
| `LOT-009` | Reserva em lote vencido | Lote vencido nao pode ser reservado nem fornecido. |
| `LOT-010` | Liberacao acima do que a reserva ainda tem | A liberacao passa da quantidade reservada. |
| `LOT-011` | Baixa de prateleira acima do disponivel | A baixa nao pode usar peca ja reservada. Libere a reserva antes. |
| `LOT-012` | Motivo de baixa ausente | Escolha o motivo: vencimento, perda ou descarte. |
| `LOT-013` | Linha de inventario sem quantidade contada | Informe a quantidade contada em todos os lotes. |
| `LOT-014` | Contagem menor que o reservado do lote | A contagem ficou abaixo do que ja esta reservado. Libere a reserva antes de ajustar. |
| `LOT-015` | Inventario inexistente ou ja encerrado | Este inventario nao esta aberto. |
| `LOT-016` | Demanda de compra ausente | Informe a demanda do periodo para calcular a compra. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para esta acao. |

Custo em branco e diferente de custo negativo: o recebimento aceita custo em branco e o budget marca incompleto. Custo zero e um custo informado.
