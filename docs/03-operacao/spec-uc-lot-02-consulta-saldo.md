# Spec curta - UC-LOT-02 (Consulta de saldo e validade)

## Identificacao

- ID: `UC-LOT-02`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-EST`
- Responsavel: Time Easy NR6
- Status: implementado na tela Lotes e saldos (`LotManagementView`)
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - o almoxarife precisa ver o que pode fornecer sem somar planilha.
- Atores: `Almoxarife`, `SESMT`, `Consulta`, `Admin`.
- Impacto se nao resolver:
  - peca vencida ou ja reservada parece livre.

## 2) Escopo

- Comportamento no escopo:
  - listar lotes da unidade, com filtro por EPI, codigo, tamanho e situacao (vigente, vencido, esgotado);
  - mostrar fisica, reservada, disponivel e validade;
  - disponivel de lote vencido e zero, mesmo com fisica positiva;
  - lista vazia quando o filtro nao acha, sem erro.
- Fora de escopo:
  - escolher o lote dentro do wizard de entrega (o wizard, no M2, consome esta mesma leitura);
  - exportar planilha.

## 3) Regras de negocio

1. A leitura e derivada dos movimentos. A tela nao tem saldo digitado.
2. Situacao: Vencido quando a validade e anterior a hoje; Esgotado quando a fisica e zero; Vigente nos demais, ainda que o disponivel seja zero por reserva.
3. Ordenacao padrao: validade mais proxima primeiro, para a peca que vence antes aparecer no topo.
4. Todos os papeis do shell que enxergam Estoque podem consultar. Nenhum papel altera quantidade nesta tela.
5. Falha de leitura mostra erro amigavel e nao uma lista pela metade.

## 4) Catalogo de erros

Nao ha erro de dominio novo. Falha tecnica de leitura usa a frase "Nao foi possivel consultar os lotes." `AUTH-004` se o papel nao ve o modulo.

## 5) Criterios de aceite

- `CA-01`: lote vigente mostra as tres quantidades coerentes com os movimentos.
- `CA-02`: lote vencido com fisica positiva mostra disponivel zero e situacao Vencido.
- `CA-03`: lote com reserva mostra disponivel menor que a fisica, e a reserva visivel.
- `CA-04`: filtro sem resultado mostra lista vazia com texto, sem mensagem de erro.
- `CA-05`: ordenacao traz a validade mais proxima primeiro.
- `CA-06`: `Consulta` ve a lista e nao ve acao de recebimento, reserva ou baixa.

## 6) Tela

Mesma pagina "Lotes e saldos" do recebimento. Quem nao pode receber ve a lista e o filtro, sem o formulario.

- Filtro: "EPI, lote ou tamanho".
- Colunas com rotulo Fisica, Reservada, Disponivel, Validade, Situacao.
- Vazio com filtro: "Nenhum lote com esse filtro."
- Vazio sem movimento: o texto do `UC-LOT-01`.
- Sem codigo interno na grade.

## 7) Seguranca e auditoria

- Consulta nao gera evento de auditoria por abertura de lista.
- Falha de leitura e recusa `AUTH-004` na consulta registram log de troubleshooting, sem linha nova na auditoria.
- Risco: esconder a reserva e fazer o operador fornecer peca ja separada.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-lot-02.md`
- `docs/04-uml/estoque/UC-LOT-02 — Consultar saldo e validade de lotes.md`
