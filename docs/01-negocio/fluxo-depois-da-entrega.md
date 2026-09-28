# Modelagem — Depois da entrega

## Objetivo

Preservar valor juridico e gerencial do registro de entrega ao longo do tempo, com controle de devolucao, estorno, auditoria e relatorios.

## Escopo desta etapa

- Devolucao e descarte de itens entregues.
- Estorno formal de erro operacional.
- Trilha de auditoria append-only.
- Relatorios extraiveis para fiscalizacao e gestao.
- Evidencias para controle de cobertura e budget.

## Entradas

- Itens previamente entregues.
- Eventos de retorno (desgaste, dano, extravio, desligamento).
- Solicitacoes de correcao formal (estorno).
- Filtros de relatorio por periodo, unidade, funcao, trabalhador e EPI.

## Saidas

- Registro de devolucao/descarte por item.
- Estorno sem apagar historico.
- Historico auditavel de todas as acoes sensiveis.
- Relatorios juridicos e gerenciais exportaveis.

## Entidades principais

- `devolucao_epi_item`: retorno de item entregue.
- `entrega_estorno`: invalida logicamente entrega incorreta sem apagar prova.
- `auditoria`: log de eventos e autores.
- visoes/consultas de `cobertura` e `consumo`.

## Telas desta etapa

1. **Devolucao e descarte**
   - Localiza entrega/item e registra devolucao com motivo.
   - Suporta cenarios de desgaste, dano, extravio e desligamento.
2. **Estorno formal**
   - Registra correcao sem apagar entrega original.
   - Exige motivo e usuario responsavel.
3. **Consulta de trilha**
   - Exibe historico cronologico de operacoes por entidade/usuario.
4. **Relatorios e evidencias**
   - Gera ficha por periodo, historico por lote/CA, cobertura e consumo.

## Tabelas envolvidas

- `devolucao_epi_item`: retorno/descarte por item.
- `entrega_estorno`: estorno de item entregue.
- `auditoria`: trilha append-only.
- `entrega_epi` e `entrega_epi_item`: base para correlacao.
- `lote_epi`: base para consumo e custo.
- `trabalhador`, `funcao`, `unidade`: dimensoes para visoes gerenciais.

## Relacionamentos chave

- `entrega_epi_item` 0..1:1 `devolucao_epi_item`
- `entrega_epi_item` 0..1:1 `entrega_estorno`
- `entrega_epi` 1:N `entrega_epi_item`
- `entrega_epi_item` N:1 `lote_epi`
- `auditoria` N:1 `entidade` (relacionamento logico por `entidade` + `entidade_id`)

## Preparo para 8 unidades (modelo distribuido)

- Relatorios devem ter filtro obrigatorio por `unidade_id` e opcionalmente consolidacao corporativa.
- Estruturar views de consumo/cobertura com chave de particao por unidade e periodo.
- Planejar retencao e backup centralizados por unidade quando migrar para cliente-servidor.
- Garantir que estorno/devolucao sejam replicados com ordenacao temporal para nao quebrar trilha juridica.
- Definir politica de consolidacao: fechamento mensal por unidade e visao corporativa agregada.

## Regras de negocio essenciais

1. Devolucao nao pode ter data anterior a entrega.
2. Cada item entregue permite no maximo uma devolucao formal.
3. Estorno nao remove dado; registra evento corretivo com motivo.
4. Auditoria e append-only: sem edicao/exclusao de trilha.
5. Relatorios devem ser reproduziveis para o mesmo recorte de dados.
6. Operacoes sensiveis exigem usuario autenticado e papel autorizado.

## Validacoes minimas recomendadas

- Motivo de devolucao em dominio controlado.
- Motivo de estorno obrigatorio e nao vazio.
- Usuario responsavel registrado em devolucao/estorno.
- Registro de timestamp em todos os eventos.

## Fluxo operacional da etapa

1. Localizar item entregue.
2. Registrar devolucao/descarte com data, motivo e responsavel.
3. Se houve erro na entrega, registrar estorno formal.
4. Atualizar visoes de cobertura e pendencias.
5. Gerar relatorios conforme necessidade operacional/juridica.

## Relatorios minimos desta etapa

1. Ficha por trabalhador e periodo (entrega, devolucao, estorno).
2. Historico por EPI/CA/lote.
3. Pendencias de devolucao em desligamento.
4. Cobertura por trabalhador ativo (matriz x vigente).
5. Consumo por unidade/setor/funcao para suporte ao budget.

## Indicadores desta etapa

- Taxa de devolucao por motivo.
- Taxa de extravio por unidade/setor.
- Percentual de entregas estornadas.
- Tempo medio entre entrega e devolucao por tipo de EPI.
- Custo consumido por periodo (derivado de lote/custo unitario).

## Riscos se esta etapa falhar

- Perda de valor probatorio em fiscalizacao/processo.
- Distorcao nos numeros de cobertura e consumo.
- Decisoes de budget com base em dados incompletos.
- Falta de confianca do key user no sistema.

