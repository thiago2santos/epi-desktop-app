# Easy NR6 Gestao de EPI

Sistema desktop para gestao de EPI conforme NR-6, com foco em rastreabilidade juridica e operacao de balcao.

## Stack tecnica

- Java 25
- Spring Boot 4.1 (sem servidor web)
- JavaFX 25
- SQLite + Flyway
- JasperReports + PDFBox
- JUnit 5 + PIT Mutation Testing

## Identificadores do projeto

- Group: `br.com.easynr6`
- Artifact: `epi-desktop-app`
- Package base: `br.com.easynr6.gestaoepi`

## Requisitos

- JDK 25+
- Maven 3.9+ (ou use `./mvnw`)

## Como rodar localmente

```bash
./mvnw test
```

Para executar aplicacao:

```bash
./mvnw javafx:run
```

## Qualidade

```bash
./mvnw spotless:apply
./mvnw pmd:check
./mvnw -DskipTests compile spotbugs:check
```

Opcional (exige `NVD_API_KEY`):

```bash
./mvnw org.owasp:dependency-check-maven:check
```

Executar mutacao com PIT:

```bash
./mvnw -Pmutation clean test org.pitest:pitest-maven:mutationCoverage
```

Observacao: o profile `mutation` recompila com release 21 temporariamente, pois o ecossistema do PIT ainda pode atrasar no suporte completo ao bytecode Java 25.

### Pre-commit

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/commit-msg .githooks/pre-push
pre-commit install-hooks
pre-commit run --all-files
```

Observacao: os scripts de hook sao versionados em `.githooks` para manter o setup consistente no time.

## Release

- CI valida PRs na `main`.
- Release e gerado por tag Git no padrao `v*` (ex.: `v0.1.0`).

## Contribuicao

- Guia de contribuicao: `CONTRIBUTING.md`
- Templates de issue: `.github/ISSUE_TEMPLATE/`
- Templates de spec e validacao: `docs/99-governanca/templates/`

## Documentacao

A pasta `docs/` concentra os artefatos de negocio, arquitetura, operacao e governanca.
