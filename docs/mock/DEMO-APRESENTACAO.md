# Roteiro de demo — Easy NR6 (protótipo HTML)

Público: empresas interessadas, SESMT, almoxarifado, TI. Duração sugerida: **15–25 min**.

## Antes da reunião

1. Abrir **`shell.html`** no **Chrome ou Edge** (IndexedDB + `file://` ou servidor estático local).
2. Ter o CSV **`RelatorioCA_*.csv`** (export portal CAEPI) ou usar `data/caepi-sample.txt` se a rede/arquivo não estiver disponível.
3. Opcional: limpar dados — rodapé **Reset cadastros mock** (seed + IndexedDB completo).

## Fluxo sugerido

| # | Onde | O que mostrar | Mensagem-chave |
|---|------|----------------|----------------|
| 1 | Login / shell | Perfis (Admin, Almox, Gestor) | Mesma UX futura desktop; RBAC já simulado |
| 2 | Barra superior | Estado CAEPI | Base oficial separada do catálogo da empresa |
| 3 | **Importação CAEPI** (21) | Upload CSV completo | Parse + store `ca` (~40k+ CAs); **F5** mantém carga e cadastros |
| 4 | Mesma tela | Consulta rápida CA **365** | Dados oficiais sem digitar portal |
| 5 | **Catálogo EPI** (08) | Poucos itens locais | Cardápio da fábrica ≠ biblioteca nacional |
| 6 | **CA por EPI** (09) | Preencher da última carga | Vínculo + evidência; elimina redigir situação/validade |
| 7 | Matriz + Lotes + Entrega | Fluxo operacional | Regras NR-6 com dados já seeded |
| 8 | Simular falha CAEPI (21) | Banner degradado | App continua; mutação EPI/CA bloqueada até nova carga |
| 9 | Auditoria (18) | Evento `CAEPI_IMPORT_OK` | Rastreabilidade como no produto |

## Perguntas frequentes (respostas curtas)

- **Onde ficam os dados?** No protótipo, **IndexedDB no navegador** (cadastros + CAEPI). No produto Java: **banco local** da aplicação.
- **Preciso cadastrar todos os EPIs do Brasil?** Não — só o que a unidade **usa e entrega**.
- **Importação substitui CA por EPI?** Não — define **quais CAs existem**; a empresa ainda **amarra** ao seu EPI.
- **E offline?** Após importar, consulta CA funciona **sem internet** até a próxima atualização da base.

## Limitações honestas do mock

- Não é o binário JavaFX; é **validação de fluxo e UX**.
- Download automático FTP **simulado**; carga real via **upload manual**.
- Assinatura/biometria e impressos: placeholders onde aplicável.

## Arquivo para enviar ao cliente

Empacotar a pasta `docs/mock/` (ou zip existente) e indicar: abrir **`shell.html`**.
