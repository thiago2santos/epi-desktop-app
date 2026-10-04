# Spec curta - UC-LOT-05 (Inventario)

## Identificacao

- ID: `UC-LOT-05`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-EST`
- Responsavel: Time Easy NR6
- Status: especificado; feature propria, codigo depois do recebimento
- Modelo: `docs/03-operacao/modelo-diario-estoque.md`

## 1) Contexto

- Problema real:
  - prateleira e sistema divergem. Acertar o saldo no recebimento apaga a explicacao da diferenca.
- Ator principal: `Almoxarife` conta. `Admin` confirma. `SESMT` e `Consulta` leem o inventario encerrado.
- Impacto se nao resolver:
  - compra e consumo passam a confiar num fisico que ninguem contou.

## 2) Escopo

- Comportamento no escopo:
  - abrir um inventario de uma unidade, com uma linha por lote de fisica positiva ou reservada positiva;
  - mostrar o fisico esperado e a reservada;
  - o operador informa a quantidade contada;
  - confirmar grava `AJUSTE_INVENTARIO` so onde contagem e fisico diferem;
  - cancelar ou abandonar nao grava ajuste.
- Fora de escopo:
  - inventario ciclico por corredor ou codigo de barras;
  - contar EPI sem lote;
  - dois inventarios abertos da mesma unidade.

## 3) Regras de negocio

1. Uma unidade so tem um inventario aberto.
2. Toda linha precisa de quantidade contada inteira, maior ou igual a zero, antes de confirmar.
3. Contagem menor que a reservada daquele lote recusa o inventario inteiro. A reserva se libera antes, no `UC-LOT-03`.
4. O ajuste altera so a fisica. Reservada permanece.
5. Diferenca zero nao gera movimento.
6. Confirmacao obrigatoria, com o resumo de quantos lotes sobem, descem ou ficam iguais.
7. Inventario encerrado nao reabre. Nova contagem e outro inventario.
8. O recebimento original nao e editado.
9. Auditoria: `INVENTARIO_ABERTO` e `INVENTARIO_CONFIRMADO`, entidade `INVENTARIO`.

## 4) Catalogo de erros

`LOT-005`, `LOT-013`, `LOT-014`, `LOT-015`, `AUTH-004`.

Segundo inventario aberto na mesma unidade recusa com `LOT-015` no sentido de "ja existe inventario aberto". O texto de tela desse caso: "Esta unidade ja tem um inventario aberto."

## 5) Criterios de aceite

- `CA-01`: abrir lista os lotes com fisico esperado e reservada.
- `CA-02`: confirmar com contagem igual nao grava ajuste.
- `CA-03`: contagem maior grava ajuste positivo. A fisica passa a ser a contagem. A reservada nao muda.
- `CA-04`: contagem menor que a reservada recusa com `LOT-014` e nao grava ajuste nenhum.
- `CA-05`: linha em branco recusa com `LOT-013`.
- `CA-06`: cancelar a confirmacao deixa o inventario aberto e a fisica intacta.
- `CA-07`: o consumo do budget nao inclui o ajuste. O total de ajustes do periodo inclui.

## 6) Tela

Destino proprio no grupo Estoque: "Inventario". Nao mistura com o formulario de recebimento.

- Escolhe a unidade e abre a contagem.
- Grade: lote, EPI, tamanho, fisico esperado, reservada, quantidade contada, diferenca calculada.
- Vazio: "Nenhum lote para contar nesta unidade."
- Confirmar desabilitado enquanto faltar contagem.
- Dialogo com o resumo. Cancelar volta para a grade.
- Sucesso: "Inventario confirmado."
- Situacao do inventario em texto: Aberto, Confirmado.

## 7) Seguranca e auditoria

- Eventos: `INVENTARIO_ABERTO`, `INVENTARIO_CONFIRMADO`.
- Risco: ajuste silencioso que vira "consumo" no budget.

## 8) Documentacao impactada

- `docs/03-operacao/modelo-diario-estoque.md`
- `docs/03-operacao/matriz-testes-uc-lot-05.md`
