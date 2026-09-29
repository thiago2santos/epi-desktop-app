# Glossario ubiquo (codigo em ingles, negocio em pt-BR)

## Objetivo

Evitar mistura de idiomas e evitar traducoes literais erradas no codigo.
Este glossario define os termos oficiais para novas implementacoes.

## Termos canônicos de dominio

| Negocio (pt-BR) | Codigo (en) | Observacao |
| --- | --- | --- |
| Trabalhador | Employee | Evitar `Worker` neste projeto. |
| Funcao | JobRole | Papel ocupacional do colaborador, diferente de permissao de acesso. |
| Setor | Department | Area organizacional. |
| Papel de acesso | Role | Permissao de sistema (Admin, SESMT etc.). |
| Credencial | Credential | Inclui senha e estado de ciclo de vida. |
| Autenticacao | Authentication | Validacao de identidade. |
| Autorizacao | Authorization | Decisao de permissao por papel. |
| Trilha de auditoria | AuditTrail | Registro append-only de eventos criticos. |
| Lote de estoque | StockLot | Nome recomendado para o modulo de estoque. |
| Entrega de EPI | PPEIssue | Evento juridico de entrega. |
| Devolucao | Return | Devolucao de item entregue. |
| Estorno | Reversal | Correcao formal sem apagar evento original. |

## Regras de uso

1. Novos nomes de classe/metodo/variavel devem seguir o termo canônico.
2. Termos legados em pt-BR podem permanecer ate refatoracao segura.
3. Ao tocar arquivo legado, normalizar nomes afetados quando o risco for baixo.
4. UI e mensagens ao usuario final continuam em pt-BR.

## Escopo de migracao incremental

- **Agora**: aplicar em todo codigo novo (M1 em diante).
- **Progressivo**: refatorar trechos legados quando forem alterados por feature.
- **Sem big-bang**: evitar renomeacao global sem necessidade funcional.
