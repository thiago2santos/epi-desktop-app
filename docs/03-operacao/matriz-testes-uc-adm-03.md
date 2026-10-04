# Matriz executavel de testes - UC-ADM-03 (Parametros)

Spec: `docs/03-operacao/spec-uc-adm-03-parametros.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| ADM03-001 | INT/AUDIT | CA-01, CA-07 | Duas unidades ativas; sem padrao | Gravar a segunda | Barra mostra o nome; lotes abrem nela; auditoria com anterior vazio e a nova |
| ADM03-002 | INT | CA-02, `ADM-001` | Padrao ja gravada | Escolher unidade inativa | `ADM-001`; padrao anterior permanece |
| ADM03-003 | UI | CA-03 | Tela aberta | Tentar editar razao, CNPJ e horario | Campos so leitura |
| ADM03-004 | INT | CA-04 | Lote com disponivel 5 | Trocar a unidade padrao | Disponivel segue 5 |
| ADM03-005 | UI | CA-05 | Nenhuma padrao | Abrir o shell | Barra "Nenhuma unidade padrao." |
| ADM03-006 | RBAC | CA-06 | `SESMT` | Salvar | `AUTH-004` |
