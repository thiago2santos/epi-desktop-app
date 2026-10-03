### UC-CAE-01 — Importar base oficial CAEPI

- **Atores**: Agendador CAEPI (sistema); `Admin` ou `SESMT` para tentativa manual autorizada.
- **Descricao**: Atualiza a base local de consulta de Certificados de Aprovacao usando o arquivo oficial CAEPI, mantendo a aplicacao acessivel e impedindo mutacoes EPI/CA enquanto a carga diaria nao estiver confirmada.
- **Pre-condicoes**:
  - Fonte oficial configurada: `ftp://ftp.mtps.gov.br/portal/fiscalizacao/seguranca-e-saude-no-trabalho/caepi/`.
  - Para tentativa manual, usuario autenticado com permissao.
- **Gatilho**: Execucao diaria agendada ou nova tentativa manual autorizada.
- **Fluxo principal**:
  1. O ator inicia a tentativa e o sistema identifica se foi disparada pelo agendador ou por usuario.
  2. O sistema inicia a tarefa em background e devolve controle imediatamente a interface JavaFX.
  3. A barra de status mostra as fases e o andamento da tarefa enquanto a janela permanece responsiva.
  4. Em background, o sistema baixa e valida o ZIP e o arquivo texto CAEPI, processa todos os registros sem expor dados parciais e publica a nova base atomicamente.
  5. Sistema confirma a publicacao da base e a auditoria de sucesso no mesmo commit, registrando ator, arquivo e quantidade processada.
  6. A UI apresenta o resultado, atualiza a barra de status e remove o banner de pendencia/falha.
- **Fluxos alternativos/excecoes**:
  - Fonte indisponivel, arquivo invalido, formato inesperado ou persistencia falha: sistema preserva a ultima base completa, registra auditoria `FALHA` com motivo e registra detalhes tecnicos nos logs.
  - Falha de importacao ou ciclo diario pendente: app segue acessivel, banner fica visivel, consultas EPI/CA permanecem somente leitura e mutacoes EPI/CA ficam bloqueadas.
  - Falha de auditoria: tentativa nao pode ser considerada sucesso; cadastros EPI/CA permanecem bloqueados.
  - Usuario sem permissao tenta executar a carga: sistema nega o disparo.
  - Nova tentativa manual enquanto outra esta em andamento: nao iniciar concorrente; informar o estado atual pela UI.
- **Regras de UI**: nenhuma operacao de rede, parse ou persistencia pode bloquear a thread JavaFX; progresso percentual so e mostrado quando mensuravel, caso contrario usar indicador indeterminado.
- **Pos-condicoes**:
  - Sucesso: nova base integral disponivel, tentativa auditada e cadastros EPI/CA habilitados para o ciclo vigente.
  - Falha: ultima base completa preservada para consulta, tentativa auditada como falha e mutacoes EPI/CA indisponiveis.
- **Regras de negocio relacionadas**: falha nao pode bloquear acesso geral; carga atomica; uma auditoria por tentativa com ator, resultado e motivo em falha; banner persistente ate sucesso; cadastros fora de EPI/CA nao sao bloqueados.
- **Especificacao**: `docs/03-operacao/spec-uc-cae-01-importar-base-caepi.md`.
- **Matriz de testes**: `docs/03-operacao/matriz-testes-uc-cae-01.md`.
- **Decisoes ainda abertas**: agenda/fuso, comportamento com app fechado, referencia temporal da exportacao, regra de conciliacao de CAs e retencao, conforme secao 12 da especificacao.
