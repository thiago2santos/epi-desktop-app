# UI/UX Blueprint — Sistema de Gestão de EPI (NR-6)

## Objetivo

Definir padrão de interface, mapa de telas e plano de execução para codificação das telas com consistência visual e foco operacional.

Este documento foi construído com base nos artefatos já existentes:

- `docs/02-arquitetura/arquitetura-modular.md`
- `docs/04-uml/casos-de-uso-uml.md`
- `docs/01-negocio/fluxo-antes-da-entrega.md`
- `docs/01-negocio/fluxo-durante-a-entrega.md`
- `docs/01-negocio/fluxo-depois-da-entrega.md`

## Decisões já fechadas

1. **Stack UI**: JavaFX + AtlantaFX.
2. **Navegação principal**: menu lateral por módulos (evolução: **home por papel** — ver identidade UX).
3. **Escopo da primeira rodada de telas**: quase completo.
4. **Tom visual**: corporativo sóbrio, foco em clareza; paleta e padrões assinatura em **`easy-nr6-ux-identity.md`** + **`design-tokens.css`**.

> **Identidade de experiência (v0.1):** `docs/02-arquitetura/easy-nr6-ux-identity.md` — marca perceptível, três padrões Easy NR6, tokens, processo mock → tela ouro → campo. Este blueprint continua sendo o mapa de telas e matriz de componentes JavaFX.
>
> **Heurísticas de uso:** `docs/02-arquitetura/usabilidade/README.md`. O Cursor aplica o recorte curto em `.cursor/rules/` quando a tela JavaFX ou a landing está aberta.

## Princípios de design (regras do produto)

1. **Clareza operacional**: cada tela responde "o que fazer agora?".
2. **Segurança de registro**: dados jurídicos sensíveis não são ambíguos.
3. **Velocidade no balcão**: fluxo de entrega/devolução com mínimo de atrito.
4. **Consistência**: mesmo padrão de botões, filtros, tabelas e mensagens.
5. **Prevenção de erro**: bloquear ações inválidas antes da confirmação.
6. **Rastreabilidade visível**: histórico e status sempre acessíveis.
7. **Trabalho em background**: tarefas demoradas devem informar andamento sem bloquear a thread principal nem a navegação.

## Feedback de tarefas em background

- Importações e operações demoradas devem manter a janela JavaFX responsiva; rede, parse e persistência não executam na thread da UI.
- Uma barra de status informa a tarefa ativa, fase e resultado. Usar progresso percentual apenas quando mensurável; caso contrário, indicador indeterminado e texto da etapa.
- O feedback de andamento não deve substituir banners persistentes de indisponibilidade ou alertas que exigem ação do operador.

## Identidade visual (v1)

## Diretriz de marca para produto B2B SST

- Percepção desejada: confiável, técnico, organizado, auditável.
- Evitar: visual "experimental", excesso de cores vibrantes, excesso de elementos decorativos.

## Paleta base sugerida

- Primária: azul institucional (ações principais, links e foco).
- Neutros: cinzas claros/médios (fundos, bordas, tabelas).
- Sucesso: verde para confirmação e status regular.
- Alerta: âmbar para atenção.
- Crítico: vermelho para bloqueio e erros.

## Tipografia e hierarquia

- Fonte sem serifa padrão do sistema (boa legibilidade em desktop).
- Título de página + subtítulo curto orientado a tarefa.
- Seções por blocos com espaçamento consistente.

## Moodboard funcional (benchmark v1)

Objetivo: usar referências reais de mercado para padronizar telas antes de novas implementações.

## Referências de produto (links)

### Contexto Brasil (NR-6 e validade jurídica)

- [WOTY - Gestão de EPIs](https://www.woty.com.br/funcionalidades/gestao-de-epis)
- [Ficha EPI](https://fichaepi.com.br/)
- [Valida EPI](https://validaepi.com.br/)
- [EntregaEPI](https://entregaepi.com.br/)
- [Arkium EPI](https://www.arkium.com.br/produtos/epi)

### Contexto internacional (padrões de UX operacional)

- [iProtectU PPE](https://iprotectu.com/health-and-safety-modules/personal-protective-equipment-ppe-software/)
- [4HSE - PPE management](https://docs.4hse.com/guides/prevention-actions/ppe-management/)
- [Donesafe - PPE module](https://www.donesafe.com/module/ppe/)
- [EcoGestor PPE](https://www.ecogestor.com/en/ppe-2/)
- [ServiceNow - PPE dashboard overview](https://www.servicenow.com/docs/r/employee-service-management/safe-workplace/ppe-overview-dashboard.html)

## Padrões observados (o que copiar)

1. Dashboard com cards de prioridade do dia (pendências, vencimentos, bloqueios).
2. Fluxo de entrega guiado em etapas (wizard), com resumo final antes da confirmação.
3. Tabela operacional com filtros rápidos (usuário, período, status, tipo de evento).
4. Feedback visual explícito: sucesso em verde, erro em vermelho, alerta em âmbar.
5. Evidência auditável sempre associada à ação (quem, quando, o quê, justificativa).
6. Ações destrutivas com confirmação forte e texto de impacto.

## Antipadrões observados (o que evitar)

1. Telas muito carregadas com muitos blocos competindo por atenção.
2. Mensagens genéricas sem orientação de correção.
3. Fluxos críticos com botões ambíguos ("Salvar" sem dizer o resultado esperado).
4. Navegação sem contexto de "onde estou" no módulo.

## Tradução para o Easy NR6 (decisão de design)

- Login: minimalista, foco em autenticação e erro claro com código.
- Dashboard: poucas métricas com ação imediata; sem excesso de gráficos.
- Entrega (wizard): passo a passo obrigatório com bloqueios de domínio.
- Administração de usuários: tarefas separadas por tela (cadastro, papel, reset, gestão).
- Auditoria: tabela com filtros + possibilidade de drill-down por evento.

## Checklist rápido de referência antes de codar tela nova

1. Esta tela já possui referência visual equivalente no moodboard?
2. Há ação primária clara e única?
3. Mensagens de sucesso/erro estão padronizadas?
4. Existe estado vazio e estado de erro previstos?
5. A tela preserva rastreabilidade e governança do fluxo?

## Arquitetura de navegação

## Estrutura principal (menu lateral)

1. Dashboard
2. Operação
   - Entrega de EPI
   - Devolução/Descarte
   - Estorno
3. Cadastros
   - Trabalhadores
   - Funções/Setores/GHE
   - EPI e CA
4. Estoque
   - Lotes e saldos
5. Regras
   - Matriz Função/GHE x EPI
   - Periodicidade
6. Relatórios
7. Auditoria
8. Administração
   - Usuários e papéis
   - Parâmetros

## Número de telas (baseline)

Escopo "quase completo": **18 telas** (algumas com subfluxos/modal).

## Lista de telas do MVP quase completo

1. Login
2. Dashboard
3. Entrega de EPI (wizard)
4. Devolução/Descarte
5. Estorno
6. Consulta de histórico por trabalhador
7. Trabalhadores (lista/cadastro)
8. Funções, Setores e GHE
9. EPI (lista/cadastro)
10. CA por EPI
11. Lotes (recebimento e saldo)
12. Matriz Função/GHE x EPI
13. Periodicidade
14. Relatórios (hub + filtros)
15. Cobertura (matriz x vigente)
16. Pendências operacionais
17. Usuários e papéis
18. Auditoria (consulta de eventos)

## Fluxos críticos e comportamento esperado

## F1 — Entrega de EPI

- Buscar trabalhador.
- Carregar itens esperados pela matriz.
- Selecionar lote válido e quantidade.
- Registrar validação do trabalhador.
- Confirmar entrega e termo.
- Exibir confirmação com número de registro.

**Bloqueios obrigatórios**:
- lote vencido;
- saldo insuficiente;
- item fora da matriz sem justificativa;
- ausência de validação.

## F2 — Devolução/Descarte

- Buscar item entregue.
- Informar motivo e data.
- Confirmar.

**Bloqueio obrigatório**:
- data de devolução anterior à entrega.

## F3 — Estorno

- Selecionar entrega/item.
- Informar motivo obrigatório.
- Confirmar sem apagar histórico.

## F4 — Relatórios

- Selecionar tipo de relatório.
- Informar filtros mínimos (unidade, período, trabalhador quando aplicável).
- Visualizar e exportar PDF.

## Biblioteca de componentes (v1)

## Componentes essenciais

1. Sidebar com seção ativa.
2. Header de página (título + ações principais).
3. Tabela padrão com paginação/filtros.
4. Formulário padrão (grid 2 colunas desktop).
5. Botões:
   - primário (ação principal),
   - secundário (ação complementar),
   - destrutivo (estorno/cancelamentos críticos).
6. Badges de status:
   - vigente, pendente, vencido, estornado.
7. Modal de confirmação com resumo de impacto.
8. Toast/alerta:
   - sucesso, aviso, erro.
9. Empty state (sem dados) com orientação de próximo passo.
10. Loading state para operações longas.

## Guia de uso de componentes JavaFX (UX rules v1)

Objetivo: padronizar decisões de interface e evitar decisões ad-hoc em cada tela.

Legenda de decisão:

- **USAR**: componente recomendado para padrão do produto.
- **CONDICIONAL**: permitido com critério claro.
- **EVITAR**: não usar no fluxo normal do sistema.

### 1) Campos de entrada

- **TextField** — **USAR**
  - Use para entradas curtas e previsíveis (login, matrícula, código, nome curto).
  - Evitar campo muito estreito para conteúdo longo.
- **PasswordField** — **USAR**
  - Use apenas para credencial; nunca exibir senha em texto puro por padrão.
- **TextArea** — **CONDICIONAL**
  - Use para justificativas, observações e motivos de exceção.
  - Evitar para campos que exigem formato rígido.
- **DatePicker** — **USAR**
  - Use para data operacional (entrega, devolução, validade).
  - Permitir digitação manual quando fizer sentido de produtividade.
- **Spinner** — **USAR**
  - Use para quantidade numérica pequena/ajustável (ex: quantidade de item).
  - Evitar quando digitação livre é mais rápida no cenário real.
- **Slider** — **EVITAR**
  - Não usar para dados críticos/auditáveis; baixa precisão para operação NR-6.
- **ColorPicker** — **EVITAR**
  - Sem caso de uso atual no domínio.

### 2) Escolha de opções

- **RadioButton (ToggleGroup)** — **USAR**
  - Use quando só 1 opção é válida e o conjunto é pequeno (2-5 opções).
  - Não usar se o usuário puder marcar múltiplas opções.
- **CheckBox** — **USAR**
  - Use para opções independentes e seleção múltipla.
  - Rótulo deve ser positivo e objetivo.
- **ComboBox** — **CONDICIONAL**
  - Use para lista longa de opção única.
  - Evitar para listas muito curtas (preferir radios) e para seleção múltipla.
- **ChoiceBox** — **EVITAR**
  - Preferir ComboBox por flexibilidade e consistência.

### 3) Ações e comandos

- **Button** — **USAR**
  - Ação primária clara por tela, com verbo explícito (ex: "Confirmar entrega").
  - Evitar texto genérico como "OK" em ações críticas.
- **ToggleButton** — **CONDICIONAL**
  - Use quando houver alternância visível de estado.
  - Evitar para comandos destrutivos.
- **Hyperlink** — **CONDICIONAL**
  - Use para ajuda, documentação e navegação auxiliar.
  - Evitar como ação principal de formulário.

### 4) Estruturas de dados (core operacional)

- **TableView** — **USAR**
  - Componente padrão para auditoria, listagens e gestão operacional.
  - Sempre que possível: filtros, ordenação, paginação e colunas com largura coerente.
- **ListView** — **CONDICIONAL**
  - Use para listas simples sem estrutura tabular.
  - Evitar quando houver múltiplos atributos comparáveis (preferir TableView).
- **TreeTableView / TreeView** — **CONDICIONAL**
  - Use somente se houver hierarquia real de dados.
  - Evitar criar hierarquia artificial só por visual.
- **Pagination** — **USAR**
  - Recomendado para listas extensas e auditoria histórica.

### 5) Navegação e organização de conteúdo

- **Sidebar (VBox + Button)** — **USAR**
  - Decisão já fechada para módulos.
- **MenuBar / Menu / MenuItem** — **USAR**
  - Usar como navegação complementar e ações globais (sessão, sair, atalhos).
- **TabPane** — **CONDICIONAL**
  - Use para poucas visões irmãs no mesmo contexto.
  - Evitar excesso de abas ou labels longas.
- **Accordion / TitledPane** — **CONDICIONAL**
  - Bom para muito conteúdo secundário e FAQs operacionais.
  - Evitar esconder conteúdo que o usuário precisa comparar simultaneamente.
- **SplitPane** — **CONDICIONAL**
  - Use quando comparação lado a lado agrega valor (ex: detalhe + histórico).
  - Evitar layout complexo para tarefas simples.

### 6) Feedback e validação

- **Label (inline feedback)** — **USAR**
  - Padrão já adotado: erro vermelho, sucesso verde.
- **Alert / Dialog** — **USAR**
  - Use para confirmação de ações destrutivas ou irreversíveis.
  - Máximo de duas ações; texto explícito (ex: "Excluir usuário").
- **Toast (implementação custom)** — **CONDICIONAL**
  - Use apenas para confirmação breve e não crítica.
  - Nunca usar como único canal para erro crítico ou ação obrigatória.
- **Tooltip** — **CONDICIONAL**
  - Use para ajuda contextual curta.
  - Evitar depender de tooltip para regra essencial de negócio.

### 7) Layout e legibilidade

- **GridPane** — **USAR**
  - Padrão para formulários (alinhamento consistente campo/rótulo).
- **BorderPane** — **USAR**
  - Padrão para casca de tela (top/left/center).
- **HBox/VBox** — **USAR**
  - Padrão para composição rápida e consistente.
- **FlowPane/TilePane** — **CONDICIONAL**
  - Use para coleções visuais (cards/atalhos), sem rigidez de grade.
- **StackPane** — **CONDICIONAL**
  - Útil para sobreposições e estados de loading.
- **AnchorPane** — **EVITAR**
  - Evitar acoplamento de posição absoluta em telas de negócio.

### 8) Visualização analítica

- **BarChart / LineChart / PieChart** — **CONDICIONAL**
  - Use apenas quando responder pergunta gerencial objetiva.
  - Evitar gráfico decorativo sem decisão acionável.
- **ProgressIndicator / ProgressBar** — **USAR**
  - Use para operações assíncronas perceptíveis.

### 9) Componentes a evitar por padrão do produto

- **WebView** — **EVITAR**
  - Só usar quando houver necessidade real de renderização web interna.
- **HTMLEditor** — **EVITAR**
  - Complexidade alta e baixo valor para o domínio atual.
- **Canvas / desenho livre** — **EVITAR**
  - Evitar para telas operacionais padronizadas.

## Regras de ouro (aplicáveis a qualquer componente)

1. Se o dado é crítico/auditável, priorizar precisão e legibilidade, não "efeito visual".
2. Não esconder ações críticas atrás de interação ambígua.
3. Mensagem de erro deve explicar ação corretiva.
4. Sempre preferir componente nativo e previsível antes de customização avançada.
5. Em dúvida entre duas opções, escolher a de menor carga cognitiva para o operador.

## Referências de UX usadas nesta matriz

- Nielsen Norman Group:
  - https://www.nngroup.com/articles/checkboxes-vs-radio-buttons/
  - https://www.nngroup.com/articles/checkboxes-design-guidelines/
  - https://www.nngroup.com/articles/radio-buttons-default-selection/
  - https://www.nngroup.com/articles/web-form-design/
  - https://www.nngroup.com/articles/tabs-used-right/
  - https://www.nngroup.com/articles/accordions-on-desktop/
- GOV.UK Design System:
  - https://design-system.service.gov.uk/components/radios/
  - https://design-system.service.gov.uk/components/checkboxes/
  - https://design-system.service.gov.uk/components/select/
  - https://design-system.service.gov.uk/components/date-input/
  - https://design-system.service.gov.uk/patterns/dates/
- Material Design:
  - https://m3.material.io/components/dialogs/guidelines
  - https://m1.material.io/components/dialogs.html
  - https://m1.material.io/components/buttons.html
- Acessibilidade para tabelas/grids:
  - https://primer.style/product/components/data-table/accessibility/
  - https://www.accessible-data-interfaces.com/accessible-data-tables-grid-systems/

## Referências oficiais JavaFX/OpenJFX (base técnica)

- API oficial de controles (`javafx.scene.control`):
  - https://openjfx.io/javadoc/23/javafx.controls/javafx/scene/control/package-summary.html
- Guia CSS oficial JavaFX:
  - https://docs.oracle.com/en/java/java-components/javafx/25/docs/javafx.graphics/javafx/scene/doc-files/cssref.html
- Tutorial oficial de componentes (TableView, ComboBox, DatePicker etc):
  - https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/index.html
- Tutorial oficial de layouts:
  - https://docs.oracle.com/javase/8/javafx/layout-tutorial/index.html
- Guia de customização de controles:
  - https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm

## Como usar a documentação oficial no dia a dia

1. Sempre validar na API oficial se o controle possui recursos nativos antes de criar workaround.
2. Priorizar cell factories em `TableView`, `ListView`, `TreeView` e `ComboBox` para customização de item.
3. Usar CSS externo para tema e consistência, evitando excesso de `setStyle(...)` inline.
4. Em customização avançada, separar regra de negócio (`Control`) da renderização (`Skin`).

## Padrões de UX para mensagens

- Linguagem objetiva, sem jargão técnico desnecessário.
- Erro deve dizer:
  1) o que aconteceu,
  2) por que ocorreu,
  3) como corrigir.

Exemplo:
- "Entrega não concluída: lote selecionado está vencido. Selecione um lote vigente para continuar."

## Acessibilidade e usabilidade mínima

- Contraste adequado em textos e estados.
- Navegação por teclado nos fluxos críticos.
- Tamanho mínimo de clique em botões e ações de tabela.
- Ordem de foco previsível em formulários.

## Item 6 executável — Subida das telas com backlog priorizado

## Estratégia de implementação

Implementar em ondas, mantendo sempre "entregável testável" ao final de cada etapa.

## Onda 1 — Base de navegação e estrutura (fundação)

1. Casca da aplicação:
   - login,
   - layout principal com sidebar,
   - header padrão.
2. Sistema de tema:
   - cores/tokens,
   - tipografia,
   - estilos globais.
3. Componentes base:
   - botões, inputs, tabela, modal, toast.

**Critério de pronto**:
- aplicação navegável entre módulos, mesmo com telas parciais.

## Onda 2 — Fluxo core operacional

1. Entrega de EPI (wizard completo).
2. Devolução/Descarte.
3. Estorno.
4. Consulta de histórico por trabalhador.

**Critério de pronto**:
- executar cenário de ponta a ponta (entrega -> devolução/estorno -> consulta).

## Onda 3 — Cadastros e regras de negócio

### Importacao CSV de cadastros (FE-CAD-01 — registrado)

Hub **Importar cadastros** (Admin/SESMT), espelhando espirito da revisao CAEPI:

- upload por tipo (setor, funcao, trabalhador) ou wizard com ordem recomendada;
- **staging**: tabela com linhas validas (verde claro) e com pendencia (amarelo claro);
- destaque na celula causadora; filtros todas / validas / com pendencia;
- celula acionavel **somente** quando faltar mestre (setor/funcao) e o usuario precisar cadastrar — deep link com campo pre-preenchido; revalidar ao retornar;
- confirmacao importa linhas validas; resumo + auditoria.

Spec: `docs/03-operacao/spec-uc-cad-imp-01-importacao-csv-cadastros.md`. **Nao implementar ate DoR.**

0. (Futuro pós-DoR) Importação CSV com staging — `FE-CAD-01`.
1. Trabalhadores.
2. Funções/Setores/GHE.
3. EPI e CA.
4. Lotes.
5. Matriz e periodicidade.

**Critério de pronto**:
- usuário consegue preparar dados sem intervenção técnica.

## Onda 4 — Relatórios, auditoria e administração

1. Hub de relatórios.
2. Cobertura e pendências.
3. Auditoria (consulta).
4. Usuários/papéis e parâmetros.

**Critério de pronto**:
- operação e evidência com autonomia de uso.

## Checklist antes de codar cada tela

Para evitar "inventar coisa do nada", toda tela deve responder:

1. Qual caso de uso ela atende?
2. Qual ator principal usa?
3. Qual decisão o usuário toma nela?
4. Quais validações de domínio se aplicam?
5. Qual ação principal e qual ação de escape?

Se alguma resposta estiver indefinida, pausar e validar com usuário de negócio.

## Decisões de execução fechadas

1. Ordem das ondas após o core:
   - Onda 3 (cadastros/regras) -> Onda 4 (relatórios/admin).
2. Tela de Auditoria na primeira entrega:
   - nível básico (filtros + lista de eventos + exportação).
3. Cobertura e Pendências:
   - telas operacionais próprias (com possibilidade de exportação via módulo de relatórios).

## Próximo passo sugerido

Fechar estas 3 decisões pendentes e, em seguida, detalhar wireframe textual de:

1. Login
2. Dashboard
3. Entrega de EPI (wizard)

Essas 3 telas formam a espinha da experiência para começar implementação com baixa ambiguidade.

## Referencial de qualidade para validar design (feedback loop)

Para sabermos se a UI está chegando no resultado esperado, usar este scorecard por tela (0 a 2 por critério):

- 0 = não atende
- 1 = atende parcialmente
- 2 = atende bem

Critérios:

1. **Clareza da tarefa**: fica evidente o que fazer na tela.
2. **Tempo de execução**: fluxo parece rápido para rotina real.
3. **Prevenção de erro**: bloqueios e validações estão claros.
4. **Confiabilidade percebida**: layout transmite segurança e controle.
5. **Consistência visual**: padrão de campos/botões/mensagens respeitado.
6. **Aderência ao caso de uso**: cobre o cenário modelado sem lacunas.

Meta mínima por tela:

- **>= 10/12** para seguir para desenvolvimento.
- **< 10/12** exige ajustes antes de codar.

## Critérios de aceite por perfil de usuário

- **SESMT**: entende status de conformidade e consegue atuar sem depender de TI.
- **Almoxarife**: conclui operação de balcão com poucos passos e baixo risco de erro.
- **Consulta/Auditoria**: encontra evidência com filtros simples e leitura objetiva.

## Wireframes textuais (v1)

Os wireframes abaixo são de baixa fidelidade textual para validação funcional antes do visual final.

### Wireframe 01 — Login

**Objetivo da tela**
Autenticar usuário e direcionar ao contexto correto de operação.

**Estrutura**

1. **Topo**
   - Logo/Nome do produto.
   - Subtítulo curto: "Gestão de EPI conforme NR-6".
2. **Card central**
   - Campo `Login`.
   - Campo `Senha`.
   - Checkbox "Lembrar usuário neste dispositivo" (opcional).
   - Botão primário: `Entrar`.
   - Link discreto: `Problemas de acesso?`.
3. **Rodapé discreto**
   - Versão do sistema.
   - Contato de suporte.

**Estados**

- Sucesso: redireciona para Dashboard.
- Erro de credencial: mensagem objetiva sem expor regra interna.
- Usuário inativo: mensagem específica orientando contato com admin.

**Critério de aceite funcional**

- Login válido em até 2 ações (preencher + clicar).
- Erros de autenticação com mensagem clara.

### Wireframe 02 — Dashboard

**Objetivo da tela**
Dar visão imediata do que exige ação hoje.

**Estrutura**

1. **Header da página**
   - Título: `Painel Operacional`.
   - Filtro global de unidade (quando aplicável).
   - Ação rápida: `Nova entrega`.
2. **Bloco de indicadores (cards)**
   - Entregas realizadas hoje.
   - Pendências de devolução.
   - Itens com validade/lote crítico.
   - Trabalhadores sem cobertura completa.
3. **Bloco "Ações prioritárias"**
   - Lista de pendências acionáveis (top 10).
   - Ação por linha: `Resolver`.
4. **Bloco "Atalhos"**
   - Entrega
   - Devolução
   - Estorno
   - Relatórios
5. **Bloco "Últimas atividades"**
   - Linha do tempo resumida de eventos recentes.

**Estados**

- Sem dados: orientação de primeira configuração (cadastros/lotes/matriz).
- Erro de carga: opção `Tentar novamente`.

**Critério de aceite funcional**

- Usuário identifica em até 10 segundos qual ação executar.
- Dashboard evita abrir 4 telas para saber prioridade.

### Wireframe 03 — Entrega de EPI (Wizard)

**Objetivo da tela**
Concluir entrega com segurança jurídica e velocidade operacional.

**Estrutura geral**

- Stepper horizontal com 5 passos:
  1. Trabalhador
  2. Itens da matriz
  3. Lote e quantidade
  4. Validação do recebimento
  5. Confirmação

#### Passo 1 — Trabalhador

- Busca por matrícula/nome.
- Exibe função/setor e status ativo.
- Botão `Continuar` habilita somente com trabalhador válido.

#### Passo 2 — Itens da matriz

- Lista de EPIs esperados para função/GHE.
- Colunas: EPI, obrigatoriedade, última entrega, status de vigência.
- Exceção fora da matriz (ação secundária com justificativa obrigatória).

#### Passo 3 — Lote e quantidade

- Para cada item selecionado:
  - seleção de lote;
  - validade da peça (somente leitura);
  - saldo atual (somente leitura);
  - quantidade a entregar.
- Bloqueios automáticos:
  - lote vencido;
  - saldo insuficiente.

#### Passo 4 — Validação do recebimento

- Método de validação (conforme política ativa).
- Campo/referência da validação por item.
- Exibição do termo de responsabilidade.
- Confirmação de ciência.

#### Passo 5 — Confirmação

- Resumo completo antes do commit:
  - trabalhador, itens, lotes, CAs, quantidades, operador.
- Botão primário: `Confirmar entrega`.
- Pós-sucesso:
  - número do registro;
  - opção `Imprimir comprovante`;
  - opção `Nova entrega`.

**Critérios de aceite funcionais**

- Fluxo completo em até 2-3 minutos por atendimento comum.
- Impossível confirmar entrega inválida (sem validação, lote vencido, saldo insuficiente).
- Comprovante gerado imediatamente após sucesso.

## Como coletar feedback de referência (rápido)

Após protótipo clicável das 3 telas:

1. Sessão de 30 min com key user.
2. Executar 3 cenários:
   - primeira entrega;
   - troca por desgaste;
   - consulta de pendência.
3. Aplicar scorecard (12 pontos).
4. Registrar:
   - onde travou;
   - dúvida recorrente;
   - clique desnecessário;
   - sugestão do usuário.
5. Ajustar e repetir até atingir nota alvo.

## Redesenho UX — Módulo Administração (v2)

### Mapa de dores atuais x requisito de redesign

| Dor observada | Risco | UC impactado | Requisito de redesign |
| --- | --- | --- | --- |
| Ações administrativas concentradas e pouco guiadas | Erro operacional e curva de aprendizado alta | UC-ADM-01, UC-ADM-02, UC-ADM-04 | Separar tarefas em páginas dedicadas e criar hub de entrada com objetivo claro por ação |
| Gestão de usuários em lista textual única | Baixa legibilidade para auditoria/decisão | UC-ADM-01, UC-ADM-02 | Trocar por `TableView` com colunas explícitas, filtro e seleção previsível |
| Atribuição de papel com pouca clareza de múltiplos papéis | Configuração incorreta de RBAC | UC-ADM-02 | Exibir papéis atuais, permitir adicionar/remover papel com confirmação e feedback |
| Ações destrutivas sem confirmação forte contextual | Bloqueio/exclusão acidental | UC-ADM-01 (status), UC-ADM-04 (recuperação) | Introduzir `Dialog` com texto de impacto e confirmação explícita |
| Mensagens inconsistentes por contexto | Dificuldade de suporte e diagnóstico | UC-ADM-01, UC-ADM-02, UC-ADM-04 | Padronizar feedback: sucesso verde por 5s, erro vermelho persistente com código |
| Falta de orientação visual da política de credencial | Rejeição frequente de operação | UC-ADM-01, UC-ADM-04 | Exibir regra de senha forte ao lado do campo e validar antes do submit |

### Arquitetura de informação (admin v2)

1. **Hub Administração**
   - Entradas principais: Cadastrar usuário, Atribuir papel, Resetar credencial, Gerenciar usuários.
   - Indicadores rápidos: ativos, bloqueados, troca obrigatória pendente.
2. **Cadastrar usuário**
   - Formulário completo com seleção de múltiplos papéis.
3. **Atribuir papel**
   - Seleção de usuário + papel com ações de adicionar/remover.
4. **Resetar credencial**
   - Seleção de usuário + nova credencial + confirmação.
5. **Gerenciar usuários**
   - Tabela operacional com filtro, edição, bloqueio, reativação e exclusão.

### Wireframes textuais — Administração (v2)

#### A) Hub Administração

- **Objetivo**: Direcionar o admin para a tarefa certa em 1 clique.
- **Estrutura**:
  - Título da página + subtítulo.
  - Cards de ação (4): cadastro, papel, reset, gestão.
  - Painel de indicadores operacionais.
- **Estados**:
  - normal: cards habilitados;
  - sem dados: indicadores zerados com orientação.

#### B) Cadastrar Usuário

- **Objetivo**: Criar conta com credencial forte e papéis iniciais.
- **Estrutura**:
  - Campos: nome, login, credencial inicial, ativo.
  - Grupo de papéis (`CheckBox`) com seleção múltipla.
  - Bloco de ajuda da política de senha.
  - Ações: `Cadastrar`, `Limpar`.
- **Validações**:
  - nome/login obrigatórios;
  - ao menos um papel;
  - política de senha;
  - login único.

#### C) Atribuir Papel

- **Objetivo**: Ajustar RBAC sem editar cadastro completo.
- **Estrutura**:
  - Seleção de usuário.
  - Lista de papéis atuais.
  - Seleção de papel alvo.
  - Ações: `Adicionar papel`, `Remover papel`.
- **Validações**:
  - usuário obrigatório;
  - papel obrigatório;
  - erro amigável para combinação inválida.

#### D) Resetar Credencial

- **Objetivo**: Recuperar acesso com segurança e troca obrigatória.
- **Estrutura**:
  - Seleção de usuário.
  - Nova credencial + confirmação.
  - Aviso de impacto: credencial anterior inválida e troca obrigatória no próximo login.
  - Ação: `Resetar credencial`.
- **Validações**:
  - confirmação de credencial;
  - aderência à política.

#### E) Gerenciar Usuários

- **Objetivo**: Operar manutenção de ciclo de vida do usuário.
- **Estrutura**:
  - Campo filtro (login/nome).
  - `TableView`: id, login, nome, status, troca obrigatória, papéis.
  - Painel de edição: nome, login, ativo.
  - Ações: `Salvar edição`, `Bloquear`, `Reativar`, `Excluir`.
- **Validações e segurança**:
  - confirmação para bloquear, reativar e excluir;
  - impedir exclusão do próprio admin;
  - erro com código padronizado.

### Critérios de aceite UX (admin v2)

1. Admin conclui cada UC em tela dedicada, sem ambiguidade de ação principal.
2. A lista de usuários é legível e filtrável sem depender de texto livre em bloco único.
3. Todas as ações críticas têm confirmação explícita.
4. Feedback visual segue padrão (sucesso verde temporário, erro vermelho persistente).
5. Fluxo atende UC-ADM-01, UC-ADM-02 e UC-ADM-04 com baixa carga cognitiva.
