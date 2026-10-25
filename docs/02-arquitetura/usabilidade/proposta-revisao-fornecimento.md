# Proposta — passo Revisão do fornecimento

Tela: Registrar fornecimento, passo 5. Spec de regra: `docs/03-operacao/spec-uc-ent-01-fornecimento.md`. Heurísticas: `heuristicas-nielsen.md`.

Status: implementado na tela Registrar fornecimento, passo Revisão.

## O que a revisão é

A revisão não é um formulário e não é um resumo de rascunho. É a última leitura de um ato que, depois de confirmado, não se edita. Quem olha é o almoxarife. O trabalhador já falou no passo anterior, na primeira pessoa. Aqui a voz volta para o operador: conferir o que vai ficar na ficha.

Gestão da informação, nesta ordem, de cima para baixo:

1. **Quem.** Nome, matrícula, unidade, setor e função do instante. É o cabeçalho da prova.
2. **O que sai.** Uma linha por peça, com os dados que a fiscalização e o estoque usam: EPI, CA, lote, validade, quantidade, motivo. Sem isso a revisão não impede erro.
3. **A prova.** Orientação de uso por item, data de treinamento quando a matriz exige, e o aceite do termo. O método é `SENHA`. A senha e o hash não aparecem.
4. **A consequência.** A ficha não poderá ser editada e a quantidade sai do disponível. Isso fica numa faixa, antes do botão, não só dentro do diálogo.

O passo anterior já mostrou o termo por extenso. Repetir o parágrafo inteiro aqui compete com a tabela. A revisão guarda uma linha: “Termo aceito.” O texto integral continua no passo Ciência e termo e na ficha gravada.

## O que as fichas de papel ensinam

Três modelos em `docs/03-operacao`: `ficha-de-epi.pdf`, `FICHA ENTREGA EPI - Ficha_entrega_epi.pdf` e `FICHA DE FORNECIMENTO DE EQUIPAMENTO DE PROTEÇÃO INDIVIDUAL (EPI) - FP.SSO_.022-01-Ficha-de-Entrega-de-EPI.pdf`.

As três são o mesmo objeto, apesar do miolo jurídico diferente:

1. Título no centro: ficha de entrega de EPI.
2. Quem, em células com borda: nome, local, função, matrícula ou setor. O rótulo fica na célula, o valor ao lado ou abaixo.
3. Termo de responsabilidade, com título próprio, e um lugar de assinatura.
4. Tabela pautada, uma linha por entrega: data, quantidade, EPI, CA, assinatura. Devolução só aparece no controle acumulado, não na entrega do dia.

A revisão copia essa ordem e a grade. Não copia o parágrafo inteiro (ele já foi lido no passo anterior), a coluna de assinatura por linha, as linhas em branco nem o logo. Lote, validade e motivo continuam, porque a ficha de papel não impede erro de estoque e a nossa precisa impedir.

## Forma

A mesma caixa branca do passo Ciência e termo, no centro da área. Largura bastante para a tabela, não a coluna estreita das dez linhas do termo. Anterior na borda esquerda dessa caixa. Na direita, “Confirmar fornecimento”. Não há Próximo.

Dentro da caixa, de cima para baixo, com vão entre os blocos:

- Título: “Ficha de entrega de EPI”. O passo “Revisão” fica só na faixa de passos.
- Grade de identificação: nome e matrícula; local de trabalho e setor; função na linha de baixo.
- Título “Termo de responsabilidade” e a linha “Termo aceito.”
- Grade dos itens: EPI, CA, Lote, Validade, Qtd, Motivo. Cabeçalho em fundo mais baixo. Motivo “Outro” mostra o texto na célula. Sob a linha, a orientação de uso e, se houver, a data do treinamento e a exceção.
- Faixa no pé da caixa: “Esta ficha não poderá ser editada. A quantidade sai do disponível.”

A linha solta com o nome do trabalhador, acima da caixa, sai neste passo. O cabeçalho da caixa já é essa identificação. Repetir os dois é ruído.

Nada se edita aqui. Lote, quantidade e motivo errados voltam por Anterior. O diálogo de confirmação permanece: “Confirmar fornecimento? Esta ficha não poderá ser editada.” Enter na revisão só abre o diálogo. Enter no diálogo confirma. Esc no diálogo volta. Anterior não chama o serviço.

Sucesso continua na faixa da própria tela, com o número da ficha. Sem `Alert` de sucesso.

## Heurísticas

| # | Heurística | O que a revisão faz |
|---|---|---|
| 1 | Visibilidade do status | O passo 5 está em destaque. A caixa deixa claro que a ficha ainda não foi gravada. |
| 2 | Mundo real | EPI, CA, lote e validade, na língua do balcão. Código `ENT-` não aparece. |
| 3 | Controle e liberdade | Anterior desfaz o passo sem gravar. Esc no diálogo não confirma. |
| 4 | Consistência | As colunas repetem o passo Lote e quantidade. A caixa repete a do termo. Os botões acompanham as laterais da caixa, como na ciência. |
| 5 | Prevenção de erro | Não há campo novo. O que falta (CA, lote, motivo, termo) está visível antes do commit. O botão primário não grava direto: abre o diálogo. |
| 6 | Reconhecimento | O operador reconhece a peça, o lote e a quantidade. Não precisa lembrar o que marcou três passos atrás. |
| 7 | Eficiência | Uma ação primária: Confirmar fornecimento. |
| 8 | Estética mínima | Cabeçalho, tabela, prova, consequência. Sem parágrafo de instrução e sem repetir o termo inteiro. |
| 9 | Recuperar o erro | Se a confirmação falha, a revisão permanece com os dados e a frase do que fazer. |
| 10 | Ajuda | A faixa do pé explica a consequência. Tooltip só se uma coluna não se explicar sozinha. |

## Fora desta proposta

- Imprimir comprovante e “Nova entrega” no sucesso. O comprovante é o `UC-REL-01`.
- Senha visível, hash ou segunda caixa de aceite.
- Editar lote ou quantidade na revisão.
