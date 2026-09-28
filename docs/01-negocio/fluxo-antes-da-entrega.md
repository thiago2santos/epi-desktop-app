# Modelagem — Antes da entrega

## Objetivo

Garantir que a entrega de EPI ocorra com base tecnica, legal e operacional consistente, evitando registros invalidos na etapa core.

## Escopo desta etapa

- Cadastro de EPI e CA.
- Recebimento por lote.
- Controle de saldo por lote.
- Matriz por funcao/GHE.
- Parametros de periodicidade e regras de reposicao.

## Entradas

- Dados da empresa/unidade.
- Cadastro de funcoes, setores e GHE (quando houver).
- Informacoes do EPI (descricao, tipo, fabricante, CAs).
- Informacoes de recebimento (lote, validade da peca, quantidade, custo unitario).

## Saidas

- Base de EPIs habilitados para entrega.
- Lotes validos com saldo disponivel.
- Regras por funcao/GHE para sugerir e bloquear entrega.
- Parametros para calculo de cobertura e reposicao.

## Entidades principais

- `epi`: item de protecao individual.
- `epi_ca`: CAs associados ao EPI.
- `lote_epi`: lote fisico recebido (quantidade, saldo, validade).
- `funcao` e `ghe` (quando aplicavel): perfis de exposicao.
- `matriz_funcao_epi`: relacao do que cada funcao/GHE deve receber.
- `parametro_periodicidade`: janela esperada de troca por item/perfil.

## Telas desta etapa

1. **Cadastro de EPI e CA**
   - Campos: descricao, tipo, fabricante, CAs, status.
   - Acoes: criar, inativar, consultar historico de CA.
2. **Recebimento por lote**
   - Campos: unidade, EPI, lote, validade da peca, quantidade, custo unitario.
   - Acoes: entrada de lote, ajuste controlado, consulta de saldo.
3. **Matriz funcao/GHE x EPI**
   - Campos: unidade, funcao/GHE, EPI, obrigatoriedade, periodicidade.
   - Acoes: ativar/inativar regra, versionar vigencia.
4. **Painel de preparacao para entrega**
   - Visao de lotes validos, itens sem CA completo e riscos de ruptura.

## Tabelas envolvidas

- `unidade`: filial/planta (ex.: Itupeva).
- `setor`: area organizacional da unidade.
- `funcao`: cargo/função por unidade.
- `ghe`: grupo homogeneo de exposicao (opcional na V1, previsto no modelo).
- `epi`: cadastro mestre de EPI.
- `epi_ca`: CAs por EPI (um-para-muitos).
- `lote_epi`: recebimento por lote e saldo.
- `matriz_funcao_epi`: regras de fornecimento por funcao/GHE.
- `parametro_periodicidade`: regras de reposicao por perfil e vigencia.
- `auditoria`: rastreio de alteracoes de cadastro critico.

## Relacionamentos chave

- `unidade` 1:N `setor`
- `unidade` 1:N `funcao`
- `setor` 1:N `funcao`
- `epi` 1:N `epi_ca`
- `epi` 1:N `lote_epi`
- `funcao` N:N `epi` (via `matriz_funcao_epi`)
- `ghe` N:N `epi` (via `matriz_funcao_epi`, quando usado)
- `funcao/ghe` 1:N `parametro_periodicidade`

## Preparo para 8 unidades (modelo distribuido)

- Incluir `unidade_id` nas tabelas operacionais desta etapa (`setor`, `funcao`, `ghe`, `lote_epi`, `matriz_funcao_epi`, `parametro_periodicidade`).
- Definir unicidade por escopo de unidade (ex.: `UNIQUE(unidade_id, codigo_lote, epi_id)`).
- Tratar `epi` como catalogo corporativo e `lote_epi` como dado local de cada unidade.
- Permitir operacao local por unidade no piloto (Itupeva), mas com chave pronta para consolidacao corporativa.
- Evitar acoplamento a arquivo unico sem escopo: toda consulta de operacao deve filtrar por `unidade_id`.

## Regras de negocio essenciais

1. Nao permitir EPI sem CA registrado para itens que exigem CA.
2. Recebimento por lote deve guardar validade da peca e saldo inicial.
3. Saldo nao pode ficar negativo.
4. Matriz por funcao/GHE precisa estar ativa para orientar entrega automatica.
5. Entrega fora da matriz sera excecao (tratada na etapa de entrega).
6. Parametros de periodicidade devem ser versionados por vigencia.
7. Para EPI conjugado, os componentes e seus CAs devem ficar definidos previamente.

## Validacoes minimas recomendadas

- `numero_ca` em formato consistente e sem duplicidade indevida por EPI.
- `data_validade_peca >= data_recebimento` no lote.
- `quantidade_recebida > 0`.
- `custo_unitario >= 0`.
- `matriz_funcao_epi` com unicidade por funcao/GHE + EPI.

## Fluxo operacional da etapa

1. Cadastrar EPI e CAs.
2. Cadastrar funcao/GHE.
3. Definir matriz de obrigatoriedade por perfil.
4. Registrar recebimento por lote.
5. Validar saldo inicial e vigencia.
6. Liberar itens para a etapa de entrega.

## Riscos se esta etapa falhar

- Entrega sem respaldo legal (CA ausente/incorreto).
- Ruptura de estoque por saldo inconsistente.
- Distorcao no budget por custo/lote sem rastreabilidade.
- Alto volume de excecoes no balcao de entrega.

## Indicadores desta etapa

- Percentual de EPIs com CA completo.
- Lotes com validade proxima do vencimento.
- Divergencia entre consumo previsto (matriz) e estoque disponivel.
- Quantidade de excecoes por falta de parametrizacao previa.

