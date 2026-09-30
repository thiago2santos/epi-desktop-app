# Metodologia de Refinamento Spec-Driven (padrao do projeto)

## Objetivo

Padronizar como refinamos backlog antes de implementar, para reduzir retrabalho e garantir aderencia a negocio, norma e testabilidade.

## Principios

1. `Core first`: regra de negocio no dominio/aplicacao, nunca na tela.
2. `Sem vazamento de infra`: JDBC/framework ficam nos adapters.
3. `KISS + YAGNI`: construir apenas o necessario para o proximo incremento validavel.
4. `Teste antes do codigo`: cenario definido antes de implementar.
5. `Rastreabilidade completa`: norma -> regra -> teste -> evidencia -> backlog.

## Fluxo oficial de refinamento

### Etapa 1 - Congelar base de referencia

- consolidar fonte normativa/funcional oficial;
- explicitar o que esta no escopo e fora do escopo;
- registrar premissas e limites de MVP.

Saida obrigatoria:
- `spec` curta inicial.

### Etapa 2 - Definir comportamento de negocio

- atores;
- pre-condicoes;
- fluxo principal;
- fluxos alternativos/excecoes;
- pos-condicoes;
- regras de negocio.

Saida obrigatoria:
- criterios de aceite objetivos (`CA-xx`).

### Etapa 3 - Fechar catalogo de erros

- codificar erros de dominio (ex.: `CAD-03x`, `AUTH-00x`);
- mensagem amigavel para UI;
- sem expor detalhe sensivel.

Saida obrigatoria:
- tabela de erros por caso de uso.

### Etapa 4 - Modelagem tecnica minima

- entidades/campos/constraints/FKs;
- use cases necessarios;
- portas (repositories/services externos);
- adapters de infra.

Saida obrigatoria:
- recorte incremental de implementacao (primeiro slice vertical).

### Etapa 5 - Matriz executavel de testes

Para cada cenario, definir:
- ID;
- tipo (unit/integration/rbac/ui/audit);
- pre-condicao;
- passos;
- resultado esperado;
- erro esperado (quando houver);
- referencia na spec.

Saida obrigatoria:
- matriz de testes executavel.

### Etapa 6 - Planejamento de entrega incremental

- quebrar em commits pequenos e coesos;
- definir ordem: dominio -> aplicacao -> infra -> UI -> testes -> docs;
- definir gate de pronto por etapa.

Saida obrigatoria:
- plano de implementacao com DoR/DoD.

## Padrao de artefatos por caso de uso

Para cada UC relevante, manter:

1. `spec` curta do UC;
2. matriz de testes do UC;
3. atualizacao no backlog/refinamento;
4. atualizacao em UML (quando impacto funcional);
5. checkpoint de continuidade (quando muda prioridade).

## Gate de inicio de implementacao

Nao iniciar codigo se faltar qualquer item:

- spec com regra e criterios de aceite;
- erros de dominio definidos;
- matriz de testes minima;
- recorte tecnico incremental;
- decisao de RBAC/auditoria quando aplicavel.

## Gate de encerramento do item

Nao fechar item se faltar:

- implementacao funcional;
- testes passando para cenarios definidos;
- evidencias de validacao;
- docs atualizadas (status + proximo passo).

## Aplicacao atual no projeto

A metodologia foi aplicada de ponta a ponta em:

- `UC-CAD-03` (Trabalhador);
- `UC-CAD-02` (Setores/Funcoes);
- `UC-CAD-04/05` (EPI + CA baseado na NR-6);
- `UC-AUD-01` (consulta de auditoria baseline).

## Relacao com documentos existentes

- `docs/99-governanca/modelo-jira-spec-driven-e-rastreabilidade.md`
- `docs/03-operacao/backlog-implementacao-mvp.md`
- `docs/99-governanca/templates/spec-curta-template.md`
- `docs/99-governanca/templates/checklist-validacao-template.md`
