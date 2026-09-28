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

## Como rodar localmente

```bash
mvn clean test
```

Para executar aplicacao:

```bash
mvn spring-boot:run
```

## Qualidade

Executar mutacao com PIT:

```bash
mvn -Pmutation clean test org.pitest:pitest-maven:mutationCoverage
```

Observacao: o profile `mutation` recompila com release 21 temporariamente, pois o ecossistema do PIT ainda pode atrasar no suporte completo ao bytecode Java 25.

## Documentacao

A pasta `docs/` concentra os artefatos de negocio, arquitetura, operacao e governanca.
