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
2. **Navegação principal**: menu lateral por módulos.
3. **Escopo da primeira rodada de telas**: quase completo.
4. **Tom visual**: corporativo sóbrio (azul/cinza, foco em clareza).

## Princípios de design (regras do produto)

1. **Clareza operacional**: cada tela responde "o que fazer agora?".
2. **Segurança de registro**: dados jurídicos sensíveis não são ambíguos.
3. **Velocidade no balcão**: fluxo de entrega/devolução com mínimo de atrito.
4. **Consistência**: mesmo padrão de botões, filtros, tabelas e mensagens.
5. **Prevenção de erro**: bloquear ações inválidas antes da confirmação.
6. **Rastreabilidade visível**: histórico e status sempre acessíveis.

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

