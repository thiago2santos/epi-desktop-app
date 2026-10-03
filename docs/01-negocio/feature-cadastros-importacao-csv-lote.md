# Feature — Importacao em lote de cadastros via CSV (com validacao previa)

## Objetivo

Permitir que unidades adotem o **Easy NR6** carregando **cadastros organizacionais** a partir de planilhas/CSV padronizados, com uma **etapa intermediaria de validacao** antes de persistir — reduzindo digitacao manual, erros de typing e abandono na fase inicial.

Esta feature complementa (nao substitui) o cadastro tela a tela ja existente (`UC-CAD-02`, `UC-CAD-03`, etc.) e a importacao da base oficial **CAEPI** (`UC-CAE-01`).

## Motivacao de produto (adocao)

- Plantas medias/grandes chegam com **listas de RH/planilhas** (setores, cargos, matriculas).
- Digitar milhares de trabalhadores na UI e **barreira de entrada** para pilotos comerciais.
- Erros de digitacao em matricula, setor ou funcao geram **retrabalho** e desconfianca no go-live.
- Uma experiencia tipo **“confira antes de publicar”** (semelhante em espirito a revisao de carga CAEPI) aumenta confianca do SESMT/Admin.

## Problema que a feature resolve

| Hoje | Com a feature |
|------|----------------|
| Cadastro unitario por tela | Carga em lote + revisao |
| Erro descoberto apos salvar | Erro destacado **antes** do commit |
| Dependencia setor/funcao “na cabeca” do operador | Pre-requisitos e **ordem de carga** explicitados |
| Planilha externa sem contrato | **Layout CSV versionado** por tipo de cadastro |

## Escopo da feature (visao)

### No escopo (refinar na spec)

1. **Layouts CSV documentados** para entidades minimas do MVP organizacional:
   - setor (departamento);
   - funcao (vinculada a setor);
   - trabalhador (matricula, nome, setor, funcao, status).
2. **Pipeline em duas fases** por arquivo (ou por “pacote” de arquivos):
   - **Parse + validacao** (dominio, sem persistir);
   - **Publicacao** apenas das linhas elegiveis (conforme politica abaixo).
3. **Tela de revisao (staging)**:
   - tabela com todas as linhas do arquivo;
   - linhas **validas** com fundo verde claro;
   - linhas **com pendencia** com fundo amarelo claro;
   - celulas problematicas destacadas + codigo/mensagem operacional;
   - filtros: ver **todas**, **apenas validas**, **apenas com pendencia** (rotulos amigaveis na UI, nao “OK/NOK”).
4. **Navegacao contextual** (somente quando necessario):
   - se a pendencia for **cadastro mestre ausente** (ex.: setor ou funcao inexistente), a celula pode ser **acionavel** e abrir o cadastro correspondente **pre-preenchido** com o valor lido do CSV;
   - apos o usuario concluir o cadastro manual, retorno ao staging para **revalidar** a linha (re-parse ou refresh da linha).
5. **RBAC**, **auditoria** da tentativa (arquivo, contagem valida/invalida, resultado) e mensagens alinhadas ao catalogo de erros do dominio (`CAD-*`).

### Fora de escopo (nesta fase — candidatos)

- Importacao em lote de **EPI**, **CA**, **lotes**, **matriz** (podem ser fases futuras com o mesmo padrao UX).
- Sincronizacao bidirecional com ERP/RH externo.
- Merge automatico de homonimos de setor/funcao sem confirmacao.
- Substituir completamente telas de cadastro unitario.

## Pre-requisitos e ordem recomendada de carga

Ordem logica para evitar pendencias evitaveis:

```text
1) Setores
2) Funcoes (exige setor existente ou resolvido)
3) Trabalhadores (exige setor + funcao coerentes)
```

A UI deve **orientar** essa ordem (wizard ou hub “Importar cadastros”) e permitir **revalidar** trabalhadores apos corrigir mestres.

## Experiencia de revisao (diretriz UX — registro do produto)

Inspiracao alinhada a `docs/02-arquitetura/easy-nr6-ux-identity.md`:

- **Verde claro**: linha pronta para importar conforme regras vigentes.
- **Amarelo claro**: linha com uma ou mais pendencias; usuario ainda pode inspecionar o restante dos dados.
- **Destaque na celula**: foco no campo que causa a pendencia (matricula duplicada, setor desconhecido, funcao incompativel com setor, etc.).
- **Filtros** persistentes na sessao da revisao.
- **Acao primaria** desabilitada ou confirmatoria se existir linha amarela, conforme decisao de negocio (importar so validas vs bloquear ate zerar pendencias — ver spec § decisoes).

## Relacao com CAEPI e catálogo EPI

- CSV de **cadastros organizacionais** nao substitui `UC-CAE-01`.
- Trabalhador importado **nao** implica EPI/CA; matriz e entrega continuam fluxos proprios.

## Rastreabilidade

| Artefato | Caminho |
|----------|---------|
| Spec curta (refinamento) | `docs/03-operacao/spec-uc-cad-imp-01-importacao-csv-cadastros.md` |
| UML textual | `docs/04-uml/cadastros/UC-CAD-IMP-01 — Importar cadastros via CSV (validacao previa).md` |
| Matriz de testes (stub) | `docs/03-operacao/matriz-testes-uc-cad-imp-01.md` |
| Backlog | `docs/03-operacao/backlog-implementacao-mvp.md` (FE-CAD-01 / UC-CAD-IMP-01) |

## Status

- **Registrado** em out/2026 para refinamento posterior.
- **DoR nao atendido** — nao iniciar implementacao ate fechar layouts CSV, politica de publicacao parcial e matriz de testes executavel.
