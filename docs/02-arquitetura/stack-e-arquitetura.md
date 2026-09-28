# Stack e Arquitetura — Controle de EPI com foco em Budget

## Contexto de negocio

A empresa possui 8 unidades e precisa controlar o budget da area de Seguranca do Trabalho. Na pratica, o budget de EPI depende de rastreabilidade operacional:

- O que foi entregue, para quem, quando e por qual motivo.
- O que foi devolvido/descartado e quando.
- Quais lotes e CAs foram consumidos.
- Qual o consumo por funcao, setor, unidade e periodo.

Sem esse lastro operacional, o budget vira estimativa fraca e dificil de defender em auditoria, fiscalizacao ou discussao gerencial.

## Tese central do sistema

O **controle de entrega** e o nucleo do produto, mas ele so funciona com atividades predecessoras e sucessoras:

- **Antes da entrega**: cadastro de EPI/CA, recebimento por lote, saldo, matriz por funcao/GHE e parametros de periodicidade.
- **Durante a entrega**: registro imutavel, validacao do trabalhador e vinculo com lote/CA.
- **Depois da entrega**: devolucao/descarte, estorno formal, trilha de auditoria e relatorios extraiveis.

Em outras palavras: a entrega e o evento juridico principal, mas depende de uma cadeia de dados para ser confiavel.

## Stack proposta e justificativa

## 1) Java 25

**Por que faz sentido:**
- Plataforma estavel para software corporativo de longa vida.
- Bom suporte a regras de negocio, validacoes, transacoes e testes.
- Ecossistema maduro para seguranca, relatorios e persistencia.

**Risco:**
- Curva maior que stacks de script para equipe sem experiencia Java.

**Mitigacao:**
- Arquitetura simples (camadas claras, sem overengineering).
- Convencoes de projeto e testes automatizados desde o inicio.

## 2) JavaFX (desktop)

**Por que faz sentido:**
- Adequado para operacao local em ambiente industrial.
- Evita dependencia inicial de navegador/infra web.
- Boa experiencia para fluxo transacional de balcao (entrega/devolucao).

**Risco:**
- Distribuicao e atualizacao em multiplas unidades exigem processo.

**Mitigacao:**
- Empacotar com `jpackage`.
- Separar instalacao de aplicativo e dados do banco.
- Definir rotina de upgrade por unidade.

## 3) Spring (como container DI e transacao)

**Por que faz sentido:**
- Organiza composicao de servicos (CDI/IoC) sem acoplamento manual.
- Facilita controle transacional da operacao critica (entrega + baixa de lote + auditoria no mesmo commit).
- Ajuda a evoluir para mais modulos sem reescrever base.

**Risco:**
- Adotar Spring completo para app desktop pode parecer pesado.

**Mitigacao:**
- Usar Spring Boot sem camada web.
- Limitar dependencias ao necessario (contexto, transacao, seguranca de senha, etc.).

## 4) SQLite (relacional/transacional local)

**Por que faz sentido na fase inicial:**
- Simples de operar (arquivo unico), sem servidor dedicado.
- ACID suficiente para POC e operacao local por unidade.
- Bom encaixe com controle juridico imutavel e trilha auditavel.

**Risco principal:**
- Escalabilidade de concorrencia e governanca multiunidade centralizada.

**Mitigacao:**
- Assumir claramente o limite: 1 instancia de escrita por base.
- Se cada unidade tiver seu proprio banco local, consolidar via exportacao/sincronizacao.
- Planejar migracao para Postgres quando houver necessidade de operacao central em tempo real.

## 5) JasperReports (relatorios operacionais e legais)

**Por que faz sentido:**
- Excelente para relatorios tabulares recorrentes (ficha de entrega, historico por periodo, cobertura por trabalhador).
- Permite padronizar layout para auditoria/fiscalizacao.
- Integracao consolidada no ecossistema Java para exportacao em PDF.

**Risco:**
- Templates (`.jrxml`) exigem governanca de versao e padrao visual.

**Mitigacao:**
- Criar biblioteca de templates base (cabecalho da empresa, rodape legal, assinaturas).
- Versionar templates junto com o codigo e cobrir com testes de regressao de relatorio.

## 6) PDFBox (documentos/evidencias)

**Por que faz sentido:**
- Gera e manipula PDF para ficha de entrega, comprovantes e dossie de auditoria.
- Boa interoperabilidade para arquivamento e compartilhamento.

**Ponto de atencao:**
- PDFBox nao substitui bem um motor de layout tabular recorrente.

**Diretriz pratica:**
- Usar JasperReports para gerar os relatorios padronizados.
- Manter PDFBox para pos-processamento (carimbo, merge, anexos, assinatura tecnica futura).

## O que precisa existir alem da entrega para o budget funcionar

Para converter operacao em numero financeiro confiavel, o sistema deve registrar tambem:

1. **Recebimento por lote**: quantidade, validade da peca, fabricante, CA, custo unitario.
2. **Matriz funcao/GHE x EPI**: quais itens sao obrigatorios por perfil.
3. **Motivo da movimentacao**: primeira entrega, desgaste, extravio, mudanca de funcao.
4. **Ciclo de reposicao**: periodicidade esperada versus consumo real.
5. **Eventos de perda**: dano precoce, extravio, descarte antecipado.
6. **Consolidacao por unidade**: consumo e custo por unidade/setor/funcao/periodo.

Sem esses pontos, existe registro legal de entrega, mas nao inteligencia para budget.

## Proposta de fronteira da V1

Entregar com qualidade o nucleo legal + operacional:

- Cadastro de EPI, CA e lote.
- Matriz por funcao.
- Entrega individual com validacao do trabalhador.
- Devolucao/descarte e estorno formal.
- Relatorio extraivel por trabalhador e periodo.
- Indicadores minimos de consumo por unidade e funcao.

Tudo que for previsao avancada pode ficar para V2.

## Crescimento esperado (e como evitar reescrita)

Mesmo "simples", tende a crescer rapido por pressao juridica e financeira. A arquitetura deve prever:

- Multiunidade (filial/unidade como chave de particionamento funcional).
- Controle de autorizacao por papel e por escopo de unidade.
- Trilha de auditoria append-only.
- Modelo imutavel para eventos juridicos (entrega/devolucao/estorno).
- Separacao entre dominio operacional e camada de apresentacao.

Com essas bases, da para migrar o armazenamento ou expandir modulos sem quebrar historico.

## Decisao arquitetural sugerida (resumo)

1. Manter stack proposta para iniciar rapido com seguranca juridica.
2. Tratar entrega como evento imutavel e audivel.
3. Incluir desde o inicio os dados minimos de custo/lote para suportar budget.
4. Definir gatilho objetivo para migracao de SQLite para banco cliente-servidor.

## Gatilhos de migracao (SQLite -> Postgres, por exemplo)

Considerar migracao quando houver um ou mais cenarios:

- Necessidade de operacao simultanea intensa entre usuarios/unidades.
- Consolidacao central em tempo real (sem lote/exportacao).
- Integracao nativa com outros sistemas corporativos em alta frequencia.
- Requisitos de governanca, backup central e observabilidade mais rigidos.

## Conclusao

Seu raciocinio esta correto: o core e a entrega, mas ela nao se sustenta isolada.  
A stack proposta e coerente para iniciar com velocidade e controle de risco, desde que o desenho ja prepare:

- rastreabilidade juridica (NR-6),
- base analitica minima para budget,
- e caminho de evolucao sem reescrever o sistema.

