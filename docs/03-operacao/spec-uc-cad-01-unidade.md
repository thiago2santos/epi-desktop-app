# Spec curta - UC-CAD-01 (Unidade)

## Identificacao

- ID Jira/Issue: a definir
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CAD`
- Responsavel: Time Easy NR6
- Status: implementado (dominio, repositorio, tela e testes da matriz)

## 1) Contexto

- Problema real:
  - o setor ja exige uma unidade ativa, mas a lista vem so do seed da Access. Nao ha como incluir, corrigir ou inativar uma planta sem alterar a migration.
- Ator principal:
  - `Admin` e `SESMT`.
- Impacto se nao resolver:
  - nova planta ou CNPJ corrigido depende de carga tecnica;
  - setor continua apontando para unidade que o operador nao consegue manter.

## 2) Escopo

- Comportamento no escopo:
  - listar as unidades da empresa ja semeada (Access Gestao de Documentos Ltda.);
  - criar unidade com nome, CNPJ e status;
  - editar o nome;
  - inativar e reativar sem delete fisico;
  - recusar CNPJ invalido ou repetido;
  - recusar inativacao quando a unidade ainda tem setor ativo.
- Fora de escopo:
  - segunda empresa, edicao da razao social ou da raiz do CNPJ;
  - endereco, municipio, CEP e consulta na Receita;
  - importacao CSV (`UC-CAD-IMP-01`);
  - troca da unidade de um setor ja gravado (o setor continua preso a unidade em que nasceu).

A tabela publica de enderecos permanece em `docs/01-negocio/unidades-de-implantacao.md`. Este caso de uso nao a copia para o banco.

## 3) Regras de negocio

1. A unidade pertence a unica empresa existente. O operador nao escolhe nem cria empresa.
2. Nome obrigatorio, gravado sem espaco nas pontas. O nome nao e unico: duas plantas podem se chamar Belo Horizonte se o CNPJ for outro.
3. CNPJ obrigatorio. A tela aceita mascara; o dominio guarda 14 digitos. Digitos verificadores invalidos recusam o cadastro.
4. CNPJ unico na tabela `unit`.
5. Unidade nova pode nascer ativa ou inativa. So unidade ativa aparece no cadastro de setor.
6. Edicao altera o nome. O CNPJ nao muda depois de gravado.
7. Inativar exige confirmacao. Unidade com setor ativo e recusada. Reativar nao exige setor.
8. Nao ha delete fisico.
9. Criar, editar, inativar e reativar geram auditoria append-only.
10. `Almoxarife` e `Consulta` nao mutam unidade.

## 4) Catalogo de erros de dominio (CAD-04x)

Codigos novos. Nao reutilizar `CAD-001` a `CAD-039`.

- `CAD-041` Nome ou CNPJ ausente.
- `CAD-042` CNPJ invalido (tamanho diferente de 14 digitos ou digito verificador incorreto).
- `CAD-043` CNPJ ja cadastrado.
- `CAD-044` Unidade alvo de edicao ou status nao encontrada.
- `CAD-045` Inativacao recusada: a unidade ainda tem setor ativo.

A tela mostra o texto amigavel. O codigo fica no log e na auditoria.

## 5) Criterios de aceite

- `CA-01`: operador autorizado cria unidade com nome e CNPJ valido e a ve na lista.
- `CA-02`: CNPJ com mascara `22.755.266/0002-68` persiste como `22755266000268`.
- `CA-03`: CNPJ invalido ou duplicado nao grava e devolve `CAD-042` ou `CAD-043`.
- `CA-04`: editar o nome atualiza a unidade e o combo de setor passa a exibir o nome novo.
- `CA-05`: inativar unidade com setor ativo falha com `CAD-045` e o registro continua ativo.
- `CA-06`: inativar unidade sem setor ativo, e reativar depois, muda so o status e preserva o id.
- `CA-07`: `Admin` e `SESMT` executam o fluxo; `Almoxarife` e `Consulta` recebem recusa de permissao.
- `CA-08`: cada mutacao gera evento de auditoria com ator, acao, entidade `UNIDADE` e id.

## 6) Modelo de dados

Tabela `unit` ja existente. Sem migration neste caso de uso.

- `id`
- `company_id` (empresa unica do seed)
- `name`
- `cnpj` (14 digitos, unico)
- `active`

## 7) Recorte tecnico (quando for implementar)

Ordem: dominio, aplicacao, repositorio, tela, testes.

- `UnitPolicy`: nome, normalizacao e digitos do CNPJ, bloqueio de inativacao.
- Casos de uso: criar, editar nome, alterar status, listar (ativas e inativas, com filtro por nome ou CNPJ).
- Repositorio: alem de `listActiveUnits`, incluir busca, insert, update de nome e update de status. Contar setores ativos da unidade.
- Tela no mesmo padrao de setor: lista, formulario, salvar, inativar, reativar, confirmacao na inativacao.
- Eventos: `UNIDADE_CRIADA`, `UNIDADE_EDITADA`, `UNIDADE_INATIVADA`, `UNIDADE_REATIVADA`.
- RBAC: `Admin` e `SESMT`, no mesmo autorizador dos demais cadastros mestres.

## 8) Seguranca e auditoria

- Impacto em login/senha/usuario/papel: nao.
- Eventos auditados: os quatro da secao 7.
- Risco se implementar errado: inativar planta que ainda organiza setor, ou gravar CNPJ duplicado e quebrar o vinculo do setor.

## 9) Documentacao impactada

- `docs/04-uml/cadastros/UC-CAD-01 — Cadastrar empresa e unidade.md`
- `docs/04-uml/casos-de-uso-uml.md`
- `docs/03-operacao/matriz-testes-uc-cad-01.md`
- `docs/03-operacao/backlog-implementacao-mvp.md`
- `docs/01-negocio/unidades-de-implantacao.md` (referencia de negocio; nao vira formulario)
