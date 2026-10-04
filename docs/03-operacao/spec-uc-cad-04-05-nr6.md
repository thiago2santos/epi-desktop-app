# Spec Curta Normativa - UC-CAD-04/05 (EPI + CA)

## Identificacao

- ID Jira/Issue: a definir
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CAD`
- Responsavel: Time Easy NR6
- Status: refinamento critico concluido, pronto para implementacao incremental

## 1) Contexto

- Problema real:
  - sem cadastro consistente de EPI + CA, o sistema nao consegue sustentar conformidade minima com a NR-6 e nem preparar os fluxos de entrega com validade juridica.
- Ator principal:
  - `SESMT` (com suporte de `Admin` para governanca e manutencao).
- Impacto se nao resolver:
  - bloqueio do DoR de M2;
  - risco de uso de EPI sem CA valido/adequado;
  - fragilidade na trilha de auditoria para fiscalizacao.

## 2) Escopo

- Comportamento no escopo:
  - cadastrar EPI com classificacao normativa (Anexo I);
  - vincular um ou mais CAs ao EPI com dados de situacao/vigencia e evidencia de consulta oficial;
  - ativar/inativar EPI e vinculos de CA sem delete fisico;
  - bloquear inconsistencias normativas no momento do cadastro.
- Fora de escopo:
  - integracao online obrigatoria com CAEPI no MVP;
  - automacao de captura de dados de fabricante por web scraping;
  - decisoes de entrega/lote (ficam para UC-LOT e UC-ENT).

## 3) Base normativa oficial consolidada

### Fonte oficial primaria

- NR-6 vigente (MTE): https://www.gov.br/trabalho-e-emprego/pt-br/acesso-a-informacao/participacao-social/conselhos-e-orgaos-colegiados/comissao-tripartite-partitaria-permanente/normas-regulamentadora/normas-regulamentadoras-vigentes/norma-regulamentadora-no-6-nr-6
- NR-6 consolidada (PDF): https://www.gov.br/trabalho-e-emprego/pt-br/acesso-a-informacao/participacao-social/conselhos-e-orgaos-colegiados/comissao-tripartite-partitaria-permanente/normas-regulamentadora/normas-regulamentadoras-vigentes/nr-06-atualizada-2025.pdf
- Consulta oficial de CA (CAEPI): https://caepi.mte.gov.br/internet/ConsultaCAInternet.aspx
- FAQ oficial EPI/CA (MTE): https://www.gov.br/trabalho-e-emprego/pt-br/assuntos/inspecao-do-trabalho/seguranca-e-saude-no-trabalho/equipamentos-de-protecao-individual-epi/perguntas_e_respostas

### Itens normativos aplicaveis ao UC-CAD-04/05

- NR-6 `6.4.1`: EPI so pode ser posto a venda/utilizado com indicacao de CA.
- NR-6 `6.5.1 (a)`: organizacao adquire somente EPI aprovado.
- NR-6 `6.5.2 (c)`: selecao deve considerar o Anexo I.
- NR-6 `6.9.2.1`: EPI deve ser comercializado com CA valido.
- NR-6 `6.9.2.1.1`: apos aquisicao, observar armazenamento e prazo de validade do fabricante/importador.
- NR-6 `6.9.3`: EPI deve apresentar nome comercial, lote e numero do CA, legiveis/visiveis.
- NR-6 Anexo I: taxonomia oficial de tipos de EPI por protecao.

## 4) Regras de negocio (motor normativo do cadastro)

1. `EPI` exige classificacao obrigatoria conforme grupos do Anexo I.
2. `CA` so pode ser vinculado se houver numero de CA informado e normalizado.
3. Nao permitir conflito de vigencia para o mesmo CA no mesmo EPI (sobreposicao de periodo ativo).
4. Nao permitir ativacao de vinculo de CA com situacao incompatível (`CANCELED`, `SUSPENDED`, `EXPIRED`) no momento da conferencia.
5. Exigir evidencia minima de consulta oficial do CAEPI no ato de vinculacao:
   - data/hora da consulta;
   - operador responsavel;
   - referencia textual da consulta (ex.: protocolo/observacao).
6. Bloquear EPI ativo sem pelo menos um vinculo de CA ativo e coerente.
7. Sem delete fisico para EPI/CA vinculados; usar inativacao com auditoria.

## 5) Catalogo de erros de dominio (CAD-03x)

- `CAD-031` Campos obrigatorios de EPI ausentes.
- `CAD-032` Classificacao de EPI fora do Anexo I.
- `CAD-033` EPI duplicado no escopo definido.
- `CAD-034` Numero de CA invalido/ausente.
- `CAD-035` CA com conflito de vigencia para o mesmo EPI.
- `CAD-036` Situacao do CA impede ativacao do vinculo.
- `CAD-037` Evidencia de consulta oficial do CA ausente.
- `CAD-038` Operacao nao permitida por dependencia historica.
- `CAD-039` Alvo de edicao/inativacao nao encontrado.

## 6) Modelo de dados alvo (MVP)

### EPI

- `id`
- `epi_code` (opcional no MVP, recomendado para rastreio interno)
- `description`
- `annex_group` (A..I)
- `manufacturer_name`
- `active`
- `created_at`
- `updated_at`

### EPI_CA

- `id`
- `epi_id`
- `ca_number`
- `ca_status` (`ACTIVE`, `SUSPENDED`, `CANCELED`, `EXPIRED`)
- `valid_from` (quando aplicavel)
- `valid_until` (quando aplicavel)
- `official_check_at`
- `official_check_note`
- `active`
- `created_at`
- `updated_at`

## 7) UX e telas (JavaFX)

### 7.1 Tela: Cadastro de EPI

- Objetivo:
  - manter cadastro mestre de EPI com classificacao do Anexo I.
- Componentes:
  - `TextField` para `epi_code` (opcional) e `description`;
  - `ComboBox<AnnexGroup>` para `annex_group` (A..I);
  - `TextField` para `manufacturer_name`;
  - `CheckBox` para `active`;
  - `Button` de `Novo`, `Salvar`, `Editar`, `Inativar/Reativar`, `Limpar`;
  - `TableView<EpiRow>` para listagem e selecao.
- Campos e regras de tela:
  - `description` obrigatorio (`CAD-031`);
  - `annex_group` obrigatorio e valido (`CAD-032`);
  - `manufacturer_name` obrigatorio (`CAD-031`);
  - `epi_code` opcional no MVP, mas unico quando informado.
- Busca/lista:
  - filtro por `description`, `annex_group`, `active`;
  - ordenacao por `description` asc por padrao.
- Feedback:
  - sucesso em verde por 5 segundos;
  - erro em vermelho com codigo (`CAD-03x`).

Wireframe textual (macro):

- Barra superior: titulo da tela + acoes globais
- Formulario (esquerda/topo): campos de EPI + botoes de acao
- Lista (direita/baixo): tabela de EPIs cadastrados + status

### 7.2 Tela: Vinculo de CA por EPI

- Objetivo:
  - vincular e manter CAs de um EPI com evidencia oficial.
- Componentes:
  - `ComboBox<EpiOption>` para selecao de EPI;
  - `TextField` para `ca_number`;
  - `ComboBox<CaStatus>` para `ca_status`;
  - `DatePicker` para `valid_from` e `valid_until`;
  - `DateTime` (via `DatePicker` + `TextField` de hora) para `official_check_at`;
  - `TextArea` para `official_check_note`;
  - `Button` **Consultar CAs** abre a lista da ultima carga com sucesso, filtrada no inicio pelo fabricante do EPI e com busca livre;
  - no caminho da base, data/hora, situacao, validade e evidencia vem da carga (`UC-CAE-01`, secao 15) e a tela nao deixa editar esses campos;
  - anexo de print PNG/JPG da consulta online, obrigatorio quando nao ha carga com sucesso ou o numero nao esta na base; opcional quando o CA veio da base;
  - `CheckBox` para `active`;
  - `Button` de `Vincular`, `Atualizar`, `Inativar/Reativar`, `Limpar`;
  - `TableView<EpiCaRow>` para historico/lista de vinculos do EPI.
- Campos e regras de tela:
  - `ca_number` obrigatorio (`CAD-034`);
  - evidencia oficial obrigatoria (`official_check_at`, `official_check_note`) (`CAD-037`);
  - rejeitar notas vagas (`ok`, `consultei`, etc.); aceitar template CAEPI, texto estruturado ou nota curta + anexo de print;
  - nao aceitar periodo com `valid_until < valid_from`;
  - bloquear status impeditivo para ativacao (`CAD-036`);
  - bloquear conflito de vigencia no mesmo EPI (`CAD-035`).
- Feedback:
  - sucesso em verde por 5 segundos;
  - erro em vermelho com codigo (`CAD-03x`).

Wireframe textual (macro):

- Painel superior: seletor de EPI + resumo rapido (descricao/fabricante/status)
- Formulario central: dados do CA + evidencia de consulta
- Tabela inferior: vinculos existentes, vigencia e situacao

### 7.3 Comportamentos UX transversais

- Confirmacao obrigatoria para inativar/reativar EPI e vinculo de CA;
- Sem delete fisico em UI para entidades com historico;
- Botoes de acao desabilitados para perfis sem permissao (reforco visual; regra final no backend);
- Mensagens sempre com codigo rastreavel (`CAD-03x`) para facilitar suporte e teste.

## 8) Criterios de aceite (objetivos)

- `CA-01`: sistema cadastra EPI somente com classificacao valida do Anexo I.
- `CA-02`: sistema permite vincular CA com evidencia minima de consulta oficial registrada.
- `CA-03`: sistema bloqueia vinculo de CA em conflito de vigencia no mesmo EPI.
- `CA-04`: sistema bloqueia ativacao de EPI sem CA ativo coerente.
- `CA-05`: sistema registra auditoria para criar/editar/inativar EPI e para vincular/inativar CA.
- `CA-06`: `SESMT/Admin` permitido; perfis sem autorizacao negados.

## 9) Enum tecnico e traducao para UI

- Enum de dominio recomendado: `CaStatus` com valores canonicos em ingles:
  - `ACTIVE`
  - `SUSPENDED`
  - `CANCELED`
  - `EXPIRED`
- Persistencia:
  - gravar no banco o valor canonico em ingles (`TEXT`), sem acento e sem locale.
- UI:
  - exibir rotulos em pt-BR via recurso do enum (ex.: `toDisplayLabelPtBr()` ou `displayLabel(locale)`), sem alterar o valor persistido.
- Beneficio:
  - evita retrabalho de traducao futura e preserva padrao tecnico unico no codigo.

## 10) Matriz de rastreabilidade norma -> regra -> teste

| Norma | Regra de negocio | Caso de teste minimo |
|---|---|---|
| 6.4.1 + 6.9.2.1 | EPI ativo depende de CA valido/coerente | bloquear ativacao de EPI sem CA ativo |
| 6.5.1(a) | EPI deve ter base de aprovacao documentada | exigir vinculacao de CA para estado operacional |
| 6.5.2(c) + Anexo I | classificacao normativa obrigatoria | rejeitar EPI com grupo fora de A..I |
| 6.9.2.1.1 | registrar dados para decisao sobre uso apos aquisicao | validar campos de vigencia/situacao do CA |
| 6.9.3 | rastreabilidade de CA/lote no cadastro | validar presenca de numero CA + metadados obrigatorios |

## 11) Plano de testes (pre-codigo)

Matriz executavel vinculada: `docs/03-operacao/matriz-testes-uc-cad-04-05.md`

### Unitarios (policy)
- validar obrigatoriedade e normalizacao de campos EPI;
- validar taxonomia do Anexo I;
- validar regra de conflito de vigencia de CA;
- validar bloqueio por status de CA.

### Integracao (service + jdbc)
- fluxo feliz de criar EPI e vincular CA com evidencia;
- duplicidade e conflito de vigencia;
- inativacao com dependencia historica;
- auditoria persistida dos eventos criticos.

### Seguranca/RBAC
- `ADMIN` e `SESMT` podem operar;
- `CONSULTA` e `ALMOXARIFE` nao podem alterar cadastro mestre de EPI/CA.

## 12) Recorte tecnico de entrada (baixo retrabalho)

- `domain`:
  - `EpiPolicy`
  - `CaPolicy`
- `application/usecase`:
  - `CreateEpiUseCase`
  - `UpdateEpiUseCase`
  - `SetEpiStatusUseCase`
  - `BindCaToEpiUseCase`
  - `UpdateCaBindingUseCase`
  - `SetCaBindingStatusUseCase`
  - `ListEpiUseCase`
  - `ListCaByEpiUseCase`
- `application/port`:
  - `EpiRepository`
- `infra/jdbc`:
  - `JdbcEpiRepository`
- `audit`:
  - `EPI_CREATED`, `EPI_UPDATED`, `EPI_DEACTIVATED`, `EPI_REACTIVATED`
  - `EPI_CA_BOUND`, `EPI_CA_UPDATED`, `EPI_CA_DEACTIVATED`, `EPI_CA_REACTIVATED`

## 13) Criterio de saida do refinamento

- base normativa congelada e referenciada;
- regras/testes rastreaveis aprovados;
- catalogo de erros fechado;
- pronto para implementacao incremental em commits `feat` / `test` / `docs`.
