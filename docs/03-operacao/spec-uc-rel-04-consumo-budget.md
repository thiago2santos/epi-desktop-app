# Spec curta - UC-REL-04 (Consumo para budget)

## Identificacao

- ID: `UC-REL-04`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-REL`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois de existir `BAIXA_FORNECIMENTO`
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - o gestor de seguranca do trabalho precisa estimar o budget com o que foi fornecido, nao com o que foi comprado nem com o que se perdeu na prateleira.
- Atores: `SESMT`, `Admin`, `Consulta`. `Almoxarife` nao fecha budget.
- Impacto se nao resolver:
  - perda, ajuste de inventario e reserva entram como se fossem consumo da area.

## 2) Escopo

- Comportamento no escopo:
  - no periodo, somar o consumo por EPI e o consumo geral;
  - recortar por unidade, setor e funcao quando a baixa de fornecimento tiver esses snapshots;
  - valor = quantidade vezes o custo unitario gravado na propria baixa;
  - mostrar a parte, no mesmo relatorio, o total de perdas (`BAIXA_PRATELEIRA`) e o total de ajustes (`AJUSTE_INVENTARIO`), sem soma-los ao consumo;
  - marcar linha com custo nulo como custo incompleto e excluir so o valor, nao a quantidade, dessa linha.
- Fora de escopo:
  - centro de custo contabil, cotacao e ordem de compra;
  - o desenho do arquivo (a exportacao usa o `UC-TRV-03`; o calculo continua o desta spec);
  - prever o proximo mes (isso e demanda do `UC-LOT-06`).

## 3) Regras de negocio

1. Consumo do periodo = soma das `BAIXA_FORNECIMENTO` com data no periodo, menos as `ESTORNO_FORNECIMENTO` do periodo que apontam para essas baixas. Estorno de fornecimento antigo, feito neste periodo, diminui o consumo deste periodo.
2. Reserva, liberacao e recebimento nao entram.
3. Perda e ajuste aparecem em blocos proprios, com quantidade e valor pelo custo do recebimento de origem quando houver.
4. O agrupamento por setor e funcao usa o snapshot da baixa, nao o cadastro atual do trabalhador.
5. Sem movimento no periodo, o relatorio abre com totais zero e o texto de vazio. Nao e erro.
6. `Almoxarife` recebe `AUTH-004` nesta tela.

## 4) Catalogo de erros

Nao ha codigo `LOT-` novo. Periodo invalido (fim antes do inicio) recusa na tela, sem gravar: "A data final precisa ser igual ou posterior a inicial." `AUTH-004` para almoxarife.

## 5) Criterios de aceite

- `CA-01`: duas baixas do mesmo EPI no periodo somam a quantidade e o valor no item e no total geral.
- `CA-02`: estorno no periodo reduz o consumo.
- `CA-03`: baixa de prateleira nao altera o consumo e aparece em perdas.
- `CA-04`: ajuste de inventario nao altera o consumo e aparece em ajustes.
- `CA-05`: reserva nao altera nenhum dos tres totais.
- `CA-06`: baixa sem custo entra na quantidade e o valor da linha fica "custo incompleto".
- `CA-07`: filtro por funcao usa a funcao gravada na baixa, mesmo que o trabalhador tenha mudado de funcao depois.
- `CA-08`: periodo sem movimento mostra zeros e "Nenhum consumo no periodo."

## 6) Tela

Destino no grupo Relatorios: "Consumo para budget".

- Filtros abertos: periodo e unidade. Setor, funcao e EPI a um clique.
- Tres blocos com titulo: Consumo, Perdas, Ajustes de inventario.
- Consumo tem a linha do item e a linha "Total do periodo".
- Vazio: "Nenhum consumo no periodo."
- Custo incompleto em texto na linha, nao so em cor.
- Sem `Alert` de sucesso. A tela e a consulta.

## 7) Seguranca e auditoria

- Consulta nao grava movimento.
- Risco: somar perda no budget de EPI fornecido e inflar a estimativa da area.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-rel-04.md`
- `docs/04-uml/casos-de-uso-uml.md`
