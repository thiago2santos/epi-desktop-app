# Migracao e padronizacao da documentacao

## Objetivo

Mapear arquivos atuais para nomes/pastas padronizados e identificar oportunidades de merge.

## Mapa de migracao sugerido

| Arquivo atual | Destino padronizado | Acao sugerida |
|---|---|---|
| `PLANO.md` | `docs/00-visao/visao-produto-e-escopo.md` | manter como base e evoluir |
| `CATADAO_DEFINICOES.md` | `docs/00-visao/resumo-executivo-definicoes.md` | manter; revisar a cada marco |
| `STACK_E_ARQUITETURA.md` | `docs/02-arquitetura/stack-e-arquitetura.md` | manter e versionar |
| `ARQUITETURA_MODULAR.md` | `docs/02-arquitetura/arquitetura-modular.md` | manter como documento vivo |
| `MODELAGEM_ENTREGA_EPI.md` | `docs/01-negocio/modelagem-entrega-epi.md` | manter enquanto houver detalhe especifico |
| `modelagem_fluxo_epi/ANTES_DA_ENTREGA.md` | `docs/01-negocio/fluxo-antes-da-entrega.md` | renomear e mover |
| `modelagem_fluxo_epi/DURANTE_A_ENTREGA.md` | `docs/01-negocio/fluxo-durante-a-entrega.md` | renomear e mover |
| `modelagem_fluxo_epi/DEPOIS_DA_ENTREGA.md` | `docs/01-negocio/fluxo-depois-da-entrega.md` | renomear e mover |
| `ROTEIRO_ENTREVISTA_KEY_USER.md` | `docs/03-operacao/roteiro-entrevista-key-user.md` | manter para discovery continuo |
| `MANUAL_OPERACIONAL_V0.md` | `docs/03-operacao/manual-operacional-v0.md` | manter e evoluir para v1 |
| `CASOS_DE_USO_UML.md` | `docs/04-uml/casos-de-uso-uml.md` | manter como base textual UML |
| `diagramas/casos_de_uso_geral.puml` | `docs/05-diagramas/casos-de-uso-geral.puml` | mover e padronizar nome |
| `diagramas/casos_de_uso_geral.drawio` | `docs/05-diagramas/casos-de-uso-geral.drawio` | mover e padronizar nome |

## Oportunidades de merge

1. **Visao/escopo**
   - Consolidar `PLANO.md` + `CATADAO_DEFINICOES.md`.
   - Saida final: um documento executivo e um tecnico (sem duplicidade).

2. **Modelagem de negocio**
   - Manter os 3 fluxos (antes/durante/depois) como fonte principal.
   - Absorver no tempo o que estiver duplicado em `MODELAGEM_ENTREGA_EPI.md`.

3. **Arquitetura**
   - `STACK_E_ARQUITETURA.md` e `ARQUITETURA_MODULAR.md` sao complementares.
   - Nao mesclar agora; usar stack para "por que" e modular para "como".

## Ordem recomendada de migracao (baixo risco)

1. Padronizar pasta `docs/` e manter arquivos atuais intactos.
2. Migrar primeiro diagramas (`.puml`, `.drawio`).
3. Migrar documentos de operacao.
4. Migrar modelagem de negocio.
5. Consolidar visao/escopo.
6. Remover arquivos antigos somente apos conferencia.

## Status

- [x] Convencao definida
- [x] Mapa de migracao definido
- [x] Migracao fisica dos arquivos concluida
- [ ] Consolidacao/merge concluida

