# Documentacao do projeto

## Objetivo

Organizar e padronizar a documentacao para facilitar manutencao, onboarding e rastreabilidade de decisoes.

## Convencao de nomes

- Usar `kebab-case` em minusculas.
- Sem acentos, sem espacos e sem caracteres especiais.
- Sufixo por tipo quando fizer sentido:
  - `-v1`, `-v2` para versoes;
  - `-template` para modelos;
  - `-runbook` para operacao.

Exemplos:
- `visao-produto.md`
- `stack-e-arquitetura.md`
- `casos-de-uso-uml.md`

## Estrutura de pastas

```text
docs/
  00-visao/
  01-negocio/
  02-arquitetura/
  03-operacao/
  04-uml/
  05-diagramas/
  99-governanca/
  README.md
  migracao-documentacao.md
```

## Criterio de agrupamento

- `00-visao`: contexto do problema, escopo e direcao do produto.
- `01-negocio`: modelagem funcional e regras de processo.
- `02-arquitetura`: stack, modulos, decisoes tecnicas.
- `03-operacao`: manual do usuario, roteiro de implantacao, checklists.
- `04-uml`: casos de uso e artefatos textuais de analise.
- `05-diagramas`: arquivos `.puml`, `.drawio`, imagens exportadas.
- `99-governanca`: convencoes, politicas de atualizacao, changelog da doc.

## Regra de evolucao

1. Criar/editar doc na pasta correta.
2. Atualizar `migracao-documentacao.md` quando houver consolidacao/merge.
3. Manter um unico documento "fonte da verdade" por assunto.
4. Evitar duplicidade de conteudo entre arquivos.

## Artefatos de governanca recomendados (ativos)

- Backlog de execucao: `docs/03-operacao/backlog-implementacao-mvp.md`
- Modelo Jira/spec-driven/rastreabilidade: `docs/99-governanca/modelo-jira-spec-driven-e-rastreabilidade.md`

Esses documentos devem ser atualizados junto com mudancas de escopo, fluxo de trabalho ou criterios de qualidade.

