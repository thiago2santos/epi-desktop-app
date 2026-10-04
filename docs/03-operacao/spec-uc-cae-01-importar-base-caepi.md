# Spec Curta - UC-CAE-01 Importar base oficial CAEPI

## Identificacao

- ID: `UC-CAE-01`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CAD`
- Responsavel: Time Easy NR6
- Status: especificado; decisoes da secao 12 fechadas; implementacao nao iniciada

## 1) Contexto

- Problema real:
  - os dados de Certificados de Aprovacao (CA) precisam acompanhar a base oficial CAEPI sem depender de atualizacao manual isolada.
- Atores:
  - `Agendador CAEPI` (sistema), responsavel pela tentativa diaria;
  - `Admin` ou `SESMT`, quando uma tentativa manual for autorizada.
- Impacto se nao resolver:
  - cadastros de EPI/CA podem operar com uma base desatualizada ou sem evidencia confiavel da carga oficial;
  - uma falha de importacao pode ficar invisivel e permitir novos cadastros inconsistentes.

## 2) Fontes e formatos conhecidos

### Fonte A — bulk oficial (automatica e manual preferencial)

- Diretorio oficial informado: `ftp://ftp.mtps.gov.br/portal/fiscalizacao/seguranca-e-saude-no-trabalho/caepi/`
- Arquivo observado no diretorio: `tgg_export_caepi.zip`.
- O ZIP contem `tgg_export_caepi.txt`, texto UTF-8 delimitado por `|`, com 19 colunas (`sourceKind=TGG_PIPE`).
- Metadados de importacao devem registrar `sourceKind=TGG_PIPE` e nome do artefato (`tgg_export_caepi.zip` ou `.txt`).

### Fonte B — exportacao manual do portal (graceful degradation)

- Exportacao CSV gerada pela consulta CAEPI no portal (`RelatorioCA_YYYYMMDD_HHMMSS.csv` observado em operacao).
- Delimitador `;`, UTF-8 com BOM, campos entre aspas; cabecalho inclui `NR Registro CA`, `DATA DE VALIDADE`, `SITUACAO`, `EQUIPAMENTO`, `RAZAO SOCIAL`, etc.
- Coluna `SITUACAO` observada: `VÁLIDO`, `VENCIDO` (normalizar acentos na implementacao).
- Mesmo pipeline de validacao/publicacao atomica; parser dedicado (`sourceKind=CAEPI_RELATORIO_CSV`).
- **Nao substitui** a Fonte A para o agendador diario (FTP), mas **equivale** a carga vigente quando a importacao manual for bem-sucedida.

### Regras comuns

- Multiplas linhas/variantes por numero de CA em ambas as fontes; indice de consulta deve deduplicar por CA (ver §12 item 4 — proposta MVP abaixo).
- A data efetiva de referencia da exportacao e como comprovar integridade do arquivo ainda precisam ser definidos.

**Proposta MVP para deduplicacao por CA (indice de consulta, nao espelho historico de variantes):** para cada `caNumber`, manter uma unica entrada escolhendo a variante com maior prioridade de situacao (`ACTIVE` > `SUSPENDED` > `CANCELED` > `EXPIRED`); empate por maior `DATA DE VALIDADE` quando disponivel (Fonte B).

## 3) Escopo

### No escopo

- tentar obter e importar a base oficial CAEPI uma vez por dia;
- permitir nova tentativa manual por `Admin` ou `SESMT`, conforme autorizacao;
- executar download, validacao, parse e persistencia integralmente em background, sem bloquear a thread principal do JavaFX;
- validar o download, ZIP, arquivo texto, estrutura e registros antes de publicar a nova base;
- publicar a carga de forma atomica: a aplicacao nao deve expor carga parcial;
- manter a ultima base integralmente importada para consulta quando uma tentativa falhar;
- registrar uma auditoria para cada tentativa, concluida ou nao, identificando ator, resultado e motivo de falha;
- emitir logs tecnicos para diagnostico, sem substituir a auditoria;
- informar o estado da base por banner visivel enquanto a carga diaria estiver pendente ou falhar;
- informar fase e andamento da tentativa em uma barra de status visivel, mantendo as telas e a navegacao responsivas;
- bloquear criacao, edicao e vinculacao nos cadastros de EPI e CA enquanto nao houver importacao bem-sucedida para o ciclo diario vigente;
- manter login, navegacao, consultas e demais modulos acessiveis durante a falha.

### Duas vias de carga (obrigatorio no produto)

1. **Automatica em background** — tentativa diaria (e opcionalmente na inicializacao, conforme decisao pendente §12) via agendador/servico; download da fonte oficial; nunca bloqueia a UI; barra de status + banner.
2. **Manual autorizada** — `Admin`/`SESMT` envia artefato oficial quando a automatica falha ou para recuperacao (**graceful degradation**):
   - preferencial: `tgg_export_caepi.zip` ou `tgg_export_caepi.txt` (Fonte A);
   - alternativa observada: `RelatorioCA_*.csv` exportado do portal (Fonte B), mesmo commit atomico e auditoria com `sourceKind` distinto.

Regras de degradacao:

- Falha **automatica** com **ultima base completa** valida: app continua; consulta CAEPI read-only; mutacoes EPI/CA bloqueadas ate nova carga bem-sucedida (automatica **ou** manual).
- Falha automatica **sem** base previa: mesmo bloqueio de mutacao; banner orienta **carga manual** ou nova tentativa.
- Carga **manual** bem-sucedida equivale a carga vigente (mesmo commit atomico, mesma auditoria, `lastMode=MANUAL` nos metadados operacionais).
- Nunca duas importacoes concorrentes (CA-CAE-12).

Referencia de UX no prototipo HTML: `docs/mock/pages/21-caepi-import.html` (simula AUTO + upload manual real com parser).

### Fora de escopo nesta especificacao inicial

- bloquear cadastros que nao dependam da base CAEPI;
- impedir inicializacao ou uso geral da aplicacao por falha de download/importacao;
- definir a regra de conciliacao entre registros CAEPI e os cadastros locais de EPI;
- automatizar consulta online individual ao site CAEPI;
- definir retencao historica de cada arquivo bruto ou das versoes antigas da base.

## 4) Regras de negocio

1. Cada tentativa deve ter um identificador de correlacao e exatamente um evento de auditoria terminal (`SUCESSO` ou `FALHA`). Uma tentativa interrompida inesperadamente deve ser reconhecida como falha antes de liberar mutacoes EPI/CA.
2. A auditoria deve identificar:
   - ator (`SISTEMA` ou `USUARIO`);
   - identificador do usuario quando manual, ou identificador do processo agendador quando automatica;
   - instante da tentativa;
   - resultado;
   - arquivo/fonte tentado;
   - motivo da falha quando houver.
3. O motivo de falha na auditoria deve ser claro e seguro para consulta operacional. Detalhes tecnicos, como classe da excecao e contexto de rede sem credenciais, ficam nos logs.
4. Uma tentativa com download, validacao, parse, persistencia ou registro de auditoria incompleto nao pode ser reportada como sucesso. A publicacao da nova base e o evento de auditoria de sucesso devem ser confirmados atomicamente.
5. A publicacao da base deve ser atomica. Se qualquer validacao ou persistencia falhar, a carga parcial e descartada e a ultima base completa permanece disponivel para consulta.
6. A falha de importacao nao deve encerrar a aplicacao, impedir login ou indisponibilizar consultas e modulos sem dependencia do catalogo CAEPI.
7. Quando nao existir importacao bem-sucedida para o ciclo diario vigente, os cadastros de EPI e CA ficam bloqueados para criacao, edicao e vinculacao. Consultas a dados ja importados permanecem disponiveis como somente leitura.
8. Enquanto a base estiver pendente, atrasada ou com falha, a UI deve apresentar banner persistente com o estado, o instante da ultima importacao bem-sucedida (se houver) e orientacao operacional. O banner so desaparece apos sucesso do ciclo vigente.
9. Tentativas manuais respeitam RBAC e geram auditoria com o usuario autenticado como ator.
10. O arquivo e dados importados devem ser tratados como entrada externa nao confiavel: falhas de formato devem ser explicitas, sem publicar dados incompletos.
11. Nenhuma operacao de rede, leitura/descompactacao do arquivo, parse ou carga no banco pode executar na thread principal do JavaFX.
12. A barra de status apresenta eventos de progresso da tentativa (por exemplo, aguardando, baixando, validando, importando, concluida ou falha). Percentual so deve ser exibido quando puder ser medido; caso contrario, usar indicador indeterminado e texto da fase, sem inventar progresso.
13. Uma tentativa manual retorna controle imediatamente para a UI. O usuario pode continuar navegando e usando modulos sem dependencia CAEPI enquanto a tarefa roda.
14. Atualizacoes de controles JavaFX decorrentes de eventos em background devem ocorrer de forma segura na thread de UI.

## 5) Fluxos

### Fluxo principal - tentativa diaria

1. O agendador inicia a tentativa diaria em background.
2. A UI recebe o evento de inicio e atualiza a barra de status sem bloquear a thread principal.
3. O sistema registra o inicio operacional da tentativa para correlacionar logs.
4. Em background, o sistema baixa e valida o arquivo compactado e sua estrutura, publicando eventos de andamento para a UI.
5. Em background, o sistema processa e valida todos os registros sem modificar a base publicada.
6. O sistema substitui/publica a base em uma operacao atomica.
7. O sistema registra auditoria de sucesso, incluindo origem e resultado da carga.
8. A UI recebe o resultado, informa o horario do sucesso, remove o banner de pendencia/falha e finaliza a barra de status.

### Fluxo alternativo - tentativa manual

1. `Admin` ou `SESMT` autorizado solicita nova tentativa.
2. O sistema inicia o mesmo processo em background e devolve controle imediatamente a UI.
3. A barra de status apresenta fase e andamento; o usuario continua podendo navegar e utilizar o restante do app.
4. O sistema executa os mesmos passos de validacao, publicacao e auditoria do fluxo diario.
5. A auditoria identifica o usuario que disparou a tentativa.

### Fluxos alternativos/excecoes

- `CAE-001` Fonte indisponivel, timeout ou download incompleto: registrar falha; manter ultima base completa; banner informa indisponibilidade.
- `CAE-002` Arquivo ausente, ZIP invalido ou arquivo interno ausente: registrar falha; nao publicar alteracoes parciais.
- `CAE-003` Estrutura, delimitador, codificacao ou cabecalho inesperado: registrar falha com motivo operacional; manter base anterior.
- `CAE-004` Registro invalido ou falha ao persistir/publicar a carga: abortar atomicamente, auditar falha e manter base anterior.
- `CAE-005` Falha ao persistir o evento de auditoria: a tentativa nao pode ser considerada concluida com sucesso; emitir log de erro critico e manter cadastros EPI/CA bloqueados ate haver estado confiavel.
- `CAE-006` Usuario sem permissao solicita tentativa manual: negar a operacao e registrar o evento de seguranca conforme a politica geral de auditoria.
- `CAE-007` Cadastro EPI/CA solicitado sem importacao bem-sucedida no ciclo vigente: bloquear a mutacao e informar motivo, horario da ultima carga bem-sucedida e caminho para consulta.
- `CAE-008` A carga diaria ainda nao ocorreu no ciclo vigente: manter a aplicacao disponivel, mostrar banner de atualizacao pendente e bloquear mutacoes EPI/CA conforme a regra 7.

## 6) Estados operacionais e feedback

| Estado da base CAEPI | Acesso geral ao app | Consulta EPI/CA | Mutacao EPI/CA | Banner |
|---|---|---|---|---|
| Importada com sucesso no ciclo vigente | Disponivel | Disponivel | Disponivel | Nao exibir alerta de pendencia |
| Tentativa em andamento, sem sucesso no ciclo vigente | Disponivel | Disponivel sobre a ultima base completa | Bloqueada | Informar atualizacao em andamento/pendente |
| Tentativa falhou ou ciclo diario pendente | Disponivel | Disponivel sobre a ultima base completa, se houver | Bloqueada | Informar falha/pendencia, ultima carga bem-sucedida e acao recomendada |
| Nunca houve carga completa | Disponivel | Estado vazio/indisponibilidade da base, sem simular catalogo atual | Bloqueada | Informar que a base oficial ainda nao foi importada |

### Barra de status da tarefa em background

- Visivel enquanto houver tentativa em execucao; apresentar fase atual e mensagem curta para o usuario.
- Fases minimas: `Aguardando`, `Baixando arquivo`, `Validando arquivo`, `Importando registros`, `Concluida` e `Falhou`.
- Durante a tarefa, usar progresso indeterminado, exceto quando a etapa oferecer medida confiavel para progresso percentual.
- Ao concluir, mostrar resultado final e permitir consulta do motivo em caso de falha; o banner de estado da base continua visivel conforme a tabela acima.
- A barra nao bloqueia a janela, menu, navegacao ou operacoes de modulos nao dependentes de CAEPI.
- Se ja houver tentativa em execucao, outra tentativa manual nao inicia uma segunda carga concorrente; a UI informa que a atualizacao ja esta em andamento.

## 7) Auditoria e logs

- Um evento terminal de resultado por tentativa, inclusive falha antes do download concluir, identificado por um ID de correlacao.
- A tentativa deve ter estado recuperavel para que uma interrupcao inesperada seja finalizada como falha antes de qualquer desbloqueio de EPI/CA.
- Campos minimos de auditoria: ator/tipo de ator, instante, resultado, identificacao do arquivo/fonte, quantidade de registros processados quando disponivel e motivo de falha.
- Logs tecnicos devem permitir correlacionar etapas da tentativa e diagnosticar falhas sem registrar credenciais, segredos ou conteudo sensivel desnecessario.
- Auditoria e append-only; o registro da tentativa nao pode ser editado ou removido pela UI.
- O evento de sucesso e a publicacao da nova base devem participar do mesmo commit atomico; nao pode haver base nova sem auditoria de sucesso correspondente.
- Caso a arquitetura atual nao consiga persistir a auditoria durante uma indisponibilidade do banco, o comportamento de contingencia deve ser fechado antes da implementacao; nesse estado, sucesso nunca pode ser exibido nem os cadastros EPI/CA liberados.

## 8) Criterios de aceite

- `CA-CAE-01`: o processo tenta atualizar a base CAEPI diariamente, com origem oficial configurada.
- `CA-CAE-02`: uma carga valida so se torna visivel apos validacao completa e publicacao atomica.
- `CA-CAE-03`: arquivo invalido, download incompleto ou erro de persistencia nao substitui a ultima base completa.
- `CA-CAE-04`: cada tentativa concluida registra auditoria com ator (`SISTEMA`/`USUARIO`), resultado e, em falha, motivo.
- `CA-CAE-05`: falha ou pendencia da carga nao impede login, inicializacao ou acesso aos modulos sem dependencia CAEPI.
- `CA-CAE-06`: sem sucesso no ciclo diario vigente, criacao, edicao e vinculacao de EPI/CA ficam indisponiveis; consulta permanece em modo somente leitura.
- `CA-CAE-07`: banner apresenta estado da carga, ultima importacao bem-sucedida e motivo/orientacao quando houver pendencia ou falha.
- `CA-CAE-08`: usuario sem permissao nao consegue disparar carga manual.
- `CA-CAE-09`: logs permitem correlacionar tentativa, etapas e resultado sem expor credenciais ou segredos.
- `CA-CAE-10`: download, validacao, parse e persistencia ocorrem fora da thread principal; a UI continua responsiva durante toda a tentativa.
- `CA-CAE-11`: a barra de status apresenta as fases da tentativa, progresso percentual apenas quando mensuravel, e resultado/motivo ao concluir.
- `CA-CAE-12`: uma nova solicitacao manual durante uma tentativa em andamento nao cria uma segunda importacao concorrente.

## 9) Catalogo inicial de erros

| Codigo | Condicao | Mensagem operacional |
|---|---|---|
| `CAE-001` | Fonte indisponivel ou download incompleto | Nao foi possivel baixar o arquivo oficial CAEPI. A ultima base completa foi preservada. |
| `CAE-002` | ZIP ou arquivo interno invalido/ausente | O arquivo recebido nao pode ser aberto. A ultima base completa foi preservada. |
| `CAE-003` | Cabecalho ou formato inesperado | O formato do arquivo CAEPI nao corresponde ao esperado. A carga foi interrompida. |
| `CAE-004` | Registro inconsistente ou falha ao publicar | A carga CAEPI nao foi concluida. Nenhuma carga parcial foi publicada. |
| `CAE-005` | Auditoria indisponivel | Nao foi possivel confirmar a auditoria da tentativa; a base nao sera considerada atualizada. |
| `CAE-006` | Usuario sem permissao para carga manual | Seu perfil nao permite iniciar a atualizacao CAEPI. |
| `CAE-007` | Mutacao EPI/CA bloqueada por carga pendente/falha | Os cadastros de EPI e CA estao temporariamente indisponiveis ate a atualizacao oficial ser concluida. |

## 10) Cenarios de teste

Matriz inicial: `docs/03-operacao/matriz-testes-uc-cae-01.md`.

## 11) Recorte tecnico para refinamento posterior

- Agendador/servico de tentativa diaria e disparo manual autorizado.
- Porta de download da fonte CAEPI.
- Validador/parse do formato oficial.
- Persistencia versionada ou estrategia equivalente para troca atomica da base.
- Estado operacional da base (ultima tentativa, ultima carga valida, motivo e ciclo de referencia).
- Integracao com `AuditTrail` e logs tecnicos correlacionaveis.
- Politica central de disponibilidade para mutacoes EPI/CA, aplicada fora da UI tambem.
- Banner global e feedback nos cadastros EPI/CA.
- Barra de status global para progresso dos trabalhos CAEPI em background; atualizacoes de UI sincronizadas com JavaFX.

As decisoes operacionais estao na secao 12. A matriz de testes ja tem resultado esperado.

## 12) Decisoes fechadas

1. Fuso `America/Sao_Paulo`. No desktop de demonstracao, a tentativa roda ao abrir o aplicativo se a ultima carga completa for de um dia anterior, e as 06:00 se o aplicativo seguir aberto. Aplicativo fechado nao tem servico externo: a tentativa acontece na proxima abertura. No modo oficial, o servico central roda o ciclo; o cliente so mostra o estado.
2. O ciclo e a data local da tentativa. O arquivo nao traz data de extracao. A integridade exigida e o ZIP conter `tgg_export_caepi.txt`, ou o CSV da Fonte B no caminho manual. A tentativa guarda o tamanho e o SHA-256 dos bytes lidos. Nao ha assinatura do orgao para conferir.
3. A carga publica um catalogo CAEPI separado. Nao cria EPI e nao reescreve o vinculo `epi_ca`. O operador continua escolhendo o CA na hora de vincular. A consulta por numero usa o indice deduplicado.
4. Um registro por numero de CA. Prioridade `ACTIVE`, depois `SUSPENDED`, `CANCELED`, `EXPIRED`. Empate fica com a maior data de validade. Variantes de marca nao entram neste indice.
5. Carga manual: `Admin` e `SESMT`, na tela "Importacao CAEPI". A mesma tela lista as ultimas 30 tentativas: data, modo, resultado, ator e motivo. `Almoxarife` e `Consulta` veem o banner e nao enviam arquivo.
6. Fica a ultima base completa publicada e a trilha de auditoria das tentativas. O arquivo bruto e descartado depois do parse. Sucesso substitui a base publicada inteira.
7. Se a auditoria de sucesso nao gravar, a publicacao desfaz. A tentativa e falha. Sem sucesso no ciclo, mutacao de EPI e CA continua bloqueada.

## 13) Tela

Destino ja previsto: "Importacao CAEPI". Titulo sem codigo de caso de uso.

- Banner persistente no shell enquanto o ciclo vigente nao tiver sucesso: estado em texto, instante da ultima carga completa e a acao ("Aguarde a tentativa" ou "Envie o arquivo oficial").
- Barra de fase: aguardando, baixando, validando, importando, concluida ou falha. Percentual so quando a leitura do arquivo informar tamanho. Sem percentual inventado.
- Sucesso: "Base CAEPI atualizada." e o banner some.
- Falha: a frase do codigo `CAE-`, sem mostrar o codigo.
- Segunda tentativa enquanto a primeira corre: "A atualizacao ja esta em andamento."

## 14) Criterio de saida do refinamento

- decisoes da secao 12 fechadas;
- matriz `matriz-testes-uc-cae-01.md` com o resultado da deduplicacao definido;
- pronto para implementacao sem alterar a disponibilidade dos modulos que nao dependem do catalogo CAEPI.
