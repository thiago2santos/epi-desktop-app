# Checklist de Usabilidade - M1 (Cadastros + Auditoria)

## Objetivo

Executar validacao guiada com key user para confirmar clareza de fluxo, mensagens, campos e navegacao dos modulos entregues no baseline de M1.

## Escopo da rodada

- `Cadastros > Empregados`
- `Cadastros > Setores`
- `Cadastros > Funcoes`
- `Cadastros > EPI`
- `Cadastros > CA por EPI`
- `Auditoria`

## Como executar

Para cada roteiro abaixo, marcar:

- `Aprovado`
- `Aprovado com ressalva`
- `Reprovado`

E registrar:

- evidencia (print/video/log);
- duvida do usuario;
- impacto operacional;
- sugestao de ajuste.

## Roteiro 1 - Setores

- [ ] Criar setor novo.
- [ ] Editar nome do setor.
- [ ] Inativar setor (com confirmacao).
- [ ] Reativar setor.
- [ ] Validar feedback verde/vermelho e clareza da mensagem.

## Roteiro 2 - Funcoes

- [ ] Criar funcao vinculada a setor ativo.
- [ ] Editar funcao e trocar setor quando permitido.
- [ ] Inativar funcao com confirmacao.
- [ ] Reativar funcao.
- [ ] Validar se setor recem-criado aparece para vinculo sem confusao.

## Roteiro 3 - Empregados

- [ ] Cadastrar empregado com matricula, nome, setor, funcao e status.
- [ ] Validar bloqueio de matricula duplicada.
- [ ] Editar empregado.
- [ ] Inativar e reativar empregado.
- [ ] Buscar por matricula e por nome.

## Roteiro 4 - EPI

- [ ] Cadastrar EPI com descricao e grupo do Anexo I.
- [ ] Validar bloqueio de obrigatorios e inconsistencias (`CAD-03x`).
- [ ] Editar EPI.
- [ ] Inativar e reativar EPI.
- [ ] Confirmar se o usuario entende o campo de classificacao normativa.

## Roteiro 5 - CA por EPI

- [ ] Selecionar EPI e vincular CA com situacao, vigencia e evidencia oficial.
- [ ] Validar campo unico de consulta oficial (`dd/MM/yyyy HH:mm`):
  - autopreenchimento ao focar vazio;
  - validacao ao perder foco;
  - mensagem de formato invalido.
- [ ] Editar vinculo de CA.
- [ ] Inativar e reativar vinculo.
- [ ] Validar bloqueios (`CAD-035`, `CAD-036`, `CAD-037`) com mensagem compreensivel.

## Roteiro 6 - Auditoria

- [ ] Abrir modulo lateral `Auditoria`.
- [ ] Verificar listagem em ordem decrescente (evento mais recente primeiro).
- [ ] Buscar por acao (ex.: `EPI_CREATED`).
- [ ] Buscar por entidade (ex.: `EPI`, `EPI_CA`, `EMPLOYEE`, `USUARIO`).
- [ ] Buscar por usuario/login e por `entidade_id`.
- [ ] Confirmar entendimento das colunas por key user.

## Perguntas de validacao qualitativa (obrigatorias)

- [ ] "Voce entendeu claramente o que cada tela faz?"
- [ ] "Algum campo gerou duvida de formato ou significado?"
- [ ] "As mensagens de erro ajudam voce a corrigir sem apoio tecnico?"
- [ ] "Em qual parte voce perdeu mais tempo?"
- [ ] "O que impediria uso no balcao amanha?"

## Criterio de encerramento da rodada

Rodada considerada concluida quando:

- todos os roteiros forem executados;
- nao houver bloqueador critico aberto;
- houver plano de acao para ressalvas/reprovacoes;
- backlog e checkpoint forem atualizados com resultado.

## Registro final (resumo)

- Data:
- Participantes:
- Resultado geral: `Aprovado` | `Aprovado com ressalvas` | `Reprovado`
- Bloqueadores:
- Ajustes priorizados:
- Proximo passo:
