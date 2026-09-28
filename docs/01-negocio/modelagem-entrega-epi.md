# Modelagem — Entrega de EPI (NR-6)

## Objetivo

Modelar a parte de entrega/devolucao de EPI com validade juridica, atendendo ao item 6.5.1 (d) da NR-6 (registro de fornecimento) e permitindo extracao de relatorio (6.5.1.1).

## Escopo desta modelagem

- Registro formal de entrega por trabalhador.
- Registro de devolucao/descarte por item entregue.
- Registro de ciencia/validacao do trabalhador por item.
- Registro do CA do EPI entregue (incluindo EPI conjugado com mais de um CA).
- Termo de responsabilidade (item 6.6.1) aceito no ato da entrega.
- Trilha de auditoria e imutabilidade do historico.

## Campos obrigatorios cobertos

### 1) Cabecalho
- Empresa: razao social, CNPJ.
- Trabalhador: nome, matricula (ou CPF), funcao/cargo, setor.

### 2) Corpo da movimentacao por item
- Data/hora da entrega.
- Descricao do EPI.
- Numero do CA (um ou varios, para conjugados).
- Quantidade.
- Motivo da entrega (primeira entrega, substituicao, extravio etc.).
- Data de devolucao/descarte.
- Validacao do trabalhador (assinatura/biometria/assinatura digital).

### 3) Termo de responsabilidade
- Texto padrao vinculado ao evento.
- Registro de aceite (data/hora, metodo e evidencias de validacao).

## Modelo conceitual (resumo)

- `empresa` 1---N `trabalhador`
- `epi` 1---N `epi_ca`
- `epi` 1---N `lote_epi`
- `funcao` N---N `epi` (via `matriz_funcao_epi`)
- `entrega_epi` 1---N `entrega_epi_item`
- `entrega_epi_item` 1---N `entrega_item_ca`
- `entrega_epi_item` 0..1---1 `devolucao_epi_item`
- `entrega_epi` 1---1 `termo_responsabilidade_aceite`
- Todos os eventos relevantes geram `auditoria`

## Regras de negocio essenciais

1. **Imutabilidade legal**: entrega e devolucao nao sofrem `UPDATE`/`DELETE`; erro operacional vira `estorno`.
2. **CA obrigatorio na entrega**: cada item entregue precisa ter pelo menos um CA registrado.
3. **Conjugado**: EPI com componentes deve registrar todos os CAs em `entrega_item_ca`.
4. **Lote valido**: nao permitir entrega com lote vencido na data da entrega.
5. **Matriz de funcao**: entrega fora da matriz exige motivo de excecao e usuario autorizador.
6. **Aceite do trabalhador**: cada item entregue precisa de metodo de validacao e evidencia minima.
7. **Devolucao unica por item**: item entregue pode ter no maximo uma devolucao associada.
8. **Rastreabilidade**: cada registro deve guardar usuario operador, data/hora e origem da operacao.

## Dicionario de tabelas

- `empresa`: dados institucionais.
- `trabalhador`: cadastro de empregado.
- `funcao`: funcao/cargo e setor.
- `epi`: catalogo de EPIs.
- `epi_ca`: historico de CAs do EPI.
- `lote_epi`: lote fisico recebido (fabricante, lote, validade da peca, saldo).
- `matriz_funcao_epi`: define obrigatoriedade por funcao/GHE.
- `entrega_epi`: cabecalho da entrega.
- `entrega_epi_item`: itens entregues (quantidade, motivo, lote, ciencia).
- `entrega_item_ca`: CAs efetivamente vinculados ao item entregue.
- `devolucao_epi_item`: devolucao/descarte do item entregue.
- `termo_responsabilidade_aceite`: aceite do termo legal no evento.
- `auditoria`: trilha append-only de eventos sensiveis.

## Fluxo minimo de tela (POC)

1. Selecionar trabalhador ativo.
2. Carregar matriz da funcao e itens pendentes de vigencia/troca.
3. Selecionar lote valido e quantidade.
4. Capturar validacao do trabalhador por item (assinatura/biometria/digital).
5. Capturar aceite do termo de responsabilidade.
6. Confirmar entrega (transacao unica): grava cabecalho, itens, CAs por item, termo e auditoria.
7. Em devolucao/descarte: selecionar item entregue, informar data/motivo/responsavel e confirmar.

## Validacoes recomendadas

- CNPJ em formato valido.
- CPF opcional se matricula for identificador principal.
- `data_entrega <= agora`.
- `quantidade > 0`.
- `metodo_validacao IN ('ASSINATURA_MANUAL', 'ASSINATURA_DIGITAL', 'BIOMETRIA')`.
- `motivo_entrega` controlado por dominio.
- Impedir devolucao anterior a entrega.

## Relatorios obrigatorios (minimos)

1. Ficha por trabalhador e periodo (entregas/devolucoes com CA e validacao).
2. Posicao de cobertura por trabalhador ativo (matriz x itens vigentes).
3. Historico por EPI/CA/lote para auditoria/fiscalizacao.

## Texto sugerido para termo de responsabilidade

"Eu, [Nome do Funcionario], declaro que recebi gratuitamente os EPIs constantes nesta ficha, adequados ao risco da minha atividade e em perfeito estado de conservacao. Comprometo-me a utiliza-los estritamente para a finalidade a que se destinam, responsabilizando-me por sua guarda e conservacao, e a comunicar imediatamente a empresa qualquer dano, extravio ou alteracao que os torne improprios para uso, conforme determina a NR-6."

