# Spec curta - UC-CAD-07 (GHE)

## Identificacao

- ID Jira/Issue: a definir
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CAD`
- Responsavel: Time Easy NR6
- Status: implementado na tela GHE (`GheManagementView`)

## 1) Contexto

- Problema real:
  - funcoes diferentes podem exigir a mesma lista de EPI porque a exposicao e a mesma. Repetir a matriz em cada funcao faz o SESMT divergir uma lista da outra.
- Ator principal:
  - `SESMT`. Tambem `Admin`.
- Impacto se nao resolver:
  - a mesma exposicao vira varias listas;
  - o balcao nao tem um perfil unico para quem divide o risco.

O GHE nao e exigencia da NR-06. A norma pede a selecao do EPI pela atividade e o registro dessa selecao. O grupo e o modo desta versao de reutilizar essa lista entre funcoes.

## 2) Escopo

- Comportamento no escopo:
  - criar GHE na unidade, com nome e status;
  - editar o nome;
  - vincular e desvincular funcoes ativas da mesma unidade;
  - inativar e reativar sem delete fisico;
  - definir o perfil vigente que a matriz, o fornecimento, a solicitacao e a cobertura leem.
- Fora de escopo:
  - campo de GHE no trabalhador. A funcao vigente herda o grupo;
  - varias exposicoes por funcao. Uma funcao pertence a no maximo um GHE;
  - risco, agente, PGR e arquivo S-2240;
  - a lista de EPI do grupo. Isso continua no `UC-MAT-01`;
  - periodicidade. Continua no `UC-MAT-02`, por EPI.

## 3) Regras de negocio

1. O GHE pertence a uma unidade. O operador nao muda a unidade depois de gravar.
2. Nome obrigatorio, gravado sem espaco nas pontas. Unico na unidade, sem diferenciar maiusculas. Duas unidades podem usar o mesmo nome.
3. GHE novo pode nascer ativo ou inativo.
4. A funcao vinculada e uma `job_role` ativa cujo setor pertence a mesma unidade. Funcao inativa, de outra unidade ou inexistente nao entra.
5. Uma funcao pertence a no maximo um GHE, ativo ou inativo. Para troca de grupo, desvincula antes. Vincular de novo a funcao que ja esta neste GHE nao cria outra linha.
6. Desvincular pede confirmacao. A funcao volta a ser o proprio perfil da matriz.
7. Inativar pede confirmacao e nao apaga os vinculos. Enquanto o GHE estiver inativo, o perfil vigente de cada funcao membro e a propria funcao. Reativar restaura o mesmo grupo como perfil vigente.
8. Nao ha delete fisico. Inativar funcao no `UC-CAD-02` nao exige sair do GHE. Funcao inativa nao aparece para vinculo novo e o vinculo ja gravado permanece.
9. O trabalhador nao ganha coluna. O perfil vigente e resolvido pela funcao dele, na hora da leitura:
   - funcao membro de GHE ativo: a lista e a do GHE. A lista propria da funcao nao se soma;
   - GHE ativo sem EPI: a lista vigente e vazia. Nao ha fallback para a matriz da funcao;
   - sem GHE, ou com o GHE inativo: a lista e a da propria funcao.
10. `Almoxarife` e `Consulta` nao mantem GHE.
11. Criar, editar o nome, vincular, desvincular, inativar e reativar geram auditoria append-only.

## 4) Catalogo de erros de dominio (CAD-05x)

Codigos novos. Nao reutilizar `CAD-001` a `CAD-045`.

| Codigo | Quando | Texto de tela |
|---|---|---|
| `CAD-051` | Nome ou unidade ausente | Informe a unidade e o nome do GHE. |
| `CAD-052` | Nome ja usado na unidade | Ja existe um GHE com esse nome nesta unidade. |
| `CAD-053` | GHE alvo inexistente | Este GHE nao foi encontrado. |
| `CAD-054` | Funcao inativa, de outra unidade ou inexistente | Escolha uma funcao ativa desta unidade. |
| `CAD-055` | Funcao ja pertence a outro GHE | Esta funcao ja esta em outro GHE. Tire-a de la antes. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para alterar o GHE. |

A tela mostra o texto. O codigo fica no log e na auditoria.

## 5) Criterios de aceite

- `CA-01`: SESMT cria GHE ativo com nome e o ve na lista da unidade.
- `CA-02`: o mesmo nome em outra unidade grava. O mesmo nome na unidade, mudando so maiusculas, recusa com `CAD-052`.
- `CA-03`: vincular funcao ativa da unidade grava um vinculo. Vincular de novo nao duplica.
- `CA-04`: funcao de outra unidade, inativa, ou ja membro de outro GHE recusa com `CAD-054` ou `CAD-055` e nao grava.
- `CA-05`: desvincular pede confirmacao. Cancelar preserva o vinculo. Confirmar devolve a funcao ao perfil proprio.
- `CA-06`: inativar preserva o id e os vinculos. Com o GHE inativo, o perfil vigente passa a ser a funcao. Reativar volta o perfil para o GHE.
- `CA-07`: GHE ativo sem linha de matriz deixa a lista vigente vazia, mesmo que a funcao tenha linhas proprias.
- `CA-08`: `Admin` e `SESMT` executam o fluxo. `Almoxarife` e `Consulta` recebem `AUTH-004`.
- `CA-09`: cada mutacao gera auditoria com ator, acao, entidade `GHE` e id.

## 6) Modelo de dados

Migration nova. Nao alterar `employee`.

- `ghe`: `id`, `unit_id`, `name`, `active`. Unicidade do nome normalizado por `unit_id`.
- `ghe_job_role`: `ghe_id`, `job_role_id`. `job_role_id` unico na tabela, para garantir um GHE por funcao.

O setor ja carrega `unit_id`. A funcao entra no GHE pela `job_role`, nao por uma coluna no trabalhador.

## 7) Tela

Destino: "GHE", ao lado de setores e funcoes. Titulo sem o codigo do caso de uso.

- Unidade aberta. Lista: nome, quantidade de funcoes, status.
- Formulario: nome e status inicial. A unidade nao se edita.
- Funcoes ativas da unidade que ainda nao estao em outro GHE. O rotulo traz o setor, para homonimos nao se confundirem.
- Desvincular: "Tirar esta funcao do GHE? A lista dela volta a ser a matriz da propria funcao." Confirmar / Cancelar.
- Inativar: "Inativar este GHE? Enquanto estiver inativo, cada funcao volta a usar a matriz propria." Confirmar / Cancelar.
- Vazio: "Nenhum GHE nesta unidade."
- Sucesso na propria tela, sem `Alert`. Erro amigavel, sem mostrar o codigo.

## 8) Recorte tecnico

Cadastro entregue antes da tela do `UC-MAT-01`. A lista de EPI continua na matriz.

- `GhePolicy`: nome, unicidade na unidade, funcao da mesma unidade, um GHE por funcao.
- Casos de uso: criar, editar nome, vincular, desvincular, alterar status, listar.
- Eventos: `GHE_CRIADO`, `GHE_EDITADO`, `GHE_FUNCAO_VINCULADA`, `GHE_FUNCAO_DESVINCULADA`, `GHE_INATIVADO`, `GHE_REATIVADO`.
- RBAC no mesmo autorizador dos cadastros mestres.
- Falha tratada na tela registra troubleshooting, sem senha, token ou hash. O codigo da regra fica no log e na auditoria.

## 9) Seguranca e auditoria

- Impacto em login/senha/usuario/papel: nao.
- Eventos auditados: os seis da secao 8.
- Risco se implementar errado: somar a lista do GHE com a da funcao, ou deixar o trabalhador preso a um grupo depois de mudar de funcao.

## 10) Documentacao impactada

- `docs/04-uml/cadastros/UC-CAD-07 — Cadastrar GHE.md`
- `docs/04-uml/casos-de-uso-uml.md`
- `docs/03-operacao/matriz-testes-uc-cad-07.md`
- `docs/03-operacao/spec-uc-mat-01-matriz.md`
- `docs/03-operacao/spec-uc-mat-02-periodicidade.md`
- `docs/03-operacao/spec-uc-ent-01-fornecimento.md`
- `docs/03-operacao/spec-uc-sol-01-solicitar-epi-para-trabalhador.md`
- `docs/03-operacao/spec-uc-rel-03-cobertura.md`
- `docs/03-operacao/backlog-implementacao-mvp.md`
