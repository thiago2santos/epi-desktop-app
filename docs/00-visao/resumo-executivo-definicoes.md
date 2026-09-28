# Catadao de definicoes — projeto de controle de EPI

## 1) Contexto de negocio consolidado

- O problema inicial e controle de budget da area de Seguranca do Trabalho.
- A dor real mostrou que sem rastreabilidade de EPI (entrega/devolucao/lote/CA), nao existe budget confiavel.
- O sistema precisa servir ao mesmo tempo para operacao diaria e evidencia juridica.
- A prova de conceito/piloto sera na unidade de Itupeva.

## 2) Tese central validada

- O core do sistema e a entrega de EPI (evento juridico principal).
- Porem a entrega depende de atividades anteriores e posteriores:
  - antes: cadastro, matriz, lote, saldo e periodicidade;
  - durante: entrega imutavel com ciencia/validacao;
  - depois: devolucao, estorno, auditoria e relatorios.

## 3) Escopo modelado por etapa

### Antes da entrega

- Cadastro de EPI e CA.
- Recebimento por lote (validade da peca, quantidade, custo, saldo).
- Matriz por funcao/GHE.
- Parametros de periodicidade/reposicao.

### Durante a entrega

- Registro de entrega por trabalhador.
- Vinculo item x lote x CA.
- Validacao do trabalhador por item.
- Aceite do termo de responsabilidade.
- Evento imutavel com baixa transacional de lote.

### Depois da entrega

- Devolucao/descarte.
- Estorno formal (sem apagar historico).
- Trilha de auditoria append-only.
- Relatorios juridicos e gerenciais extraiveis.

## 4) Regras de negocio criticas fechadas

- Entrega legal nao pode ser editada nem apagada.
- Erro operacional corrige por estorno, nao por alteracao direta.
- Nao entregar lote vencido.
- Nao permitir saldo negativo.
- Entrega fora da matriz so com excecao justificada/autorizada.
- Devolucao nao pode ser anterior a entrega.
- EPI conjugado deve registrar todos os CAs dos componentes.

## 5) Stack definida e justificativa macro

- `Java 25`: base corporativa estavel para regras/transacoes.
- `JavaFX`: operacao desktop de balcao para rotina local.
- `Spring Boot` sem web: DI, servicos e transacao.
- `SQLite` (inicio): base relacional/transacional simples para piloto.
- `JasperReports`: geracao de relatorios padronizados.
- `PDFBox`: pos-processamento de PDF (merge/carimbo/anexos).

## 6) Diretriz de arquitetura e crescimento

- Comecar simples para validar rapido no campo.
- Modelar desde ja pensando em expansao para 8 unidades.
- Tratar `unidade_id` como chave funcional de segregacao/consolidacao.
- Migrar para cliente-servidor cedo no multiusuario (sem esperar dor escalar).

## 7) Relatorios ja previstos

- Ficha por trabalhador e periodo.
- Historico por EPI/CA/lote.
- Cobertura por trabalhador ativo (matriz x vigente).
- Pendencias de devolucao (incluindo desligamento).
- Consumo por unidade/setor/funcao para apoio ao budget.

## 8) Distribuicao e adocao

- Canal de distribuicao: release para download.
- Pagina de apoio/download: somente opcao 100% gratuita.
- Assinatura digital de executavel: somente apos validacao da POC.
- Manual operacional deve deixar claro o procedimento de instalacao e atualizacao.

## 9) Operacao humana no dia a dia

- Key user principal: Seguranca do Trabalho.
- Duas pessoas devem operar no dia a dia (key user + colega).
- Perfis de acesso previstos: Admin, SESMT, Almoxarife, Consulta.
- Fluxo precisa ser simples para uso continuo no balcao.

## 10) O que ja foi produzido no repositorio

- `PLANO.md`: norte de produto e escopo inicial.
- `MODELAGEM_ENTREGA_EPI.md`: modelagem juridica/funcional da entrega.
- `schema_entrega_epi.sql`: schema inicial com constraints e triggers.
- `STACK_E_ARQUITETURA.md`: justificativa tecnica e caminho de evolucao.
- `ROTEIRO_ENTREVISTA_KEY_USER.md`: roteiro de validacao em campo.
- `modelagem_fluxo_epi/ANTES_DA_ENTREGA.md`
- `modelagem_fluxo_epi/DURANTE_A_ENTREGA.md`
- `modelagem_fluxo_epi/DEPOIS_DA_ENTREGA.md`
- `MANUAL_OPERACIONAL_V0.md`: operacao da instalacao ao primeiro relatorio.

## 11) Pendencias conhecidas (nao bloqueantes)

- Fechar layout final dos relatorios (`.jrxml`).
- Definir pacote final de instalacao sem admin (fluxo oficial).
- Detalhar politica formal de backup/restauracao.
- Definir estrategia final de consolidacao entre unidades.

## 12) Identidade comercial e identificadores tecnicos (fechado)

- Marca: `Easy NR6`.
- Produto: `Easy NR6 Gestao de EPI`.
- Dominio principal: `easynr6.com.br`.
- Group base sugerido: `br.com.easynr6`.
- Artifact sugerido: `epi-desktop-app`.

Observacao tecnica:
- para pacote Java, usar sem hifen (ex.: `br.com.easynr6.gestaoepi`);
- para Maven, `artifactId` com hifen e valido.

## 13) Proximo passo recomendado

- Revisar este catadao com o key user.
- Marcar o que ja esta 100% fechado versus o que ainda e decisao em aberto.
- A partir disso, quebrar em backlog de implementacao por modulo:
  1. base de seguranca e auditoria;
  2. antes da entrega;
  3. durante a entrega;
  4. depois da entrega;
  5. relatorios e empacotamento.

