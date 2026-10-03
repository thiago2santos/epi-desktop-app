# Matriz de Testes - UC-SOL-01 Solicitar EPI para trabalhador

## Vinculo com a especificacao

- Documento base: `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md`
- A matriz e inicial; os cenarios dependentes de decisoes pendentes precisam ser fechados antes do DoR.

## Legenda

- `DOM`: regras puras de dominio e invariantes.
- `APP`: caso de uso/orquestracao da aplicacao.
- `REPO`: persistencia, consultas, constraints e migracoes.
- `CONTRACT`: contrato comportamental compartilhado por adaptadores.
- `CONCURRENCY`: transacoes e operacoes concorrentes contra banco real.
- `E2E`: fluxo ponta a ponta entre componentes reais do produto.
- `INT`: integracao de componentes, persistencia e transacoes.
- `RBAC`: papel, escopo e autorizacao.
- `SEC`: isolamento de dados, nao enumeracao e controles de seguranca.
- `AUDIT`: trilha append-only.
- `UI`: fluxo e feedback.
- `REPORT`: historico/forecast.

## Status e principios de validacao

Este documento define o contrato de comportamento a validar antes e durante a implementacao. **Nao significa que os testes ou a feature ja existam.** Os cenarios abaixo sao planejamento; nao foram escritos nem executados.

- Testar comportamento observavel, regras e invariantes, nao nomes/estrutura interna de classes. A implementacao pode evoluir sem exigir reescrita dos testes de negocio.
- Usar testes unitarios para regras puras e orquestracao isolada; usar banco real temporario para provar constraints, transacoes e persistencia; usar componentes reais nos fluxos de integracao e E2E.
- Mocks/fakes verificam colaboracao e falhas simuladas, mas nao provam rollback, isolamento transacional, locks, constraints ou seguranca de consultas no banco.
- Toda negacao relevante deve verificar o resultado funcional e os efeitos colaterais: ausencia de gravacao parcial, ausencia de entrega/baixa e auditoria de seguranca quando a politica assim exigir.
- Datas devem ser controlaveis nos testes (relogio injetavel/fixo); nao depender da hora real, ordem aleatoria, rede externa ou dados de producao.
- Casos cuja regra ainda esteja pendente permanecem como perguntas de refinamento; nao escolher uma resposta por conveniencia de teste.

## Camadas e responsabilidade minima

| Camada/suite | O que precisa provar | O que nao prova sozinha |
|---|---|---|
| Dominio (`DOM`) | Validacoes, elegibilidade, transicoes, quantidades, calculo do saldo e invariantes independentes de infraestrutura | RBAC efetivo no servico, persistencia, SQL, atomicidade ou concorrencia real |
| Aplicacao (`APP`) | Ordem/orquestracao do caso de uso, autorizacao em cada operacao, chamadas de portas, resultado e erros de negocio | Comportamento real de adapters ou banco |
| Persistencia (`REPO`) | Mapeamento, filtros de tenant/escopo, ordenacao, constraints, migracoes, eventos e rollback em banco temporario | Semantica de outro banco/dialeto nao testado |
| Contrato de adaptador (`CONTRACT`) | Mesmos resultados e invariantes para cada implementacao de repository/port | Diferencas especificas de concorrencia ou configuracao de cada banco |
| Concorrencia (`CONCURRENCY`) | Corridas reais de envio, idempotencia, atendimento, cancelamento e estoque com transacoes simultaneas | Comportamento de todos os bancos/alvos se executado em apenas um |
| Integracao (`INT`) | Fronteiras entre solicitacao, identidade, matriz, CAEPI, auditoria, estoque e UC-ENT-01 | Experiencia completa do usuario |
| Interface (`UI`) | Conteudo, estados, feedback, navegacao e ausencia de acoes indevidas visiveis ao papel | Autorizacao ou regra confiavel; estas devem ser testadas no servico |
| Ponta a ponta (`E2E`) | Percurso principal e falhas criticas por componentes reais no modo oficial quando disponivel | Cobertura exaustiva de regras; nao substitui testes menores |
| Relatorios/consultas (`REPORT`) | Semantica de agregacao, filtros e ausencia de dupla contagem | Correcao do dado de origem se eventos/transacoes nao forem testados |

### Contrato de adaptadores e bancos

- Definir um conjunto comum de testes de contrato para operacoes de repository: criar/consultar, escopo, idempotencia, transicoes, eventos, ordenacao, concorrencia otimista e comportamento diante de conflito.
- Rodar o contrato em cada adaptador implementado. SQLite temporario e apropriado para o modo individual de demonstracao; ele nao comprova equivalencia com PostgreSQL.
- Quando o banco oficial PostgreSQL existir, rodar nele os testes de migracao, constraints, transacao, isolamento e concorrencia relevantes. Nao declarar comportamento multiusuario oficial validado apenas por teste SQLite.
- Testes de UI/client nao podem acessar repository ou banco oficial diretamente para contornar o servico central.

## Cobertura obrigatoria por comportamento

As linhas existentes na matriz sao cenarios agregados de aceite. Antes do DoR, decompor cada grupo em casos executaveis independentes quando houver resultados diferentes (por exemplo, cada transicao valida/invalida e cada papel). No minimo, a suite deve cobrir:

### Dominio e estados

- Quantidade nula, zero, negativa, fracionaria e acima do limite aprovado; validar limites exatos inferior/superior e unidade inteira.
- Estado inicial correto segundo politica aprovada; todas as transicoes permitidas e negacao de todas as transicoes proibidas, incluindo estado terminal e reabertura.
- Aprovacao integral/rejeicao integral no MVP; atendimento em uma ou varias parcelas; cancelamento de saldo sem alterar entregas ja realizadas.
- Invariantes: quantidade atendida e cancelada nao excede a aprovada; pedido sem aprovacao nao pode ser atendido; rejeitado/cancelado nao pode gerar nova entrega.
- Valores ausentes/inconsistentes e limites temporais (atribuicao iniciando/terminando, trabalhador inativo, CAEPI/matriz com vigencia no instante da operacao), com politica de fuso/instante definida.
- Snapshot do trabalhador, unidade, setor, funcao, gestor e descricao do EPI mantem os valores do envio apos alteracao de cadastro, transferencia ou renomeacao.

### Aplicacao, autorizacao e privacidade

- Gestor autorizado cria, consulta e acompanha pedido de trabalhador vigente no escopo; validar escopo de novo em cada consulta e mutacao.
- Negar acesso por papel, tenant, unidade, atribuicao expirada/inexistente, trabalhador fora do escopo e tentativa de usar IDs adulterados diretamente.
- Pesquisa por nome/matricula, consulta de historico, detalhe de pedido e erros nao revelam a existencia nem dados de trabalhador fora do escopo.
- Cobrir cada papel e acao: Gestor, SESMT, Almoxarife, Admin e Consulta; nenhum acesso administrativo implica aprovacao tecnica e nenhum papel de leitura implica mutacao.
- Separar solicitante, destinatario, analista e atendente na identidade e auditoria; cobrir auto-solicitacao/conflito depois de aprovada a politica.
- Para toda negacao, conferir que nao houve pedido, transicao, entrega ou baixa parcial; validar evento de seguranca quando exigido pela politica de auditoria.

### Auditoria e atomicidade

- Criacao, decisao, recusa, cancelamento, alteracao de estado de estoque/prioridade e cada entrega parcial/final geram eventos com ator, instante, estado anterior/novo, justificativa quando obrigatoria e referencias corretas.
- Evento de dominio e auditoria correlacionada sao gravados atomicamente; uma falha em qualquer gravacao deve desfazer a operacao inteira.
- Injetar falha em cada fronteira de escrita relevante: pedido, evento, auditoria, entrega, item/CA/aceite e movimento/baixa de estoque. Verificar banco apos rollback e tentar consulta/reexecucao.
- Evento append-only nao pode ser alterado/apagado pelo fluxo normal; tentativa de correcao deve seguir o mecanismo de evento/estorno aprovado.
- Repeticao idempotente nao gera segundo pedido nem segundo evento de envio; tentativa negada ou conflito e auditado conforme a politica definida.

### Persistencia e consultas

- Executar migracoes em banco temporario vazio e validar upgrade de schema suportado, constraints, chaves estrangeiras, unicidades e indices essenciais.
- Verificar leituras e escritas filtradas por tenant/unidade/escopo; manipular filtros e identificadores nao pode ampliar acesso.
- Validar campos obrigatorios, relacionamentos ausentes, estado/quantidade incoerentes, ordenacao estavel de eventos e historicos, paginacao e filtros de periodo.
- Verificar que snapshots sao imutaveis para o pedido, enquanto consultas de autorizacao usam atribuicao vigente conforme regra aprovada.
- Confirmar que falha de gravacao nao deixa pedido sem evento, entrega sem pedido correlacionado (quando aplicavel), pedido atendido sem entrega ou saldo alterado sem entrega.

### Duplicidade, idempotencia e concorrencia

- Repetir a mesma requisicao com mesmo ator/tenant/chave/conteudo antes e depois de timeout: retornar o resultado original sem efeitos duplicados.
- Reutilizar chave idempotente com outro conteudo, ator ou tenant conforme os escopos definidos: rejeitar ou tratar pelo contrato aprovado, sem associar dados de terceiros.
- Submeter simultaneamente pedidos potencialmente duplicados; resultado deve seguir a politica de duplicidade aprovada, sem duplicacao silenciosa.
- Dois atendentes tentam atender o mesmo saldo ao mesmo tempo; nunca exceder quantidade aprovada nem estoque valido.
- Cancelamento compete com atendimento; somente um resultado consistente pode vencer, sem entrega desacompanhada ou saldo cancelado e atendido em duplicidade.
- Alteracao concorrente usa controle de versao/conflito definido; cliente recebe estado recuperavel e nao sobrescreve silenciosamente atualizacao alheia.
- Testar concorrencia com transacoes simultaneas no banco alvo, com barreiras/sincronizacao deterministicas; nao usar apenas chamadas sequenciais ou sleeps arbitrarios.

### Estoque, UC-ENT-01, matriz e CAEPI

- Criar/enviar pedido nao reserva nem reduz estoque. Verificar saldos e movimentos antes/depois.
- Atendimento so ocorre por UC-ENT-01, com lote valido, CA aplicavel, saldo suficiente e evidencia de ciencia/aceite; vinculo pedido-entrega e baixa sao atomicos.
- Lote vencido, bloqueado, insuficiente ou concorrente nao permite baixa/entrega irregular.
- Atendimentos parciais repetidos derivam total atendido das entregas vinculadas; saldo pendente, cancelamento de saldo e estado final permanecem corretos.
- EPI fora da matriz exige decisao SESMT autorizada e auditada; perfil Gestor nao pode autoaprovar ou contornar a analise.
- EPI inativo, CA inaplicavel, matriz vencida/ausente e cada estado da importacao CAEPI devem seguir regras aprovadas; nenhum cliente pode declarar elegibilidade sem validacao do servico.
- Cobrir carga CAEPI atual, atrasada, falha, inexistente e ultima carga completa, mas manter expectativa bloqueada ate decisao sobre uso de catalogo anterior.
- Testar substituicao somente se e quando houver regra formal: sem equivalencia automatica, com decisao tecnica e trilha correspondente.

### Historico, demanda e forecast

- Entregas efetivas, pedidos, rejeicoes, cancelamentos, devolucoes e estornos aparecem em categorias distintas.
- Pedido pendente/rejeitado/cancelado nao e consumo; pedidos rejeitados/cancelados nao entram na demanda aberta conforme regra aprovada.
- Parcial: apresentar total solicitado, aprovado, atendido e saldo sem dupla contagem entre pedido e entrega.
- Estorno/devolucao ajusta a visao correspondente sem apagar evento original; diferenciar consumo bruto/liquido apenas conforme definicao de negocio aprovada.
- Filtros de periodo, unidade, setor, funcao e EPI respeitam tenant e autorizacao; fronteiras inclusivas/exclusivas de datas devem ser determinadas e testadas.
- Forecast e indicador de demanda, nao compra, disponibilidade, reserva ou promessa de prazo. Testar explicitamente textos/rotulos e agregacoes para evitar essa interpretacao.
- Validar consultas com conjunto conhecido pequeno e calcular resultado esperado independentemente da implementacao da consulta.

### Cliente JavaFX e fluxo ponta a ponta

- Estados de tela: inicial/sem selecao, carregando, sem resultado, resultado, envio em andamento, sucesso, erro recuperavel, conflito, duplicidade e falha de conectividade.
- Alternar trabalhador/unidade limpa dados contextuais antigos e carrega historico/pedidos do novo contexto autorizado; falha nao deixa dados de pessoa anterior visiveis como se fossem atuais.
- Mensagens distinguem pedido de entrega, estado inicial e falha; nao exibir sucesso antes da confirmacao do servico nem estado "Aprovado / Fila" sem decisao real.
- Timeline e estados visuais derivam de eventos/estado persistidos e cobrem trajetorias diferentes: analise, recusa, espera, parcial, atendimento, cancelamento e conflito.
- Gestor nao ve botoes de entrega, aprovacao, administracao, custos, saldo por lote ou dados fora do escopo; ocultar controle na UI nao substitui a negacao do servico.
- Cobrir fluxo integrado: Gestor cria -> SESMT decide quando requerido -> Almoxarife atende via UC-ENT-01 -> historico e demanda refletem o resultado.
- Fluxos ponta a ponta sao poucos e criticos; regras e combinacoes exaustivas permanecem em DOM/APP/REPO/CONCURRENCY.

### Modo local e modo oficial

- Demonstracao SQLite e individual: nao existe expectativa de fila compartilhada entre instalacoes, sincronizacao ou concorrencia multiusuario.
- Validar o modo oficial somente quando servico central e banco remoto estiverem disponiveis: dois clientes observam a mesma fila por meio do servico, com autorizacao e concorrencia centralizadas.
- Indisponibilidade/perda de conexao nao deve simular commit local ou exibir sucesso falso; o resultado incerto deve poder ser consultado/repetido com idempotencia.
- A matriz de banco deve declarar explicitamente qual adaptador/versao foi exercitado e quais garantias ainda nao foram verificadas.

## Decisoes que bloqueiam expectativas executaveis

Os testes de invariantes marcados como obrigatorios independem das decisoes abaixo; onde houver alternativas de negocio, nao fixar o resultado ate a spec ser aprovada:

1. Papel final, cardinalidade, escopo, delegacao/substituicao e se o gestor pode solicitar para si.
2. Campos obrigatorios, motivo, urgencia, data necessaria, limites e comportamento de emergencia.
3. Estados, aprovacao automatica ou manual, quem decide, autoaprovacao e conflito de interesse; fora da matriz e papel do SESMT.
4. Duplicidade: estados que contam como abertos, janela, permissao de coexistencia, mensagem/confirmacao e resultado de concorrencia.
5. Idempotencia: escopo de unicidade da chave, conteudo que identifica repeticao/conflicto, retencao e consulta do resultado apos timeout.
6. Quem cancela e ate quando; tratamento de transferencia/desligamento; como pedidos abertos sao encaminhados.
7. Atendimento parcial, espera por estoque, expiracao/escalonamento e cancelamento do saldo.
8. Politica de CAEPI atrasado/falho/sem carga e uso da ultima versao completa.
9. Visibilidade/retencao do historico e definicoes exatas de devolucao, estorno, consumo liquido e forecast.
10. Topologia/contratos do servico oficial, bancos/adaptadores suportados e politica de disponibilidade/retry.
11. Semantica local do SQLite demo: limitar-se a fluxo individual ou permitir demonstracao simulada, sem promessa de fila compartilhada.

Antes do DoR, cada decisao aplicavel deve atualizar a spec, criterios de aceite, codigos de erro e resultado esperado dos cenarios relacionados. Nao usar teste para decidir regra de negocio.

## Cenarios

| ID | Tipo | Referencia na spec | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| SOL01-001 | INT/AUDIT/UI | Fluxo principal, CA-SOL-03 | Gestor e trabalhador dentro do escopo; EPI ativo; dados validos | Criar solicitacao | Pedido persistido com solicitante, destinatario, snapshot organizacional, EPI, quantidade, motivo e auditoria; sem baixa/reserva |
| SOL01-002 | RBAC | Regra 15, CA-SOL-01 | Usuario com papel Almoxarife ou Consulta | Abrir/acessar funcao de solicitacao | Acesso negado no servico mesmo que a tela seja chamada diretamente |
| SOL01-003 | RBAC/AUDIT | `SOL-002`, CA-SOL-01 | Gestor autenticado; trabalhador de outra equipe/unidade | Pesquisar por codigo/nome direto e tentar enviar pedido | Trabalhador nao e revelado; acao negada e evento de seguranca conforme politica |
| SOL01-004 | INT | Regra 2, CA-SOL-02 | Trabalhador com gestor/setor vigentes | Criar pedido e depois transferir trabalhador | Pedido preserva snapshot de gestor, setor, funcao e unidade no instante do envio |
| SOL01-005 | INT | `SOL-003` | Trabalhador inativo ou sem lotacao/gestor vigente | Tentar selecionar e solicitar EPI | Operacao bloqueada ou encaminhada conforme decisao aprovada; sem assumir escopo |
| SOL01-006 | INT | Regra 5, `SOL-005` | Gestor e trabalhador autorizados | Enviar quantidade zero, negativa, fracionaria ou acima do limite | Validacao rejeita pedido com mensagem clara; nenhum registro parcial |
| SOL01-007 | INT/UI | Regra 9, `SOL-006`, CA-SOL-04 | Ja existe solicitacao aberta para mesmo trabalhador/EPI | Enviar pedido igual outra vez | Pedido existente e exibido; duplicidade silenciosa nao criada |
| SOL01-008 | INT/AUDIT | Regra 6, `SOL-007`, CA-SOL-05 | EPI fora da matriz vigente | Enviar solicitacao | Estado `PENDENTE_ANALISE`; somente SESMT autorizado aprova/recusa com motivo auditado |
| SOL01-009 | RBAC/AUDIT | `SOL-010` | Gestor solicita para si ou tenta aprovar excecao propria | Criar/analisar o pedido | Autoaprovacao negada; fluxo segue a politica de conflito definida |
| SOL01-010 | INT | Regras 4/7, CA-SOL-06 | Solicitacao aprovada e estoque insuficiente | Criar pedido e consultar lotes | Saldo permanece inalterado e nao reservado; pedido aguarda estoque ou parcial segundo regra aprovada |
| SOL01-011 | INT/AUDIT | `SOL-008`, CA-SOL-07 | Pedido aprovado; saldo cobre apenas parte; lote valido | Atender parte via UC-ENT-01 | Entrega parcial formal, saldo debitado na mesma transacao, CA/lote/aceite e auditoria gravados; pedido permanece parcial com saldo pendente correto |
| SOL01-012 | INT | Regras 4/8, CA-SOL-07 | Pedido aprovado; tentar atender com lote vencido ou saldo insuficiente | Confirmar atendimento | Entrega recusada; nenhuma baixa ou atendimento inconsistente |
| SOL01-013 | INT/AUDIT | Regras 10-13, CA-SOL-08/09/10 | Pedido aberto existente | Aprovar, recusar, aguardar estoque, cancelar e atender em cenarios separados | Somente transicoes autorizadas; justificativas registradas; rejeitados/cancelados retidos sem contar como consumo |
| SOL01-014 | INT/REPORT | Regras 11-14, CA-SOL-09/10 | Trabalhador possui entregas, estorno, devolucao e pedidos em varios estados | Consultar historico e agregados do forecast | Entregas/estornos/devolucoes/pedidos aparecem separados; quantidade parcial nao e dupla contada; cancelados/rejeitados nao contam como demanda aberta ou consumo |
| SOL01-015 | RBAC | Regra 15, CA-SOL-09 | Gestor A e Gestor B com escopos distintos | Consultar historico, tentar alterar filtros/IDs e abrir pedido de trabalhador de B | Gestor A so acessa dados permitidos pelo seu escopo |
| SOL01-016 | INT | `SOL-011`, `SOL-012` | Pedido aberto; trabalhador troca de gestor/setor ou e desligado | Recarregar fila e tentar atender | Snapshot historico intacto; destino/transicao segue regra aprovada; nenhum pedido some ou muda autoria |
| SOL01-017 | INT/AUDIT | `SOL-013`, CA-SOL-11 | Dois almoxarifes atendem simultaneamente o mesmo pedido | Confirmar entregas concorrentes para saldo maior que solicitado | Controle transacional/idempotente impede exceder quantidade aprovada ou saldo |
| SOL01-018 | INT/AUDIT | `SOL-014`, CA-SOL-11 | Pedido aberto; cancelamento e atendimento concorrentes | Executar ambas as acoes em concorrencia | Uma transicao vencedora; sem pedido cancelado com entrega nao correlacionada |
| SOL01-019 | INT | `SOL-015`, CA-SOL-11 | Resposta da criacao se perde apos commit | Repetir requisicao com mesma chave idempotente | Mesmo pedido retornado; nenhuma duplicata criada |
| SOL01-020 | RBAC/AUDIT | Regras 3/15 | Servico oficial central em execucao | Criar, revisar e atender pedido por diferentes usuarios | Auditoria identifica separadamente solicitante, destinatario, analista e atendente; acesso validado no backend |
| SOL01-021 | INT/REPORT | Regra 14, CA-SOL-10 | Pedidos aprovados, abertos, parciais, rejeitados e entregas reais no periodo | Consultar visao de demanda | Metricas segregadas por estado, periodo, unidade/setor/EPI; sem tratar demanda como consumo ou ordem de compra |
| SOL01-022 | INT | Premissa 4 e `SOL-004` | Importacao CAEPI esta atrasada/falhou, existe ultima carga completa | Tentar criar pedido com EPI do catalogo | Resultado conforme politica `UC-CAE-01` aprovada; sem considerar cadastro atual como automaticamente validado |
| SOL01-023 | INT | Premissas 1-3, CA-SOL-12 | Duas instalacoes SQLite separadas e modo oficial cliente-servidor | Criar pedido em uma instalacao e consultar na outra | Demo SQLite nao declara fila compartilhada; modo oficial compartilha pedido via servico central |
| SOL01-024 | UI | Fluxo principal | Gestor abre fluxo de solicitacao | Consultar trabalhador, historico, quantidade, erro e confirmacao | Tela restrita ao objetivo; historico distingue entrega e pedido; nenhum acesso a cadastros/estoque/admin |
| SOL01-025 | INT/AUDIT | `SOL-016` | Urgencia por dano/extravio configurada | Solicitar fluxo urgente | Encaminhamento/prioridade respeita politica aprovada, registra justificativa e nao elimina controles nem auditoria de entrega |
| SOL01-026 | INT/AUDIT | Modelo sec.6.1, CA-SOL-02/03/13 | Atribuicao vigente de gestor e trabalhador; mudanca organizacional depois do envio | Criar solicitacao e encerrar/vencer atribuicao | Pedido preserva snapshot; nova consulta depende da atribuicao vigente; mudanca nao altera escopo/autoria historica |
| SOL01-027 | INT/AUDIT | Modelo sec.6.1, CA-SOL-08/11/13 | Pedido aprovado e duas entregas parciais | Vincular duas linhas de entrega ao mesmo pedido | Quantidade atendida deriva das entregas; soma atendida + cancelada nunca excede aprovada; cada transicao tem evento imutavel e auditoria correlacionada |
| SOL01-028 | INT | Modelo sec.6.1, `SOL-015` | Cliente oficial repete envio apos timeout com mesma chave/ator; depois tenta reutilizar chave em outro pedido | Reenviar requisicao com chave idempotente | Repeticao devolve pedido original; reutilizacao conflitante e rejeitada; nao surgem pedidos duplicados |
| SOL01-029 | RBAC/INT | Modelo sec.6.1, CA-SOL-01/13 | Gestores e trabalhadores de tenants/unidades diferentes | Consultar, solicitar e manipular IDs de outro escopo | Servico central aplica tenant/unidade e atribuicao vigente a cada consulta/mutacao; nenhum dado fora do escopo e revelado |
| SOL01-030 | UI | Mock sec.6.2, CA-SOL-01/09/14 | Gestor com escopo e historico misto | Abrir tela, trocar trabalhador e inspecionar campos/acoes | Exibe apenas pessoas autorizadas; historico efetivo separado de pedidos; sem custo, saldo por lote, cadastro mestre ou acoes de entrega/aprovacao |

## Evidencias esperadas

- matriz usuario/papel/escopo e casos de negacao;
- eventos de auditoria da solicitacao, decisao, cancelamento e atendimento;
- saldos antes/depois para provar que pedido nao reserva estoque e entrega debita atomicamente;
- consultas de historico e forecast demonstrando agregacoes sem dupla contagem;
- testes de concorrencia/idempotencia contra o backend e banco central;
- evidencias de que SQLite local nao sincroniza nem promete fila multiusuario.

## Gate de testes para iniciar implementacao

- Regras e decisoes bloqueadoras da secao 15 da spec foram resolvidas ou explicitamente retiradas do primeiro incremento.
- Casos agregados desta matriz foram decompostos em entradas/bordas/negacoes/transicoes com resultado e erro esperado inequivocos.
- Cada criterio de aceite e regra de negocio tem pelo menos um caso rastreavel; invariantes criticos possuem casos positivos e negativos.
- Contratos das portas/adaptadores e fronteiras transacionais estao definidos o suficiente para selecionar testes reais, sem acoplar regra ao framework.
- Ambientes de teste identificados: banco temporario para persistencia, banco oficial para validacao multiusuario quando existir, e fluxo manual guiado para UX.
- Nenhum teste dependente de decisao pendente e contado como aprovado por escolher uma resposta arbitraria.

## Gate de encerramento da implementacao

- Executar suites unitarias, aplicacionais, de persistencia/contrato e integracao pertinentes ao incremento; registrar comandos e resultados.
- Executar testes de concorrencia/idempotencia contra o banco-alvo oficial antes de declarar o fluxo multiusuario pronto.
- Executar fluxo ponta a ponta e validacao manual guiada de UI nos papeis e estados relevantes.
- Registrar falhas simuladas/rollback, saldos antes/depois, eventos de auditoria e consultas de historico/forecast como evidencias.
- Aplicar mutation testing em mudancas relevantes de regra de negocio, autorizacao ou auditoria conforme `docs/99-governanca/politica-testes-e-mutation.md`; investigar mutantes sobreviventes significativos.
- Atualizar esta matriz e a spec quando o comportamento implementado divergir por decisao aprovada; nao ajustar expectativas apenas para fazer a suite passar.
