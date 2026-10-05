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

### UC-ADM-03 — Parametros da instalacao
- **Atores**: Admin
- **Descricao**: Grava a unidade padrao da instalacao. Empresa, CNPJ, horario CAEPI, motivos e termo ficam so leitura.
- **Pre-condicoes**: Ao menos uma unidade ativa.
- **Gatilho**: Apontar a barra e os filtros para a planta desta instalacao.
- **Fluxo principal**:
  1. Admin escolhe a unidade ativa.
  2. Salva.
- **Fluxos alternativos/excecoes**:
  - `ADM-001` Unidade inativa.
- **Pos-condicoes**: A barra mostra o nome. Lote, ficha e matriz nao mudam.
- **Status**: especificado em `docs/03-operacao/spec-uc-adm-03-parametros.md`.

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
  - `CAD-044` Unidade alvo nao encontrada.
  - `CAD-045` Unidade com setor ativo nao inativa.
- **Pos-condicoes**: Unidades ativas disponiveis no cadastro de setor.
- **Regras relacionadas**: uma empresa; CNPJ unico; nome pode repetir; auditoria append-only.
- **Status**: implementado em `docs/03-operacao/spec-uc-cad-01-unidade.md`.

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
  1. SESMT informa descricao e classificacao do EPI conforme Anexo I da NR-6. O fabricante da peca entra no recebimento do lote.
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

### UC-CAD-07 — Cadastrar GHE
- **Atores**: SESMT, Admin
- **Descricao**: Grupo de exposicao da unidade. Funcoes membros compartilham a lista de EPI.
- **Pre-condicoes**: Unidade existente. Funcao ativa dessa unidade, para o vinculo.
- **Gatilho**: Varias funcoes exigem o mesmo conjunto de EPI.
- **Fluxo principal**:
  1. Operador informa unidade, nome e status.
  2. Vincula funcoes ativas da mesma unidade.
  3. Edita o nome, desvincula ou inativa sem delete fisico.
- **Fluxos alternativos/excecoes**:
  - `CAD-051` a `CAD-055`.
- **Pos-condicoes**: GHE ativo e o perfil vigente das funcoes membros. Sem GHE ativo, o perfil e a funcao.
- **Regras relacionadas**: um GHE por funcao; trabalhador sem coluna de GHE; lista de EPI no `UC-MAT-01`.
- **Status**: implementado. Spec em `docs/03-operacao/spec-uc-cad-07-ghe.md`.

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
- **Atores**: Almoxarife, Admin
- **Descricao**: Grava o movimento `RECEBIMENTO` com CA da compra, validade da peca, tamanho e custo.
- **Pre-condicoes**: Unidade ativa. EPI com CA ativo.
- **Gatilho**: Recebimento de material.
- **Fluxo principal**:
  1. Operador escolhe unidade, EPI e o CA da peca.
  2. Informa lote, validade, quantidade e custo, se souber.
  3. Confirma. Peca ja vencida pede confirmacao e entra com disponivel zero.
- **Fluxos alternativos/excecoes**:
  - `LOT-001` a `LOT-005` e `LOT-007`.
- **Pos-condicoes**: Fisica igual a quantidade. Reservada zero.
- **Status**: implementado. Spec em `docs/03-operacao/spec-uc-lot-01-recebimento.md`.

### UC-LOT-02 — Consultar saldo e validade de lotes
- **Atores**: Almoxarife, SESMT, Consulta, Admin
- **Descricao**: Lista fisica, reservada, disponivel e situacao (Vigente, Vencido, Esgotado).
- **Pre-condicoes**: Diario de estoque.
- **Gatilho**: Preparar fornecimento, reserva ou compra.
- **Fluxo principal**:
  1. Operador filtra.
  2. Sistema ordena pela validade mais proxima.
  3. Lote vencido mostra disponivel zero.
- **Fluxos alternativos/excecoes**:
  - Filtro sem resultado: lista vazia.
- **Pos-condicoes**: Operador distingue peca livre, reservada e vencida.
- **Status**: implementado. Spec em `docs/03-operacao/spec-uc-lot-02-consulta-saldo.md`.

### UC-LOT-03 — Reservar e liberar
- **Atores**: Almoxarife, Admin
- **Descricao**: Separa disponivel vigente e libera a reserva sem fornecimento.
- **Pre-condicoes**: Lote vigente com disponivel.
- **Gatilho**: Separacao para atendimento.
- **Fluxo principal**:
  1. Operador reserva uma quantidade.
  2. Pode liberar no todo ou em parte, com confirmacao.
- **Fluxos alternativos/excecoes**:
  - `LOT-008` Acima do disponivel.
  - `LOT-009` Lote vencido.
  - `LOT-010` Liberacao acima do restante.
- **Pos-condicoes**: Compra nao conta essa quantidade de novo. `UC-SOL-01` nao cria a reserva.
- **Status**: especificado em `docs/03-operacao/spec-uc-lot-03-reserva.md`.

### UC-LOT-04 — Baixa de prateleira
- **Atores**: Almoxarife, Admin
- **Descricao**: Baixa vencimento, perda ou descarte do que nao foi fornecido.
- **Pre-condicoes**: Fisica maior que a reservada.
- **Gatilho**: Peca imprestavel na prateleira.
- **Fluxo principal**:
  1. Operador escolhe motivo e quantidade.
  2. Confirma.
- **Fluxos alternativos/excecoes**:
  - `LOT-011` A quantidade entra na reserva.
  - `LOT-012` Motivo ausente.
- **Pos-condicoes**: Perda a parte do consumo.
- **Status**: especificado em `docs/03-operacao/spec-uc-lot-04-baixa-prateleira.md`.

### UC-LOT-05 — Inventariar estoque
- **Atores**: Almoxarife, Admin; leitura para SESMT e Consulta
- **Descricao**: Conta a unidade e grava ajuste so da diferenca.
- **Pre-condicoes**: Nenhum inventario aberto na unidade.
- **Gatilho**: Contagem fisica.
- **Fluxo principal**:
  1. Operador informa a quantidade contada.
  2. Confirma o resumo.
  3. Sistema grava `AJUSTE_INVENTARIO` onde houve diferenca.
- **Fluxos alternativos/excecoes**:
  - `LOT-013` Linha em branco.
  - `LOT-014` Contagem abaixo do reservado.
  - `LOT-015` Inventario aberto ou encerrado.
- **Pos-condicoes**: Fisico reconciliado. Ajuste fora do consumo.
- **Status**: especificado em `docs/03-operacao/spec-uc-lot-05-inventario.md`.

### UC-LOT-06 — Necessidade de compra
- **Atores**: SESMT, Admin; leitura para Almoxarife e Consulta
- **Descricao**: A comprar = demanda informada menos disponivel vigente.
- **Pre-condicoes**: Saldos do diario.
- **Gatilho**: Planejar reposicao.
- **Fluxo principal**:
  1. Operador informa a demanda do periodo.
  2. Sistema desconta reserva e ignora peca vencida.
  3. Sugere valor pelo ultimo custo, ou marca custo incompleto.
- **Fluxos alternativos/excecoes**:
  - Demanda coberta: a comprar zero.
- **Pos-condicoes**: Nenhum movimento de estoque.
- **Status**: especificado em `docs/03-operacao/spec-uc-lot-06-necessidade-compra.md`.

---

## Modulo: Matriz

### UC-MAT-01 — Definir matriz funcao x EPI
- **Atores**: SESMT, Admin
- **Descricao**: EPIs exigidos por perfil, com CA esperado, Individual ou Posto, e flag de treinamento. O perfil e a funcao ou o GHE ativo.
- **Pre-condicoes**: Perfil ativo. EPI ativo com CA ativo. O perfil vigente esta em `UC-CAD-07`.
- **Gatilho**: Implantacao ou revisao do que o perfil usa.
- **Fluxo principal**:
  1. SESMT escolhe a funcao ou o GHE ativo.
  2. Inclui o EPI.
  3. Ajusta modo e treinamento, ou inativa a linha com confirmacao.
- **Fluxos alternativos/excecoes**:
  - `MAT-001` a `MAT-004`, `MAT-007` e `MAT-008`.
- **Pos-condicoes**: Lista ativa do perfil vigente para o fornecimento e a cobertura.
- **Status**: implementado. Spec em `docs/03-operacao/spec-uc-mat-01-matriz.md`.

### UC-MAT-02 — Definir periodicidade de troca
- **Atores**: SESMT, Admin
- **Descricao**: Dias de troca e aviso por EPI, e o texto da cobertura.
- **Pre-condicoes**: EPI em linha ativa da matriz.
- **Gatilho**: Parametrizar a troca.
- **Fluxo principal**:
  1. SESMT informa dias e aviso.
  2. Salva. Nao ha prazo implicito de 180 dias.
- **Fluxos alternativos/excecoes**:
  - `MAT-005` e `MAT-006`.
  - Sem valor: cobertura "Sem prazo".
- **Pos-condicoes**: Situacao em texto: Vigente, Troca em N dias, Prazo vencido, Pendente, Sem prazo ou Posto.
- **Status**: especificado em `docs/03-operacao/spec-uc-mat-02-periodicidade.md`.

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
- **Especificacao**: `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md`, secao 15 fechada.
- **Matriz de testes**: `docs/03-operacao/matriz-testes-uc-sol-01.md`.
- **Status**: especificado; codigo depois do fornecimento.
- **Persistencia e UX propostas**: modelo logico e mock textual nas secoes 6.1/6.2 da especificacao.

### UC-ENT-01 — Registrar fornecimento de EPI
- **Atores**: Almoxarife, SESMT, Admin. Trabalhador no balcao, sem login.
- **Descricao**: Ficha imutavel e `BAIXA_FORNECIMENTO` na mesma transacao.
- **Pre-condicoes**: Trabalhador ativo. Lote vigente com disponivel.
- **Gatilho**: Admissao, troca, dano, extravio ou mudanca de funcao.
- **Fluxo principal**:
  1. Operador localiza o trabalhador e ve a cobertura em texto.
  2. Escolhe item, lote e quantidade.
  3. Registra orientacao e o termo.
  4. Confirma.
- **Fluxos alternativos/excecoes**:
  - `ENT-001` a `ENT-012`.
  - Fora da matriz: so SESMT ou Admin.
  - Cancelar o dialogo nao grava.
- **Pos-condicoes**: Disponivel diminui. Ficha nao se edita.
- **Status**: especificado em `docs/03-operacao/spec-uc-ent-01-fornecimento.md`.

### UC-ENT-02 — Registrar aceite do termo de responsabilidade
- **Atores**: Almoxarife, SESMT, Admin
- **Descricao**: Termo `TERMO-NR6-01` na mesma confirmacao do fornecimento. Metodo `ASSINATURA_MANUAL`.
- **Pre-condicoes**: Ficha em revisao.
- **Gatilho**: Passo Ciencia e termo.
- **Fluxo principal**:
  1. Sistema mostra o texto com o nome do trabalhador.
  2. Operador marca o aceite no balcao.
  3. A confirmacao grava o termo junto da ficha.
- **Fluxos alternativos/excecoes**:
  - `ENT-009` Sem aceite. Nada gravado.
- **Pos-condicoes**: Ficha sem termo nao existe.
- **Status**: especificado em `docs/03-operacao/spec-uc-ent-02-termo.md`.

### UC-ENT-03 — Consultar historico por trabalhador
- **Atores**: SESMT, Almoxarife, Consulta, Admin
- **Descricao**: Lista fornecimento, devolucao e estorno do periodo. Pedido nao entra.
- **Pre-condicoes**: Trabalhador cadastrado.
- **Gatilho**: Consulta operacional ou fiscalizacao.
- **Fluxo principal**:
  1. Operador busca matricula ou nome e informa o periodo.
  2. Sistema lista data, EPI, CA, lote, quantidade, motivo e situacao em texto.
- **Fluxos alternativos/excecoes**:
  - Periodo vazio: "Nenhum fornecimento no periodo."
- **Pos-condicoes**: Nenhuma gravacao.
- **Status**: especificado em `docs/03-operacao/spec-uc-ent-03-historico.md`.

---

## Modulo: Pos-entrega

### UC-POS-01 — Registrar devolucao ou descarte
- **Atores**: Almoxarife, SESMT, Admin
- **Descricao**: Registra a saida do item da guarda do trabalhador. Nao devolve quantidade ao disponivel.
- **Pre-condicoes**: Item que ainda conta.
- **Gatilho**: Desgaste, dano, descarte, desligamento ou extravio.
- **Fluxo principal**:
  1. Operador escolhe o item, a data e o motivo.
  2. Confirma.
- **Fluxos alternativos/excecoes**:
  - `POS-001` a `POS-003`.
- **Pos-condicoes**: Item sai da cobertura. Saldo da prateleira intacto.
- **Status**: especificado em `docs/03-operacao/spec-uc-pos-01-devolucao.md`.

### UC-POS-02 — Registrar estorno de fornecimento
- **Atores**: SESMT, Admin
- **Descricao**: Corrige lancamento com movimento `ESTORNO_FORNECIMENTO`. A ficha original permanece.
- **Pre-condicoes**: Item que ainda conta. Motivo com 10 caracteres.
- **Gatilho**: Erro de lancamento.
- **Fluxo principal**:
  1. Operador descreve o motivo e confirma.
  2. Sistema soma a fisica e nao recria reserva.
- **Fluxos alternativos/excecoes**:
  - `POS-004` e `POS-005`.
- **Pos-condicoes**: Item estornado. Consumo do periodo do estorno diminui.
- **Status**: especificado em `docs/03-operacao/spec-uc-pos-02-estorno.md`.

### UC-POS-03 — Consultar pendencias de devolucao
- **Atores**: Almoxarife, SESMT, Admin
- **Descricao**: Lista item individual de trabalhador inativo que ainda nao voltou.
- **Pre-condicoes**: Fornecimentos registrados.
- **Gatilho**: Desligamento.
- **Fluxo principal**:
  1. Operador filtra.
  2. Abre a devolucao da linha.
- **Fluxos alternativos/excecoes**:
  - Posto, estorno e item ja devolvido ficam de fora.
  - Lista vazia: "Nenhuma pendencia de devolucao."
- **Pos-condicoes**: Nenhuma gravacao na consulta.
- **Status**: especificado em `docs/03-operacao/spec-uc-pos-03-pendencias.md`.

---

## Modulo: Relatorios

### UC-REL-01 — Gerar ficha por trabalhador e periodo
- **Atores**: SESMT, Consulta, Admin
- **Descricao**: PDF do periodo com fornecimento, devolucao, estorno, CA, lote e termo. E o item `6.5.1.1`.
- **Pre-condicoes**: Fichas registradas. O periodo pode estar vazio.
- **Gatilho**: Fiscalizacao ou arquivo.
- **Fluxo principal**:
  1. Operador escolhe o trabalhador e o periodo.
  2. Ve a mesma lista do historico.
  3. Gera o PDF pelo `UC-TRV-03`.
- **Fluxos alternativos/excecoes**:
  - Sem fatos: PDF com a frase de vazio.
  - Almoxarife: `AUTH-004`.
- **Pos-condicoes**: Arquivo com o snapshot da epoca, nao o organograma de hoje.
- **Status**: especificado em `docs/03-operacao/spec-uc-rel-01-ficha.md`.

### UC-REL-02 — Historico por EPI, CA e lote
- **Atores**: SESMT, Consulta, Admin, Almoxarife
- **Descricao**: Quem recebeu a peca, mais recebimento, baixa de prateleira, ajuste e estorno.
- **Pre-condicoes**: Movimentos do diario ou fichas.
- **Gatilho**: Defeito de lote ou fiscalizacao do CA.
- **Fluxo principal**:
  1. Operador informa EPI, CA ou lote, e o periodo.
  2. Sistema lista os fatos.
  3. O PDF repete a consulta.
- **Fluxos alternativos/excecoes**:
  - Sem EPI, CA e lote: nao consulta.
  - Reserva nao entra.
- **Pos-condicoes**: Nenhuma gravacao na consulta.
- **Status**: especificado em `docs/03-operacao/spec-uc-rel-02-historico-epi.md`.

### UC-REL-03 — Cobertura dos trabalhadores ativos
- **Atores**: SESMT, Admin, Almoxarife, Consulta
- **Descricao**: Lista a planta com a mesma situacao do `UC-MAT-02`.
- **Pre-condicoes**: Unidade escolhida.
- **Gatilho**: Rotina do dia.
- **Fluxo principal**:
  1. Operador escolhe a unidade.
  2. Sistema resume cada trabalhador ativo: Sem matriz, pendente, em troca ou vencido, Sem prazo, ou OK.
  3. A linha abre os itens. O PDF repete o recorte.
- **Fluxos alternativos/excecoes**:
  - Trabalhador inativo fica na pendencia de devolucao, nao aqui.
- **Pos-condicoes**: Nenhuma gravacao na consulta.
- **Status**: especificado em `docs/03-operacao/spec-uc-rel-03-cobertura.md`.

### UC-REL-04 — Gerar relatorio de consumo para budget
- **Atores**: SESMT, Consulta, Admin
- **Descricao**: Soma o fornecimento do periodo, com perdas e ajustes em blocos separados.
- **Pre-condicoes**: Movimentos `BAIXA_FORNECIMENTO` com custo e snapshot de unidade, setor e funcao.
- **Gatilho**: Estimar o budget da seguranca do trabalho.
- **Fluxo principal**:
  1. Operador define periodo e recorte.
  2. Sistema soma baixas de fornecimento menos estornos.
  3. Mostra consumo por item e o total. Perdas e ajustes ficam em blocos separados.
- **Fluxos alternativos/excecoes**:
  - Custo ausente na baixa: quantidade entra, valor fica incompleto.
  - Periodo sem movimento: totais zero.
- **Pos-condicoes**: Nenhuma movimentacao. Almoxarife nao abre esta tela.
- **Status**: especificado em `docs/03-operacao/spec-uc-rel-04-consumo-budget.md`.

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
- **Atores**: Quem pode abrir o relatorio de origem
- **Descricao**: JasperReports grava o PDF das linhas que a tela ja mostrou. Sem tela propria.
- **Pre-condicoes**: Consulta feita em `UC-REL-01`, `UC-REL-02`, `UC-REL-03` ou `UC-REL-04`.
- **Gatilho**: Botao Gerar PDF.
- **Fluxo principal**:
  1. Operador escolhe o caminho.
  2. A geracao corre fora da thread da janela.
  3. O arquivo leva filtros, instante e operador.
- **Fluxos alternativos/excecoes**:
  - Cancelar o arquivo: nada gravado.
  - `REL-001` Falha apaga o parcial.
- **Pos-condicoes**: Auditoria `RELATORIO_EXPORTADO`. PDFBox fica fora.
- **Status**: especificado em `docs/03-operacao/spec-uc-trv-03-pdf.md`.

---

## Observacoes finais

- Este documento representa o baseline funcional atual e deve evoluir junto com o backlog.
- Mudancas em regras legais (NR-6) ou em arquitetura devem refletir aqui.
- Quando houver prototipo de tela, cada caso de uso deve referenciar a tela correspondente.
