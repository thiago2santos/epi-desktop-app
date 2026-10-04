# Spec curta - UC-TRV-03 (Exportar relatorio em PDF)

## Identificacao

- ID: `UC-TRV-03`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-REL`
- Responsavel: Time Easy NR6
- Status: especificado; nao tem tela propria
- Motor: JasperReports, como na visao do produto. PDFBox fica fora desta versao.

## 1) Contexto

- Problema real:
  - ficha, historico, cobertura e budget precisam sair no mesmo tipo de arquivo, com o mesmo cuidado de nao travar a janela nem deixar arquivo pela metade.
- Ator: o mesmo papel que pode abrir o relatorio de origem.
- Impacto se nao resolver:
  - cada tela inventa um PDF e a prova fica diferente da consulta.

## 2) Escopo

- Comportamento no escopo:
  - receber do caso de origem o titulo, os filtros e as linhas ja calculadas;
  - gerar o PDF fora da thread da janela;
  - o operador escolhe o caminho do arquivo;
  - auditar a emissao.
- Casos que usam este contrato: `UC-REL-01`, `UC-REL-02`, `UC-REL-03`, `UC-REL-04`.
- Fora de escopo:
  - calcular de novo a regra de negocio dentro do motor;
  - carimbo, assinatura digital e juncao de arquivos;
  - enviar e-mail.

## 3) Regras de negocio

1. O PDF contem titulo, filtros aplicados, linhas, instante da geracao e nome do operador. Nao contem dado que a tela de origem nao mostrou.
2. A geracao nao corre na thread do JavaFX. A fase visivel e "Gerando o PDF...". Percentual so se o motor informar pagina atual; senao, indicador indeterminado.
3. O operador confirma o caminho. Cancelar o dialogo de arquivo nao gera nada e nao audita.
4. Falha no meio apaga o arquivo incompleto se ele chegou a ser criado. A tela diz: "Nao foi possivel gerar o PDF. Nada foi salvo."
5. Sucesso: "PDF salvo." e o nome do arquivo, em faixa na tela de origem. Sem `Alert`.
6. Quem nao abre o relatorio de origem nao exporta. A recusa e o `AUTH-004` daquele caso.
7. Auditoria na mesma conclusao do arquivo: `RELATORIO_EXPORTADO`, entidade `RELATORIO`, com identificador do relatorio, filtros e ator. A falha tambem audita, com resultado de falha, sem caminho de arquivo.
8. Dois operadores podem gerar o mesmo recorte. Cada arquivo leva o seu instante e o seu operador.

## 4) Catalogo de erros

| Codigo | Quando | Texto de tela |
|---|---|---|
| `REL-001` | Falha ao escrever o PDF | Nao foi possivel gerar o PDF. Nada foi salvo. |
| `AUTH-004` | Papel sem a tela de origem | Voce nao tem permissao para emitir este relatorio. |

## 5) Criterios de aceite

- `CA-01`: ficha gerada abre com as linhas que a previa mostrou, mais instante e operador.
- `CA-02`: cancelar a escolha do arquivo nao cria arquivo nem auditoria de sucesso.
- `CA-03`: falha forcada na escrita remove o parcial e mostra a frase do `REL-001`.
- `CA-04`: a janela continua navegavel enquanto o PDF gera.
- `CA-05`: almoxarife na cobertura exporta; na ficha do trabalhador e no budget, nao.
- `CA-06`: auditoria de sucesso guarda o identificador do relatorio e os filtros, sem copiar todas as linhas.

## 6) Tela

Nao ha destino proprio. O botao "Gerar PDF" fica na tela do caso de origem e so habilita depois da consulta.

## 7) Seguranca e auditoria

- Evento: `RELATORIO_EXPORTADO`.
- Risco: arquivo pela metade parecer uma ficha oficial, ou o PDF trazer custo numa ficha que a tela nao mostrou.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-rel-01-ficha.md`
- `docs/03-operacao/spec-uc-rel-02-historico-epi.md`
- `docs/03-operacao/spec-uc-rel-03-cobertura.md`
- `docs/03-operacao/spec-uc-rel-04-consumo-budget.md`
- `docs/03-operacao/matriz-testes-uc-trv-03.md`
- `docs/04-uml/casos-de-uso-uml.md`
