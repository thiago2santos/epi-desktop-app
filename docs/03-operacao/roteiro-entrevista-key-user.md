# Roteiro de entrevista com key user (operacao diaria do sistema de EPI)

## Objetivo

Conduzir uma conversa estruturada com o key user para tomar decisoes de implantacao, operacao diaria e validacao da POC em campo.

## Perguntas que eu vou fazer ao key user

### 1) Instalacao e ambiente do PC

- No PC de voces, da para executar aplicativo baixado da internet sem bloqueio de politica interna?
- Existem restricoes de antivirus, proxy ou SmartScreen que podem impedir o uso?
- Voces conseguem instalar algo na pasta do usuario (sem permissao de admin)?
- Se nao der para instalar, uma versao portatil (executar direto) funcionaria na rotina?

### 2) Atualizacao e suporte

- Para voces, atualizacao manual (baixar nova versao e instalar) atende por enquanto?
- Em caso de erro na atualizacao, quem aciona primeiro: eu, TI local ou outro ponto focal?
- Qual janela de horario e melhor para atualizar sem impactar operacao?

### 3) Backup e recuperacao

- Se o computador der problema, como recuperamos os registros ja lancados?
- Quem vai ser responsavel por rodar/verificar backup com frequencia?
- Em quanto tempo voces precisam restaurar o sistema para voltar a operar?

### 4) Operacao entre unidades

- Cada unidade vai operar seu proprio banco local ou voces esperam visao consolidada em tempo real?
- No curto prazo, consolidacao por exportacao periodica atende?
- Quais filtros sao obrigatorios nos relatrios gerenciais: unidade, setor, funcao, periodo, tipo de EPI?

### 5) Conectividade e rotina

- A operacao precisa funcionar mesmo sem internet durante todo o dia?
- Existem momentos em que a rede cai e isso impactaria registro de entrega/devolucao?

### 6) Validacao do trabalhador

- Para a fase inicial, a validacao por assinatura simples no sistema atende o processo de voces?
- Existe exigencia imediata de biometria ou assinatura digital formal?
- Em auditoria interna, qual evidencia voces consideram minima para comprovar o aceite?

### 7) Fluxo real do balcao

- Quais sao os tres cenarios mais frequentes no dia a dia: primeira entrega, troca por desgaste, extravio?
- O que mais atrasa hoje na rotina de entrega e que o sistema precisa eliminar?
- Em desligamento ou mudanca de funcao, como voces controlam devolucao hoje?

### 8) Criterio de sucesso da POC

- O que precisa funcionar muito bem para voces considerarem a POC aprovada?
- Quantos dias de uso real sao necessarios para validar confianca no sistema?
- Quais relatorios sao obrigatorios para apresentar ao gestor e liberar continuidade?

## Premissas ja definidas nesta conversa

- Canal de distribuicao principal: **sim**, usar release para download.
- Hospedagem da pagina de download/instrucoes: **somente opcao totalmente gratuita**.
- Assinatura digital do executavel: **somente apos validacao da POC**.

## Observacao para a proxima reuniao

Levar esse roteiro e registrar respostas objetivas por item, com dono e prazo quando surgir pendencia tecnica (TI, SESMT, almoxarifado ou gestor).

