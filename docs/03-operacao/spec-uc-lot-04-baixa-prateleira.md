# Spec curta - UC-LOT-04 (Baixa de prateleira)

## Identificacao

- ID: `UC-LOT-04`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-EST`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois do `UC-LOT-01`
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - peca vencida, perdida ou descartada na prateleira continua na fisica e distorce consumo e compra.
- Ator principal: `Almoxarife`. Tambem `Admin`.
- Impacto se nao resolver:
  - o budget de seguranca mistura perda com o que o trabalhador recebeu.

## 2) Escopo

- Comportamento no escopo:
  - baixar quantidade do disponivel com motivo `VENCIMENTO`, `PERDA` ou `DESCARTE`;
  - gravar `BAIXA_PRATELEIRA`;
  - a reserva nao e consumida por esta baixa.
- Fora de escopo:
  - devolucao ou descarte de peca ja fornecida ao trabalhador (isso e o fluxo de devolucao da entrega);
  - fornecer a peca (nao gera ficha nem `BAIXA_FORNECIMENTO`).

## 3) Regras de negocio

1. Motivo obrigatorio, um dos tres.
2. Quantidade inteira, maior que zero, menor ou igual ao disponivel. Disponivel de lote vencido para fornecimento e zero, mas a baixa de prateleira pode usar a fisica que nao esta reservada. Para vencimento, o teto e fisica menos reservada, mesmo que a leitura "disponivel para fornecer" esteja zerada pelo prazo.
3. Peca reservada nao entra na baixa. O operador libera a reserva antes.
4. Confirmacao obrigatoria. Cancelar nao grava.
5. A baixa e perda, nao consumo. O `UC-REL-04` mostra esse total a parte.
6. Nao ha delete do movimento.
7. Auditoria: `LOTE_BAIXA_PRATELEIRA`, entidade `LOTE`.

## 4) Catalogo de erros

`LOT-002`, `LOT-006`, `LOT-011`, `LOT-012`, `AUTH-004`.

O `LOT-011` compara com fisica menos reservada, nao com o disponivel de fornecimento. Lote vencido ainda pode ser baixado se essa sobra for positiva.

## 5) Criterios de aceite

- `CA-01`: baixa por vencimento diminui a fisica e nao mexe na reservada.
- `CA-02`: quantidade acima de fisica menos reservada recusa com `LOT-011`.
- `CA-03`: sem motivo recusa com `LOT-012`.
- `CA-04`: cancelar a confirmacao mantem a fisica.
- `CA-05`: o consumo do `UC-REL-04` nao inclui esta quantidade. O total de perdas inclui.
- `CA-06`: auditoria com o motivo no detalhe.

## 6) Tela

Acao "Baixar da prateleira" no lote.

- Motivo em combo: Vencimento, Perda, Descarte. Quantidade.
- Salvar so liga com motivo e quantidade.
- Dialogo: "Baixar N do lote? Isso nao e fornecimento a trabalhador."
- Sucesso: "Baixa registrada."
- Texto de ajuda curto: a peca reservada precisa ser liberada antes.

## 7) Seguranca e auditoria

- Evento: `LOTE_BAIXA_PRATELEIRA`.
- Risco: usar esta baixa no lugar da entrega e a ficha do trabalhador ficar vazia.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-lot-04.md`
