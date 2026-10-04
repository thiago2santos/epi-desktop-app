# Spec curta - UC-REL-03 (Cobertura dos trabalhadores ativos)

## Identificacao

- ID: `UC-REL-03`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-REL`
- Responsavel: Time Easy NR6
- Status: especificado; codigo depois do `UC-MAT-02`
- Leitura: a regra 6 de `docs/03-operacao/spec-uc-mat-02-periodicidade.md`
- Exportacao: `docs/03-operacao/spec-uc-trv-03-pdf.md`

## 1) Contexto

- Problema real:
  - o balcao ve a cobertura de uma pessoa. O SESMT precisa ver a planta: quem esta vigente, em troca, vencido ou sem a peca.
- Atores: `SESMT`, `Admin`, `Almoxarife`, `Consulta`.
- Impacto se nao resolver:
  - a formula existe e nao ha lista do dia.

## 2) Escopo

- Comportamento no escopo:
  - listar trabalhadores ativos da unidade;
  - para cada um, aplicar a leitura ja definida na periodicidade;
  - resumir a lacuna e abrir os itens;
  - exportar o mesmo recorte em PDF.
- Fora de escopo:
  - trabalhador inativo (a pendencia dele e o `UC-POS-03`);
  - mudar matriz, periodicidade ou ficha a partir daqui;
  - uma formula nova de vigencia.

## 3) Regras de negocio

1. A situacao de cada item e a da regra 6 do `UC-MAT-02`: Posto, Sem prazo, Pendente, Vigente, Troca em N dias, Prazo vencido. Texto. Cor nao substitui o texto.
2. Trabalhador ativo sem nenhuma linha de matriz ativa na funcao entra com o resumo "Sem matriz" e zero itens.
3. Resumo do trabalhador, nesta ordem, a primeira que couber:
   - "Sem matriz";
   - "N pendente" se houver item Pendente;
   - "N em troca ou vencido" se houver Troca em N dias ou Prazo vencido;
   - "OK" se todo item individual estiver Vigente ou Sem prazo, e os de Posto nao contam como lacuna.
4. "Sem prazo" nao e lacuna e nao e OK silencioso: o resumo OK so vale quando nao ha Pendente nem troca nem vencido. Se a unica ressalva for Sem prazo, o resumo e "Sem prazo".
5. Filtros: unidade obrigatoria. Setor, funcao e situacao do resumo a um clique. Situacao filtra o resumo, nao esconde o fato de a pessoa existir quando o filtro e "todas".
6. Filtro sem pessoa: "Nenhum trabalhador ativo nesta unidade."
7. A tela nao grava. O relogio da consulta e o da leitura da periodicidade.
8. O PDF lista trabalhador, matricula, funcao, resumo e, em seguida, os itens com a situacao. Mesmas regras de linha.

## 4) Catalogo de erros

Unidade ausente: "Escolha a unidade." `AUTH-004` nao se aplica aos quatro papeis desta tela. `Gestor` nao abre: "Voce nao tem permissao para ver a cobertura."

## 5) Criterios de aceite

- `CA-01`: trabalhador com luva Pendente e capacete Vigente aparece "1 pendente", e o detalhe mostra as duas situacoes.
- `CA-02`: item Posto nao entra na contagem de pendente.
- `CA-03`: funcao sem matriz mostra "Sem matriz".
- `CA-04`: so "Sem prazo" no individual mostra o resumo "Sem prazo".
- `CA-05`: trabalhador inativo nao aparece.
- `CA-06`: a situacao do item e a mesma frase que o painel do `UC-ENT-01` mostra para essa pessoa.
- `CA-07`: o PDF repete resumo e itens da consulta.

## 6) Tela

Destino ja previsto: "Cobertura". Titulo sem codigo de caso de uso.

- Unidade aberta. Setor, funcao e situacao a um clique.
- Grade: trabalhador, matricula, funcao, resumo em texto.
- Abrir a linha mostra os EPIs e a situacao, sem sair da lista.
- Botao "Gerar PDF" depois da consulta.
- Vazio com a frase da regra 6.
- Sem `Alert`. A consulta e a propria tela.

## 7) Seguranca e auditoria

- Consulta nao audita. Exportacao: `RELATORIO_EXPORTADO`, identificador `COBERTURA`.
- Risco: inventar outra conta de vigencia e a planta divergir do balcao.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-mat-02-periodicidade.md`
- `docs/03-operacao/spec-uc-trv-03-pdf.md`
- `docs/03-operacao/matriz-testes-uc-rel-03.md`
- `docs/04-uml/casos-de-uso-uml.md`
