# Manual Operacional v0.1 — Sistema de Controle de EPI

## Objetivo deste manual

Guiar o usuario desde a instalacao ate a emissao do primeiro relatorio de entrega de EPI, com foco em operacao real de campo e aderencia ao que ja foi modelado.

## Publico-alvo

- Operadores do sistema (SESMT e almoxarifado).
- Responsavel local pela implantacao na unidade piloto (Itupeva).

## Escopo da versao v0.1

Este manual cobre:

- Instalacao e primeiro acesso.
- Configuracoes iniciais.
- Cadastros obrigatorios antes de operar.
- Fluxo diario de entrega, devolucao e estorno.
- Emissao de relatorios previstos na modelagem.

Este manual ainda nao cobre:

- Integracoes externas.
- Operacao multiunidade em tempo real.
- Assinatura digital formal do executavel.

## 1) Preparacao antes da instalacao

## 1.1 Checklist de ambiente

- Sistema operacional Windows (piloto).
- Permissao para executar aplicativo baixado da internet.
- Pasta local para dados e backup.
- Operador responsavel pelo primeiro login.

## 1.2 Decisoes operacionais da unidade

Antes de instalar, definir:

- Quem sera Admin local do sistema.
- Quem opera como SESMT.
- Quem opera como Almoxarife.
- Rotina de backup (quem faz e quando faz).

## 2) Instalacao e primeiro inicio

## 2.1 Instalacao

1. Baixar a versao oficial do sistema.
2. Executar o instalador da versao.
3. Confirmar abertura do sistema.

> Nota: a estrategia final de instalador sem permissao de admin esta em definicao. Este manual deve ser revisado quando o pacote de distribuicao estiver fechado.

## 2.2 Primeiro inicio

1. Abrir o sistema.
2. Entrar com usuario inicial de administracao.
3. Validar se a base de dados foi criada corretamente.
4. Confirmar acesso ao menu de configuracao.
5. Alterar imediatamente a credencial inicial de administracao quando aplicavel.

## 2.3 Politica de senha (baseline operacional)

- Comprimento minimo: 12 caracteres.
- Exigir ao menos 3 de 4 grupos (maiuscula, minuscula, numero, especial).
- Nao conter login do usuario.
- Nao aceitar senhas triviais/proibidas.
- Senhas devem ser pessoais e intransferiveis.

## 3) Configuracao inicial obrigatoria

## 3.1 Dados da empresa e unidade

Preencher:

- Razao social.
- CNPJ.
- Unidade (piloto: Itupeva).

## 3.2 Usuarios e papeis

Cadastrar usuarios e papeis minimos:

- Admin.
- SESMT.
- Almoxarife.
- Consulta (somente relatorios).

Diretrizes obrigatorias de credencial:

- aplicar politica de senha forte no cadastro inicial;
- evitar compartilhamento de credencial entre operadores;
- manter usuario individual para rastreabilidade de auditoria.

## 3.3 Parametros de operacao

Configurar:

- Formato de identificacao do trabalhador (matricula principal; CPF opcional).
- Politica de estorno (motivo obrigatorio).
- Dominio de motivos de entrega/devolucao.
- Parametros de credencial (quando disponiveis em tela):
  - limite de tentativas invalidas;
  - janela de bloqueio temporario.

## 4) Cadastros base (antes de entregar qualquer EPI)

## 4.1 Cadastro de funcoes e setores

1. Cadastrar setores da unidade.
2. Cadastrar funcoes/cargos.
3. (Opcional) Cadastrar GHE na unidade e vincular as funcoes que compartilham a lista. Tela GHE (`UC-CAD-07`). Sem GHE ativo, vale a matriz da funcao.

## 4.2 Cadastro de trabalhador

Campos minimos:

- Nome completo.
- Matricula.
- Funcao/cargo.
- Setor.
- Status ativo.

## 4.3 Cadastro de EPI e CA

1. Cadastrar descricao e tipo do EPI.
2. Vincular um ou mais CAs por EPI.
3. Marcar status ativo para uso em entrega.

## 4.4 Matriz funcao/GHE x EPI

Tela Matriz (`UC-MAT-01`). O perfil e a funcao ativa fora de GHE ativo, ou o GHE ativo.

1. Escolher o perfil.
2. Incluir o EPI ativo que tenha CA ativo. O CA esperado nasce desse CA.
3. Ajustar Individual ou Posto, e se exige treinamento.
4. Inativar a linha, se o EPI sair da lista. O registro permanece.
5. Na tela Periodicidade (`UC-MAT-02`), informar dias e aviso por EPI. Sem numero salvo, a cobertura diz Sem prazo.

## 4.5 Recebimento por lote

Para cada recebimento:

- EPI.
- Codigo do lote.
- Fabricante.
- Data de validade da peca.
- Quantidade recebida.
- Custo unitario.

Validar:

- saldo inicial > 0;
- lote dentro da validade;
- rastreabilidade do lote para entrega futura.

## 5) Operacao diaria

## 5.0 Acesso e credenciais (rotina diaria)

1. Novo usuario criado deve trocar a credencial no primeiro acesso.
2. Usuario nao deve operar em credencial compartilhada.
3. Tentativas invalidas repetidas podem bloquear temporariamente a conta.
4. Reset de credencial deve ser feito apenas por `Admin`.
5. Eventos de criacao/reset/bloqueio devem permanecer auditaveis.

## 5.1 Fluxo de entrega (durante a entrega)

Na tela Registrar fornecimento:

1. Buscar trabalhador ativo. O painel ao lado mostra a cobertura em texto.
2. Carregar itens esperados pela matriz da funcao/GHE.
3. Selecionar lote valido e quantidade.
4. Capturar a orientacao de uso por item e, se a matriz exige, a data do treinamento.
5. Capturar aceite do termo de responsabilidade.
6. Confirmar no dialogo. Voltar nao grava. A ficha fica imutavel.

Regras criticas:

- Nao entregar lote vencido.
- Nao permitir saldo negativo.
- Excecao fora da matriz exige justificativa/autorizacao.
- Entrega nao pode ser editada; erro vira estorno.

## 5.2 Fluxo de devolucao/descarte (depois da entrega)

Tela Devolucao / descarte. Almoxarife, SESMT e Admin.

1. Localizar o trabalhador, ativo ou inativo.
2. Escolher o item que ainda conta.
3. Informar data e motivo da devolucao/descarte.
4. Confirmar no dialogo. Voltar nao grava.

Regras criticas:

- data de devolucao nao pode ser anterior a data de entrega nem posterior a hoje;
- o saldo da prateleira nao muda;
- uma devolucao por item.

## 5.3 Fluxo de estorno

Tela Estorno. SESMT e Admin. Almoxarife nao corrige a propria ficha.

1. Localizar o trabalhador e o item que ainda conta.
2. Descrever o motivo, com pelo menos 10 caracteres.
3. Confirmar no dialogo. Voltar nao grava.

Regras criticas:

- estorno corrige sem apagar a ficha;
- a quantidade inteira volta para a fisica. A reserva nao e recriada;
- item ja devolvido nao estorna.

## 6) Relatorios previstos e uso operacional

## 6.1 Relatorios minimos da operacao

1. Ficha por trabalhador e periodo (entregas, devolucoes e estornos).
2. Historico por EPI/CA/lote.
3. Cobertura por trabalhador ativo (matriz x vigente).
4. Pendencias de devolucao (ex.: desligamento).
5. Consumo por unidade/setor/funcao (base para budget).

## 6.2 Primeiro relatorio (passo a passo)

Objetivo: emitir uma ficha de entrega por trabalhador.

1. Abrir menu de relatorios.
2. Selecionar relatorio "Ficha por trabalhador e periodo".
3. Informar matricula e periodo.
4. Gerar visualizacao.
5. Validar dados exibidos (EPI, CA, lote, motivo, ciencia).
6. Exportar/imprimir PDF.

## 6.3 Layout de relatorio

Status atual:

- Os tipos de relatorio estao definidos na modelagem.
- O layout final (`.jrxml`) ainda sera fechado.
- A recomendacao e usar JasperReports para layout e PDFBox para pos-processamento, quando necessario.

## 7) Rotina de controle e qualidade dos dados

## 7.1 Fechamento diario

- Conferir entregas do dia.
- Verificar itens com excecao fora da matriz.
- Verificar eventuais estornos abertos.

## 7.2 Fechamento semanal

- Conferir saldos criticos de lote.
- Verificar proximidade de vencimento.
- Revisar pendencias de devolucao.

## 7.3 Fechamento mensal (apoio a budget)

- Emitir consumo por unidade/setor/funcao.
- Comparar consumo real versus esperado da matriz.
- Destacar perdas por extravio/dano.

## 8) Backup e recuperacao (procedimento minimo)

## 8.1 Backup

- Periodicidade minima: diaria.
- Guardar copia em local diferente da maquina operacional.
- Responsavel nomeado por unidade.

## 8.2 Teste de restauracao

- Realizar teste periodico de restauracao.
- Confirmar abertura do sistema e acesso aos relatorios principais.

> Nota: a politica formal de backup/restauracao deve ser detalhada na proxima versao do manual quando o empacotamento e o local definitivo do banco forem fechados.

## 9) Problemas comuns e acao imediata

- **Lote vencido na entrega**: selecionar lote alternativo valido.
- **Saldo insuficiente**: registrar novo recebimento de lote antes de concluir entrega.
- **Item fora da matriz**: registrar justificativa e autorizacao conforme perfil.
- **Erro de lancamento**: nao editar entrega; aplicar estorno formal.
- **Conta bloqueada por tentativas invalidas**: aguardar janela de desbloqueio ou acionar Admin para recuperacao.
- **Credencial esquecida**: solicitar reset ao Admin e realizar troca obrigatoria no proximo login.

## 10) Proximas evolucoes do manual

1. Incorporar capturas de tela reais da aplicacao.
2. Publicar versao com layout final de todos os relatorios.
3. Incluir procedimento oficial de atualizacao de versao.
4. Incluir guia de operacao cliente-servidor para expansao multiusuario e multiunidade.
