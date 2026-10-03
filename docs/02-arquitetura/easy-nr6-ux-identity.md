# Easy NR6 — Identidade de experiência (UX)

Documento de referência para mocks, AtlantaFX e implementação JavaFX. Complementa `ui-ux-blueprint.md` (mapa de telas e componentes) com **marca de produto**, **padrões distintivos** e **tokens** — sem copiar layout de concorrentes.

**Status:** rascunho v0.1 (2026-10-01)
**Produto:** Easy NR6 Gestão de EPI
**Stack UI alvo:** JavaFX 25 + AtlantaFX + CSS externo (sem `setStyle` inline em telas de negócio)

---

## Resumo executivo (1 página)

### O que somos na interface

Um sistema **de prova operacional** para NR-6: claro no balcão, austero na auditoria, honesto sobre o que é **fato legal** versus **intenção** ou **cadastro mestre**.

### Promessa perceptível

| Usuário sente | Interface mostra |
|---------------|------------------|
| Confiança | Eventos legais com identificador, operador, data/hora e estado imutável |
| Velocidade | Uma ação primária por contexto; busca por matrícula; atalhos de teclado no balcão |
| Segurança | Bloqueios explicados com norma ou regra de negócio, não “erro genérico” |
| Diferença | **Pedido ≠ entrega**, **CA na compra ≠ validade da peça**, **matriz vs exceção** sempre visíveis |

### Três padrões assinatura Easy NR6

1. **Faixa de registro legal** — Em telas que gravam evento jurídico (entrega, devolução, estorno), barra fixa antes do commit: resumo + aviso de imutabilidade + operador logado.
2. **Duas colunas: fato × contexto** — Coluna principal = ação; coluna secundária = histórico efetivo, pedidos abertos, cobertura ou timeline (nunca misturar pedido com entrega na mesma tabela).
3. **Status operacional persistente** — Barra global: unidade (quando aplicável), estado CAEPI (`UC-CAE-01`), conectividade (modo oficial), sem substituir alertas que exigem ação.

### Tom de voz (UX writing)

- **Verbos de ação:** “Registrar fornecimento”, “Confirmar ciência”, “Registrar estorno” — evitar “Salvar” em fluxos legais.
- **Erros em 3 partes:** o que falhou; por quê (regra ou NR-6); o que fazer agora.
- **Sem culpar o usuário:** “Não foi possível…” + causa + próximo passo.
- **Português BR claro:** evitar siglas soltas sem expansão na primeira menção por sessão.

### O que não somos

- Dashboard decorativo com gráficos sem ação.
- “ERP genérico” cinza com dezenas de módulos equivalentes.
- App que esconde bloqueio jurídico atrás de modal genérico.
- Cópia visual de FichaEPI / WOTY / etc. (benchmark = **tarefa e tempo**, não screenshot).

---

## Fundação visual

### Personalidade da marca (B2B SST)

- **Confiável** — tipografia legível, alinhamento rigoroso, poucos adornos.
- **Técnico** — dados CA, lote, matrícula sempre escaneáveis (tabular nums onde couber).
- **Humano no balcão** — passos guiados, linguagem do almoxarife, não do desenvolvedor.
- **Auditável** — histórico e trilha a um clique; estados “congelados” visualmente distintos de editáveis.

### Direção AtlantaFX

1. Escolher **tema base** (recomendado para v1: variante **Primer Light** como default operacional; oferecer **Primer Dark** ou **Nord Dark** como preferência do usuário em planta).
2. Aplicar tema **uma vez** na subida da aplicação (`UserAgentBuilder` / API AtlantaFX).
3. Sobrescrever apenas via **`design-tokens.css`** (ver `design-tokens.css` nesta pasta) — cores semânticas Easy NR6, não redefinir controle por controle.
4. Componentes reutilizáveis JavaFX (pacote `ui.components` quando existir): `PageHeader`, `LegalCommitBar`, `StatusBanner`, `BadgeCell`, `StepIndicator`, `EmptyStatePane`.

### Paleta semântica (tokens)

Não confundir “cor bonita” com “cor com significado”. Cada token abaixo tem **um** significado no produto.

| Token | Uso | Nota |
|-------|-----|------|
| `--enr6-brand-primary` | Ação primária, foco, links | Identidade Easy NR6 (ver CSS) |
| `--enr6-surface-default` | Fundo de página | |
| `--enr6-surface-raised` | Cards, painéis | |
| `--enr6-border-subtle` | Divisores, tabelas | |
| `--enr6-text-primary` / `--muted` | Corpo e rótulos | Contraste WCAG AA mínimo |
| `--enr6-status-ok` | Vigente, ativo, cobertura OK | Sempre com ícone + texto |
| `--enr6-status-warn` | Matriz, CAEPI degradado, exceção pendente | Âmbar reservado a **atenção operacional** |
| `--enr6-status-danger` | Bloqueio legal, lote vencido, saldo zero | Vermelho = **não pode commitar** |
| `--enr6-status-info` | Informativo, pedido aberto | Azul suave; não é “sucesso” |
| `--enr6-record-frozen` | Registro imutável / somente leitura legal | Fundo levemente distinto + ícone cadeado |

Cor de marca proposta v0.1: **slate profundo + acento teal** (evitar o azul “GitHub default” dos protótipos HTML — teal comunica segurança/técnico sem parecer gov.br genérico). Ajustável após teste com usuário.

### Tipografia e densidade

- **Título de página:** 18–20px semibold + subtítulo **orientado à tarefa** (uma linha).
- **Corpo / tabelas:** 13–14px; balcão pode usar preset “Conforto” (+1px) nas preferências.
- **Espaçamento:** escala 4px — 8, 12, 16, 24, 32 (padding de painel padrão: 16).
- **Raio:** 6px cards; 4px inputs (consistente com protótipo SOL, refinado para tema).

### Iconografia

- Preferir **SVG ou icon font** consistente (ex.: Ikonli + conjunto único).
- Ícone nunca sozinho em ação destrutiva ou legal — sempre rótulo visível.

---

## Arquitetura de informação por papel

Cada papel tem **início** (home), não apenas lista de módulos.

| Papel | Home (primeira impressão) | Ação primária do dia |
|-------|---------------------------|----------------------|
| Almoxarife | Fila: entregas pendentes / retiradas | Registrar fornecimento (wizard) |
| SESMT | Exceções matriz + cobertura crítica | Analisar exceção / matriz |
| Gestor | Solicitações + status dos subordinados | Nova solicitação (`UC-SOL-01`) |
| Consulta | Relatórios essenciais + filtros salvos | Extrair ficha por período |
| Admin | Saúde sistema + usuários | Governança (secundário ao operacional) |

**Navegação:** sidebar agrupa por **fluxo**, não por tabela de banco:

1. Início (role-aware)
2. Operação (entrega, devolução, estorno)
3. Demanda (solicitações — modo oficial)
4. Cadastros & regras (SESMT/Admin)
5. Estoque & lotes
6. Relatórios
7. Auditoria
8. Administração

Módulos sem implementação: **não** botão morto — item com badge “Em breve” ou oculto por feature flag.

---

## Linguagem visual de domínio (diferenciação real)

### Dois relógios (sempre que CA/lote aparecer)

- Rótulo **“CA consultado na compra”** (data + número).
- Rótulo **“Validade da peça”** (lote).
- Nunca uma única coluna “validade” ambígua.

### Pedido vs entrega

- **Pedido:** badge “Demanda”, ícone envelope/relógio, sem CA de consumo.
- **Entrega:** badge “Fornecimento”, referência a lote baixado e ciência.
- Copy fixa em formulários de solicitação: *“Solicitação registrada; isto não é entrega nem reserva de estoque.”*

### Matriz vs exceção

- Dentro da matriz: fluxo verde/neutro.
- Fora da matriz: banner âmbar + caminho SESMT explícito; nunca mostrar “aprovado” antes do evento no backend.

### Imutabilidade

- Registros legais: sem botão Editar; ações **Estornar** / **Devolver** como eventos novos.
- Visual `record-frozen` + tooltip explicando estorno.

---

## Padrões de interação

### Wizard (entrega e fluxos críticos)

- 5 passos máximo; indicador de progresso horizontal com títulos verbos.
- **Voltar** preserva dados; **Avançar** valida só o passo atual.
- Último passo = revisão + faixa de registro legal + um botão primário.
- Atalhos: `Enter` avança quando válido; `Esc` cancela com confirmação se houver dados.

### Tabelas operacionais

- Filtro rápido sempre visível (matrícula, período, status).
- Linha clicável = detalhe lateral ou drawer (timeline preferível a modal profundo).
- Status em **BadgeCell** (texto + cor semântica).

### Feedback

- **Toast** para sucesso operacional não legal (ex.: filtro aplicado).
- **Banner persistente** para CAEPI atrasado, offline, unidade trocada.
- **Modal** só para confirmação irreversível ou estorno.

### Background work

- Barra de status inferior: tarefa, fase, resultado (`UC-CAE-01` import).
- UI nunca congela; indicador indeterminado quando progresso desconhecido.

---

## Acessibilidade e balcão

- Contraste AA; foco visível em todos os controles interativos.
- Alvos clicáveis ≥ 40px na altura em fluxos de operação.
- Teclado: login, wizard entrega, busca trabalhador — ordem de tab lógica.
- Cor + forma + texto para todo estado crítico (daltonismo).

---

## Processo de design (mocks → produção)

### Fase 1 — Protótipo de experiência (HTML ou Figma)

| Artefato | Fluxo | Estados obrigatórios |
|----------|--------|----------------------|
| Catálogo [`docs/mock/index.html`](../mock/index.html) | Todas as telas MVP (22 páginas) | Ver README em `docs/mock/` |
| [`docs/mock/pages/02-entrega-wizard.html`](../mock/pages/02-entrega-wizard.html) | Wizard entrega (`UC-ENT`) | OK; lote vencido; fora da matriz |
| [`docs/mock/pages/16-solicitar-epi-gestor.html`](../mock/pages/16-solicitar-epi-gestor.html) | Gestor solicita (`UC-SOL-01`) | OK; fora matriz; duplicidade |

Regras do mock:

- Usar tokens e nomes de classes alinhados a `design-tokens.css`.
- Cada tela inclui copy real NR-6 / spec, não lorem ipsum.
- Interações JS mínimas só para alternar estados (não simular backend).

### Fase 2 — Tela ouro JavaFX

Implementar **uma** tela completa com AtlantaFX antes de espalhar tema:

- **Opção A (recomendada):** passo “Revisão + commit” do wizard de entrega + componentes base.
- **Opção B:** Gestor solicitação (se prioridade comercial for fila central).

Critério de pronto da tela ouro: zero `setStyle` inline; CSS + componentes reutilizáveis; RBAC reflete botões desabilitados.

### Fase 3 — Validação em campo

- 3–5 usuários por papel; tarefa cronometrada (ex.: registrar entrega fictícia).
- Checklist: `docs/03-operacao/checklist-usabilidade-m1-cadastros-auditoria.md` (expandir para operação quando M2 iniciar).

---

## Princípios modernos adotados (checklist)

Referência cruzada com `ui-ux-blueprint.md`; aqui o compromisso de produto:

- [ ] **Jobs-to-be-done** — home por papel, não por módulo técnico.
- [ ] **Progressive disclosure** — campos avançados recolhidos.
- [ ] **Calm technology** — uma ação primária por painel.
- [ ] **Content design** — erros orientam correção e norma.
- [ ] **Design tokens** — CSS central, temas AtlantaFX.
- [ ] **Estados completos** — vazio, loading, degradado, read-only, frozen.
- [ ] **Inclusive / WCAG 2.2** — contraste, teclado, não só cor.
- [ ] **Situational awareness** — barra global de status.
- [ ] **Ethical legal UX** — confirmar consequência antes de commit imutável.
- [ ] **Discovery contínua** — mock → campo → ajuste token/copy, não big bang.

---

## Governança

- Mudança de token ou padrão assinatura: atualizar este arquivo + `design-tokens.css` + snippet em `ui-ux-blueprint.md`.
- Novo fluxo legal: obrigatório **faixa de registro legal** + spec UC + cenários de teste UI.
- Benchmark concorrente: documentar **tarefa medida**, não screenshot arquivado como spec visual.

---

## Referências internas

- Mapa de telas e matriz de componentes: `ui-ux-blueprint.md`
- Solicitação gestor (comportamento mock): `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md` §6.2
- Visão produto e NR-6: `docs/00-visao/visao-produto-e-escopo.md`
- Tokens CSS: `design-tokens.css`
