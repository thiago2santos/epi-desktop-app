# Matriz executavel de testes - UC-ENT-01 (Fornecimento)

Spec: `docs/03-operacao/spec-uc-ent-01-fornecimento.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| ENT01-001 | INT/AUDIT | CA-01, CA-10 | Trabalhador ativo; item na matriz; lote vigente qtd 5; custo 10 | Confirmar 1, ciencia e termo | Ficha imutavel; CA do lote; disponivel 4; `BAIXA_FORNECIMENTO` com setor, funcao e custo 10; `FORNECIMENTO_REGISTRADO` |
| ENT01-002 | INT | CA-02, `ENT-004` | Lote vencido | Confirmar | `ENT-004`; sem ficha e sem movimento |
| ENT01-003 | INT | CA-03, `ENT-005` | Disponivel 1 | Pedir 2 | `ENT-005` |
| ENT01-004 | RBAC/INT | CA-04, `ENT-006` | Item fora da matriz; `ALMOXARIFE`; texto longo | Confirmar | `ENT-006` |
| ENT01-005 | INT | CA-05 | Item fora da matriz; `SESMT`; texto com 10 caracteres | Confirmar | Ficha com excecao |
| ENT01-006 | INT | `ENT-007` | `SESMT`; texto com 3 caracteres | Confirmar | `ENT-007` |
| ENT01-007 | UI | CA-06 | Revisao pronta | Voltar no dialogo | Nada gravado |
| ENT01-008 | INT | CA-07 | Falha forcada no movimento | Confirmar | Sem ficha, sem termo, sem auditoria de sucesso |
| ENT01-009 | INT | CA-08 | Reserva restante 2 no lote; disponivel 5 | Ligar a reserva e fornecer 2 | Fisica cai 2; restante da reserva 0; outra reserva intacta |
| ENT01-010 | INT | CA-09, `ENT-008` | Matriz exige treinamento | Confirmar sem data | `ENT-008` |
| ENT01-011 | INT | `ENT-012` | Mesmo lote em duas linhas | Confirmar | `ENT-012` |
| ENT01-012 | INT | Regra 12 | Lote sem custo | Confirmar | Movimento com custo nulo; ficha gravada |
| ENT01-013 | RBAC | Regra 14 | `CONSULTA` | Abrir a confirmacao | `AUTH-004` |
| ENT01-014 | INT | Regra 2 | Relogio fixo | Confirmar | Data da ficha igual ao relogio |

## Gate para iniciar o codigo

`ENT01-001`, `ENT01-002`, `ENT01-003`, `ENT01-004`, `ENT01-007`, `ENT01-008`.
