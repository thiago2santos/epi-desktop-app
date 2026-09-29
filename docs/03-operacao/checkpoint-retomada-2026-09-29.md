# Checkpoint de retomada — 2026-09-29

## Estado atual do projeto

- Fundacao tecnica (`R0` e `M0`) concluida.
- Autenticacao desacoplada por portas/use cases/adapters concluida.
- M1 em andamento com dois blocos essenciais entregues:
  - `UC-CAD-03`: cadastro de empregado/trabalhador.
  - `UC-CAD-02`: cadastro de setores e funcoes (pre-requisito para trabalhador).

## Avancos implementados nesta fase

### Cadastros (UI)

- Modulo `Cadastros` reorganizado com abas:
  - `Empregados`
  - `Setores`
  - `Funcoes`
- Feedback visual padrao mantido:
  - sucesso em verde (5s),
  - erro em vermelho.
- Confirmacao obrigatoria em inativacao de registros.
- Sincronizacao entre abas corrigida:
  - setor recem-criado passa a ficar disponivel para cadastro de funcao apos troca de aba.

### Regras de negocio e servicos

- Regras de consistencia e dependencia para `Department` e `JobRole` implementadas.
- Bloqueios de inativacao por dependencia ativa:
  - setor com funcao ativa;
  - funcao com empregado ativo.
- Auditoria de eventos criticos implementada para criar/editar/inativar/reativar em setor e funcao.

### Testes

- Testes unitarios adicionados para policies de setor/funcao.
- Testes de integracao adicionados para fluxos completos de estrutura organizacional.
- Suite de testes validada com sucesso apos as mudancas.

## Ponto de atencao obrigatorio

> **Pendencia de usabilidade**: antes de preparar release, executar rodada formal de testes de usabilidade com key user no modulo `Cadastros` (abas novas) e registrar evidencias.

Checklist minimo sugerido para usabilidade:
- criar/editar/inativar/reativar setor;
- criar/editar/inativar/reativar funcao;
- cadastrar trabalhador usando setor/funcao recem-criados;
- validar clareza das mensagens de erro/sucesso;
- validar entendimento da navegacao por abas.

## De onde continuar na proxima retomada

Prioridade recomendada (M1):
1. `UC-CAD-04/05`: cadastro de EPI e vinculo de CA.
2. `UC-LOT-01/02`: lotes (entrada, validade, saldo).
3. `UC-MAT-01/02`: matriz e periodicidade.
4. Fechar checklist DoR de M2.

## Comandos de validacao rapida para retomada

- Testes completos:
  - `./mvnw -q test`
- App desktop em dev:
  - `SPRING_PROFILES_ACTIVE=dev EASYNR6_DB_URL=jdbc:sqlite:easynr6-dev.db EASYNR6_BOOTSTRAP_ADMIN_MODE=strict EASYNR6_BOOTSTRAP_ADMIN_PASSWORD='Admin#Dev2026!' ./mvnw javafx:run`
- PIT focado em empregado/estrutura (quando necessario):
  - `./mvnw -q -Pmutation clean test-compile`
  - `./mvnw -q -Pmutation -Dpitest.target.classes=br.com.easynr6.gestaoepi.modules.employee.**.* -Dpitest.target.tests=br.com.easynr6.gestaoepi.modules.employee.**.* pitest:mutationCoverage`

## Observacao final de governanca

- Ao retomar, iniciar pela validacao de usabilidade pendente e registrar resultado no backlog antes de seguir para o proximo cadastro essencial.
