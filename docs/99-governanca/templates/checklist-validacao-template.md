# Template - Checklist de validacao

Use este checklist como evidencia de validacao antes de fechar a task.

## Identificacao

- ID Jira/Issue:
- Tipo: `UC` | `FEAT` | `TECH`
- Branch/PR:
- Responsavel:

## Cenarios de teste

- [ ] Cenario feliz executado e aprovado
- [ ] Cenario de bloqueio executado e aprovado
- [ ] Cenario de borda executado e aprovado

## Qualidade tecnica

- [ ] `pre-commit run --all-files` sem falhas
- [ ] `./mvnw -B test` sem falhas
- [ ] `./mvnw -B pmd:check` sem falhas
- [ ] `./mvnw -B -DskipTests compile spotbugs:check` sem falhas

## Seguranca e auditoria (quando aplicavel)

- [ ] politica de seguranca validada (ex.: senha, bloqueio, RBAC)
- [ ] sem segredo exposto em log/console
- [ ] cada tratamento de excecao registrou log de troubleshooting
- [ ] evento sensivel auditado corretamente
- [ ] cenario de acesso nao autorizado validado

## Evidencias

- resultado esperado:
- resultado observado:
- evidencias (prints/logs/relatorios):

## Documentacao

- [ ] docs atualizados
- [ ] sem impacto documental (justificado)

Justificativa (se "sem impacto documental"):

## Definition of Done

- [ ] implementacao concluida
- [ ] criterios de aceite atendidos
- [ ] rastreabilidade issue <-> commit/PR registrada
- [ ] para auth/credencial: gate de seguranca e auditoria aprovado
