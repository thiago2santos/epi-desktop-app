# Politica de testes e mutation testing

## Diretriz principal

Qualidade de teste nao sera medida por percentual de cobertura de linha.
O foco e validar comportamento de negocio, risco operacional e governanca.

## Piramide de validacao adotada

- unitario: regras de negocio, validadores e use cases puros;
- integracao: persistencia, migracoes, RBAC, credencial e auditoria;
- fluxo manual guiado: validacao UX e casos ponta a ponta criticos.

## Uso do PIT (mutation testing)

PIT deve ser usado quando houver alteracao relevante de regra de negocio, seguranca, autenticacao, credencial, RBAC ou auditoria.
Nao e obrigatorio executar PIT a cada pequena alteracao de layout ou refactor sem impacto funcional.

## Perfis Maven

- baseline amplo:
  - `./mvnw -Pmutation clean test org.pitest:pitest-maven:mutationCoverage`
- foco em modulos criticos de identidade/autenticacao:
  - `./mvnw -Pmutation-critical clean test org.pitest:pitest-maven:mutationCoverage`

## Critério pratico de uso

Executar PIT quando houver pelo menos um dos pontos abaixo:

1. Mudanca em politica de senha, estado de credencial ou bloqueio;
2. Mudanca em regras de autorizacao por papel;
3. Mudanca em fluxo auditavel critico;
4. Mudanca em regra de negocio de entrega/estorno/devolucao que impacte rastreabilidade.

## Registro de evidencias

Quando PIT for executado:

- anexar resumo do resultado na task/PR;
- registrar classes-alvo avaliadas;
- documentar mutantes sobreviventes aceitos com justificativa, quando aplicavel.
