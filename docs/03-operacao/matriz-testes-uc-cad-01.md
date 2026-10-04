# Matriz executavel de testes - UC-CAD-01 (Unidade)

## Objetivo

Cenarios para validar o cadastro de unidade antes e durante a implementacao. Nenhum destes testes existe em codigo ainda: a matriz e o criterio de aceite do corte seguinte.

## Vinculo com a especificacao

- Documento base: `docs/03-operacao/spec-uc-cad-01-unidade.md`
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
| CAD01-001 | INT/AUDIT | Regras 1-2, CA-01, CA-08 | Fluxo feliz | `SESMT` autenticado; empresa Access existente | Criar unidade "Lagoa Santa", CNPJ valido de 14 digitos, ativa | Registro na `unit` da empresa seed; evento `UNIDADE_CRIADA` |
| CAD01-002 | UNIT/INT | Regras 2-3, `CAD-041` | Obrigatorios | Contexto pronto | Criar sem nome ou sem CNPJ | Bloqueio `CAD-041`; nada gravado |
| CAD01-003 | UNIT | Regras 3, CA-02 | Mascara | CNPJ com pontuacao | Normalizar `22.755.266/0002-68` | `22755266000268` |
| CAD01-004 | UNIT/INT | Regras 3, CA-03, `CAD-042` | Digito | Contexto pronto | Informar 14 digitos com verificador incorreto, ou tamanho diferente de 14 | Bloqueio `CAD-042` |
| CAD01-005 | INT | Regras 4, CA-03, `CAD-043` | Duplicidade | CNPJ ja na `unit` | Criar outra unidade com o mesmo CNPJ | Bloqueio `CAD-043` |
| CAD01-006 | INT | Regras 2 | Nome repetido | Ja existe "Belo Horizonte" | Criar outra "Belo Horizonte" com CNPJ diferente e valido | Gravada; as duas permanecem |
| CAD01-007 | INT/AUDIT | Regras 6, CA-04, CA-08 | Edicao do nome | Unidade existente | Alterar somente o nome | Nome novo; CNPJ intacto; evento `UNIDADE_EDITADA`; combo de setor mostra o nome novo |
| CAD01-008 | INT | Regras 6, `CAD-044` | Alvo ausente | Id inexistente | Editar ou mudar status | Bloqueio `CAD-044` |
| CAD01-009 | INT/AUDIT | Regras 7-8, CA-06, CA-08 | Inativar e reativar | Unidade sem setor ativo | Inativar e depois reativar | Status muda; id preservado; eventos `UNIDADE_INATIVADA` e `UNIDADE_REATIVADA`; sem delete |
| CAD01-010 | INT | Regras 7, CA-05, `CAD-045` | Setor ativo | Unidade com setor ativo | Tentar inativar | Bloqueio `CAD-045`; unidade continua ativa |
| CAD01-011 | INT | Regras 5 | Lista do setor | Unidade inativa e outra ativa | Abrir cadastro de setor | So a unidade ativa entra no combo |
| CAD01-012 | RBAC | CA-07 | Admin | Usuario `ADMIN` | Criar, editar, inativar, reativar | Permitido |
| CAD01-013 | RBAC | CA-07 | SESMT | Usuario `SESMT` | Criar, editar, inativar, reativar | Permitido |
| CAD01-014 | RBAC | Regra 10, CA-07 | Almoxarife | Usuario `ALMOXARIFE` | Tentar mutar | Recusa de permissao |
| CAD01-015 | RBAC | Regra 10, CA-07 | Consulta | Usuario `CONSULTA` | Tentar mutar | Recusa de permissao |
| CAD01-016 | AUDIT | CA-08 | Trilha | Mutacao autorizada | Criar, editar, inativar, reativar | Cada evento com ator, acao, entidade `UNIDADE` e id |
| CAD01-017 | UI | CA-01, CA-05 | Formulario | Tela de unidade aberta | Salvar valido; tentar inativar com setor ativo | Sucesso na lista; erro amigavel sem expor o codigo `CAD-045` |
| CAD01-018 | UI | Regra 7 | Confirmacao | Unidade ativa selecionada | Acionar inativar e cancelar o dialogo | Status permanece ativo |

## Gate para iniciar o codigo

Obrigatorios na primeira entrega:

- `CAD01-001`, `CAD01-002`, `CAD01-003`, `CAD01-004`, `CAD01-005`
- `CAD01-009`, `CAD01-010`
- `CAD01-012`, `CAD01-014`
- `CAD01-016`
