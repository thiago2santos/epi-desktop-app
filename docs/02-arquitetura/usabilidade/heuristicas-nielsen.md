# As 10 heurísticas de Nielsen no Easy NR6

Fonte de produto: Jakob Nielsen. A aplicação desta lista está em `easy-nr6-ux-identity.md` e nas telas JavaFX já existentes.

1. **Visibilidade do status.** O operador sabe o que aconteceu. Sucesso, aviso e erro usam `MensagemTemporaria` com `Enr6Styles`. Tarefa longa (rede, arquivo, importação) roda fora da thread do JavaFX e mostra andamento sem travar a janela.
2. **Mundo real.** A tela fala a língua do almoxarife e do SESMT. Código `CAD-` ou `AUTH-` fica no log e na auditoria. O rótulo da ação segue o caso de uso (“Registrar fornecimento”, “Inativar”), não o nome da tabela.
3. **Controle e liberdade.** Inativar pede confirmação. Cancelar não chama o serviço. O registro inativo permanece; não há delete físico de cadastro mestre.
4. **Consistência.** Lista à esquerda, formulário à direita, Salvar / Inativar / Reativar, como em unidade, EPI e setor. Visual por `Enr6Styles` e classes AtlantaFX. Tela de negócio não usa `setStyle` nem cor hexadecimal.
5. **Prevenção de erro.** O botão Salvar fica desabilitado até os obrigatórios. Máscara na entrada (CNPJ, data); o domínio grava o valor normalizado. Bloqueio jurídico aparece antes do commit.
6. **Reconhecimento.** Combo, tabela e resumo mostram a escolha. O operador não precisa lembrar o CNPJ ou o nome da unidade de uma tela para a outra: o rótulo visível distingue homônimos.
7. **Eficiência.** Atalho para quem já opera: Esc fecha o diálogo, Enter confirma o formulário em foco. Uma ação primária por contexto.
8. **Estética mínima.** Cada bloco responde “o que fazer agora?”. Sem gráfico decorativo e sem texto que repita o título.
9. **Recuperar o erro.** A frase diz o que falhou, por quê e o que fazer. O campo inválido recebe `Enr6Styles.markFieldInvalid`. Não basta pintar a borda.
10. **Ajuda.** Tooltip só em regra que a tela não explica sozinha. Documentação de caso de uso fica em `docs/`, não copiada para dentro do formulário.

Detalhe comercial (empty state, grade, tabela, toast versus diálogo): `padrao-comercial.md`.
