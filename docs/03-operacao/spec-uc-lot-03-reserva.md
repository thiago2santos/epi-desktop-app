# Spec curta - UC-LOT-03 (Reservar e liberar)

## Identificacao

- ID: `UC-LOT-03`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-EST`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois do `UC-LOT-01`
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - peca separada para um atendimento continua parecendo livre, e a compra vem curta ou o balcao promete a mesma peca duas vezes.
- Ator principal: `Almoxarife`. Tambem `Admin`.
- Impacto se nao resolver:
  - a necessidade de compra ignora o que ja tem dono.

## 2) Escopo

- Comportamento no escopo:
  - criar uma reserva sobre o disponivel vigente de um lote;
  - gravar `RESERVA` e uma reserva com quantidade restante;
  - liberar no todo ou em parte, gravando `LIBERACAO_RESERVA`;
  - referencia livre e opcional (separacao, pedido), sem exigir `UC-SOL-01`.
- Fora de escopo:
  - a solicitacao criar reserva sozinha (o `UC-SOL-01` segue sem reservar);
  - a entrega baixar a reserva (contrato do `UC-ENT-01`);
  - reservar sem lote, "no EPI" em geral.

## 3) Regras de negocio

1. So lote vigente. Disponivel zero ou lote vencido recusa.
2. Quantidade inteira, maior que zero, menor ou igual ao disponivel vigente.
3. A reserva nasce com quantidade restante igual a quantidade. Liberacao diminui a restante e nao passa dela.
4. Liberar pede confirmacao. Cancelar nao grava.
5. Reserva nao e fornecimento e nao entra no consumo nem na ficha do trabalhador.
6. Nao ha delete. Liberar ate zerar encerra a reserva.
7. `SESMT` e `Consulta` nao reservam.
8. Auditoria: `LOTE_RESERVADO` e `LOTE_RESERVA_LIBERADA`, entidade `LOTE`, id do lote. A reserva tem id proprio citado no detalhe.

## 4) Catalogo de erros

`LOT-002`, `LOT-006`, `LOT-008`, `LOT-009`, `LOT-010`, `AUTH-004`.

## 5) Criterios de aceite

- `CA-01`: reservar 2 de um lote com disponivel 5 deixa reservada 2 e disponivel 3. Fisica intacta.
- `CA-02`: reservar acima do disponivel recusa com `LOT-008` e nao grava.
- `CA-03`: lote vencido recusa com `LOT-009`.
- `CA-04`: liberar parte da reserva diminui so o restante. Liberar acima recusa com `LOT-010`.
- `CA-05`: cancelar a confirmacao de liberar mantem a reserva.
- `CA-06`: a necessidade de compra (`UC-LOT-06`) nao soma de novo essa quantidade.
- `CA-07`: auditoria dos dois eventos.

## 6) Tela

Na linha do lote vigente, acao "Reservar". No detalhe da reserva, "Liberar".

- Campos: quantidade e referencia opcional ("Separacao ou pedido").
- Salvar desabilitado sem quantidade.
- A lista de reservas do lote mostra restante e referencia.
- Liberar: "Liberar a reserva de N?" Confirmar / Cancelar.
- Sucesso: "Reserva registrada." ou "Reserva liberada."
- Lote vencido nao oferece Reservar.

## 7) Seguranca e auditoria

- Eventos: `LOTE_RESERVADO`, `LOTE_RESERVA_LIBERADA`.
- Risco: reservar peca vencida ou deixar a compra contar a reserva como falta.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-lot-03.md`
- `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md` (continua sem reservar)
