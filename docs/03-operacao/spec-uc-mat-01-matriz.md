# Spec curta - UC-MAT-01 (Matriz funcao x EPI)

## Identificacao

- ID: `UC-MAT-01`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: especificado; implementacao nao iniciada

## 1) Contexto

- Problema real:
  - sem a lista do que a funcao exige, o balcao nao sabe o que fornecer e a cobertura nao tem o que comparar.
- Ator principal: `SESMT`. Tambem `Admin`.
- Impacto se nao resolver:
  - item fora do risco entra sem registro de excecao;
  - item obrigatorio fica invisivel.

## 2) Escopo

- Comportamento no escopo:
  - manter, por perfil, os EPIs exigidos. O perfil e uma funcao ativa fora de GHE ativo, ou um GHE ativo;
  - em cada linha: EPI, CA esperado, modo Individual ou Posto, e se exige treinamento;
  - inativar a linha, sem apagar.
- Fora de escopo:
  - criar o GHE e vincular funcoes. Isso e o `UC-CAD-07`. O perfil vigente tambem esta definido la;
  - periodicidade (e o `UC-MAT-02`);
  - a ficha de fornecimento e a excecao no balcao (sao o `UC-ENT-01`);
  - treinamento como curso. Aqui so a flag.

## 3) Regras de negocio

1. A linha liga um perfil a um EPI ativo que tenha ao menos um CA ativo. O grupo do Anexo I vem do cadastro do EPI e nao se edita nesta tela. O perfil e uma funcao ativa que nao pertence a GHE ativo, ou um GHE ativo.
2. Unicidade: uma linha ativa por perfil e EPI. Linha inativa nao bloqueia uma nova inclusao.
3. O CA esperado e um vinculo de CA ativo daquele EPI. Ao incluir, o sistema sugere o CA ativo. O SESMT pode atualizar a sugestao para o CA ativo de agora. Nao aceita CA inativo.
4. Modo `INDIVIDUAL` ou `POSTO`. Padrao ao incluir: `INDIVIDUAL`. Posto e o descartavel e o creme disponiveis no posto, nao uma peca de guarda pessoal.
5. Treinamento exigido e sim ou nao. Padrao: nao. A data do treinamento, quando a flag estiver ligada, e registrada na ficha do `UC-ENT-01`.
6. Inativar pede confirmacao. Cancelar nao grava. Nao ha delete. Fornecimento ja feito permanece com a funcao gravada na ficha.
7. Funcao, GHE ou EPI inativo nao recebe linha nova. Linha ja ativa de EPI que depois inativou continua visivel e pode ser inativada.
8. `Almoxarife` e `Consulta` nao abrem esta manutencao. O almoxarife ve o resultado dentro do fornecimento.
9. Auditoria: `MATRIZ_INCLUIDA`, `MATRIZ_ALTERADA`, `MATRIZ_INATIVADA`, entidade `MATRIZ`.
10. Funcao membro de GHE ativo nao recebe linha propria. A tentativa recusa com `MAT-008`. Linhas ja gravadas nessa funcao permanecem e voltam a ser o perfil vigente se o GHE for inativado ou a funcao sair do grupo, regra 9 do `UC-CAD-07`.

## 4) Catalogo de erros

A tela mostra o texto. O codigo vai para a auditoria.

| Codigo | Quando | Texto de tela |
|---|---|---|
| `MAT-001` | Perfil ou EPI ausente | Escolha o perfil e o EPI. |
| `MAT-002` | Ja existe linha ativa para o perfil e o EPI | Este EPI ja esta na matriz deste perfil. |
| `MAT-003` | EPI sem CA ativo | Este EPI nao tem CA ativo para entrar na matriz. |
| `MAT-004` | Perfil ou EPI inativo na inclusao | Escolha um perfil ativo e um EPI ativo. |
| `MAT-007` | Linha inexistente ou ja inativa | Esta linha da matriz nao esta ativa. |
| `MAT-008` | Funcao membro de GHE ativo | Esta funcao usa a lista do GHE. Edite o grupo. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para alterar a matriz. |

## 5) Criterios de aceite

- `CA-01`: SESMT inclui EPI ativo com CA e ve a linha com modo Individual e treinamento desligado.
- `CA-02`: segunda inclusao do mesmo par ativo recusa com `MAT-002` e nao grava.
- `CA-03`: EPI sem CA ativo recusa com `MAT-003`.
- `CA-04`: mudar para Posto ou ligar treinamento grava `MATRIZ_ALTERADA` e nao cria outra linha.
- `CA-05`: inativar pede confirmacao; cancelar preserva a linha ativa.
- `CA-06`: depois de inativar, o par pode ser incluido de novo.
- `CA-07`: `Almoxarife` recebe `AUTH-004`.
- `CA-08`: funcao membro de GHE ativo nao aparece como perfil proprio. Incluir linha nela recusa com `MAT-008`. O combo oferece o GHE.

## 6) Tela

Destino ja previsto: "Matriz". Titulo da pagina sem o codigo do caso de uso.

- O perfil e uma funcao ativa que nao esta em GHE ativo, rotulada com o setor, ou um GHE ativo, rotulado com o nome do grupo.
- Incluir: combo so de EPI ativo que ainda nao esta ativo neste perfil. Botao "Incluir na matriz" desabilitado sem EPI.
- Grade: EPI, Anexo I (leitura), CA esperado, Individual ou Posto, treinamento, Inativar.
- "Atualizar CA esperado" troca o CA da linha pelo CA ativo atual. Se nao houver, a frase do `MAT-003`.
- Vazio: "Nenhum EPI na matriz deste perfil." e o combo de incluir.
- Inativar: dialogo "Tirar este EPI da matriz deste perfil? Fornecimentos ja feitos permanecem." Confirmar / Cancelar.
- Sucesso: "EPI incluido na matriz." ou "Matriz atualizada." Faixa na propria tela. Sem `Alert` de sucesso.
- Erro amigavel, sem mostrar `MAT-002`.

## 7) Seguranca e auditoria

- Login/senha: nao.
- Eventos: `MATRIZ_INCLUIDA`, `MATRIZ_ALTERADA`, `MATRIZ_INATIVADA`.
- Risco: apagar a regra e a cobertura passar a ignorar um EPI que o posto ainda deve fornecer.

## 8) Documentacao impactada

- `docs/03-operacao/matriz-testes-uc-mat-01.md`
- `docs/03-operacao/spec-uc-mat-02-periodicidade.md`
- `docs/03-operacao/spec-uc-ent-01-fornecimento.md`
- `docs/04-uml/UC-MAT-01 — Definir matriz funcao-GHE x EPI.md`
- `docs/03-operacao/spec-uc-cad-07-ghe.md`
