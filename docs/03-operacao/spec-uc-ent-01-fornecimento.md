# Spec curta - UC-ENT-01 (Registrar fornecimento)

## Identificacao

- ID: `UC-ENT-01`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois de `UC-LOT-01`, `UC-MAT-01` e `UC-MAT-02`
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`
- Norma: NR-6 `6.5.1 (c)`, `6.5.1 (d)`, `6.5.1.1`, `6.5.1.2.1`, `6.6.1`, `6.7.2`, `6.9.2.1.1`, `6.9.3`

## 1) Contexto

- Problema real:
  - o fornecimento e o registro legal de quem recebeu qual peca, com CA, lote e ciencia. Sem a transacao unica, a ficha e o saldo divergem.
- Ator principal: `Almoxarife`. Tambem `SESMT` e `Admin`. O trabalhador participa no balcao e nao tem login.
- Impacto se nao resolver:
  - nao ha prova para o item `6.5.1.1`;
  - o consumo do budget nao tem o que somar.

## 2) Escopo

- Comportamento no escopo:
  - registrar uma ficha imutavel, com um ou mais itens, para um trabalhador ativo;
  - cada item: EPI, lote vigente, quantidade, motivo, CA da peca, ciencia curta do `6.7.2`;
  - na mesma transacao, o aceite do termo (`UC-ENT-02`) e um movimento `BAIXA_FORNECIMENTO` por item;
  - se o item apontar uma reserva daquele lote, a baixa libera essa reserva junto;
  - gravar na ficha e no movimento o snapshot de nome, matricula, unidade, setor e funcao;
  - quando a linha apontar um pedido do `UC-SOL-01`, atualizar a quantidade atendida desse pedido na mesma transacao.
- Fora de escopo:
  - editar ou apagar a ficha (correcao e o `UC-POS-02`);
  - devolucao (`UC-POS-01`);
  - consulta de historico (`UC-ENT-03`);
  - a solicitacao criar a ficha sozinha;
  - biometria, assinatura digital e app do trabalhador;
  - medidas corporais (bota, luva) como cadastro.

## 3) Regras de negocio

1. Trabalhador ativo. A ficha guarda o organograma do instante da confirmacao, nao o de uma consulta posterior.
2. Data e hora sao o instante da confirmacao. O relogio e injetavel nos testes. Nao ha lancamento retroativo nesta versao.
3. Cada item tem quantidade inteira maior que zero, um lote do mesmo EPI, e um motivo: `PRIMEIRA_ENTREGA`, `TROCA_PERIODICA`, `DANO`, `EXTRAVIO`, `MUDANCA_FUNCAO` ou `OUTRO`. `OUTRO` exige texto.
4. O lote precisa estar vigente na data da ficha: validade da peca anterior a hoje bloqueia. Disponivel insuficiente bloqueia. Nao ha saldo negativo. O CA gravado no item e o CA do lote (o impresso na peca). CAs ativos adicionais do EPI podem ser incluidos para peca conjugada. O item fica com ao menos um CA.
5. A quantidade sai do disponivel vigente. Reserva de outro atendimento continua intocada. Se o operador ligar uma reserva deste lote, a quantidade nao passa do restante dessa reserva, e o movimento `BAIXA_FORNECIMENTO` diminui a reservada junto.
6. O mesmo lote nao se repete na ficha. A quantidade da linha unica e que se ajusta.
7. Item na matriz ativa da funcao vigente segue sem excecao. Item fora dela so `SESMT` ou `Admin` confirmam, com texto de excecao de pelo menos 10 caracteres. `Almoxarife` nao marca excecao.
8. Modo Posto nao dispensa a ficha: o trabalhador que retirou o descartavel ou o creme continua nomeado. A cobertura desse item, para ele, permanece "Posto", regra do `UC-MAT-02`.
9. Ciencia do `6.7.2`, por item: orientacao de uso e ajuste registrada. Se a linha da matriz exige treinamento, a data do treinamento entra no item e nao pode ser posterior a ficha. Nao ha modulo de curso.
10. Sem o aceite do termo do `UC-ENT-02`, a confirmacao nao grava nada.
11. A transacao grava cabecalho, itens, CAs, termo, movimentos e auditoria, ou nao grava nenhum. A ficha nao sofre update nem delete.
12. Cada movimento copia quantidade, custo unitario do recebimento (nulo se o lote nao tem custo), EPI, unidade, setor e funcao. Custo nulo nao bloqueia. O `UC-REL-04` marca custo incompleto.
13. O painel ao lado mostra a cobertura do `UC-MAT-02` em texto. Ela orienta. Nao bloqueia troca antecipada nem item "Vigente": o motivo explica a saida.
14. `Consulta` e `Gestor` nao registram fornecimento.
15. Pedido ligado ao item precisa estar `APROVADA`, `AGUARDANDO_ESTOQUE` ou `PARCIALMENTE_ATENDIDA`, do mesmo trabalhador e EPI, com saldo pendente suficiente. A quantidade atendida sobe na mesma transacao. Saldo zero muda o pedido para `ATENDIDA`. Sobra muda para `PARCIALMENTE_ATENDIDA`. Pedido que nao cobre a linha recusa a ficha inteira.
16. Auditoria: `FORNECIMENTO_REGISTRADO`, entidade `FORNECIMENTO`, na mesma transacao.

## 4) Catalogo de erros

| Codigo | Quando | Texto de tela |
|---|---|---|
| `ENT-001` | Trabalhador ausente ou inativo | Trabalhador nao encontrado ou inativo. |
| `ENT-002` | Ficha sem item | Inclua ao menos um EPI na ficha. |
| `ENT-003` | Quantidade invalida | A quantidade precisa ser um numero inteiro maior que zero. |
| `ENT-004` | Lote ausente, de outro EPI ou vencido | Escolha um lote vigente deste EPI. Peca vencida nao pode ser fornecida. |
| `ENT-005` | Quantidade acima do disponivel | Nao ha quantidade disponivel neste lote. |
| `ENT-006` | Fora da matriz e o papel nao e SESMT nem Admin | Este EPI nao esta na matriz da funcao. So o SESMT pode registrar a excecao. |
| `ENT-007` | Excecao sem texto de 10 caracteres | Descreva a excecao com pelo menos 10 caracteres. |
| `ENT-008` | Orientacao ausente, ou treinamento exigido sem data, ou data futura | Registre a orientacao de uso. Se a matriz exige treinamento, informe a data. |
| `ENT-009` | Termo nao aceito | O fornecimento so conclui com o aceite do termo. |
| `ENT-010` | Motivo ausente, ou Outro sem texto | Escolha o motivo. Se for outro, descreva. |
| `ENT-011` | Reserva de outro lote ou quantidade acima do restante | A reserva nao e deste lote ou nao cobre a quantidade. |
| `ENT-012` | Mesmo lote em duas linhas | Este lote ja esta nesta ficha. Ajuste a quantidade da linha. |
| `ENT-013` | Pedido ausente, de outro trabalhador ou EPI, ou quantidade acima do saldo do pedido | O pedido nao cobre este trabalhador, EPI ou quantidade. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para registrar fornecimento. |

## 5) Criterios de aceite

- `CA-01`: almoxarife confirma um item da matriz, lote vigente, ciencia e termo. Nasce a ficha, o CA do lote, o movimento `BAIXA_FORNECIMENTO` e a auditoria. Disponivel cai a quantidade. Reservada de outras reservas nao cai.
- `CA-02`: peca vencida recusa com `ENT-004` e nao grava ficha nem movimento.
- `CA-03`: quantidade acima do disponivel recusa com `ENT-005`.
- `CA-04`: item fora da matriz, almoxarife, recusa com `ENT-006` mesmo com texto preenchido.
- `CA-05`: SESMT fora da matriz, com texto de 10 caracteres, grava a ficha marcada como excecao.
- `CA-06`: cancelar o dialogo de confirmacao nao grava.
- `CA-07`: falha no movimento desfaz ficha, termo e auditoria.
- `CA-08`: item ligado a reserva daquele lote diminui fisica e a reservada dessa reserva.
- `CA-09`: matriz com treinamento obrigatorio sem data recusa com `ENT-008`.
- `CA-10`: a linha do movimento guarda setor e funcao da hora, custo do recebimento ou nulo, e a ficha nao aceita update.

## 6) Tela

Destino ja previsto: "Registrar fornecimento". Titulo sem codigo de caso de uso. Cinco passos visiveis: Trabalhador, Itens da funcao, Lote e quantidade, Ciencia e termo, Revisao. O passo atual e os ja concluidos aparecem em texto.

- Passo 1: busca por matricula ou nome. Cartao com nome, matricula, setor e funcao. Vazio ou inativo: "Trabalhador nao encontrado ou inativo."
- Painel ao lado, o tempo todo depois do trabalhador: EPI e situacao da cobertura em palavras (Vigente, Troca em N dias, Prazo vencido, Pendente, Sem prazo, Posto).
- Passo 2: linhas da matriz, mais "Incluir fora da matriz" visivel para SESMT e Admin, com o campo de excecao. Para o almoxarife o campo nao habilita.
- Passo 3: lote com Fisica, Reservada, Disponivel, situacao, e dois rotulos separados: "CA na compra" e "Validade da peca". Lote vencido permanece visivel e o proximo passo fica desabilitado, com a frase do `ENT-004`.
- Passo 4: orientacao de uso, data de treinamento quando a matriz exige, e o termo do `UC-ENT-02`.
- Passo 5: resumo. O botao primario abre o dialogo "Confirmar fornecimento? Esta ficha nao podera ser editada." Confirmar / Voltar. Enter no dialogo confirma. Enter na revisao so abre o dialogo.
- Anterior nao chama o servico. Esc no dialogo volta.
- Barra legal: "Ao confirmar, a ficha fica registrada. Correcao posterior e um estorno, nao uma edicao."
- Sucesso: "Fornecimento registrado." e o numero da ficha. Faixa na propria tela.
- Codigo `ENT-` nao aparece.

## 7) Seguranca e auditoria

- Login/senha: nao. O trabalhador nao autentica.
- Evento: `FORNECIMENTO_REGISTRADO`.
- Risco: gravar a ficha sem o movimento, ou baixar lote vencido, ou deixar o almoxarife autorizar item fora da matriz.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-ent-02-termo.md`
- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-ent-01.md`
- `docs/01-negocio/modelagem-entrega-epi.md`
- `docs/04-uml/entrega/UC-ENT-01 — Registrar fornecimento.md`
