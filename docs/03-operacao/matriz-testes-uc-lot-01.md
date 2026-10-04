# Matriz executavel de testes - UC-LOT-01 (Recebimento)

Spec: `docs/03-operacao/spec-uc-lot-01-recebimento.md`. Testes: `LotPolicyTest`, `StockManagementServiceIntegrationTest`, `RecebimentoLoteUxTest`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| LOT01-001 | INT/AUDIT | CA-01, CA-07 | Almoxarife; EPI com CA ativo; unidade ativa | Receber 10, validade futura, custo 12,50 | Movimento `RECEBIMENTO`; fisica 10; reservada 0; disponivel 10; evento `LOTE_RECEBIDO` |
| LOT01-002 | INT | CA-02 | Contexto pronto | Receber sem custo | Custo nulo; quantidades gravadas |
| LOT01-003 | UNIT/INT | `LOT-001` | Contexto pronto | Omitir codigo, validade ou quantidade | `LOT-001`; nenhum movimento |
| LOT01-004 | UNIT/INT | `LOT-002` | Contexto pronto | Quantidade 0 ou 1,5 | `LOT-002` |
| LOT01-005 | INT | CA-03, `LOT-003` | Lote ja recebido | Repetir codigo, EPI, tamanho e unidade | `LOT-003` |
| LOT01-006 | INT | Regra 3 | Mesmo codigo em outra unidade | Receber | Gravado |
| LOT01-007 | INT | `LOT-004` | EPI sem CA ativo | Receber | `LOT-004` |
| LOT01-008 | INT | `LOT-005` | Unidade inativa | Receber | `LOT-005` |
| LOT01-009 | UNIT | `LOT-007` | Contexto pronto | Custo negativo | `LOT-007` |
| LOT01-010 | INT/UI | CA-04, CA-05 | Validade anterior a hoje | Confirmar o aviso | Disponivel 0; fisica igual a quantidade |
| LOT01-011 | UI | CA-05 | Validade vencida | Cancelar o dialogo | Nenhum movimento |
| LOT01-012 | RBAC | CA-06 | `SESMT` e `CONSULTA` | Tentar receber | `AUTH-004` |
| LOT01-013 | RBAC | CA-06 | `ADMIN` | Receber | Permitido |
| LOT01-014 | UI | Tela | Tela aberta sem lotes | Abrir a lista | "Nenhum lote recebido nesta unidade." e a acao de registrar |
| LOT01-015 | UI | Tela | Erro `LOT-003` | Salvar duplicado | Frase amigavel sem o codigo |

## Gate para iniciar o codigo

`LOT01-001`, `LOT01-003`, `LOT01-005`, `LOT01-007`, `LOT01-010`, `LOT01-012`.
