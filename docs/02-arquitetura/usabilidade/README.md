# Usabilidade — Easy NR6

Referência das heurísticas de Nielsen aplicada a este repositório. O rascunho único que estava em `site/As_10_Heurísticas_de_Usabilidade` foi separado aqui.

A aplicação é **JavaFX + AtlantaFX**. A pasta `site/` é a **landing HTML estática**. O rascunho citava Next.js, Tailwind e Lucide; isso não é a stack deste repositório e não entra nas regras do Cursor.

| Arquivo | Uso |
|---|---|
| `heuristicas-nielsen.md` | As 10 heurísticas e o que cada uma significa no Easy NR6 |
| `padrao-comercial.md` | Empty state, espaçamento, tabela, erro, ajuda e o contraste amador/profissional |
| `docs/02-arquitetura/easy-nr6-ux-identity.md` | Marca, tom de voz e tokens |
| `docs/02-arquitetura/ui-ux-blueprint.md` | Mapa de telas e componentes |

Regras do Cursor, carregadas só quando o arquivo aberto casa com o glob:

- `.cursor/rules/usabilidade-javafx.mdc` — `src/main/java/**/ui/**/*.java`
- `.cursor/rules/usabilidade-site.mdc` — `site/**/*.html`
