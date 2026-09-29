# Contributing

Este repositório segue um fluxo simples para manter qualidade desde os primeiros commits.

## Branches

- `main`: branch protegida, somente via Pull Request.
- `feat/<slug>`: novas features e casos de uso.
- `fix/<slug>`: correções.
- `chore/<slug>`: manutenção técnica, tooling e documentação.

Exemplos:

- `feat/uc-entrega-wizard`
- `fix/validacao-lote-vencido`
- `chore/ajuste-pipeline-release`

## Commits

Conventional Commits são obrigatórios no `commit-msg`:

- `feat: ...`
- `fix: ...`
- `chore: ...`
- `docs: ...`
- `refactor: ...`
- `test: ...`
- `build: ...`
- `ci: ...`

## Versionamento

Este projeto usa versionamento semântico (SemVer):

- `MAJOR`: mudança incompatível.
- `MINOR`: nova funcionalidade compatível.
- `PATCH`: correção compatível.

Formato de release: tag Git `vMAJOR.MINOR.PATCH` (ex.: `v0.1.0`).

## Fluxo de Pull Request

1. Crie branch a partir de `main`.
2. Abra PR com referência à issue/tarefa.
3. Garanta `CI` verde.
4. Atualize documentação impactada.
5. Faça merge na `main`.

## Qualidade local obrigatória

Antes de abrir PR:

```bash
pre-commit run --all-files
./mvnw -B test
```

## Taxonomia de trabalho

Classifique cada item como:

- `UC`: caso de uso de negócio.
- `FEAT`: feature de produto.
- `TECH`: trabalho técnico/infrastrutura.

Referência: `docs/99-governanca/modelo-jira-spec-driven-e-rastreabilidade.md`.

## Convenção de idioma (obrigatória)

Para evitar mistura de idiomas no código e facilitar manutenção:

- **Código-fonte em inglês**:
  - nomes de classes, métodos, variáveis, pacotes e arquivos;
  - nomes de testes e fixtures;
  - mensagens de erro internas/exceções técnicas.
- **Experiência do usuário em pt-BR**:
  - labels, textos de tela, mensagens exibidas na UI;
  - fluxos operacionais voltados ao usuário final.
- **Documentação de negócio/operação em pt-BR**:
  - artefatos funcionais, backlog e guias operacionais.

### Regra de transição

- Não renomear tudo retroativamente de uma vez.
- Aplicar a convenção em toda nova implementação.
- Quando tocar código legado misto, normalizar idioma no trecho alterado sempre que viável.
