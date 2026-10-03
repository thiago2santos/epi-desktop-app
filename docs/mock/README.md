# Protótipo HTML Easy NR6

Mocks **interativos** por caso de uso, com **estado compartilhado na sessão** (`Enr6MockStore`). Não substituem a app JavaFX — servem para validar fluxo, UX e regras antes do Java.

## Como abrir

1. **Aplicativo (recomendado):** [`shell.html`](shell.html) — menu lateral, barra CAEPI, perfis RBAC.
2. Entrada: [`pages/00-login.html`](pages/00-login.html) (acesso rápido por papel) → redireciona ao shell.
3. Catálogo avulso: [`index.html`](index.html) — links diretos por caso de uso.

Arquivos em `pages/` usam [`common.css`](common.css) (tokens Easy NR6). Cada página inclui barra **mock-chrome** com link ao catálogo.

### Cadastros compartilhados

[`mock-store.js`](mock-store.js) + [`mock-idb.js`](mock-idb.js): **todos os cadastros e operação** persistem em **IndexedDB** (`enr6_mock_v1`, store `app`). Índice CAEPI grande fica no store `ca`; amostra pequena vai embutida no snapshot `app`. Memória compartilhada via `window.__enr6Db` no shell (iframes).

Login mock: `sessionStorage` (`enr6_mock_user`). Migração automática se ainda existir snapshot antigo em `sessionStorage`.

Roteiro para clientes: [`DEMO-APRESENTACAO.md`](DEMO-APRESENTACAO.md). **Reset** no rodapé restaura seed e apaga IndexedDB.

## Mapa

| Arquivo | Caso de uso |
|---------|-------------|
| `00-login.html` | Autenticação / 1º acesso |
| `01-dashboard.html` | Home por papel |
| `02-entrega-wizard.html` | UC-ENT-01 wizard 5 passos |
| `03-devolucao.html` | Devolução / descarte |
| `04-estorno.html` | Estorno formal |
| `05-historico-trabalhador.html` | Consulta histórico |
| `06`–`09` | Cadastros (trabalhador, setor, EPI, CA) |
| `10-estoque-lotes.html` | UC-LOT |
| `11`–`12` | Matriz e periodicidade |
| `13`–`15` | Relatórios, cobertura, pendências |
| `16`–`17` | UC-SOL-01 gestor e fila |
| `18-auditoria.html` | UC-AUD-01 |
| `19`–`20` | Admin usuários e parâmetros |
| `21-caepi-import.html` | UC-CAE-01 |

Referência de identidade: [`../02-arquitetura/easy-nr6-ux-identity.md`](../02-arquitetura/easy-nr6-ux-identity.md).

Alinhamento com implementação Java: [`../03-operacao/paridade-mock-java-backlog.md`](../03-operacao/paridade-mock-java-backlog.md).
