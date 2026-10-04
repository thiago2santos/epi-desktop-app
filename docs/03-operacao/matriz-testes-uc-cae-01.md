# Matriz de Testes - UC-CAE-01 Importar base oficial CAEPI

## Vinculo com a especificacao

- Documento base: `docs/03-operacao/spec-uc-cae-01-importar-base-caepi.md`
- Esta matriz inicial transforma os criterios da secao 8 em cenarios verificaveis.
- As decisoes da secao 12 da spec estao fechadas. Os cenarios abaixo sao executaveis.

## Legenda

- `INT`: integracao de download, validacao, persistencia e servico.
- `AUDIT`: trilha append-only.
- `UI`: estado visivel e disponibilidade dos modulos.
- `RBAC`: autorizacao para disparo manual.

## Cenarios

| ID | Tipo | Referencia na spec | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| CAE01-001 | INT/AUDIT | Regras 1-5, CA-CAE-01/02/04 | Fonte acessivel; arquivo valido e com registros conhecidos | Executar tentativa diaria ate o fim | Base completa publicada atomicamente; auditoria `SUCESSO` com ator `SISTEMA`, arquivo e quantidade processada |
| CAE01-002 | INT/AUDIT/UI | `CAE-001`, `CAE-007`, CA-CAE-03/05/06/07 | Existe carga anterior completa; fonte indisponivel | Executar tentativa diaria e abrir o app/cadastros | Carga anterior preservada para consulta; auditoria `FALHA` com motivo; app acessivel; banner visivel; mutacoes EPI/CA bloqueadas |
| CAE01-003 | INT | `CAE-002`, CA-CAE-03 | Existe carga anterior; ZIP truncado ou sem arquivo interno esperado | Tentar importar o arquivo invalido | Carga rejeitada sem alterar a base publicada; falha explicita |
| CAE01-004 | INT | `CAE-003`, CA-CAE-03 | Existe carga anterior; cabecalho ou delimitador divergente | Tentar processar arquivo com esquema inesperado | Parse interrompido; nenhuma carga parcial publicada; motivo registrado |
| CAE01-005 | INT/AUDIT | `CAE-004`, CA-CAE-03/04 | Base valida; falha provocada durante persistencia/publicacao | Executar importacao | Transacao/estrategia atomica mantem snapshot anterior; auditoria de falha com motivo |
| CAE01-006 | AUDIT | Regras 1-3, CA-CAE-04 | Usuario `SESMT` ou `Admin` autorizado; importacao manual falha | Disparar nova tentativa manual | Evento identifica ator como `USUARIO`, usuario responsavel, resultado `FALHA` e motivo |
| CAE01-007 | AUDIT | `CAE-005`, CA-CAE-04 | Auditoria indisponivel durante a tentativa | Disparar tentativa e simular falha de persistencia da auditoria | Tentativa nao e apresentada como sucesso; logs criticos correlacionaveis; mutacoes EPI/CA permanecem bloqueadas |
| CAE01-008 | RBAC | `CAE-006`, CA-CAE-08 | Usuario `Consulta` ou `Almoxarife` autenticado | Tentar disparar importacao manual | Operacao negada; nenhuma carga iniciada; evento de seguranca conforme politica geral |
| CAE01-009 | UI | Regras 6-8, CA-CAE-05/06/07 | Carga diaria ausente ou falha; usuario autenticado | Navegar para Cadastros, EPI/CA e modulos sem dependencia CAEPI | Banner persistente; mutacoes apenas EPI/CA bloqueadas; consultas EPI/CA em somente leitura; demais modulos acessiveis |
| CAE01-010 | INT/AUDIT/UI | Regras 4/8, CA-CAE-02/04/06/07 | Falha anterior registrada; fonte volta a responder com arquivo valido | Executar nova tentativa autorizada ate o fim | Evento de sucesso e novo snapshot confirmados no mesmo commit; mutacoes EPI/CA liberadas e banner removido |
| CAE01-011 | INT | Secao 12 item 4 | Mesmo numero de CA em duas linhas, uma `EXPIRED` e outra `ACTIVE` | Importar | O indice fica com a linha `ACTIVE`; a variante expirada nao substitui |
| CAE01-012 | UI/INT | Regra 6, CA-CAE-05 | Falha CAEPI; app aberto e operacao em modulo nao relacionado | Usar autenticacao, auditoria e cadastro nao EPI/CA | App permanece funcional sem fechamento ou bloqueio geral |
| CAE01-013 | UI/INT | Regras 11-14, CA-CAE-10 | Importacao com etapas de rede e processamento demoradas | Iniciar importacao e interagir com janela, menu e tela nao relacionada durante download/parse/persistencia | Thread principal permanece responsiva; nenhuma etapa bloqueante executada na thread JavaFX; interacao permitida |
| CAE01-014 | UI | Regra 12, CA-CAE-11 | Importacao em andamento com eventos de progresso controlados | Observar barra de status durante download, validacao e importacao; completar ou falhar a tarefa | Fases e resultado apresentados; indicador indeterminado quando nao houver medida; percentual somente quando mensuravel; mensagem final de falha inclui motivo operacional |
| CAE01-015 | UI/INT | Barra de status, CA-CAE-12 | Uma importacao ja esta em andamento | Solicitar outra tentativa manual | Nenhuma segunda importacao concorrente iniciada; UI informa que a tarefa atual continua em andamento |

## Evidencias esperadas

- arquivo de entrada de teste e identificacao/versionamento;
- quantidade lida, rejeitada e publicada;
- snapshot anterior e posterior para demonstrar atomicidade;
- evento de auditoria para cada tentativa (ator, resultado, motivo);
- logs correlacionados sem credenciais ou segredos;
- evidencias de banner e das permissoes/bloqueios na UI.
- evidencias de responsividade da janela e transicoes da barra de status durante tarefa lenta.
