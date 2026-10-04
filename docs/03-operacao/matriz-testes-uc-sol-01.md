# Matriz executavel de testes - UC-SOL-01 (Solicitar EPI)

Spec: `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md`, secao 15. Nenhum teste existe em codigo ainda.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| SOL01-001 | INT/AUDIT | CA-SOL-03 | Gestor com trabalhador no escopo; EPI na matriz | Enviar quantidade 1 e motivo | Pedido `APROVADA`; snapshot gravado; sem movimento de estoque; auditoria de envio |
| SOL01-002 | INT | CA-SOL-05, `SOL-007` | EPI fora da matriz | Enviar | Estado `PENDENTE_ANALISE`; gestor nao aprova |
| SOL01-003 | INT | CA-SOL-04, `SOL-006` | Pedido em aberto do par | Enviar de novo | Nenhum pedido novo; frase do existente |
| SOL01-004 | RBAC/SEC | CA-SOL-01, `SOL-002` | Trabalhador de outro gestor | Buscar a matricula | "Nenhum trabalhador no seu escopo com essa matricula ou nome."; sem ficha do trabalhador |
| SOL01-005 | INT | `SOL-010` | O proprio gestor como destinatario | Enviar | Recusa; sem pedido |
| SOL01-006 | INT | Regra 7 | Pedido `PENDENTE_ANALISE` | SESMT aprova | Estado `APROVADA` |
| SOL01-007 | INT | Regra 7 | Pedido `PENDENTE_ANALISE`; texto curto | SESMT recusa | Recusa sem gravar; texto exige 10 caracteres |
| SOL01-008 | INT | CA-SOL-06 | Qualquer envio | Consultar o diario | Nenhum movimento |
| SOL01-009 | INT | CA-SOL-07 | Pedido aprovado de 4; disponivel 4 | Fornecer 2 pelo `UC-ENT-01` com o pedido | Ficha de 2; pedido `PARCIALMENTE_ATENDIDA`; atendido 2 |
| SOL01-010 | INT | CA-SOL-07 | Saldo do pedido 2 | Fornecer 3 ligados ao pedido | Ficha nao grava; pedido intacto |
| SOL01-011 | INT | Regra 9 | Pedido aprovado sem atendimento | Gestor cancela | `CANCELADA` |
| SOL01-012 | INT | Regra 9 | Pedido parcial | Gestor cancela | `SOL-009`; pedido segue parcial |
| SOL01-013 | INT | CA-SOL-10 | Um aprovado de saldo 3, um em analise, um cancelado | Abrir demanda | Soma em aberto 3; analise e cancelado fora |
| SOL01-014 | INT | Regra 13 | Nenhuma carga CAEPI completa | Enviar | `SOL-004` |
| SOL01-015 | UI | CA-SOL-14 | Gestor sem trabalhadores | Abrir | "Nenhum trabalhador no seu escopo." |
| SOL01-016 | RBAC | `SOL-001` | `CONSULTA` | Abrir a solicitacao | `AUTH-004` ou `SOL-001` |

## Gate para iniciar o codigo

`SOL01-001`, `SOL01-002`, `SOL01-003`, `SOL01-004`, `SOL01-008`, `SOL01-009`.
