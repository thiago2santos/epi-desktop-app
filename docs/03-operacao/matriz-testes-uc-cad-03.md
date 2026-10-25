# Matriz executavel de testes - UC-CAD-03 (Senha de recebimento)

Spec: `docs/03-operacao/spec-uc-cad-03-senha-recebimento.md`. O cadastro de matricula, nome, setor, funcao, gestor e status continua coberto por `EmployeePolicyTest` e `EmployeeManagementServiceIntegrationTest` (`CAD-001` a `CAD-007`). Os casos abaixo sao so a senha.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| CAD03-001 | INT/AUDIT | CA-01 | Trabalhador ativo; `SESMT` | Definir senha `capa-77` | Hash gravado; tela "Senha de recebimento definida."; auditoria `SENHA_RECEBIMENTO_DEFINIDA` sem a senha e sem o hash |
| CAD03-002 | INT | CA-02 | Cadastro sem senha | Salvar o trabalhador | Trabalhador existe; fornecimento recusa com `ENT-014` |
| CAD03-003 | INT | CA-03 | Hash ja gravado | Editar nome e deixar a senha em branco | Hash igual; a senha antiga confere no fornecimento |
| CAD03-004 | INT | CA-04 | Hash de `capa-77` | Definir `bota-88` | Hash novo; `capa-77` recusa no fornecimento; `bota-88` confere |
| CAD03-005 | UNIT | CA-05, `CAD-008` | Matricula `000123` | Senha `12345`, `000123` e `123456` | `CAD-008` nas tres; hash intacto |
| CAD03-006 | RBAC | CA-06, `AUTH-004` | Hash ja gravado; `ALMOXARIFE` | Definir outra senha | `AUTH-004`; hash igual |
