# Matriz executavel de testes - UC-CAD-07 (GHE)

## Objetivo

Cenarios do cadastro de GHE e do perfil vigente. Cobertos por `GhePolicyTest`, `GheManagementServiceIntegrationTest` e `CadastroGheUxTest`. A lista de EPI ainda nao existe: o vazio do GHE ativo esta em `GhePolicy.listaVigente`. O tipo do perfil (GHE ou funcao) esta no teste de integracao.

## Vinculo com a especificacao

- Documento base: `docs/03-operacao/spec-uc-cad-07-ghe.md`
- Rastreio: regra da secao 3, criterio da secao 5, erro da secao 4.

## Legenda

- `UNIT`: policy
- `INT`: caso de uso + repositorio + banco
- `RBAC`: autorizacao por papel
- `AUDIT`: trilha append-only
- `UI`: formulario

## Matriz

| ID | Tipo | Referencia | Regra alvo | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|---|
| CAD07-001 | INT/AUDIT | Regras 1-3, CA-01, CA-09 | Fluxo feliz | `SESMT`; unidade ativa | Criar "Ruido caldeira", ativo | Registro na unidade; evento `GHE_CRIADO` |
| CAD07-002 | UNIT/INT | Regras 2, `CAD-051` | Obrigatorios | Contexto pronto | Criar sem nome ou sem unidade | `CAD-051`; nada gravado |
| CAD07-003 | INT | Regras 2, CA-02, `CAD-052` | Nome na unidade | GHE "Ruido" na unidade | Criar " ruido " na mesma unidade | `CAD-052` |
| CAD07-004 | INT | Regras 2, CA-02 | Outra unidade | GHE "Ruido" na unidade A | Criar "Ruido" na unidade B | Gravado |
| CAD07-005 | INT/AUDIT | Regras 4-5, CA-03, CA-09 | Vinculo | Funcao ativa da unidade, sem GHE | Vincular e vincular de novo | Uma linha em `ghe_job_role`; segundo vinculo nao duplica; evento `GHE_FUNCAO_VINCULADA` uma vez |
| CAD07-006 | INT | Regras 4, CA-04, `CAD-054` | Funcao invalida | Funcao inativa ou de outra unidade | Vincular | `CAD-054` |
| CAD07-007 | INT | Regras 5, CA-04, `CAD-055` | Outro grupo | Funcao ja no GHE A | Vincular ao GHE B | `CAD-055`; continua no A |
| CAD07-008 | UI/INT | Regras 6, CA-05 | Desvincular | Funcao membro | Cancelar e depois confirmar | Cancelar preserva; confirmar remove e gera `GHE_FUNCAO_DESVINCULADA` |
| CAD07-009 | INT/AUDIT | Regras 7, CA-06, CA-09 | Status | GHE ativo com funcao | Inativar e reativar | Id e vinculo preservados; eventos `GHE_INATIVADO` e `GHE_REATIVADO`; sem delete |
| CAD07-010 | INT | Regras 9, CA-06, CA-07 | Perfil vigente | Funcao com matriz propria e membro de GHE | Ler o perfil com GHE ativo, GHE ativo sem EPI, GHE inativo e funcao desvinculada | Ativo: so a lista do GHE. Ativo sem EPI: lista vazia. Inativo ou desvinculada: lista da funcao |
| CAD07-011 | INT | Regras 8 | Funcao inativada | Funcao membro e depois inativada no `UC-CAD-02` | Listar candidatos a vinculo novo | A funcao nao aparece para vinculo novo; o vinculo antigo permanece |
| CAD07-012 | RBAC | CA-08 | Admin e SESMT | `ADMIN` e `SESMT` | Criar, editar, vincular, inativar | Permitido |
| CAD07-013 | RBAC | Regra 10, CA-08, `AUTH-004` | Sem permissao | `ALMOXARIFE` e `CONSULTA` | Tentar mutar | `AUTH-004` |
| CAD07-014 | AUDIT | CA-09 | Trilha | Mutacao autorizada | Criar, editar nome, vincular, desvincular, inativar, reativar | Cada evento com ator, acao, entidade `GHE` e id |
| CAD07-015 | UI | Tela, CA-05 | Formulario | Tela aberta | Salvar valido; cancelar desvinculo; provocar `CAD-055` | Sucesso na lista; vinculo preservado no cancelamento; erro sem expor `CAD-055` |

## Gate para iniciar o codigo

`CAD07-001`, `CAD07-003`, `CAD07-005`, `CAD07-007`, `CAD07-010`, `CAD07-013`.
