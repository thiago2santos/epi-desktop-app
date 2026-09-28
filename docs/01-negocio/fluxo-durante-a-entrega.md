# Modelagem — Durante a entrega

## Objetivo

Registrar o fornecimento de EPI como evento juridico imutavel, rastreavel e auditavel, atendendo aos requisitos da NR-6.

## Escopo desta etapa

- Registro de entrega (cabecalho e itens).
- Vinculo item entregue x lote x CA.
- Validacao/ciencia do trabalhador.
- Captura do termo de responsabilidade.
- Aplicacao de bloqueios e excecoes em tempo de operacao.

## Entradas

- Trabalhador ativo com funcao vigente.
- Matriz funcao/GHE ativa.
- Lotes com saldo e validade adequados.
- Usuario operador autenticado (SESMT ou almoxarifado conforme permissao).

## Saidas

- Evento de entrega persistido e imutavel.
- Baixa de saldo do lote correspondente.
- Registro de aceite do trabalhador.
- Registro de auditoria da operacao.

## Entidades principais

- `entrega_epi`: cabecalho do ato de entrega.
- `entrega_epi_item`: itens entregues.
- `entrega_item_ca`: CAs vinculados por item entregue.
- `termo_responsabilidade_aceite`: aceite do termo legal.
- `auditoria`: rastreio de quem fez o que e quando.

## Telas desta etapa

1. **Atendimento de entrega (balcao)**
   - Busca de trabalhador por matricula/nome.
   - Carregamento de itens previstos na matriz da funcao/GHE.
2. **Selecao de lote e quantidade**
   - Exibe lotes validos, saldo e validade por EPI.
   - Bloqueia lote vencido e saldo insuficiente.
3. **Validacao do trabalhador**
   - Registro de ciencia por item (assinatura manual/digital/biometria).
   - Captura de evidencias de aceite.
4. **Fechamento da entrega**
   - Exibe termo de responsabilidade e confirma evento imutavel.
   - Emite comprovante e numero do registro.

## Tabelas envolvidas

- `trabalhador`: cadastro do empregado.
- `funcao`/`ghe`: perfil vigente na data da entrega.
- `lote_epi`: origem do item entregue.
- `entrega_epi`: cabecalho do evento.
- `entrega_epi_item`: itens entregues.
- `entrega_item_ca`: CAs registrados por item.
- `termo_responsabilidade_aceite`: aceite legal do evento.
- `auditoria`: trilha da operacao.

## Relacionamentos chave

- `trabalhador` 1:N `entrega_epi`
- `entrega_epi` 1:N `entrega_epi_item`
- `entrega_epi_item` N:1 `lote_epi`
- `entrega_epi_item` 1:N `entrega_item_ca`
- `entrega_epi` 1:1 `termo_responsabilidade_aceite`
- `entrega_epi` N:1 `funcao` (fotografia da funcao na data)

## Preparo para 8 unidades (modelo distribuido)

- Toda entrega deve carregar `unidade_id` no cabecalho (`entrega_epi`) para segregacao e consolidacao.
- Identificador do comprovante deve incluir unidade (ex.: `ITU-2026-000123`) para evitar colisao distribuida.
- Regras de permissao devem combinar papel + escopo da unidade.
- Em arquitetura cliente-servidor futura, manter transacao unica para entrega + baixa de lote + auditoria.
- No piloto em Itupeva, operar com mesma modelagem (com `unidade_id`), evitando retrabalho na expansao.

## Regras de negocio essenciais

1. Entrega e imutavel: nao aceitar edicao/exclusao apos confirmacao.
2. Item entregue deve referenciar lote valido e com saldo suficiente.
3. Cada item precisa registrar CA (um ou mais, para conjugado).
4. Entrega fora da matriz exige justificativa e autorizacao.
5. Validacao do trabalhador e obrigatoria por item.
6. Confirmacao deve ser transacional (entrega + baixa de lote + auditoria).
7. Termo de responsabilidade deve ficar vinculado ao evento de entrega.

## Validacoes minimas recomendadas

- `data_entrega <= instante_atual`.
- `quantidade > 0`.
- `saldo_lote >= quantidade` no momento da confirmacao.
- `data_validade_peca >= data_entrega`.
- `metodo_validacao` em dominio controlado.

## Fluxo operacional da etapa

1. Selecionar trabalhador.
2. Carregar itens esperados pela matriz.
3. Selecionar lote e quantidade por item.
4. Capturar validacao do trabalhador.
5. Capturar aceite do termo de responsabilidade.
6. Confirmar entrega em transacao unica.
7. Emitir comprovante/registro para consulta e relatorio.

## Excecoes previstas

- Lote vencido.
- Saldo insuficiente.
- Item fora da matriz.
- Falha de validacao do trabalhador.
- Divergencia entre funcao do trabalhador e matriz vigente.

## Indicadores desta etapa

- Tempo medio por atendimento de entrega.
- Percentual de entregas com excecao.
- Top motivos de entrega (primeira, desgaste, extravio etc.).
- Taxa de erros operacionais que viraram estorno.

