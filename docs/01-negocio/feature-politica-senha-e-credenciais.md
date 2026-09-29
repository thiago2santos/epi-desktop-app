# Feature — Politica de senha e credenciais

## Objetivo

Definir uma politica formal de credenciais para o produto `Easy NR6 Gestao de EPI`, com foco em uso comercial profissional, seguranca operacional e rastreabilidade auditavel.

Este documento e a fonte de verdade da feature antes da implementacao completa dos ajustes em casos de uso, arquitetura, operacao e testes.

## Motivacao de produto

- O sistema lida com registros sensiveis (usuarios, papeis, eventos auditaveis e evidencias juridicas).
- Um produto comercial profissional exige controles minimos de identidade e credenciais.
- Credenciais fracas elevam risco de acesso indevido, fraude operacional e perda de confianca.
- A consistencia dessa politica melhora governanca, auditoria e posicionamento comercial.

## Problema que a feature resolve

No estado atual, existem lacunas de definicao para ciclo de vida de credencial:

- regras de senha forte nao formalizadas;
- fluxo de troca obrigatoria no primeiro acesso nao definido;
- fluxo de reset administrativo nao especificado;
- bloqueio por tentativas invalidas nao especificado;
- pre-condicoes de casos de uso de administracao incompletas.

## Escopo da feature

### No escopo

1. Politica de senha forte para criacao e troca de credencial.
2. Fluxo de credencial inicial com troca obrigatoria no primeiro acesso.
3. Fluxo de reset administrativo de credencial.
4. Regra de bloqueio temporario por tentativas invalidas sucessivas.
5. Auditoria obrigatoria de eventos de credencial.
6. Ajustes de documentacao em UML textual, arquitetura, operacao e governanca.

### Fora de escopo (nesta fase)

- MFA (multi-factor authentication).
- SSO corporativo (SAML/OIDC).
- Assinatura digital ICP-Brasil para login.
- Integracao com IAM externo (ex.: AD, Keycloak em producao).
- Politicas avancadas por unidade/regiao.

## Politica de senha (baseline v1)

### Regras minimas

1. Comprimento minimo de 12 caracteres.
2. Atender ao menos 3 de 4 grupos:
   - letra maiuscula;
   - letra minuscula;
   - numero;
   - caractere especial.
3. Nao conter o login do usuario.
4. Nao aceitar senha presente em lista basica de proibidas.
5. Nao aceitar sequencias triviais conhecidas (ex.: `123456`, `abcdef`, `qwerty`).

### Armazenamento e verificacao

- Senha nunca e persistida em texto puro.
- Hash de senha deve permanecer em Argon2.
- Comparacao de senha deve ocorrer via `PasswordEncoder`.

## Ciclo de vida da credencial

## 1) Criacao de usuario (Admin)

- Admin define credencial inicial aderente a politica.
- Usuario nasce com flag de troca obrigatoria no primeiro acesso.
- Evento deve ser auditado.

## 2) Primeiro acesso do usuario criado

- Login com credencial inicial deve redirecionar para fluxo de troca obrigatoria.
- Usuario nao deve acessar modulos de negocio antes da troca.
- Troca concluida com sucesso encerra a obrigatoriedade.

## 3) Reset administrativo de credencial

- Apenas `Admin` pode resetar credencial de outro usuario.
- Nova credencial segue politica de senha forte.
- Reset reativa troca obrigatoria no proximo login.
- Evento deve ser auditado.

## 4) Tentativas invalidas de login

- Apos limite definido (baseline: 5 tentativas), conta entra em bloqueio temporario.
- Bloqueio deve ter janela de tempo configuravel (baseline sugerido: 15 minutos).
- Tentativas durante bloqueio devem ser recusadas e auditadas.

## 5) Desbloqueio

- Desbloqueio automatico por tempo decorrido (baseline v1).
- Desbloqueio manual por Admin pode ser adicionado em fase posterior.

## Eventos de auditoria obrigatorios

1. Criacao de usuario.
2. Falha de criacao por login duplicado.
3. Troca de credencial no primeiro acesso (sucesso/falha).
4. Reset administrativo de credencial.
5. Bloqueio por tentativas invalidas.
6. Tentativa de autenticacao em conta bloqueada.

Cada evento deve registrar, no minimo:

- usuario ator (quem executou a acao);
- acao;
- entidade e identificador;
- timestamp;
- detalhes resumidos.

## Regras de autorizacao (RBAC)

- `Admin` pode: criar usuario, atribuir papel, resetar credencial.
- Perfis nao-admin nao podem executar gestao de credenciais.
- Controle deve existir em UI e camada de servico (nao confiar apenas em botao oculto).

## Impacto em casos de uso

## UC-ADM-01 — Cadastrar usuario

Precisa incorporar:

- pre-condicoes completas de seguranca e auditoria;
- validacoes de politica de senha;
- tratamento de login duplicado;
- pos-condicao de troca obrigatoria no primeiro acesso.

## UC-TRV-01 — Autenticar usuario

Precisa incorporar:

- bloqueio por tentativas invalidas;
- comportamento de conta bloqueada;
- redirecionamento para troca obrigatoria quando aplicavel.

## UC novo recomendado

- `UC-ADM-04 — Resetar credencial de usuario`

## Cenarios de teste obrigatorios (antes de codar)

Para cada item da feature, manter cenario feliz, bloqueio e borda.

### Cenarios centrais sugeridos

1. Criacao de usuario com senha valida e auditoria registrada.
2. Recusa de criacao por login duplicado.
3. Recusa de senha fraca fora da politica.
4. Primeiro login com troca obrigatoria ativa.
5. Reset administrativo reativando troca obrigatoria.
6. Bloqueio apos tentativas invalidas sucessivas.
7. Recusa de acao de gestao de credencial por usuario nao-admin.

## Criterios de aceite mensuraveis

- CA-01: toda credencial nova salva atende a politica definida.
- CA-02: nenhuma senha e armazenada em texto puro.
- CA-03: toda criacao/reset de credencial gera evento auditavel.
- CA-04: usuario com troca obrigatoria nao acessa modulos ate concluir troca.
- CA-05: sistema bloqueia conta apos limite de tentativas invalidas configurado.
- CA-06: usuario sem papel `Admin` nao executa operacoes de credencial.

## Dependencias

### Tecnicas

- modulo de autenticacao ativo;
- mecanismo de auditoria append-only ativo;
- migracoes de banco aplicadas no startup.

### Dados

- tabela de usuarios e papeis ativa;
- campos de estado de credencial e tentativas disponiveis no schema.

### Decisoes

- limite final de tentativas invalidas;
- duracao final do bloqueio temporario;
- formato operacional da credencial inicial (definida manualmente pelo Admin ou gerada automaticamente).

## Riscos e mitigacoes

- Risco: politica muito rigida travar operacao no inicio.
  - Mitigacao: baseline objetiva, mensagens claras e orientacao em tela.
- Risco: tratamento de bloqueio gerar chamados operacionais recorrentes.
  - Mitigacao: documentar fluxo de desbloqueio e telemetria de falhas.
- Risco: inconsistencias entre doc e implementacao.
  - Mitigacao: atualizar UCs, arquitetura, manual e checklist de validacao na mesma rodada.

## Rastreabilidade sugerida (Jira)

- Iniciativa: `INI-02` (Governanca tecnica e qualidade de entrega)
- Epico: `EP-TECH` (Fundacao tecnica, seguranca e automacao de release)
- Feature: `[FEAT] Politica de senha e ciclo de vida de credenciais`
- Tasks filhas:
  - `[UC] Ajustar UC-ADM-01 com politica de credencial`
  - `[UC] Incluir UC-ADM-04 reset de credencial`
  - `[TECH] Hardening de bootstrap e logs de credencial`
  - `[TECH] Implementar bloqueio por tentativas invalidas`
  - `[TECH] Cobertura de testes de seguranca e auditoria`

## Documentos que devem ser atualizados na sequencia

- `docs/04-uml/casos-de-uso-uml.md`
- `docs/05-diagramas/casos-de-uso-geral.puml`
- `docs/02-arquitetura/arquitetura-modular.md`
- `docs/02-arquitetura/stack-e-arquitetura.md`
- `docs/03-operacao/manual-operacional-v0.md`
- `docs/03-operacao/backlog-implementacao-mvp.md`
- `docs/00-visao/resumo-executivo-definicoes.md`
- `README.md`
- `docs/99-governanca/modelo-jira-spec-driven-e-rastreabilidade.md`
