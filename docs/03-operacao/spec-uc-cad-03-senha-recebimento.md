# Spec curta - UC-CAD-03 (Senha de recebimento)

## Identificacao

- ID: `UC-CAD-03`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CAD`
- Responsavel: Time Easy NR6
- Status: implementado no cadastro de trabalhador. O hash Argon2 fica em `employee.senha_recebimento_hash`. A auditoria e `SENHA_RECEBIMENTO_DEFINIDA`.
- Norma: NR-6 `6.5.1 (d)`. A senha e o meio desta versao para o trabalhador autenticar o recebimento no `UC-ENT-02`. Nao cria login.
- UML: `docs/04-uml/cadastros/UC-CAD-03 — Cadastrar empregado (trabalhador).md`

## 1) Contexto

- Problema real:
  - a ficha nao pode nascer so com o almoxarife marcando que o trabalhador recebeu. Quem recebe precisa provar que e ele, no balcao.
- Ator principal: `SESMT`. Tambem `Admin`.
- Impacto se nao resolver:
  - o `UC-ENT-01` continua aceitando fornecimento sem o trabalhador.

## 2) Escopo

- Comportamento no escopo:
  - gravar, no trabalhador, o hash de uma senha de recebimento;
  - mostrar se a senha ja existe, sem mostrar o valor;
  - substituir o hash quando o SESMT define outra senha.
- Fora de escopo:
  - usuario, papel, sessao ou login do trabalhador;
  - a politica de 12 caracteres do usuario do sistema;
  - biometria, facial e assinatura ICP-Brasil;
  - o almoxarife definir, ver ou resetar a senha;
  - apagar a senha. So se substitui;
  - coluna de senha no `UC-CAD-IMP-01`;
  - parametro no `UC-ADM-03`.

## 3) Regras de negocio

1. A senha nao e credencial de sistema. O trabalhador continua sem login. O hash fica no cadastro dele e so o `UC-ENT-01` confere, na confirmacao da ficha.
2. Trabalhador pode existir sem senha. Sem hash, o fornecimento recusa com `ENT-014`.
3. Quem define e quem substitui: `SESMT` e `Admin`. `Almoxarife`, `Consulta` e `Gestor` recebem `AUTH-004`.
4. No cadastro novo, o campo e opcional. Preenchido, o hash nasce na mesma transacao do trabalhador. Vazio, o trabalhador grava e ainda nao recebe EPI.
5. Na edicao, campo vazio nao apaga o hash. Campo preenchido substitui. Nao ha acao de remover.
6. Politica, distinta da senha de usuario: de 6 a 64 caracteres; depois do trim, nao pode ser igual a matricula, sem diferenciar maiusculas; nao pode ser `123456`, `000000`, `111111`, `senha` ou `password`. Fora disso, `CAD-008`.
7. O que se guarda e o hash Argon2, o mesmo hasher das credenciais de usuario. A senha em claro nao entra em auditoria, log, ficha nem PDF.
8. Auditoria: `SENHA_RECEBIMENTO_DEFINIDA`, entidade `EMPLOYEE`, na mesma transacao do hash. O detalhe e "Senha de recebimento definida." Sem o segredo, sem o hash.

## 4) Catalogo de erros

| Codigo | Quando | Texto de tela |
|---|---|---|
| `CAD-008` | Senha curta, longa, igual a matricula ou sequencia obvia | A senha de recebimento precisa de 6 a 64 caracteres e nao pode ser a matricula nem uma sequencia obvia. |
| `CAD-006` | Trabalhador alvo inexistente | Trabalhador nao encontrado. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para definir a senha de recebimento. |

## 5) Criterios de aceite

- `CA-01`: SESMT define a senha no cadastro. O trabalhador fica com hash. A tela mostra "Senha de recebimento definida." A auditoria nao contem a senha nem o hash.
- `CA-02`: cadastro novo sem senha grava o trabalhador. O fornecimento desse trabalhador recusa com `ENT-014`.
- `CA-03`: editar o trabalhador e deixar o campo vazio conserva o hash. A senha anterior ainda confere no fornecimento.
- `CA-04`: substituir a senha troca o hash. A senha anterior recusa no fornecimento. A nova confere.
- `CA-05`: senha com 5 caracteres, igual a matricula, ou `123456`, recusa com `CAD-008` e nao grava hash.
- `CA-06`: almoxarife recebe `AUTH-004` e o hash nao muda.

## 6) Tela

Destino ja previsto: cadastro de trabalhador. Sem codigo de caso de uso no titulo.

- Campo mascarado "Senha de recebimento", com tooltip "O trabalhador digita esta senha no balcao. Ela nao abre o sistema."
- Ao lado, o estado: "Sem senha de recebimento." ou "Senha de recebimento definida."
- Salvar o cadastro com o campo preenchido grava o hash. Vazio, na edicao, nao mexe na senha.
- Sucesso da senha: "Senha de recebimento definida." Faixa na propria tela.
- Codigo `CAD-` nao aparece. A lista de trabalhadores nao mostra senha nem hash.

## 7) Seguranca e auditoria

- Login/senha de sistema: nao.
- Evento: `SENHA_RECEBIMENTO_DEFINIDA`.
- Risco: gravar a senha em claro, deixa-la no log, ou permitir que o almoxarife escolha uma senha que ele mesmo conhece.

## 8) Documentacao impactada

- `docs/03-operacao/matriz-testes-uc-cad-03.md`
- `docs/03-operacao/spec-uc-ent-01-fornecimento.md`
- `docs/03-operacao/spec-uc-ent-02-termo.md`
- `docs/03-operacao/spec-uc-cad-imp-01-importacao-csv-cadastros.md`
- `docs/04-uml/cadastros/UC-CAD-03 — Cadastrar empregado (trabalhador).md`
