# Plano Go-to-Market tecnico (100% gratuito) — Sistema de Controle de EPI

## Objetivo

Validar demanda real e linguagem comercial antes de investir pesado em produto, marketing pago e estrutura comercial.

## Resultado esperado em 30 dias

- Entender como potenciais clientes pesquisam o problema.
- Descobrir quais mensagens convertem melhor.
- Coletar contatos qualificados para demo/piloto.
- Criar baseline de metricas para decidir proximos investimentos.

## Escopo do plano

- Landing page simples com proposta de valor.
- Instrumentacao de analytics e SEO.
- Captura de leads por formulario/WhatsApp.
- Testes de copy e CTA.
- Leitura semanal de indicadores.

## Stack recomendada (gratuita)

1. **Site/Landing**
   - GitHub Pages (gratis) ou Cloudflare Pages (gratis).
2. **Analytics**
   - Google Analytics 4 (GA4).
3. **Origem de busca**
   - Google Search Console.
4. **Comportamento na pagina**
   - Microsoft Clarity.
5. **Pesquisa de termos**
   - Google Trends.
   - Google Keyword Planner (conta Google Ads, sem necessidade de campanha ativa).
6. **Formulario**
   - Formspree (plano gratis) ou Google Forms.
7. **Contato rapido**
   - Botao WhatsApp com link direto.

## Estrutura minima da landing

## Pagina unica (V1)

- **Hero**: "Controle de EPI com evidencias da NR-6 em minutos".
- **Dores**: perda de controle de entrega, dificuldade de auditoria, budget sem rastreabilidade.
- **Beneficios**: rastreio por trabalhador/lote/CA, relatorios prontos, trilha auditavel.
- **Prova de aderencia**: mencao a NR-6 e foco operacional (SESMT + almoxarifado).
- **CTA principal**: "Quero ver uma demo".
- **CTA secundario**: "Receber checklist de implantacao".
- **FAQ**: piloto em unidade unica, evolucao para multiunidade, requisitos minimos.

## Paginas opcionais (V2)

- `/solucao/controle-entrega-epi`
- `/solucao/relatorio-nr6`
- `/materiais/checklist-implantacao-epi`

## Instrumentacao (eventos minimos)

No GA4/Clarity, rastrear:

- `view_page` (pagina visitada)
- `click_whatsapp`
- `click_agendar_demo`
- `submit_form_lead`
- `download_material`

Campos minimos do formulario:

- Nome
- Empresa
- Cidade/UF
- Email/WhatsApp
- Quantidade de unidades
- Principal dor hoje (texto curto)

## Setup em 7 passos

1. Publicar landing no GitHub Pages/Cloudflare Pages.
2. Configurar GA4 e instalar tag no site.
3. Configurar Search Console e enviar sitemap.
4. Configurar Microsoft Clarity.
5. Publicar formulario de lead e validar envio.
6. Criar 2 versoes de headline/CTA para teste A/B manual.
7. Criar planilha de metricas semanais.

## Pesquisa de termos (metodo pratico)

## Lista inicial de termos

- controle de epi
- ficha de epi digital
- sistema de epi
- gestao de epi nr 6
- entrega de epi por colaborador
- relatorio de epi
- ca epi controle
- software sst epi

## Como usar

1. Avaliar no Google Trends variacao por regiao e sazonalidade.
2. Validar no Keyword Planner termos correlatos e volume relativo.
3. Priorizar termos com intencao de compra/solucao.
4. Refletir os termos priorizados em titulos, subtitulos e FAQ da landing.

## KPI de validacao (semana a semana)

- Visitantes unicos.
- CTR de busca organica (Search Console).
- Taxa de clique em CTA principal.
- Taxa de envio de formulario.
- Leads qualificados (empresa real + dor clara).
- Custo por lead (neste plano deve ser ~zero, apenas tempo).

## Metas iniciais realistas (30 dias)

- 150 a 500 visitantes (organico + rede de contatos).
- 3% a 8% de clique no CTA principal.
- 1% a 4% de conversao em lead.
- 5 a 15 conversas qualificadas.

## Cadencia operacional

## Toda semana

- Revisar consultas no Search Console.
- Revisar mapas de calor/sessoes no Clarity.
- Ajustar headline, CTA e ordem das secoes.
- Registrar hipoteses e resultado na planilha.

## A cada 15 dias

- Revisar lista de termos alvo.
- Atualizar FAQ com duvidas reais recebidas.
- Refinar formulario para melhorar qualificacao.

## Riscos e mitigacoes

- **Baixo trafego inicial**: ativar distribuicao em rede pessoal, grupos tecnicos e LinkedIn.
- **Muitos curiosos, poucos leads**: reforcar copy para dor real e CTA de problema concreto.
- **Leads sem fit**: adicionar pergunta de qualificacao (segmento, porte, unidades).
- **Metrica sem consistencia**: padronizar nomenclatura de eventos e validar tags mensalmente.

## Checklist de conformidade (LGPD basico)

- Publicar politica de privacidade.
- Informar finalidade dos dados no formulario.
- Coletar apenas dados necessarios.
- Disponibilizar canal de remocao/atualizacao de dados.

## Criterio de decisao apos 30 dias

Avancar para estrategia comercial estruturada se:

- houver recorrencia de dor real em leads;
- taxa de conversao da landing for consistente;
- existir interesse em piloto/demo em conversas reais.

Se nao houver tracao:

- reposicionar mensagem;
- ajustar segmento alvo;
- repetir ciclo por mais 30 dias antes de investir em paid media.

