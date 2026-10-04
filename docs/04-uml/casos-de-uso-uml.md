# Casos de uso (UML textual) — Sistema de Controle de EPI

## Objetivo

Documentar os casos de uso do sistema em formato textual inspirado em UML, relacionando atores, regras e fluxos operacionais.

## Convencao usada

Cada caso de uso segue o formato:

- **ID**
- **Nome**
- **Atores**
- **Descricao**
- **Pre-condicoes**
- **Gatilho**
- **Fluxo principal**
- **Fluxos alternativos/excecoes**
- **Pos-condicoes**
- **Regras de negocio relacionadas**

## Atores do sistema

- **Admin**
- **SESMT**
- **Almoxarife**
- **Consulta**
- **Gestor** (ator do fluxo de solicitacao; escopo limitado aos trabalhadores sob sua responsabilidade)
- **Trabalhador** (ator participante na validacao de entrega; nao opera com login)

---

## Modulo: Admin

### UC-ADM-01 — Cadastrar usuario
- **Atores**: Admin
- **Descricao**: Cria usuario interno para operacao do sistema.
- **Pre-condicoes**:
  - Admin autenticado e autorizado.
  - Mecanismo de auditoria operacional e append-only.
  - Tabela de usuarios ativa com unicidade de login.
- **Gatilho**: Necessidade de novo operador.
- **Fluxo principal**:
  1. Admin acessa tela de usuarios.
  2. Informa nome, login e status inicial.
  3. Define credencial inicial aderente a politica de senha.
  4. Sistema valida politica da credencial e unicidade do login.
  5. Admin confirma cadastro.
  6. Sistema cria usuario com troca obrigatoria no primeiro acesso.
  7. Sistema registra auditoria do evento.
- **Fluxos alternativos/excecoes**:
  - Login ja existente: sistema recusa e solicita novo login.
  - Credencial fora da politica: sistema recusa e orienta ajuste.
  - Falha de auditoria: sistema trata conforme politica transacional definida.
- **Pos-condicoes**:
  - Usuario criado com status inicial informado.
  - Usuario apto a receber papel.
  - Credencial marcada para troca obrigatoria no primeiro acesso.
- **Regras relacionadas**: unicidade de login; politica de senha forte; auditoria obrigatoria.

### UC-ADM-02 — Gerenciar papeis do usuario
- **Atores**: Admin
- **Descricao**: Atribui e remove papeis funcionais (Admin, SESMT, Almoxarife, Consulta).
- **Pre-condicoes**: Usuario existente.
- **Gatilho**: Definicao de permissao operacional.
- **Fluxo principal**:
  1. Admin seleciona usuario.
  2. Visualiza papeis atualmente atribuidos.
  3. Escolhe papel para adicionar ou remover.
  4. Confirma operacao.
- **Fluxos alternativos/excecoes**:
  - Papel incompativel com politica interna: operacao cancelada.
  - Tentativa de remover papel nao atribuido: sistema recusa e orienta revisao.
- **Pos-condicoes**: Usuario passa a operar no escopo permitido.
- **Regras relacionadas**: RBAC; auditoria append-only.

### UC-ADM-03 — Configurar parametros do sistema
- **Atores**: Admin
- **Descricao**: Define parametros globais da operacao.
- **Pre-condicoes**: Admin autenticado.
- **Gatilho**: Inicio de implantacao ou ajuste de politica.
- **Fluxo principal**:
  1. Admin acessa parametros.
  2. Ajusta dominios (motivos, metodos de validacao etc.).
  3. Salva configuracoes.
- **Fluxos alternativos/excecoes**:
  - Parametro invalido: sistema bloqueia salvamento.
- **Pos-condicoes**: Parametros vigentes para os modulos operacionais.
- **Regras relacionadas**: validacao de dominio; versionamento de parametro.

### UC-ADM-04 — Resetar credencial de usuario
- **Atores**: Admin
- **Descricao**: Reseta credencial de usuario interno para recuperar acesso com seguranca.
- **Pre-condicoes**:
  - Admin autenticado e autorizado.
  - Usuario alvo existente.
  - Mecanismo de auditoria ativo.
- **Gatilho**: Solicitacao de recuperacao de acesso ou comprometimento de credencial.
- **Fluxo principal**:
  1. Admin localiza usuario.
  2. Aciona reset de credencial.
  3. Define nova credencial inicial aderente a politica.
  4. Sistema marca troca obrigatoria no proximo login.
  5. Sistema registra auditoria do reset.
- **Fluxos alternativos/excecoes**:
  - Credencial fora da politica: sistema recusa e solicita ajuste.
  - Usuario inexistente/inativo para operacao: sistema recusa.
- **Pos-condicoes**:
  - Credencial anterior invalidada.
  - Nova credencial ativa com troca obrigatoria no proximo acesso.
- **Regras relacionadas**: controle por papel Admin; politica de senha forte; auditoria append-only.

---

## Modulo: Cadastros

### UC-CAD-01 — Cadastrar unidade
- **Atores**: Admin, SESMT
- **Descricao**: Mantem as unidades da empresa ja semeada. Nao cria outra sociedade.
- **Pre-condicoes**: Empresa existente. Permissao de cadastro.
- **Gatilho**: Nova planta, correcao de nome ou inativacao de unidade.
- **Fluxo principal**:
  1. Operador informa nome e CNPJ.
  2. Sistema normaliza o CNPJ para 14 digitos e grava a unidade.
  3. Operador pode editar o nome e inativar ou reativar sem delete fisico.
- **Fluxos alternativos/excecoes**:
  - `CAD-041` Nome ou CNPJ ausente.
  - `CAD-042` CNPJ invalido.
  - `CAD-043` CNPJ ja cadastrado.
  - `CAD-045` Unidade com setor ativo nao inativa.
- **Pos-condicoes**: Unidades ativas disponiveis no cadastro de setor.
- **Regras relacionadas**: uma empresa; CNPJ unico; nome pode repetir; auditoria append-only.
- **Status**: especificado em `docs/03-operacao/spec-uc-cad-01-unidade.md`; implementacao pendente.

### UC-CAD-02 — Cadastrar setor e funcao
- **Atores**: SESMT, Admin
- **Descricao**: Registra e mantem estrutura organizacional (setor e funcao) para vincular trabalhadores e matriz.
- **Pre-condicoes**: Unidade existente.
- **Gatilho**: Inclusao/ajuste de estrutura funcional.
- **Fluxo principal**:
  1. Operador cadastra setor com nome e status inicial.
  2. Operador cadastra funcao vinculada a setor ativo.
  3. Operador pode editar dados e alternar status (inativar/reativar) sem delete fisico.
  4. Para inativacao, sistema exige confirmacao explicita.
- **Fluxos alternativos/excecoes**:
  - `CAD-021` Nome de setor ja existente.
  - `CAD-024` Tentativa de inativar setor com funcao ativa vinculada.
  - `CAD-025` Nome de funcao ja existente no setor.
  - `CAD-026` Setor invalido/inativo para cadastro/edicao de funcao.
  - `CAD-028` Tentativa de inativar funcao com empregado ativo vinculado.
- **Pos-condicoes**: Setores/funcoes ativos disponiveis para cadastro de trabalhador e matriz.
- **Regras relacionadas**: unicidade case-insensitive; sem delete fisico; auditoria de criar/editar/inativar/reativar.

### UC-CAD-03 — Cadastrar empregado (trabalhador)
- **Atores**: SESMT, Admin
- **Descricao**: Inclui e mantem trabalhador apto a receber EPI (editar e alterar status sem delete fisico).
- **Pre-condicoes**: Funcao e setor/departamento cadastrados e ativos; gestor selecionado pertence ao escopo organizacional valido, quando aplicavel.
- **Gatilho**: Admissao ou regularizacao de cadastro.
- **Fluxo principal**:
  1. Operador informa matricula, nome, funcao e setor/departamento.
  2. Vincula gestor responsavel vigente, quando aplicavel.
  3. Define status inicial (ativo/inativo).
  4. Sistema valida obrigatorios, unicidade da matricula, coerencia entre funcao e setor e vigencia da relacao gestor-trabalhador.
  5. Operador confirma cadastro.
  6. Para inativacao, sistema exige confirmacao explicita em tela.
- **Fluxos alternativos/excecoes**:
  - `CAD-001` Matricula ja existente.
  - `CAD-002` Funcao invalida ou inativa.
  - `CAD-003` Inconsistencia entre funcao e setor.
  - `CAD-004` Campos obrigatorios ausentes.
  - `CAD-005` Operacao bloqueada por dependencia historica (quando aplicavel).
  - `CAD-006` Trabalhador alvo inexistente/nao selecionado para alteracao de status/edicao.
- **Pos-condicoes**: Trabalhador ativo/inativo disponivel para entrega e rastreavel por auditoria, com lotacao e gestor responsavel vigentes identificaveis.
- **Regras relacionadas**: matricula unica; consistencia funcao-setor; relacao trabalhador-gestor com vigencia/historico; sem delete fisico; auditoria em criar/editar/inativar/reativar.

### UC-CAD-04 — Cadastrar EPI
- **Atores**: SESMT
- **Descricao**: Cadastra e mantem item de EPI no catalogo com classificacao normativa.
- **Pre-condicoes**: Usuario SESMT autenticado.
- **Gatilho**: Novo item no programa de protecao.
- **Fluxo principal**:
  1. SESMT informa descricao, fabricante e classificacao do EPI conforme Anexo I da NR-6.
  2. Sistema valida obrigatorios e consistencia da classificacao.
  3. SESMT define status inicial (ativo/inativo) e confirma cadastro.
- **Fluxos alternativos/excecoes**:
  - `CAD-031` Campos obrigatorios de EPI ausentes.
  - `CAD-032` Classificacao fora do Anexo I.
  - `CAD-033` EPI duplicado no escopo.
- **Pos-condicoes**: EPI disponivel para vinculacao de CA.
- **Regras relacionadas**: classificacao normativa obrigatoria; sem delete fisico; auditoria.

### UC-CAD-05 — Vincular CA ao EPI
- **Atores**: SESMT
- **Descricao**: Registra e mantem um ou mais CAs associados ao EPI com rastreabilidade de consulta oficial.
- **Pre-condicoes**: EPI existente.
- **Gatilho**: Inclusao/atualizacao de aprovacao do item.
- **Fluxo principal**:
  1. SESMT seleciona EPI.
  2. Informa numero do CA, situacao, vigencia e evidencia da consulta CAEPI.
  3. Sistema valida conflito de vigencia e consistencia para ativacao.
  4. SESMT confirma vinculacao.
- **Fluxos alternativos/excecoes**:
  - `CAD-034` Numero de CA invalido/ausente.
  - `CAD-035` Conflito de vigencia para o mesmo EPI.
  - `CAD-036` Situacao do CA impede ativacao.
  - `CAD-037` Evidencia de consulta oficial ausente.
- **Pos-condicoes**: EPI passa a ter CA(s) rastreavel(is) para uso operacional.
- **Regras relacionadas**: aderencia aos itens 6.4.1 e 6.9 da NR-6; CA obrigatorio para entrega; auditoria.

### UC-CAD-06 — Inativar cadastro mestre
- **Atores**: Admin, SESMT
- **Descricao**: Inativa trabalhador, EPI ou funcao sem apagar historico.
- **Pre-condicoes**: Cadastro existente.
- **Gatilho**: Mudanca organizacional ou desuso de item.
- **Fluxo principal**:
  1. Operador localiza registro.
  2. Marca status inativo.
  3. Confirma operacao.
- **Fluxos alternativos/excecoes**:
  - Registro com dependencia critica: sistema alerta e solicita confirmacao.
- **Pos-condicoes**: Registro inativo para novas operacoes.
- **Regras relacionadas**: historico preservado; auditoria.

### UC-CAD-IMP-01 — Importar cadastros via CSV (validacao previa)

- **Atores**: Admin, SESMT
- **Descricao**: Carga em lote de setores, funcoes e trabalhadores via CSV padronizado, com revisao visual antes de persistir.
- **Pre-condicoes**: Layout CSV documentado; permissoes de importacao; mestres exigidos conforme tipo de arquivo.
- **Gatilho**: Adocao/go-live com volume alto ou migracao de planilha.
- **Fluxo principal**:
  1. Operador envia CSV e tipo de cadastro.
  2. Sistema valida e exibe staging (linhas validas vs linhas com pendencia).
  3. Operador filtra, corrige pendencias via cadastro mestre quando necessario, revalida.
  4. Operador confirma importacao das linhas validas.
  5. Sistema persiste e audita.
- **Fluxos alternativos/excecoes**: ver `docs/03-operacao/spec-uc-cad-imp-01-importacao-csv-cadastros.md` (`CAD-IMP-*`).
- **Pos-condicoes**: Registros validos no cadastro; tentativa auditada.
- **Regras relacionadas**: mesmas invariantes de UC-CAD-02/03; deep link para setor/funcao apenas quando acao humana for necessaria.
- **Detalhamento**: `docs/04-uml/cadastros/UC-CAD-IMP-01 — Importar cadastros via CSV (validacao previa).md`
- **Status**: registrado — DoR aberto (nao implementado).

## Modulo: Integracoes oficiais

### UC-CAE-01 — Importar base oficial CAEPI
- **Atores**: Agendador CAEPI (sistema); `Admin` ou `SESMT` para tentativa manual autorizada.
- **Descricao**: Atualiza a base local de consulta de CAs por meio do arquivo oficial CAEPI.
- **Pre-condicoes**:
  - Fonte oficial configurada.
  - Para tentativa manual, usuario autenticado com permissao.
- **Gatilho**: Tentativa diaria agendada ou nova tentativa manual.
- **Fluxo principal**:
  1. Sistema identifica o ator da tentativa (`SISTEMA` ou `USUARIO`).
  2. Inicia a tarefa em background e devolve controle imediatamente a interface JavaFX.
  3. Exibe fases e andamento na barra de status enquanto mantem a janela responsiva.
  4. Em background, baixa e valida o arquivo ZIP e o texto CAEPI, processa todos os registros sem publicar dados parciais e publica a nova base de forma atomica.
  5. Confirma a publicacao da base e a auditoria de sucesso no mesmo commit, registrando ator, arquivo e quantidade processada.
  6. Atualiza o estado exibido na UI, finaliza a barra de status e remove banner de pendencia/falha.
- **Fluxos alternativos/excecoes**:
  - Download, formato, parse ou persistencia falha: preservar a ultima carga completa, registrar auditoria de falha com motivo e emitir logs tecnicos.
  - Carga pendente/falha: manter app e modulos nao dependentes acessiveis; exibir banner; consultas EPI/CA em somente leitura e mutacoes EPI/CA bloqueadas.
  - Auditoria nao persistida: nao considerar a carga bem-sucedida e manter mutacoes EPI/CA bloqueadas.
  - Usuario sem permissao: negar tentativa manual.
- **Regra de UI**: nenhuma operacao de rede, parse ou persistencia bloqueia a thread JavaFX; tentativa manual concorrente nao inicia uma segunda carga.
- **Pos-condicoes**: Em sucesso, nova base fica disponivel e EPI/CA e habilitado para mutacao; em falha, ultima base integral permanece consultavel, mas mutacoes EPI/CA ficam indisponiveis.
- **Regras relacionadas**: uma auditoria por tentativa com tipo de ator, resultado e motivo em falha; carga atomica; falha CAEPI nao bloqueia acesso geral.
- **Especificacao**: `docs/03-operacao/spec-uc-cae-01-importar-base-caepi.md`.

---

## Modulo: Lotes e estoque

### UC-LOT-01 — Registrar recebimento de lote
- **Atores**: Almoxarife
- **Descricao**: Registra entrada de lote com dados de validade, quantidade e custo.
- **Pre-condicoes**: EPI e CA cadastrados.
- **Gatilho**: Recebimento de material.
- **Fluxo principal**:
  1. Almoxarife seleciona unidade e EPI.
  2. Informa lote, validade da peca, quantidade e custo unitario.
  3. Confirma recebimento.
- **Fluxos alternativos/excecoes**:
  - Quantidade menor ou igual a zero: sistema recusa.
  - Lote duplicado para EPI/unidade: sistema recusa.
- **Pos-condicoes**: Lote criado com saldo inicial disponivel.
- **Regras relacionadas**: saldo inicial positivo; unicidade de lote por escopo.

### UC-LOT-02 — Consultar saldo e validade de lotes
- **Atores**: Almoxarife, SESMT, Consulta
- **Descricao**: Exibe disponibilidade de lotes para entrega.
- **Pre-condicoes**: Lotes cadastrados.
- **Gatilho**: Preparacao para entrega ou controle de estoque.
- **Fluxo principal**:
  1. Operador filtra por EPI/unidade.
  2. Sistema exibe saldo, validade e status.
- **Fluxos alternativos/excecoes**:
  - Sem lote disponivel: sistema retorna lista vazia.
- **Pos-condicoes**: Operador identifica lote apto para entrega.
- **Regras relacionadas**: bloqueio de lote vencido na entrega.

---

## Modulo: Matriz

### UC-MAT-01 — Definir matriz funcao/GHE x EPI
- **Atores**: SESMT
- **Descricao**: Define quais EPIs sao obrigatorios por funcao/GHE.
- **Pre-condicoes**: Funcao/GHE e EPI cadastrados.
- **Gatilho**: Implantacao ou revisao de programa de EPI.
- **Fluxo principal**:
  1. SESMT seleciona funcao/GHE.
  2. Seleciona EPIs obrigatorios.
  3. Define modo de fornecimento e vigencia.
  4. Salva matriz.
- **Fluxos alternativos/excecoes**:
  - Duplicidade de regra ativa: sistema recusa.
- **Pos-condicoes**: Matriz ativa para orientar entrega.
- **Regras relacionadas**: unicidade de regra ativa por perfil + EPI.

### UC-MAT-02 — Definir periodicidade de reposicao
- **Atores**: SESMT
- **Descricao**: Define parametros de periodicidade por item/perfil.
- **Pre-condicoes**: Regra de matriz existente.
- **Gatilho**: Parametrizacao inicial ou ajuste tecnico.
- **Fluxo principal**:
  1. SESMT seleciona item da matriz.
  2. Informa periodicidade e vigencia.
  3. Confirma configuracao.
- **Fluxos alternativos/excecoes**:
  - Sobreposicao de vigencia: sistema bloqueia.
- **Pos-condicoes**: Regra de reposicao aplicavel ao calculo de cobertura.
- **Regras relacionadas**: versionamento por vigencia.

---

## Modulo: Entrega (core)

### UC-SOL-01 — Solicitar EPI para trabalhador
- **Atores**: Gestor; SESMT (analise tecnica); Almoxarife (atendimento).
- **Descricao**: Permite ao gestor solicitar EPI em nome de trabalhador sob seu escopo, consultar historico de entregas efetivas e acompanhar a demanda sem registrar a entrega.
- **Pre-condicoes**:
  - Gestor autenticado no servico central e autorizado para o escopo organizacional vigente.
  - Trabalhador ativo vinculado a unidade/setor/departamento e gestor responsavel.
  - EPI elegivel no catalogo confiavel, conforme estado da base CAEPI.
- **Gatilho**: Necessidade de fornecimento, reposicao, dano, perda ou planejamento operacional.
- **Fluxo principal**:
  1. Gestor seleciona trabalhador do proprio escopo.
  2. Sistema exibe snapshot organizacional vigente, resumo limitado do historico de entregas e solicitacoes em aberto.
  3. Gestor seleciona EPI, quantidade inteira positiva e motivo/contexto.
  4. Sistema valida escopo, elegibilidade, duplicidade e matriz vigente.
  5. Pedido fora da matriz e encaminhado ao SESMT para decisao; demais pedidos seguem a politica de aprovacao definida.
  6. Sistema grava solicitacao, estado inicial e auditoria, sem reservar/baixar estoque.
  7. Almoxarife autorizado atende total ou parcialmente pelo `UC-ENT-01`.
  8. Sistema vincula entrega efetiva ao pedido e atualiza, em metricas separadas, historico e demanda/forecast.
- **Fluxos alternativos/excecoes**:
  - Usuario sem papel Gestor ou trabalhador fora do escopo: negar sem revelar cadastro.
  - Trabalhador inativo/transferido, EPI inativo ou CAEPI sem estado confiavel: bloquear ou encaminhar conforme regra aprovada.
  - Solicitacao duplicada/em aberto: apresentar a existente; nao duplicar silenciosamente.
  - Fora da matriz: exige decisao SESMT com justificativa; Gestor nao autoaprova excecao.
  - Estoque insuficiente/vencido: aguardar estoque ou atender parcialmente se autorizado; pedido nao altera saldo.
  - Mudanca de gestor/setor/unidade, desligamento, cancelamento ou atendimento concorrente: preservar snapshot e aplicar transicao atomica/auditada.
- **Pos-condicoes**: Pedido e decisao ficam rastreaveis. Somente atendimento via UC-ENT-01 cria entrega, baixa estoque e entra no consumo realizado.
- **Regras relacionadas**: escopo organizacional validado no backend; pedido != entrega/reserva/consumo; historico limitado; forecast separa demanda solicitada/aprovada de fornecimento realizado.
- **Especificacao**: `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md`.
- **Matriz de testes**: `docs/03-operacao/matriz-testes-uc-sol-01.md`.
- **Persistencia e UX propostas**: modelo logico e mock textual nas secoes 6.1/6.2 da especificacao.

### UC-ENT-01 — Registrar entrega de EPI
- **Atores**: Almoxarife, SESMT; Trabalhador (participante)
- **Descricao**: Registra formalmente a entrega de EPI ao trabalhador.
- **Pre-condicoes**:
  - Trabalhador ativo;
  - lote disponivel e valido;
  - operador autenticado.
- **Gatilho**: Admissao, troca periodica, dano, extravio ou mudanca de funcao.
- **Fluxo principal**:
  1. Operador localiza trabalhador.
  2. Sistema carrega itens esperados pela matriz.
  3. Operador seleciona lote e quantidade por item.
  4. Sistema valida saldo e validade.
  5. Trabalhador realiza validacao/ciencia por item.
  6. Operador confirma entrega.
  7. Sistema grava entrega imutavel, baixa lote e registra auditoria.
- **Fluxos alternativos/excecoes**:
  - Lote vencido: bloquear item.
  - Saldo insuficiente: bloquear confirmacao.
  - Item fora da matriz: exigir justificativa e autorizacao.
  - Falha de validacao: abortar confirmacao.
- **Pos-condicoes**:
  - Entrega registrada de forma imutavel;
  - saldo atualizado;
  - trilha de auditoria persistida.
- **Regras relacionadas**:
  - imutabilidade da entrega;
  - transacao unica (entrega + saldo + auditoria).

### UC-ENT-02 — Registrar aceite do termo de responsabilidade
- **Atores**: Almoxarife, SESMT; Trabalhador (participante)
- **Descricao**: Registra aceite do termo legal associado ao evento de entrega.
- **Pre-condicoes**: Entrega em processo de confirmacao.
- **Gatilho**: Finalizacao da entrega.
- **Fluxo principal**:
  1. Sistema apresenta texto do termo.
  2. Trabalhador valida aceite conforme metodo definido.
  3. Operador confirma.
  4. Sistema vincula aceite a entrega.
- **Fluxos alternativos/excecoes**:
  - Trabalhador nao aceita: entrega nao e concluida.
- **Pos-condicoes**: Termo aceito e anexado ao evento.
- **Regras relacionadas**: termo obrigatorio para conclusao.

### UC-ENT-03 — Consultar historico de entrega por trabalhador
- **Atores**: SESMT, Almoxarife, Consulta
- **Descricao**: Consulta historico de entregas de um trabalhador.
- **Pre-condicoes**: Trabalhador cadastrado.
- **Gatilho**: Auditoria, atendimento operacional ou geracao de relatorio.
- **Fluxo principal**:
  1. Operador informa matricula e periodo.
  2. Sistema retorna entregas, itens, CAs e status.
- **Fluxos alternativos/excecoes**:
  - Sem dados no periodo: retorno vazio.
- **Pos-condicoes**: Historico disponibilizado para decisao/relatorio.
- **Regras relacionadas**: filtros por unidade e periodo.

---

## Modulo: Pos-entrega

### UC-POS-01 — Registrar devolucao/descarte
- **Atores**: Almoxarife, SESMT
- **Descricao**: Registra devolucao ou descarte de item entregue.
- **Pre-condicoes**: Item entregue existente.
- **Gatilho**: Desgaste, dano, extravio, desligamento ou outro motivo.
- **Fluxo principal**:
  1. Operador localiza item entregue.
  2. Informa data e motivo da devolucao/descarte.
  3. Confirma operacao.
  4. Sistema registra evento e auditoria.
- **Fluxos alternativos/excecoes**:
  - Data anterior a entrega: sistema recusa.
  - Ja existe devolucao para item: sistema recusa.
- **Pos-condicoes**: Devolucao/descarte registrada.
- **Regras relacionadas**: maximo uma devolucao por item.

### UC-POS-02 — Registrar estorno de entrega
- **Atores**: SESMT, Admin
- **Descricao**: Corrige erro de lancamento por estorno formal.
- **Pre-condicoes**: Item de entrega existente e autorizado para estorno.
- **Gatilho**: Identificacao de erro operacional.
- **Fluxo principal**:
  1. Operador seleciona item de entrega incorreto.
  2. Informa motivo do estorno.
  3. Confirma estorno.
  4. Sistema grava evento de estorno e auditoria.
- **Fluxos alternativos/excecoes**:
  - Motivo vazio: sistema recusa.
- **Pos-condicoes**: Item fica estornado sem apagar historico original.
- **Regras relacionadas**: proibicao de update/delete em prova legal.

### UC-POS-03 — Consultar pendencias de devolucao
- **Atores**: SESMT, Almoxarife, Consulta
- **Descricao**: Lista itens que exigem devolucao/regularizacao.
- **Pre-condicoes**: Entregas existentes.
- **Gatilho**: Rotina de controle, desligamento ou auditoria.
- **Fluxo principal**:
  1. Operador aplica filtros (unidade, periodo, trabalhador).
  2. Sistema retorna pendencias abertas.
- **Fluxos alternativos/excecoes**:
  - Nenhuma pendencia: retorno vazio.
- **Pos-condicoes**: Lista pronta para acao operacional.
- **Regras relacionadas**: consistencia com status de entrega/devolucao/estorno.

---

## Modulo: Relatorios

### UC-REL-01 — Gerar ficha por trabalhador e periodo
- **Atores**: SESMT, Consulta, Admin
- **Descricao**: Gera ficha formal com entregas/devolucoes/estornos no periodo.
- **Pre-condicoes**: Dados operacionais registrados.
- **Gatilho**: Auditoria, fiscalizacao, suporte juridico ou operacao.
- **Fluxo principal**:
  1. Operador informa trabalhador e periodo.
  2. Sistema monta dataset.
  3. JasperReports gera relatorio.
  4. Operador exporta/imprime PDF.
- **Fluxos alternativos/excecoes**:
  - Sem registros: gerar relatorio vazio com aviso.
- **Pos-condicoes**: Evidencia formal emitida.
- **Regras relacionadas**: reprodutibilidade do recorte.

### UC-REL-02 — Gerar historico por EPI/CA/lote
- **Atores**: SESMT, Consulta
- **Descricao**: Rastreia movimentacao por item, CA e lote.
- **Pre-condicoes**: Entregas registradas.
- **Gatilho**: Investigacao tecnica ou auditoria.
- **Fluxo principal**:
  1. Operador filtra EPI/CA/lote e periodo.
  2. Sistema consolida eventos.
  3. Relatorio e exibido/exportado.
- **Fluxos alternativos/excecoes**:
  - Filtro sem resultado: relatorio sem linhas.
- **Pos-condicoes**: Rastreabilidade documental disponivel.
- **Regras relacionadas**: integridade de vinculo item-lote-CA.

### UC-REL-03 — Gerar relatorio de cobertura por trabalhador ativo
- **Atores**: SESMT, Consulta
- **Descricao**: Compara exigencia da matriz com situacao vigente por trabalhador.
- **Pre-condicoes**: Matriz e entregas registradas.
- **Gatilho**: Rotina de conformidade operacional.
- **Fluxo principal**:
  1. Operador seleciona unidade e periodo de referencia.
  2. Sistema cruza matriz x entregas vigentes.
  3. Exibe cobertura e lacunas.
- **Fluxos alternativos/excecoes**:
  - Falta de matriz ativa: sistema alerta inconsistencia.
- **Pos-condicoes**: Plano de regularizacao operacional.
- **Regras relacionadas**: definicao de vigencia e periodicidade.

### UC-REL-04 — Gerar relatorio de consumo para budget
- **Atores**: SESMT, Consulta, Admin
- **Descricao**: Consolida consumo/custo por unidade, setor e funcao.
- **Pre-condicoes**: Lotes com custo e entregas registradas.
- **Gatilho**: Fechamento mensal e planejamento orcamentario.
- **Fluxo principal**:
  1. Operador define recorte temporal e organizacional.
  2. Sistema calcula consumo e custo.
  3. Relatorio e gerado para analise.
- **Fluxos alternativos/excecoes**:
  - Custo ausente em lote: sistema sinaliza dados incompletos.
- **Pos-condicoes**: Base quantitativa para tomada de decisao de budget.
- **Regras relacionadas**: rastreabilidade custo-lote-entrega.

---

## Casos de uso transversais

### UC-TRV-01 — Autenticar usuario
- **Atores**: Todos os operadores com login
- **Descricao**: Valida credenciais para acesso ao sistema.
- **Pre-condicoes**: Usuario cadastrado e ativo.
- **Gatilho**: Abertura do sistema.
- **Fluxo principal**:
  1. Usuario informa login e senha.
  2. Sistema valida credenciais.
  3. Sistema verifica estado da credencial e bloqueio de conta.
  4. Se houver troca obrigatoria pendente, sistema redireciona para troca de senha.
  5. Sistema carrega papeis/permissoes.
- **Fluxos alternativos/excecoes**:
  - Credencial invalida: acesso negado.
  - Usuario inativo: acesso negado.
  - Limite de tentativas invalidas atingido: conta bloqueada temporariamente.
  - Conta bloqueada no momento do login: acesso negado ate fim da janela de bloqueio.
- **Pos-condicoes**:
  - Sessao autenticada quando credenciais e estado da conta estao validos.
  - Evento auditavel registrado para falhas criticas de autenticacao (quando aplicavel).
- **Regras relacionadas**: senha com hash seguro; controle por papel; bloqueio por tentativas invalidas; troca obrigatoria quando sinalizada.

### UC-TRV-02 — Registrar auditoria de evento sensivel
- **Atores**: Sistema (automatico)
- **Descricao**: Registra em trilha append-only os eventos criticos.
- **Pre-condicoes**: Operacao sensivel em execucao.
- **Gatilho**: Criacao de entrega, devolucao, estorno, alteracao de cadastro critico etc.
- **Fluxo principal**:
  1. Sistema identifica evento auditavel.
  2. Grava usuario, data/hora, entidade e acao.
- **Fluxos alternativos/excecoes**:
  - Falha de auditoria: operacao principal deve ser tratada conforme politica transacional.
- **Pos-condicoes**: Trilha de auditoria persistida.
- **Regras relacionadas**: auditoria sem edicao/exclusao.

### UC-TRV-03 — Exportar relatorio em PDF
- **Atores**: SESMT, Consulta, Admin
- **Descricao**: Exporta resultado de relatorio para PDF.
- **Pre-condicoes**: Relatorio gerado.
- **Gatilho**: Necessidade de compartilhamento, impressao ou arquivo.
- **Fluxo principal**:
  1. Operador aciona exportacao.
  2. Sistema gera PDF via motor de relatorio.
  3. (Opcional) PDFBox aplica pos-processamento.
  4. Arquivo e disponibilizado.
- **Fluxos alternativos/excecoes**:
  - Falha na geracao: sistema apresenta erro e registra log.
- **Pos-condicoes**: PDF emitido com sucesso.
- **Regras relacionadas**: padrao de layout e rastreabilidade de versao.

---

## Observacoes finais

- Este documento representa o baseline funcional atual e deve evoluir junto com o backlog.
- Mudancas em regras legais (NR-6) ou em arquitetura devem refletir aqui.
- Quando houver prototipo de tela, cada caso de uso deve referenciar a tela correspondente.
