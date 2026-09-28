# Modelo Jira + Spec-Driven + Rastreabilidade

## Objetivo

Padronizar como trabalho sera planejado, implementado, testado e documentado, garantindo:

- clareza do tipo de trabalho (`UC`, `FEAT`, `TECH`);
- rastreabilidade ponta a ponta (Jira -> codigo -> teste -> docs);
- baixa chance de documentacao obsoleta.

## Taxonomia oficial de trabalho

## `UC` - Caso de uso

Uso quando a entrega representa comportamento de negocio percebido pelo usuario final.

Exemplos:
- registrar entrega de EPI;
- registrar devolucao;
- consultar historico por trabalhador.

## `FEAT` - Feature de produto

Uso quando entrega capacidade funcional transversal ou incremental de UX/produto.

Exemplos:
- filtros avancados de relatorios;
- dashboard de prioridades;
- exportacao de comprovante.

## `TECH` - Trabalho tecnico

Uso quando entrega capacidade de engenharia, qualidade, arquitetura ou operacao de software.

Exemplos:
- setup `release-please`;
- git hooks;
- hardening de permissao;
- observabilidade de eventos.

## Estrutura Jira recomendada

1. **Iniciativa**: objetivo estrategico.
2. **Epico**: grande bloco de entrega.
3. **Feature**: fatia funcional ou tecnica coesa.
4. **Task/Subtask**: execucao concreta.

## Naming padrao (sugestao)

- Iniciativa: `INI-<nome-curto>`
- Epico: `EP-<dominio>`
- Feature: `[UC|FEAT|TECH] <descricao>`
- Task: `<acao objetiva> + <artefato>`

Exemplo:
- Feature: `[UC] Entrega de EPI com validacao juridica`
- Task: `Implementar bloqueio de lote vencido no passo 3`

## Fluxo spec-driven obrigatorio

Nenhuma task de implementacao inicia sem os itens abaixo.

1. **Spec curta aprovada**
2. **Cenarios de teste definidos**
3. **Criterios de aceite mensuraveis**

## Template de spec curta (copiar para cada feature)

### 1) Contexto
- problema real:
- ator principal:
- impacto se nao resolver:

### 2) Escopo
- comportamento no escopo:
- fora de escopo:

### 3) Regras de negocio
- regra 1:
- regra 2:
- regra 3:

### 4) Criterios de aceite
- CA-01:
- CA-02:
- CA-03:

### 5) Dependencias
- dependencia tecnica:
- dependencia de dados:
- dependencia de decisao:

## Template de cenarios de teste (antes de codar)

Para cada feature/task, documentar no minimo:

1. **Cenario feliz**
   - dado:
   - quando:
   - entao:
2. **Cenario de bloqueio**
   - dado:
   - quando:
   - entao:
3. **Cenario de borda**
   - dado:
   - quando:
   - entao:

## Gate de qualidade (Definition of Done)

Uma task so pode ser encerrada quando:

1. implementacao concluida;
2. cenarios executados com evidencia;
3. criterio de aceite aprovado;
4. documentacao impactada atualizada;
5. vinculacao Jira <-> commit/PR registrada.

## Politica anti-obsolescencia de documentacao

1. Toda feature deve indicar docs impactados.
2. Toda task concluida deve registrar "status da documentacao":
   - atualizado;
   - sem impacto (com justificativa).
3. Revisao quinzenal de consistencia dos documentos centrais:
   - visao;
   - negocio;
   - arquitetura;
   - operacao;
   - governanca.
4. Documento desatualizado identificado vira task `TECH` imediata no Jira.

## Mapa minimo de rastreabilidade

Manter, por feature:

- ID Jira:
- Tipo (`UC`/`FEAT`/`TECH`):
- Documento de spec:
- Cenarios de teste:
- Artefatos de codigo:
- Evidencia de validacao:
- Docs atualizados:

Sem esses campos, a feature nao e considerada encerrada.
