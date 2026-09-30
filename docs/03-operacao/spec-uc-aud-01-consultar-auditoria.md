# Spec Curta - UC-AUD-01 Consultar Auditoria

## Identificacao

- ID: `UC-AUD-01`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-REL`
- Responsavel: Time Easy NR6
- Status: baseline implementado para consulta inicial, pronto para iteracoes de filtro avancado

## 1) Contexto

- Problema real:
  - eventos criticos sao gravados, mas sem tela de consulta operacional o key user nao valida rastreabilidade no dia a dia.
- Ator principal:
  - `Admin`, `SESMT`, `Consulta`.
- Impacto se nao resolver:
  - evidencia juridica fica pouco acessivel;
  - validacao de comportamento por QA/key user fica lenta.

## 2) Escopo

- No escopo:
  - listar eventos de auditoria recentes;
  - filtrar por termo livre (acao, entidade, entidade_id, usuario, detalhes);
  - exibir colunas minimas para rastreio (`instante`, `usuario`, `acao`, `entidade`, `entidade_id`, `detalhes`).
- Fora de escopo (nesta iteracao):
  - exportacao PDF/CSV;
  - filtros por intervalo de data com componente dedicado;
  - paginação server-side.

## 3) Regras de negocio

1. Auditoria e append-only: tela nunca edita/exclui registros.
2. Consulta exibe eventos em ordem decrescente (mais novo primeiro).
3. Perfis sem permissao de auditoria nao devem acessar o modulo.
4. Quando nao houver resultado, tela deve exibir estado vazio sem erro.
5. Campos nulos devem ser exibidos como `-` para evitar ambiguidade visual.

## 4) UX/Tela (JavaFX)

- Tela: `Auditoria`.
- Componentes:
  - `TextField` de busca livre;
  - `Button` buscar;
  - `Button` atualizar;
  - `TableView` com colunas:
    - instante;
    - usuario (`id (login)`);
    - acao;
    - entidade;
    - entidade_id;
    - detalhes.
  - label informativo com quantidade encontrada.
- Feedback:
  - erro em vermelho para falha de consulta;
  - sucesso/estado neutro com texto discreto para quantidade.

## 5) Criterios de aceite

- `CA-AUD-01`: modulo `Auditoria` abre tela funcional ao clicar no menu lateral.
- `CA-AUD-02`: listagem retorna eventos reais persistidos na tabela `auditoria`.
- `CA-AUD-03`: filtro livre restringe resultados por termo informado.
- `CA-AUD-04`: ordenacao padrao exibe eventos mais novos primeiro.
- `CA-AUD-05`: sem qualquer acao de update/delete em registros auditados.

## 6) Cenarios de teste (o que e por que)

### Cenario feliz

- **Teste**: abrir modulo e listar eventos apos operacoes criticas.
- **Por que**: valida fim a fim que os eventos gravados sao consultaveis pelo usuario.

### Filtro funcional

- **Teste**: buscar por `EPI_CREATED`, `USUARIO_BLOQUEADO`, `EMPLOYEE_UPDATED`, login e entidade_id.
- **Por que**: garante rastreio rapido por operador e por entidade investigada.

### RBAC

- **Teste**: `Admin/SESMT/Consulta` acessam; perfil sem permissao nao acessa.
- **Por que**: auditoria contem dados sensiveis de operacao.

### Integridade

- **Teste**: confirmar ausencia de botoes/rotas para editar/excluir evento.
- **Por que**: protege premissa juridica de trilha append-only.

### Borda

- **Teste**: termo vazio, termo muito longo e resultado sem linhas.
- **Por que**: evita quebra de UX em uso real de suporte.

## 7) Evolucao recomendada (proxima iteracao)

- filtro por periodo (de/ate);
- filtro por acao e entidade com `ComboBox`;
- exportacao CSV/PDF para evidencias externas;
- mascaramento de dados sensiveis em `detalhes` quando aplicavel.
