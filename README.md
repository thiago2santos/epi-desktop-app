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

## Observabilidade local (OpenTelemetry + Grafana LGTM)

Subir stack local:

```bash
docker compose -f docker-compose.observability.yml up -d
```

Rodar aplicacao com exportacao OTLP ligada:

```bash
EASYNR6_OBS_ENABLED=true ./mvnw javafx:run
```

Paineis e traces: `http://localhost:3000` (`admin` / `admin`).

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

Para rodar apenas os modulos mais criticos de regra e seguranca:

```bash
./mvnw -Pmutation-critical clean test org.pitest:pitest-maven:mutationCoverage
```

### Pre-commit

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/commit-msg .githooks/pre-push
pre-commit install-hooks
pre-commit run --all-files
```

Observacao: os scripts de hook sao versionados em `.githooks` para manter o setup consistente no time.

## Autenticacao (desacoplada)

- A UI depende da porta `AuthenticationProvider`.
- A trilha de auditoria depende da porta `AuditTrail`.
- Implementacao atual (infra): JDBC local (`AuthService` + `AuditService`).
- Senhas sao protegidas com `Argon2` (`PasswordEncoder`).

### Politica de credencial (baseline)

- Senha minima de 12 caracteres.
- Exigir ao menos 3 de 4 grupos: maiuscula, minuscula, numero e especial.
- Nao aceitar senha contendo o login do usuario.
- Nao aceitar senha trivial/proibida.
- Credencial inicial deve ser trocada no primeiro acesso (quando aplicavel).
- Eventos sensiveis de credencial devem gerar trilha de auditoria.

Para trocar provedor (ex.: Keycloak), adicione nova implementacao das portas e mude:

```bash
EASYNR6_AUTH_PROVIDER=keycloak
```

Observacao: evite credenciais bootstrap fixas em ambiente real; use credencial inicial temporaria e rotacao imediata no primeiro acesso.

Para inicializacao com bootstrap admin habilitado em base vazia, defina:

```bash
EASYNR6_BOOTSTRAP_ADMIN_PASSWORD="<senha-forte>"
```

Modo de bootstrap:

- `EASYNR6_BOOTSTRAP_ADMIN_MODE=dev` (padrao): se a senha nao for informada, gera credencial temporaria e exige troca no primeiro acesso.
- `EASYNR6_BOOTSTRAP_ADMIN_MODE=strict`: exige `EASYNR6_BOOTSTRAP_ADMIN_PASSWORD` e falha startup sem ela.

## Release

- CI valida PRs na `main`.
- Release e gerado por tag Git no padrao `v*` (ex.: `v0.1.0`).

## Contribuicao

- Guia de contribuicao: `CONTRIBUTING.md`
- Templates de issue: `.github/ISSUE_TEMPLATE/`
- Templates de spec e validacao: `docs/99-governanca/templates/`

## Documentacao

A pasta `docs/` concentra os artefatos de negocio, arquitetura, operacao e governanca.
