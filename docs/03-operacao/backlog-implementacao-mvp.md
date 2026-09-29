# Backlog de implementacao do MVP (retomada refinada)

## Objetivo

Transformar as definicoes ja consolidadas em um plano executavel de implementacao do MVP, alinhando:

- regras de negocio e conformidade;
- execucao de telas e experiencia operacional;
- narrativa comercial da landing para captacao de pilotos.

## Premissas de execucao (fechadas)

1. Nenhum caso de uso entra em desenvolvimento sem especificacao e cenarios de teste definidos.
2. Toda entrega deve ser classificavel como:
   - `UC` (relacionada a caso de uso);
   - `FEAT` (feature de produto);
   - `TECH` (infra/arquitetura/governanca tecnica).
3. Toda entrega precisa ser testavel e verificavel por criterio objetivo de aceite.
4. Toda mudanca relevante de escopo ou decisao deve atualizar a documentacao-fonte para evitar obsolescencia.

## Entradas de referencia

- `docs/00-visao/resumo-executivo-definicoes.md`
- `docs/02-arquitetura/ui-ux-blueprint.md`
- `docs/03-operacao/comparativo-posicionamento-landing.md`
- `docs/04-uml/casos-de-uso-uml.md`

## Definicoes de identidade (fechadas)

- Marca: `Easy NR6`.
- Produto: `Easy NR6 Gestao de EPI`.
- Dominio principal: `easynr6.com.br`.

## Escopo de retomada (primeiro ciclo)

1. Entregar fluxo operacional fim a fim:
   - entrega;
   - devolucao/descarte;
   - estorno;
   - consulta de historico.
2. Garantir bloqueios juridicos e trilha auditavel.
3. Fechar base de cadastros e regras para uso autonomo do key user.
4. Publicar landing com posicionamento hibrido (compliance + operacao/budget).

## Status consolidado da retomada (checkpoint tecnico)

- `R0`: concluido (workflow release, hooks de qualidade, templates de governanca).
- `M0`: concluido (shell desktop, autenticacao com resultado tipado, RBAC, auditoria append-only, politica de credenciais em runtime).
- `M0` hardening arquitetural: concluido (identidade desacoplada por `ports + use cases + adapters`, com fachadas compativeis para UI).
- `M1`: em andamento (primeiro cadastro essencial entregue: empregado + funcao/setor minimo para vinculo).
- `M2`: permanece bloqueado por DoR ate fechamento real de M1.

## Priorizacao por modulo (ordem de implementacao)

## R0 - Setup de repositorio e governanca de entrega (Must)

- **[TECH]** Configurar workflow de release por tag (`v*`) no GitHub Actions.
- **[TECH]** Configurar `pre-commit` (stages `pre-commit`, `commit-msg`, `pre-push`).
- **[TECH]** Definir convencao de branch e versionamento semantico.
- **[TECH]** Criar templates:
  - issue/task;
  - feature spec;
  - checklist de validacao.
- **[TECH]** Mapear configuracao de referencia do repositorio `pdf-toolkit` e adaptar ao contexto deste projeto.

**Criterio de pronto**
- fluxo de release automatizado operacional;
- hooks executando verificacoes basicas;
- padrao de contribuicao documentado.

## M0 - Fundacao tecnica e seguranca (Must)

- **[FEAT]** Estrutura base da app desktop (login + shell com menu lateral).
- **[TECH]** Controle de acesso por perfil (`Admin`, `SESMT`, `Almoxarife`, `Consulta`).
- **[UC]** Trilha de auditoria append-only para eventos criticos.
- **[FEAT]** Padrao de mensagens de erro e confirmacao em fluxos sensiveis.
- **[FEAT]** Politica de credenciais (senha forte, troca obrigatoria inicial, reset administrativo, bloqueio por tentativas invalidas).

**Criterio de pronto**
- usuario autenticado navega entre modulos;
- eventos criticos geram rastreio consistente;
- permissao bloqueia acoes fora do perfil.
- ciclo de credencial aplica politica de senha e rastreabilidade.

## M1 - Prontidao operacional para entrega real (Must)

- **[UC]** `Trabalhadores` (minimo operacional: matricula, nome, funcao/setor, status).
- **[UC]** `Funcoes/Setores/GHE` (minimo para vinculo com matriz).
- **[UC]** `EPI e CA` (minimo para itens entregaveis).
- **[UC]** `Lotes` (entrada, validade da peca e saldo).
- **[UC]** `Matriz Funcao/GHE x EPI` (minimo para orientar/bloquear entrega).
- **[UC]** `Periodicidade` (parametro minimo para cobertura/reposicao).
- **[TECH]** Dominios minimos de validacao (motivos, metodo de validacao, status).

**Criterio de pronto**
- existe base minima consistente para registrar entrega real;
- validacoes de consistencia impedem cadastro ambiguo;
- operacao consegue preparar dados sem apoio tecnico.

## M2 - Core operacional governado (Must)

- **[UC]** `Registrar entrega de EPI` com commit transacional:
  `entrega + baixa de lote + auditoria`.
- **[UC]** `Registrar aceite do termo de responsabilidade`.
- **[UC]** Tela wizard de `Entrega de EPI` (5 passos) integrada ao caso de uso real.
- **[UC]** Tela `Historico por trabalhador`.
- **[UC]** Tela `Devolucao/Descarte`.
- **[UC]** Tela `Estorno`.

**Justificativa do wizard (5 passos)**
- reduz erro operacional em fluxo juridicamente sensivel;
- torna obrigatorias as validacoes em sequencia antes do commit;
- melhora velocidade de balcao por orientar o operador passo a passo;
- facilita auditoria por separar claramente intencao, validacao e confirmacao.

**Regras obrigatorias**
- impedir lote vencido;
- impedir saldo insuficiente;
- impedir entrega sem validacao;
- impedir entrega fora da matriz sem justificativa/autorizacao;
- impedir devolucao com data anterior a entrega;
- impedir alteracao/apagamento de entrega legal (somente estorno formal).

**Criterio de pronto**
- cenario ponta a ponta executavel e persistido:
  `entrega -> devolucao/estorno -> consulta`;
- entrega registrada com rastreabilidade juridica e trilha auditavel.

## M3 - Relatorios e evidencia (Should)

- **[FEAT]** Hub de relatorios com filtros em camadas:
  - camada 1 (essenciais): unidade e periodo;
  - camada 2 (operacionais): trabalhador, setor, funcao, GHE, EPI, CA, lote;
  - camada 3 (analiticos): status de vigencia, pendencia, excecao de matriz, estornado.
- **[UC]** Relatorios:
  - ficha por trabalhador;
  - historico por EPI/CA/lote;
  - cobertura (matriz x vigente);
  - pendencias de devolucao.
- **[FEAT]** Exportacao PDF padronizada.

**Diretriz de usabilidade**
- filtros essenciais devem abrir por padrao para simplicidade;
- filtros avancados devem estar a 1 clique para ganho operacional real.

**Criterio de pronto**
- usuario gera evidencia juridica e gerencial sem manipulacao manual de dados.

## M4 - Cadastros completos e governanca de dados (Should)

- **[FEAT]** Expandir telas de cadastro para modo completo (filtros, historico, inativacao com impacto, consistencias avancadas).
- **[TECH]** Revisar e endurecer validacoes cruzadas entre cadastro, matriz e operacao.
- **[FEAT]** Melhorias de usabilidade para autonomia de key user em carga/manutencao de base.

**Criterio de pronto**
- qualidade de dados sustentada em uso continuo;
- baixa incidencia de excecoes por falha de cadastro.

## M5 - Landing e validacao comercial (Should)

- **[FEAT]** Ajustar copy principal para estrategia hibrida:
  abertura em compliance, meio com ganho operacional/budget, fechamento consultivo.
- **[FEAT]** Revisar landing com a nova identidade (`Easy NR6`) e validar coerencia do posicionamento comercial.
- **[TECH]** Instrumentar eventos:
  - `view_page`;
  - `click_agendar_demo`;
  - `click_whatsapp`;
  - `submit_form_lead`.
- **[FEAT]** Rodar teste manual de headline:
  - versao A (compliance-first);
  - versao B (budget-first).

**Criterio de pronto**
- landing publicada com rastreio;
- baseline de metricas pronta para 30 dias.

## Sequenciamento sugerido (ondas)

1. **Onda 1**: M0 (fundacao e seguranca)
2. **Onda 2**: M1 (prontidao operacional pre-core)
3. **Onda 3**: M2 (core operacional governado)
4. **Onda 4**: M3 (relatorios e evidencia)
5. **Onda 5**: M4 (cadastros completos e governanca de dados)
6. **Onda 6**: M5 (landing e validacao de mercado)

## Ordem recomendada (com governanca)

1. `R0` setup de repositorio e padroes (release por tag, pre-commit, templates)
2. `M0` fundacao tecnica e seguranca
3. `M1` prontidao operacional para entrega real (cadastros minimos + lote + matriz)
4. `M2` core operacional governado
5. `M3` relatorios e evidencia
6. `M4` cadastros completos e governanca de dados
7. `M5` landing e validacao de mercado

## Dependencias criticas

- Sem `R0`, nao ha fluxo confiavel de qualidade e release.
- Sem `M0`, nao ha governanca minima para operacao real.
- Sem `M1`, nao ha base de dados minima para entrega real.
- Sem `M2`, nao ha evento juridico de entrega com governanca transacional.
- Sem `M3`, perde-se ganho de auditoria e valor para decisor.
- Sem `M4`, cresce o risco de degradacao da qualidade de dados no uso continuo.
- Sem `M5`, nao ha aprendizado estruturado de tracao comercial.

## Definition of Ready (DoR) para iniciar M2

M2 (core operacional governado) so pode iniciar quando todos os itens abaixo estiverem verdadeiros.

### 1) Dados mestres minimos prontos (M1)

- [ ] Cadastro de trabalhador ativo disponivel (matricula, nome, funcao/setor).
- [ ] Cadastro de EPI com CA valido disponivel para entrega.
- [ ] Lotes cadastrados com validade da peca e saldo disponivel.
- [ ] Matriz funcao/GHE x EPI ativa para os perfis piloto.
- [ ] Parametros minimos de periodicidade configurados.

### 2) Regras e dominios minimos definidos

- [ ] Dominio de motivos de entrega definido.
- [ ] Dominio de metodos de validacao do trabalhador definido.
- [ ] Politica de excecao fora da matriz definida (justificativa + autorizacao).
- [ ] Politica de estorno formal definida (sem update/delete de prova legal).

### 3) Fundacao tecnica e seguranca validada

- [x] RBAC operacional para `Admin`, `SESMT`, `Almoxarife`, `Consulta`.
- [x] Auditoria append-only ativa e validada.
- [x] Politica de credenciais baseline ativa (senha forte + rastreabilidade).
- [x] Migracoes de banco aplicadas com sucesso no ambiente de desenvolvimento.

### 4) Prontidao de implementacao do caso de uso

- [ ] Spec curta de `UC-ENT-01` aprovada.
- [ ] Spec curta de `UC-ENT-02` aprovada.
- [ ] Cenarios de teste definidos antes do codigo (feliz, bloqueio, borda).
- [ ] Criterios de aceite mensuraveis aprovados pelo responsavel funcional.

### 5) Gate transacional e juridico (obrigatorio)

- [ ] Decisao explicita de transacao unica:
      `entrega + baixa de lote + auditoria`.
- [ ] Estrategia de imutabilidade da entrega registrada.
- [ ] Estrategia de vinculacao do termo de responsabilidade registrada.
- [ ] Estrategia de identificador de comprovante/registro definida.

### 6) Evidencia minima para liberar execucao de M2

- [ ] Checklist de validacao de M1 preenchido e aprovado.
- [ ] Evidencia de carga minima de dados (amostra real piloto) registrada.
- [ ] Risco residual aceito formalmente para iniciar M2.

Regra de governanca: se qualquer item acima estiver pendente, M2 permanece bloqueado.

## Definicao de pronto global (MVP)

Considerar o MVP pronto quando todos os itens abaixo forem verdadeiros:

1. key user executa fluxo principal sem apoio tecnico;
2. regras juridicas criticas estao bloqueadas em tela e servico;
3. historico e estorno sao rastreaveis ponta a ponta;
4. relatorios minimos saem em PDF com dados consistentes;
5. landing gera leads com eventos de conversao rastreados.
6. cada entrega possui rastreabilidade para `UC`, `FEAT` ou `TECH`.
7. cada entrega possui evidencias de teste e resultado esperado x observado.

## Primeiro sprint de retomada (execucao imediata)

### Sprint R0 - setup e governanca (5-7 dias)

- configurar release por tag (`v*`) no GitHub Actions;
- configurar `pre-commit` para qualidade e seguranca local;
- criar modelo de issue/spec/checklist de teste;
- alinhar naming de tickets no Jira com taxonomia `UC`/`FEAT`/`TECH`;
- registrar baseline do pipeline local de validacao.

### Sprint R1 - fundacao tecnica (7 dias)

- implementar shell de navegacao + login;
- estruturar controle de perfil;
- implementar eventos basicos de auditoria;
- implementar baseline de credenciais seguras (senha forte + troca obrigatoria inicial);
- publicar esqueleto do wizard de entrega (passos e validacoes de navegacao).

### Sprint R2 - prontidao de dados e regras (7 dias)

- fechar cadastros minimos operacionais (trabalhador, funcao/setor/GHE, EPI/CA);
- fechar lote (entrada, validade, saldo) e matriz/periodicidade minima;
- validar consistencia minima da base para entrega real.

### Sprint R3 - core entrega governado (7 dias)

- implementar caso de uso real de entrega com commit transacional;
- integrar wizard aos dados reais de trabalhador/matriz/lote/CA;
- implementar termo de responsabilidade e historico por trabalhador;
- implementar devolucao/descarte e estorno com rastreabilidade.

### Sprint R4 - evidencia e consolidacao (7 dias)

- liberar primeiro pacote de relatorios;
- endurecer validacoes de cadastro e operacao;
- revisar landing para rodada inicial de captacao;
- registrar metricas e feedbacks do piloto.

## Fluxo spec-driven (obrigatorio por item de backlog)

Para cada item `UC` ou `FEAT`, seguir esta ordem:

1. **Spec funcional curta**
   - problema;
   - usuario/ator;
   - regra de negocio;
   - comportamento esperado;
   - nao-escopo.
2. **Cenarios de teste (antes do codigo)**
   - cenario feliz;
   - cenario de bloqueio/erro;
   - cenario de borda;
   - criterio de aceite mensuravel.
3. **Implementacao**
4. **Evidencia de validacao**
   - resultado do teste manual/automatizado;
   - status: aprovado ou pendente ajuste.
5. **Atualizacao de documentacao-fonte**
   - atualizar doc de negocio, arquitetura ou operacao correspondente.

## Estrutura Jira sugerida (iniciativas -> epicos -> features -> tasks)

### Iniciativas (nivel estrategico)

- `INI-01` Plataforma de operacao e conformidade NR-6.
- `INI-02` Governanca tecnica e qualidade de entrega.
- `INI-03` Validacao comercial e aquisicao de pilotos.

### Epicos (nivel produto)

- `EP-CORE` Entrega/devolucao/estorno/historico.
- `EP-CAD` Cadastros e regras de matriz.
- `EP-REL` Relatorios e evidencias.
- `EP-TECH` Fundacao tecnica, seguranca e automacao de release.
- `EP-GTM` Landing e captacao de leads.

### Features (nivel funcional)

Cada feature deve herdar uma tag:
- `[UC]` quando implementa comportamento de caso de uso;
- `[FEAT]` quando agrega capacidade de produto sem ser um UC completo;
- `[TECH]` quando e infraestrutura/arquitetura/qualidade.

### Tasks (nivel execucao)

Toda task deve conter:
- referencia do pai (feature/epico/iniciativa);
- definicao de pronto;
- cenarios de teste vinculados;
- evidencia de validacao.
- quando envolver autenticacao/credencial, incluir validacoes de seguranca e auditoria.

## Politica para evitar documentacao obsoleta

1. Toda PR/entrega deve responder: "quais docs foram impactados?".
2. Se a resposta for "nenhum", justificar explicitamente no fechamento da task.
3. Marcar revisao quinzenal de consistencia da pasta `docs/`.
4. Sempre manter um unico documento fonte por assunto, atualizando referencias cruzadas.
5. Registrar no Jira uma subtask de documentacao para toda feature de negocio.

## Registro de risco da retomada

- **Risco**: iniciar entrega real sem base de cadastro/lote/matriz pronta.
  **Mitigacao**: bloquear inicio de M2 ate criterio de pronto de M1.
- **Risco**: UI crescer com inconsistencia entre telas.
  **Mitigacao**: aplicar scorecard do `ui-ux-blueprint` por tela.
- **Risco**: landing atrair lead sem fit.
  **Mitigacao**: reforcar qualificacao no formulario e FAQ.
- **Risco**: documentacao descolar da implementacao real.
  **Mitigacao**: tratar atualizacao de docs como criterio obrigatorio de pronto.
