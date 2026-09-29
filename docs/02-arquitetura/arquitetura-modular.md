# Arquitetura modular do sistema de EPI (documento vivo)

## Objetivo

Definir a arquitetura pratica do sistema para permitir:

- entrega rapida no piloto (Itupeva);
- evolucao segura para multiusuario;
- escalabilidade para 8 unidades sem reescrita do dominio.

Este documento deve ser atualizado conforme o projeto evoluir.

## Principios arquiteturais

1. **Dominio primeiro**: regras NR-6 e regras operacionais ficam no dominio, nao na tela.
2. **Imutabilidade juridica**: entrega/devolucao nao sofrem alteracao destrutiva; erro vira estorno.
3. **Auditoria append-only**: toda acao sensivel e rastreavel.
4. **Segregacao por unidade**: dados operacionais com `unidade_id`.
5. **Evolucao por adaptadores**: trocar infraestrutura sem quebrar casos de uso.

## Visao macro

```text
[JavaFX UI]
     |
     v
[Application Use Cases]
     |
     v
[Domain Rules/Models]
     |
     v
[Infrastructure: SQLite/Postgres, Jasper, PDFBox, Auth, Audit]
```

## Estrutura sugerida do repositorio

```text
epi/
  docs/
    ARQUITETURA_MODULAR.md
    PLANO.md
    CATADAO_DEFINICOES.md
    STACK_E_ARQUITETURA.md
    MANUAL_OPERACIONAL_V0.md
    modelagem_fluxo_epi/
  app/
    pom.xml
    src/main/java/br/com/empresa/epi/
      bootstrap/
      config/
      shared/
        auth/
        audit/
        exceptions/
        time/
      modules/
        cadastros/
        lotes_estoque/
        matriz/
        entrega/
        pos_entrega/
        relatorios/
        admin/
      ui/
    src/main/resources/
      db/migration/
      reports/
      i18n/
    src/test/java/
  installer/
    windows/jpackage/
```

## Padrao interno por modulo

Cada modulo segue o mesmo formato:

```text
modules/<modulo>/
  domain/
    model/
    rules/
    ports/
  application/
    usecases/
    dto/
  infrastructure/
    persistence/
    mappers/
```

- `domain/ports`: interfaces para persistencia/servicos externos.
- `application/usecases`: orquestracao transacional.
- `infrastructure/persistence`: implementacao concreta (SQLite agora, Postgres depois).

## Mapa de modulos e responsabilidades

## 1) Modulo `cadastros`

**Responsabilidade**
- Cadastros mestres: empresa, unidade, setor, funcao, trabalhador, EPI, CA.

**Entidades centrais**
- `Empresa`, `Unidade`, `Setor`, `Funcao`, `Trabalhador`, `Epi`, `EpiCa`.

**Casos de uso (exemplos)**
- `CadastrarTrabalhador`
- `CadastrarEpi`
- `VincularCaAoEpi`
- `InativarCadastro`

**Tabelas principais**
- `empresa`, `unidade`, `setor`, `funcao`, `trabalhador`, `epi`, `epi_ca`.

**Dependencias**
- Fornece dados para `matriz`, `lotes_estoque` e `entrega`.

## 2) Modulo `lotes_estoque`

**Responsabilidade**
- Recebimento por lote, controle de saldo, validade da peca e custo.

**Entidades centrais**
- `LoteEpi`, `MovimentoLote` (opcional para historico detalhado).

**Casos de uso**
- `RegistrarRecebimentoLote`
- `ConsultarSaldoLote`
- `BloquearLoteVencido`

**Tabelas**
- `lote_epi` (+ opcional `movimento_lote`).

**Dependencias**
- Consumido por `entrega` e `relatorios`.

## 3) Modulo `matriz`

**Responsabilidade**
- Definir obrigatoriedade por funcao/GHE, periodicidade e vigencia.

**Entidades**
- `MatrizFuncaoEpi`, `ParametroPeriodicidade`.

**Casos de uso**
- `DefinirMatrizFuncaoEpi`
- `AtualizarVigenciaMatriz`
- `CalcularPendenciaDeReposicao` (consulta)

**Tabelas**
- `matriz_funcao_epi`, `parametro_periodicidade`, `ghe` (quando aplicavel).

**Dependencias**
- Fornece regra para `entrega` e leitura para `relatorios`.

## 4) Modulo `entrega` (core)

**Responsabilidade**
- Registrar entrega legal imutavel com vinculo lote/CA e ciencia do trabalhador.

**Entidades**
- `Entrega`, `EntregaItem`, `EntregaItemCa`, `TermoResponsabilidadeAceite`.

**Casos de uso**
- `RegistrarEntrega`
- `ConsultarEntregaPorTrabalhador`

**Tabelas**
- `entrega_epi`, `entrega_epi_item`, `entrega_item_ca`, `termo_responsabilidade_aceite`.

**Regras criticas**
- lote valido;
- saldo suficiente;
- CA registrado;
- excecao fora da matriz com justificativa;
- transacao unica com auditoria.

## 5) Modulo `pos_entrega`

**Responsabilidade**
- Devolucao, descarte, estorno e pendencias de retorno.

**Entidades**
- `DevolucaoEpiItem`, `EntregaEstorno`.

**Casos de uso**
- `RegistrarDevolucao`
- `RegistrarEstornoEntrega`
- `ListarPendenciasDevolucao`

**Tabelas**
- `devolucao_epi_item`, `entrega_estorno`.

**Dependencias**
- Usa `entrega` e alimenta `relatorios`.

## 6) Modulo `relatorios`

**Responsabilidade**
- Gerar evidencias juridicas e visoes gerenciais.

**Entidades/Projecoes**
- `FichaTrabalhadorPeriodo`
- `HistoricoEpiCaLote`
- `CoberturaTrabalhador`
- `ConsumoUnidadeSetorFuncao`

**Casos de uso**
- `GerarFichaTrabalhadorPeriodo`
- `GerarHistoricoEpiCaLote`
- `GerarRelatorioCobertura`
- `GerarRelatorioConsumo`

**Tecnologias**
- JasperReports para layout e geracao de relatorio.
- PDFBox para pos-processamento (merge/carimbo/anexo).

## 7) Modulo `admin`

**Responsabilidade**
- Usuarios, papeis, parametros gerais, rotinas de suporte/backup.
- Ciclo de vida de credenciais (criacao, reset, troca obrigatoria inicial e politicas de bloqueio).

**Entidades**
- `Usuario`, `Papel`, `UsuarioPapel`, `ParametroSistema`.
- `EstadoCredencial` (modelagem de estado para troca obrigatoria e bloqueio temporario).

**Casos de uso**
- `CadastrarUsuario`
- `AtribuirPapel`
- `AtualizarParametroSistema`
- `ResetarCredencialUsuario`

**Tabelas**
- `usuario`, `papel`, `usuario_papel`, `parametro_sistema`.
- Campos/estrutura de credencial no `usuario` (estado de troca obrigatoria, tentativas invalidas, janela de bloqueio), conforme evolucao de schema.

**Status arquitetural implementado (identidade)**
- Portas explicitas de identidade: `IdentityRepository`, `CredentialHasher`, `Clock`.
- Casos de uso aplicacionais para autenticacao e gestao administrativa de credencial/papel.
- Adaptadores de infraestrutura para JDBC local, Argon2 e relogio de sistema.
- Fachadas de compatibilidade mantidas (`AuthenticationProvider`, `CredentialManager`) para nao quebrar UI.
- Regra de negocio de credencial e autorizacao movida para camada de use case (fora da fachada JDBC).

## Componentes compartilhados (`shared`)

- `auth`: autenticacao e autorizacao por papel (RBAC), politica de senha e verificacao de estado de credencial.
- `audit`: log append-only (`auditoria`).
- `exceptions`: erros de negocio padronizados.
- `time`: relogio de sistema para padronizar timestamps e testes.

## Fluxo entre modulos (alto nivel)

```text
cadastros ----> matriz -------\
    |            |             \
    |            v              \
    +-------> lotes_estoque ---> entrega ---> pos_entrega ---> relatorios
                                      \-------------> shared/audit
```

## Fronteiras transacionais (importante)

- `RegistrarEntrega`:
  - grava `entrega` + `entrega_item` + `entrega_item_ca` + `termo`
  - baixa saldo do lote
  - grava auditoria
  - tudo no mesmo commit

- `RegistrarDevolucao`:
  - grava devolucao
  - atualiza visao operacional de pendencia (quando aplicavel)
  - grava auditoria

- `RegistrarEstornoEntrega`:
  - grava estorno
  - grava auditoria
  - nunca apaga evento original

## Fronteiras transacionais para credenciais (importante)

- `CadastrarUsuario`:
  - valida politica de senha
  - valida unicidade de login
  - grava usuario e estado de troca obrigatoria inicial
  - grava auditoria do evento
  - commit unico

- `ResetarCredencialUsuario`:
  - valida autorizacao de Admin
  - grava novo hash de credencial
  - reativa troca obrigatoria no proximo login
  - grava auditoria do reset
  - commit unico

- `AutenticarUsuario`:
  - valida hash da senha
  - atualiza contadores de tentativas invalidas/quebra de bloqueio
  - avalia bloqueio temporario e estado de troca obrigatoria
  - registra eventos criticos auditaveis (quando aplicavel)

## Modelo de dados: diretrizes

1. Tabelas operacionais com `unidade_id`.
2. `created_at`, `created_by` em eventos sensiveis.
3. Dominios controlados para motivos e metodos de validacao.
4. Indices por `unidade_id`, `trabalhador_id`, `data_evento`.
5. Triggers/bloqueios para invariantes juridicos (imutabilidade, datas).

## Evolucao tecnica planejada

## Fase A — Piloto local (Itupeva)
- JavaFX + Spring sem web + SQLite.
- Um banco por unidade piloto.
- Relatorios locais exportaveis.

## Fase B — Multiusuario controlado
- Backend Spring Boot com API.
- Cliente JavaFX consumindo API.
- Banco Postgres central por ambiente.

## Fase C — 8 unidades (distribuido)
- Operacao concorrente multiunidade.
- Consolidacao corporativa por `unidade_id`.
- Governanca central de backup, observabilidade e seguranca.

## Contratos que nao podem quebrar

Mesmo mudando de infraestrutura, manter:

- semantica de entrega imutavel;
- estorno como mecanismo oficial de correcao;
- trilha de auditoria completa;
- identificacao de unidade em todo evento operacional;
- relatorios legais reprodutiveis por recorte.

## Checkpoints de atualizacao deste documento

Atualizar este arquivo quando houver:

1. novo modulo ou desmembramento de modulo;
2. mudanca de fronteira transacional;
3. mudanca de schema relevante;
4. mudanca de estrategia de distribuicao;
5. migracao de armazenamento (SQLite -> Postgres).

## Estado atual

- Estrategia geral aprovada e em execucao incremental.
- Fundacao tecnica (R0/M0) operacional com login, RBAC, auditoria append-only e ciclo de credenciais.
- Arquitetura de identidade desacoplada concluida no eixo `ports + use cases + adapters` sem quebra da UI.
- Proximo foco de entrega: M1 (cadastros essenciais e base de dados operacional para liberar M2).
