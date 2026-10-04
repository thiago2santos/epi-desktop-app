# Padrão comercial de tela

O que faz a tela parecer produto de gestão, além da heurística. Vale para o desktop JavaFX. A landing em `site/` segue só o que cabe numa página estática.

## O que a tela faz

- **Lista vazia.** Placeholder que nomeia a ausência e, quando a pessoa pode criar, a ação primária (“Nenhuma unidade cadastrada.” junto de “Nova unidade”). Não deixar a área em branco.
- **Carregamento.** Não girar um indicador no meio de uma tela vazia se a operação trava a janela. Trabalho pesado sai da thread do JavaFX. Indicador indeterminado e o nome da etapa quando não há percentual.
- **Grade.** Espaçamento em múltiplos de 8 (o `spacing` do `VBox` / `HBox` e o padding do painel). Desalinho de poucos pixels quebra a leitura da tabela.
- **Tabela.** Coluna com nome do dado, status em texto (não só cor), truncamento do texto longo, busca no filtro da própria lista. Borda suave, no padrão AtlantaFX já usado (`CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN`).
- **Sucesso.** Faixa temporária (`MensagemTemporaria`). Não usar `Alert` nativo para “salvo”.
- **Ação destrutiva.** Confirmação explícita. Cancelar mantém o status. Inativar unidade, setor, EPI ou usuário continua diálogo, não toast.
- **Erro.** Três partes: o que falhou, a regra, o próximo passo. Classe `enr6-feedback-danger` ou `enr6-field-invalid`.
- **Status nunca só pela cor.** Ativo/inativo, vigente e bloqueio levam ícone ou texto junto da cor (`--enr6-status-*` em `design-tokens.css`).
- **Conectividade.** Quando o modo oficial existir, a faixa global (`enr6-status-strip`) avisa offline. Ela não substitui o alerta que exige ação.

## Ajuda e contato

Canal de suporte, quando existir, fica no menu **Ajuda** do desktop (e-mail e horário), e na landing como link de contato visível. Não inventar botão flutuante de chat nem página de status que o produto ainda não opera.

## O que fica para uma decisão depois

Textos de tela estão em português no código. Não introduzir dicionário i18n numa tela nova sem essa decisão de arquitetura. Contraste segue os tokens; não criar paleta paralela.

## Amador e profissional

| Peça | Amador | Easy NR6 |
|---|---|---|
| Tabela | Borda pesada, coluna sem nome, sem filtro | Borda suave, status em texto, filtro na lista |
| Formulário | Salvar ativo com obrigatório vazio | Salvar desabilitado até o campo obrigatório |
| Inativar | Some o registro, ou segue sem perguntar | Confirma; cancelar não grava; o id permanece |
| Sucesso | `Alert` do sistema | Faixa temporária na própria tela |
| Erro | “Erro 404” ou só o código `CAD-045` | Frase do operador; o código fica na auditoria |
| Espera | Janela congelada | `Task` fora da thread do JavaFX |
| Cor | Hex no meio da tela | `Enr6Styles` e `design-tokens.css` |
