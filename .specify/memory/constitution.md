# Easy NR6 Gestao de EPI Constitution

## Core Principles

### I. Rastreabilidade jurídica imutável

O fornecimento de EPI que serve de prova (NR-6, item 6.5.1.1) MUST ser um
evento imutável. Correção MUST ser estorno: um registro novo que aponta o
original. O schema MUST recusar UPDATE e DELETE sobre a ficha, os itens, o CA
vinculado, o termo, a devolução, o estorno e a trilha de auditoria. O CA válido
na compra (item 6.9.2.1) e a validade da peça na entrega (item 6.9.2.1.1) MUST
permanecer fatos distintos. Lote vencido MUST NOT ser baixado. Entrega fora da
matriz MUST exigir exceção registrada. O relatório por trabalhador e período
MUST ser reproduzível a partir dos eventos. A ficha MUST NOT afirmar uso
efetivo, EPI eficaz, neutralização de insalubridade nem geração de eSocial.

Rationale: editar a prova, ou misturar o relógio do CA com o da peça, invalida
o registro na fiscalização.

### II. Domínio primeiro

Regras de negócio e invariantes MUST residir no domínio e nos casos de uso.
JDBC, JavaFX, JasperReports, PDFBox e provedores de identidade MUST permanecer
em adapters atrás de portas. A tela MUST NOT ser a única barreira: o caso de
uso MUST recusar a ação quando o papel ou o escopo organizacional não
permitir. Trocar o adapter local (SQLite) pelo serviço central e PostgreSQL
MUST NOT reescrever o domínio nem a semântica dos eventos.

Rationale: a tela e o banco mudam; a prova jurídica e as regras da NR-6 não.

### III. Spec e teste antes do código

Implementação de caso de uso MUST NOT começar sem spec com critérios de
aceite, catálogo de erros de domínio, matriz de testes mínima, recorte
incremental e, quando a ação for sensível, decisão de RBAC e auditoria. A
ordem de construção MUST ser domínio, aplicação, infraestrutura, UI, testes e
documentação. Qualidade MUST ser julgada por comportamento, risco operacional
e governança, não por cobertura de linha. Mudança em senha, autorização,
auditoria ou em regra de fornecimento, estorno ou devolução MUST rodar
mutation testing (PIT) no perfil aplicável e MUST registrar mutantes
sobreviventes aceitos com justificativa.

Rationale: o gate de início impede código sem cenário verificável. PIT cobre
regras em que um teste verde ainda deixa o comportamento quebrável.

### IV. Autorização, credencial e auditoria

Toda ação sensível MUST gerar auditoria append-only com instante, ator, ação,
entidade e identificador. Papel e escopo MUST ser avaliados no serviço que
persiste. Senha de usuário MUST usar Argon2id, cumprir o mínimo de 12
caracteres e ao menos 3 de 4 grupos (maiúscula, minúscula, número, especial),
e MUST NOT conter o login nem valor trivial. Credencial inicial MUST exigir
troca no primeiro acesso quando aplicável. Senha em claro MUST NOT aparecer em
auditoria, log, ficha ou PDF. Credencial de bootstrap MUST NOT ficar fixa em
ambiente real. Erro de domínio MUST chegar à UI como frase em pt-BR, sem
código interno nem detalhe sensível.

Rationale: quem opera o balcão não é o trabalhador da ficha. A trilha precisa
separar os dois, e a senha não pode vazar pelo registro.

### V. Simplicidade e modos de operação

Cada incremento MUST entregar só o necessário para o próximo corte validável.
Módulo novo MUST seguir portas, casos de uso e adapters. O modo local MUST ser
um operador e uma base SQLite, e MUST NOT ser descrito como fila compartilhada
nem sincronização entre máquinas. Dois processos MUST NOT gravar o mesmo
arquivo SQLite, inclusive por pasta de rede. Operação oficial multiusuário
MUST ser cliente JavaFX, serviço central e PostgreSQL remoto. Autorização,
transação, auditoria, idempotência e concorrência desse modo MUST executar no
serviço central. O cliente MUST NOT conectar direto ao banco remoto.

Rationale: tratar o SQLite local como produto multiusuário quebra a prova e a
concorrência. O domínio permanece o mesmo nos dois modos.

## Restrições de tecnologia e conformidade

- Runtime: Java 25, JavaFX 25, Spring Boot 4.1 sem servidor web
  (`spring.main.web-application-type=none`). Persistência MUST ser JDBC
  explícito, sem JPA. Migrações MUST ser Flyway.
- Identidade do artefato: group `br.com.easynr6`, artifact `epi-desktop-app`,
  pacote base `br.com.easynr6.gestaoepi`.
- Relatório padronizado MUST usar JasperReports. PDFBox fica restrito a
  pós-processamento (carimbo, junção, anexo).
- Código-fonte (tipos, membros, testes, exceções técnicas) MUST estar em
  inglês. UI, mensagens ao operador e documentação de negócio MUST estar em
  pt-BR. Nome novo de domínio MUST usar o vocabulário canônico de
  `CONTRIBUTING.md` (`Employee`, `Role`, `Credential`, `AuditTrail`,
  `StockLot`).
- Dado pessoal: a matrícula MUST ser preferida ao CPF quando a matrícula
  bastar. Base distribuída que contenha dado pessoal MUST ser cifrada, e a
  chave MUST NOT residir no jar. O mecanismo adotado é SQLCipher, com a chave
  no cofre de credenciais do sistema. Base exclusiva de desenvolvimento MAY
  permanecer sem cifra.
- O instalador Windows (`jpackage`) MUST substituir o binário e MUST NOT
  sobrescrever o banco em `%ProgramData%`.
- Tabela operacional MUST carregar a unidade. Evento sensível MUST registrar
  quem criou e quando.

## Fluxo de desenvolvimento e portões de qualidade

- Branch a partir de `main`: `feat/`, `fix/` ou `chore/`. Merge em `main`
  somente por pull request, com CI verde e a documentação impactada
  atualizada.
- Mensagem de commit MUST seguir Conventional Commits.
- Release de produto MUST ser tag Git `vMAJOR.MINOR.PATCH`. O versionamento
  desta constituição é independente e segue Governance.
- Antes do pull request: `pre-commit run --all-files` e `./mvnw -B test`.
  O trecho alterado MUST passar em Spotless, PMD e SpotBugs.
- Item MUST NOT ser encerrado sem a implementação do cenário, os testes da
  matriz passando, evidência de validação e documentação de status atualizada.
- Trabalho MUST ser classificado como `UC`, `FEAT` ou `TECH`.
- Código legado de idioma misto MUST ser normalizado no trecho alterado quando
  viável. Renomeação retroativa em massa MUST NOT ser feita num único passo.

## Governance

Esta constituição prevalece sobre prática local conflitante. O procedimento
detalhado vive nos documentos de orientação e MUST permanecer consistente com
estes princípios.

Emenda: alterar `.specify/memory/constitution.md` por pull request, com
motivo, impacto nos princípios e nota de migração quando um princípio for
removido ou redefinido. A data de ratificação não muda. A data da última
emenda muda a cada alteração.

Versionamento da constituição (SemVer):

- MAJOR: remoção ou redefinição incompatível de princípio.
- MINOR: princípio ou seção nova, ou expansão material de uma regra.
- PATCH: esclarecimento, redação ou correção sem mudança de obrigação.

Todo pull request MUST verificar a conformidade dos princípios que o diff
toca. Complexidade além da fatia vertical MUST ser justificada na spec ou no
pull request. A revisão MUST recusar edição destrutiva da prova, regra de
negócio na tela, início de código sem o gate da spec, ou enfraquecimento de
auditoria, RBAC ou política de credencial.

Orientação de runtime:

- `docs/99-governanca/metodologia-refinamento-spec-driven.md`
- `docs/99-governanca/politica-testes-e-mutation.md`
- `docs/02-arquitetura/arquitetura-modular.md`
- `docs/00-visao/visao-produto-e-escopo.md`
- `CONTRIBUTING.md`

**Version**: 1.0.0 | **Ratified**: 2026-10-25 | **Last Amended**: 2026-10-25
