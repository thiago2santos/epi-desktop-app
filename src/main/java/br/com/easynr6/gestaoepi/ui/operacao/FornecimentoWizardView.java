package br.com.easynr6.gestaoepi.ui.operacao;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.issuance.application.FornecimentoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.Pagina;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.Trabalhador;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarFornecimentoUseCase.LoteVisivel;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarFornecimentoUseCase.SaidaContexto;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.ItemParaRegistrar;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.Pedido;
import br.com.easynr6.gestaoepi.modules.issuance.domain.FornecimentoPolicy;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.PeriodicidadeManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.LerCoberturaUseCase.LeituraCobertura;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/** UC-ENT-01. Cinco passos e um diálogo. Cancelar o diálogo não grava. */
public final class FornecimentoWizardView {

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final List<String> PASSOS =
      List.of("Trabalhador", "Selecionar EPIs", "Lote e quantidade", "Ciência e termo", "Revisão");

  private final UsuarioAutenticado usuario;
  private final FornecimentoManagementService fornecimento;
  private final PeriodicidadeManagementService periodicidade;
  private final EmployeeManagementService empregados;
  private final VBox corpo = new VBox(12);
  private final Label feedback = new Label();
  private final List<Rascunho> rascunhos = new ArrayList<>();
  private final Node root;
  private final TextField busca = new TextField();
  private final CheckBox somenteAtivos = new CheckBox("Só ativos");
  private final ComboBox<DepartmentOption> setor = new ComboBox<>();
  private final TableView<Trabalhador> tabela = new TableView<>();
  private final CheckBox foraDaMatriz = new CheckBox("Fora da matriz");
  private final TableView<LinhaEpi> tabelaEpis = new TableView<>();
  private final TableView<Rascunho> tabelaLotes = new TableView<>();
  private final Map<Long, List<LoteVisivel>> lotesPorEpi = new HashMap<>();
  private final FornecimentoPolicy regras = new FornecimentoPolicy();
  private final Pagination paginacao = new Pagination(1, 0);
  private final Label vazioBusca = new Label(MensagensFornecimento.BUSCA_VAZIA);
  private final Node painelBusca;
  private Button proximo;
  private boolean ajustandoPagina;
  private boolean episConfigurados;
  private boolean lotesConfigurados;
  private SaidaContexto contexto;
  private long selecionadoId;
  private int passo;
  private Trabalhador trabalhador;
  private String senhaDigitada = "";

  public FornecimentoWizardView(
      UsuarioAutenticado usuario,
      FornecimentoManagementService fornecimento,
      PeriodicidadeManagementService periodicidade,
      EmployeeManagementService empregados) {
    this.usuario = usuario;
    this.fornecimento = fornecimento;
    this.periodicidade = periodicidade;
    this.empregados = empregados;
    ReferenciaPage page = ReferenciaPage.of("", "Registrar fornecimento", "");
    page.section(corpo);
    this.painelBusca = montarBusca();
    this.root = ReferenciaPage.scroll(page);
    mostrar();
  }

  public Node root() {
    return root;
  }

  static Integer quantidade(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String texto = raw.trim();
    if (texto.chars().anyMatch(caractere -> caractere < '0' || caractere > '9')) {
      return null;
    }
    try {
      int valor = Integer.parseInt(texto);
      return valor > 0 ? valor : null;
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private void mostrar() {
    feedback.setWrapText(true);
    Node quem = linhaDoTrabalhador();
    Node miolo = conteudo();
    if (passo == 4) {
      corpo.getChildren().setAll(faixaDePassos(), miolo);
    } else if (passo == 3) {
      if (quem == null) {
        corpo.getChildren().setAll(faixaDePassos(), miolo);
      } else {
        corpo.getChildren().setAll(faixaDePassos(), quem, miolo);
      }
    } else if (quem == null) {
      corpo.getChildren().setAll(faixaDePassos(), miolo, acoes());
    } else {
      corpo.getChildren().setAll(faixaDePassos(), quem, miolo, acoes());
    }
    corpo.getChildren().add(feedback);
  }

  private Node faixaDePassos() {
    HBox faixa = new HBox();
    faixa.setAlignment(Pos.TOP_CENTER);
    faixa.setMaxWidth(Double.MAX_VALUE);
    for (int i = 0; i < PASSOS.size(); i++) {
      if (i > 0) {
        faixa.getChildren().add(conector(i <= passo));
      }
      faixa.getChildren().add(passoVisual(i));
    }
    return faixa;
  }

  private Node passoVisual(int indice) {
    Label numero = new Label(Integer.toString(indice + 1));
    numero.getStyleClass().add(Enr6Styles.STEP_INDEX);
    Label titulo = new Label(PASSOS.get(indice));
    titulo.getStyleClass().add(Enr6Styles.STEP_TITLE);
    titulo.setWrapText(true);
    titulo.setAlignment(Pos.CENTER);
    titulo.setTextAlignment(TextAlignment.CENTER);
    VBox bloco = new VBox(4, numero, titulo);
    bloco.setAlignment(Pos.TOP_CENTER);
    bloco.getStyleClass().add(Enr6Styles.STEP);
    if (indice < passo) {
      bloco.getStyleClass().add(Enr6Styles.STEP_DONE);
    } else if (indice == passo) {
      bloco.getStyleClass().add(Enr6Styles.STEP_CURRENT);
    }
    return bloco;
  }

  private Node conector(boolean feito) {
    Region linha = new Region();
    linha.getStyleClass().add(Enr6Styles.STEP_LINE);
    linha.setMaxWidth(Double.MAX_VALUE);
    if (feito) {
      linha.getStyleClass().add(Enr6Styles.STEP_LINE_DONE);
    }
    VBox caixa = new VBox(linha);
    caixa.setPadding(new Insets(13, 8, 0, 8));
    HBox.setHgrow(caixa, Priority.ALWAYS);
    return caixa;
  }

  private Node linhaDoTrabalhador() {
    if (trabalhador == null) {
      return null;
    }
    return new Label(trabalhador.nome() + " · " + detalhe(trabalhador));
  }

  private Node conteudo() {
    return switch (passo) {
      case 0 -> passoTrabalhador();
      case 1 -> passoItens();
      case 2 -> passoLotes();
      case 3 -> passoTermo();
      default -> passoRevisao();
    };
  }

  private Node passoTrabalhador() {
    return painelBusca;
  }

  private Node montarBusca() {
    busca.setPromptText("Matrícula ou nome");
    dica(busca, MensagensFornecimento.DICA_BUSCA);
    dica(somenteAtivos, MensagensFornecimento.DICA_ATIVOS);
    busca.textProperty().addListener((obs, anterior, atual) -> recarregarDoInicio());
    somenteAtivos.selectedProperty().addListener((obs, anterior, atual) -> recarregarDoInicio());
    configurarSetor();
    HBox.setHgrow(busca, Priority.ALWAYS);
    configurarTabela();
    paginacao.getStyleClass().add(Enr6Styles.PAGINATION);
    paginacao.setMaxPageIndicatorCount(8);
    paginacao.setPageFactory(indice -> paginaVazia());
    paginacao.skinProperty().addListener((obs, antigo, atual) -> esconderIndiceTextual());
    paginacao
        .currentPageIndexProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (!ajustandoPagina) {
                carregarPagina();
              }
            });
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(320);
    HBox filtro = new HBox(8, busca, setor, somenteAtivos);
    filtro.setAlignment(Pos.CENTER_LEFT);
    VBox painel = new VBox(8, filtro, tabela, paginacao);
    carregarPagina();
    return painel;
  }

  private void configurarSetor() {
    setor.setPromptText("Todos os setores");
    setor.setPrefWidth(240);
    dica(setor, MensagensFornecimento.DICA_SETOR);
    setor.setConverter(
        nomes(item -> item == null ? "Todos os setores" : item.unitName() + " · " + item.name()));
    setor.getItems().add(null);
    try {
      setor
          .getItems()
          .addAll(empregados.listActiveDepartments().stream().filter(this::daSessao).toList());
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("LISTAR_SETORES_FORNECIMENTO", usuario.id(), "-", ex);
    }
    setor.setValue(null);
    setor.valueProperty().addListener((obs, anterior, atual) -> recarregarDoInicio());
  }

  private void recarregarDoInicio() {
    if (paginacao.getCurrentPageIndex() != 0) {
      ajustandoPagina = true;
      paginacao.setCurrentPageIndex(0);
      ajustandoPagina = false;
    }
    carregarPagina();
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazioBusca);
    tabela.setRowFactory(
        tabela ->
            new TableRow<>() {
              @Override
              protected void updateItem(Trabalhador item, boolean vazio) {
                super.updateItem(item, vazio);
                getStyleClass().remove(Enr6Styles.ROW_INATIVO);
                if (!vazio && item != null && !item.ativo()) {
                  getStyleClass().add(Enr6Styles.ROW_INATIVO);
                }
              }
            });
    tabela
        .getColumns()
        .setAll(
            List.of(
                coluna("Matrícula", Trabalhador::matricula),
                coluna("Nome", Trabalhador::nome),
                coluna("Setor", item -> item.unidade() + " · " + item.setor()),
                coluna("Função", Trabalhador::funcao),
                coluna("Gestor", item -> item.gestor() == null ? "" : item.gestor()),
                coluna("Status", item -> item.ativo() ? "Ativo" : "Inativo")));
    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, anterior, atual) -> selecionar(atual));
    tabela.setOnMouseClicked(
        event -> {
          Trabalhador linha = tabela.getSelectionModel().getSelectedItem();
          if (event.getButton() == MouseButton.PRIMARY
              && event.getClickCount() == 2
              && linha != null) {
            escolher(linha);
          }
        });
    tabela.setOnKeyPressed(
        event -> {
          Trabalhador linha = tabela.getSelectionModel().getSelectedItem();
          if (event.getCode() == KeyCode.ENTER && linha != null) {
            escolher(linha);
          }
        });
  }

  private void carregarPagina() {
    String texto = busca.getText() == null ? "" : busca.getText().trim();
    limparFeedback();
    try {
      Pagina pagina =
          fornecimento.buscar(
              usuario.id(),
              texto,
              paginacao.getCurrentPageIndex(),
              somenteAtivos.isSelected(),
              setorId(),
              usuario.unidadeId());
      tabela.getItems().setAll(pagina.itens());
      if (pagina.total() == 0) {
        vazioBusca.setText(
            somenteAtivos.isSelected()
                ? MensagensFornecimento.BUSCA_VAZIA_ATIVOS
                : MensagensFornecimento.BUSCA_VAZIA);
      }
      ajustarPaginas(pagina.paginas());
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("BUSCAR_TRABALHADOR", usuario.id(), texto, ex);
      avisar(MensagensFornecimento.erro(ex));
    }
  }

  private Long setorId() {
    DepartmentOption escolhido = setor.getValue();
    return escolhido == null ? null : escolhido.id();
  }

  private boolean daSessao(DepartmentOption setor) {
    return usuario.unidadeId() == null || usuario.unidadeId().equals(setor.unitId());
  }

  private void ajustarPaginas(int paginas) {
    int destino = Math.max(1, paginas);
    if (paginacao.getPageCount() == destino) {
      return;
    }
    ajustandoPagina = true;
    paginacao.setPageCount(destino);
    ajustandoPagina = false;
  }

  private void selecionar(Trabalhador candidato) {
    if (candidato == null || !candidato.ativo()) {
      trabalhador = null;
      if (candidato != null) {
        avisarAviso(MensagensFornecimento.TRABALHADOR_INATIVO);
      }
    } else {
      if (selecionadoId != candidato.id()) {
        rascunhos.clear();
        senhaDigitada = "";
        selecionadoId = candidato.id();
        foraDaMatriz.setSelected(false);
        contexto = null;
        lotesPorEpi.clear();
      }
      trabalhador = candidato;
      limparFeedback();
    }
    if (proximo != null && passo == 0) {
      proximo.setDisable(trabalhador == null);
    }
  }

  private void escolher(Trabalhador candidato) {
    if (candidato == null || !candidato.ativo()) {
      selecionar(candidato);
      return;
    }
    selecionar(candidato);
    passo = 1;
    limparFeedback();
    mostrar();
  }

  private static void dica(Node alvo, String texto) {
    Tooltip tooltip = new Tooltip(texto);
    tooltip.setWrapText(true);
    tooltip.setMaxWidth(280);
    Tooltip.install(alvo, tooltip);
  }

  private void esconderIndiceTextual() {
    paginacao.applyCss();
    Node indice = paginacao.lookup(".page-information");
    if (indice == null) {
      return;
    }
    indice.setVisible(false);
    indice.setManaged(false);
  }

  private static Region paginaVazia() {
    Region region = new Region();
    region.setMinHeight(0);
    region.setPrefHeight(0);
    region.setMaxHeight(0);
    return region;
  }

  private static TableColumn<LinhaEpi, String> colunaEpi(
      String titulo, Function<LinhaEpi, String> valor) {
    TableColumn<LinhaEpi, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    column.setSortable(false);
    return column;
  }

  private static TableColumn<Trabalhador, String> coluna(
      String titulo, Function<Trabalhador, String> valor) {
    TableColumn<Trabalhador, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }

  private Node passoItens() {
    configurarEpis();
    preencherEpis();
    VBox.setVgrow(tabelaEpis, Priority.ALWAYS);
    tabelaEpis.setPrefHeight(320);
    return new VBox(8, foraDaMatriz, tabelaEpis);
  }

  private void configurarEpis() {
    if (episConfigurados) {
      return;
    }
    foraDaMatriz.setOnAction(event -> preencherEpis());
    dica(foraDaMatriz, MensagensFornecimento.DICA_FORA);
    dica(tabelaEpis, MensagensFornecimento.DICA_EPIS);
    tabelaEpis.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabelaEpis.setPlaceholder(new Label(MensagensFornecimento.FUNCAO_SEM_EPI));
    tabelaEpis.setRowFactory(
        tabela ->
            new TableRow<>() {
              @Override
              protected void updateItem(LinhaEpi item, boolean vazio) {
                super.updateItem(item, vazio);
                getStyleClass().remove(Enr6Styles.ROW_FORA);
                if (!vazio && item != null && !item.naMatriz()) {
                  getStyleClass().add(Enr6Styles.ROW_FORA);
                }
              }
            });
    TableColumn<LinhaEpi, LinhaEpi> marca = new TableColumn<>();
    marca.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    marca.setCellFactory(coluna -> new MarcaCell());
    marca.setSortable(false);
    marca.setMinWidth(48);
    marca.setMaxWidth(48);
    TableColumn<LinhaEpi, String> epi = colunaEpi("EPI", LinhaEpi::nome);
    TableColumn<LinhaEpi, String> matriz =
        colunaEpi("Matriz", linha -> linha.naMatriz() ? "Sim" : "Não");
    TableColumn<LinhaEpi, LinhaEpi> situacao = new TableColumn<>("Situação");
    situacao.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    situacao.setCellFactory(coluna -> new SituacaoCell());
    situacao.setSortable(false);
    tabelaEpis.getColumns().setAll(List.of(marca, epi, matriz, situacao));
    tabelaEpis
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (atual.intValue() >= 0) {
                tabelaEpis.getSelectionModel().clearSelection();
              }
            });
    episConfigurados = true;
  }

  private void preencherEpis() {
    if (trabalhador == null) {
      return;
    }
    try {
      Map<Long, String> situacoes = new HashMap<>();
      for (LeituraCobertura leitura : periodicidade.cobertura(trabalhador.id())) {
        situacoes.put(leitura.epiId(), leitura.situacao());
      }
      List<LinhaEpi> linhas = new ArrayList<>();
      for (LinhaMatriz matriz : fornecimento.itensDaFuncao(usuario.id(), trabalhador.id())) {
        linhas.add(
            new LinhaEpi(
                matriz.epiId(),
                matriz.epi(),
                true,
                situacoes.getOrDefault(matriz.epiId(), ""),
                matriz.exigeTreinamento(),
                matriz.modo() == ModoMatriz.POSTO));
      }
      if (foraDaMatriz.isSelected() || temForaSelecionado()) {
        for (EpiOpcao epi : fornecimento.episForaDaMatriz(usuario.id(), trabalhador.id())) {
          if (foraDaMatriz.isSelected() || contem(epi.id())) {
            linhas.add(new LinhaEpi(epi.id(), epi.descricao(), false, "", false, false));
          }
        }
      }
      tabelaEpis.getItems().setAll(linhas);
      tabelaEpis.refresh();
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("LISTAR_EPIS_FORNECIMENTO", usuario.id(), "-", ex);
      avisar(MensagensFornecimento.erro(ex));
    }
  }

  private void alternarEpi(LinhaEpi linha, boolean marcado) {
    rascunhos.removeIf(item -> item.epiId == linha.epiId());
    if (marcado) {
      rascunhos.add(linha.naMatriz() ? Rascunho.daFuncao(linha) : Rascunho.fora(linha));
    }
    limparFeedback();
    if (!marcado && !linha.naMatriz() && !foraDaMatriz.isSelected()) {
      preencherEpis();
    } else {
      tabelaEpis.refresh();
    }
    atualizarProximo();
  }

  private void atualizarProximo() {
    if (proximo != null && passo >= 1 && passo <= 3) {
      proximo.setDisable(!passoPronto());
    }
  }

  private boolean temForaSelecionado() {
    return rascunhos.stream().anyMatch(item -> item.excecao != null);
  }

  private boolean podeExcecao() {
    return usuario.temPapel(Papel.ADMIN) || usuario.temPapel(Papel.SESMT);
  }

  private Optional<Rascunho> rascunhoDe(long epiId) {
    return rascunhos.stream().filter(item -> item.epiId == epiId).findFirst();
  }

  private Node passoLotes() {
    configurarLotes();
    carregarLotes();
    sugerirMotivos();
    tabelaLotes.getItems().setAll(rascunhos);
    tabelaLotes.refresh();
    VBox.setVgrow(tabelaLotes, Priority.ALWAYS);
    tabelaLotes.setPrefHeight(320);
    return tabelaLotes;
  }

  private void configurarLotes() {
    if (lotesConfigurados) {
      return;
    }
    tabelaLotes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabelaLotes.setPlaceholder(new Label("Inclua ao menos um EPI na ficha."));
    TableColumn<Rascunho, String> epi = new TableColumn<>("EPI");
    epi.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().epi));
    epi.setSortable(false);
    TableColumn<Rascunho, Rascunho> lote = colunaDoRascunho("Lote", coluna -> new LoteCell());
    TableColumn<Rascunho, String> ca = new TableColumn<>("CA");
    ca.setCellValueFactory(data -> new SimpleStringProperty(caDo(data.getValue())));
    ca.setSortable(false);
    TableColumn<Rascunho, String> validade = new TableColumn<>("Validade");
    validade.setCellValueFactory(data -> new SimpleStringProperty(validadeDo(data.getValue())));
    validade.setSortable(false);
    TableColumn<Rascunho, Rascunho> disponivel =
        colunaDoRascunho("Disponível", coluna -> new DisponivelCell());
    TableColumn<Rascunho, Rascunho> quantidade =
        colunaDoRascunho("Qtd", coluna -> new QuantidadeCell());
    quantidade.setMinWidth(72);
    quantidade.setMaxWidth(90);
    TableColumn<Rascunho, Rascunho> motivo = colunaDoRascunho("Motivo", coluna -> new MotivoCell());
    tabelaLotes
        .getColumns()
        .setAll(List.of(epi, lote, ca, validade, disponivel, quantidade, motivo));
    tabelaLotes
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (atual.intValue() >= 0) {
                tabelaLotes.getSelectionModel().clearSelection();
              }
            });
    lotesConfigurados = true;
  }

  private static TableColumn<Rascunho, Rascunho> colunaDoRascunho(
      String titulo,
      javafx.util.Callback<TableColumn<Rascunho, Rascunho>, TableCell<Rascunho, Rascunho>> celula) {
    TableColumn<Rascunho, Rascunho> coluna = new TableColumn<>(titulo);
    coluna.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    coluna.setCellFactory(celula);
    coluna.setSortable(false);
    return coluna;
  }

  private void carregarLotes() {
    garantirContexto();
    lotesPorEpi.clear();
    for (Rascunho rascunho : rascunhos) {
      lotesPorEpi.put(
          rascunho.epiId, fornecimento.lotes(usuario.id(), trabalhador.unitId(), rascunho.epiId));
    }
  }

  private void sugerirMotivos() {
    for (Rascunho rascunho : rascunhos) {
      rascunho.motivo =
          regras.sugerirMotivo(
              rascunho.motivo, rascunho.posto, temEntrega(rascunho.epiId), funcaoMudou());
    }
  }

  private void garantirContexto() {
    if (contexto != null || trabalhador == null) {
      return;
    }
    contexto = fornecimento.saida(usuario.id(), trabalhador.id());
  }

  private boolean temEntrega(long epiId) {
    garantirContexto();
    return contexto != null && contexto.episComEntrega().contains(epiId);
  }

  private boolean funcaoMudou() {
    garantirContexto();
    if (contexto == null || trabalhador == null) {
      return false;
    }
    return regras.funcaoMudou(trabalhador.funcao(), contexto.funcaoUltimaFicha());
  }

  private boolean motivoPermitido(Rascunho rascunho, MotivoFornecimento motivo) {
    return regras.motivoPermitido(
        motivo, rascunho.posto, temEntrega(rascunho.epiId), funcaoMudou());
  }

  private boolean tinhaFicha() {
    return contexto != null
        && contexto.funcaoUltimaFicha() != null
        && !contexto.funcaoUltimaFicha().isBlank();
  }

  private static boolean loteUsavel(LoteVisivel lote) {
    return lote != null
        && !LotPolicy.SITUACAO_VENCIDO.equals(lote.situacao())
        && !LotPolicy.SITUACAO_ESGOTADO.equals(lote.situacao());
  }

  private String caDo(Rascunho rascunho) {
    LoteVisivel lote = lote(rascunho);
    return lote == null || lote.ca() == null ? "" : lote.ca();
  }

  private String validadeDo(Rascunho rascunho) {
    LoteVisivel lote = lote(rascunho);
    return lote == null || lote.validade() == null ? "" : DATA.format(lote.validade());
  }

  private Node passoTermo() {
    VBox coluna = new VBox(10);
    coluna.setAlignment(Pos.TOP_LEFT);
    coluna.setFillWidth(true);
    coluna.setPrefWidth(300);
    coluna.setMinWidth(300);
    coluna.setMaxWidth(300);
    Label kicker = new Label("Termo de responsabilidade");
    kicker.getStyleClass().add(Enr6Styles.SHEET_KICKER);
    kicker.setMaxWidth(Double.MAX_VALUE);
    Label nome = new Label(trabalhador.nome());
    nome.getStyleClass().add(Enr6Styles.SHEET_NAME);
    nome.setWrapText(true);
    nome.setMaxWidth(Double.MAX_VALUE);
    Label meta = new Label(detalhe(trabalhador));
    meta.getStyleClass().add(Enr6Styles.SHEET_META);
    meta.setWrapText(true);
    meta.setMaxWidth(Double.MAX_VALUE);
    VBox pecas = new VBox(8);
    pecas.setFillWidth(true);
    for (Rascunho rascunho : rascunhos) {
      pecas.getChildren().add(blocoCiencia(rascunho));
    }
    Text corpo = new Text(fornecimento.textoDoTermo(trabalhador.nome()));
    corpo.getStyleClass().add(Enr6Styles.SHEET_BODY);
    corpo.setWrappingWidth(300);
    corpo.setTextAlignment(TextAlignment.JUSTIFY);
    PasswordField senha = new PasswordField();
    senha.setText(senhaDigitada);
    senha.setPromptText(MensagensFornecimento.SENHA_ROTULO);
    senha.setMaxWidth(Double.MAX_VALUE);
    dica(senha, MensagensFornecimento.DICA_SENHA);
    senha
        .textProperty()
        .addListener(
            (obs, antigo, novo) -> {
              senhaDigitada = novo == null ? "" : novo;
              aoAjustarTermo();
            });
    Label rotuloSenha = new Label(MensagensFornecimento.SENHA_ROTULO);
    VBox blocoSenha = new VBox(4, rotuloSenha, senha);
    blocoSenha.setFillWidth(true);
    blocoSenha.setPadding(new Insets(16, 0, 0, 0));
    coluna.getChildren().addAll(kicker, nome, meta, pecas, corpo, blocoSenha);
    return folhaCentral(coluna, false);
  }

  private Node folhaCentral(Node conteudo, boolean revisao) {
    VBox folha = new VBox(conteudo);
    folha.getStyleClass().add(Enr6Styles.SHEET);
    if (revisao) {
      folha.getStyleClass().add(Enr6Styles.SHEET_REVIEW);
    }
    folha.setAlignment(Pos.TOP_CENTER);
    folha.setMaxWidth(Region.USE_PREF_SIZE);
    Region botoes = (Region) acoes();
    botoes.prefWidthProperty().bind(folha.widthProperty());
    botoes.minWidthProperty().bind(folha.widthProperty());
    botoes.maxWidthProperty().bind(folha.widthProperty());
    VBox bloco = new VBox(revisao ? 24 : 12, folha, botoes);
    bloco.setAlignment(Pos.TOP_CENTER);
    bloco.setMaxWidth(Region.USE_PREF_SIZE);
    HBox centro = new HBox(bloco);
    centro.setAlignment(Pos.TOP_CENTER);
    centro.setMaxWidth(Double.MAX_VALUE);
    return centro;
  }

  private Node blocoCiencia(Rascunho rascunho) {
    CheckBox ciencia = new CheckBox();
    ciencia.setSelected(rascunho.ciencia);
    dica(ciencia, MensagensFornecimento.DICA_CIENCIA);
    ciencia.setOnAction(
        event -> {
          rascunho.ciencia = ciencia.isSelected();
          aoAjustarTermo();
        });
    Node linha = linhaDaCaixa(ciencia, MensagensFornecimento.CIENCIA_CAIXA + " — " + rascunho.epi);
    if (!rascunho.exigeTreinamento) {
      return linha;
    }
    DatePicker data = new DatePicker(rascunho.treinamento);
    data.setPromptText("Data do treinamento");
    data.setMaxWidth(220);
    data.setDayCellFactory(
        picker ->
            new DateCell() {
              @Override
              public void updateItem(LocalDate dia, boolean vazio) {
                super.updateItem(dia, vazio);
                setDisable(vazio || dia.isAfter(LocalDate.now()));
              }
            });
    marcarTreinamento(data, rascunho);
    data.valueProperty()
        .addListener(
            (obs, antigo, novo) -> {
              rascunho.treinamento = novo;
              marcarTreinamento(data, rascunho);
              aoAjustarTermo();
            });
    return new VBox(6, linha, new Label("Data do treinamento"), data);
  }

  private Node linhaDaCaixa(CheckBox caixa, String texto) {
    Text frase = new Text(texto);
    frase.setWrappingWidth(272);
    frase.setOnMouseClicked(event -> caixa.fire());
    HBox linha = new HBox(8, caixa, frase);
    linha.setAlignment(Pos.TOP_LEFT);
    return linha;
  }

  private void aoAjustarTermo() {
    if (rascunhos.stream().anyMatch(this::treinamentoFuturo)) {
      avisar(MensagensFornecimento.ORIENTACAO);
    } else if (MensagensFornecimento.ORIENTACAO.equals(feedback.getText())) {
      limparFeedback();
    }
    atualizarProximo();
  }

  private void marcarTreinamento(DatePicker data, Rascunho rascunho) {
    boolean futuro = treinamentoFuturo(rascunho);
    Enr6Styles.markFieldInvalid(data.getEditor(), futuro);
    Tooltip tooltip = data.getTooltip();
    if (tooltip == null) {
      tooltip = new Tooltip();
      tooltip.setWrapText(true);
      tooltip.setMaxWidth(280);
      data.setTooltip(tooltip);
    }
    tooltip.setText(
        futuro ? MensagensFornecimento.ORIENTACAO : MensagensFornecimento.DICA_TREINAMENTO);
  }

  private Node passoRevisao() {
    VBox coluna = new VBox(22);
    coluna.setAlignment(Pos.TOP_LEFT);
    coluna.setFillWidth(true);
    coluna.setPrefWidth(720);
    coluna.setMaxWidth(720);
    Label titulo = new Label(MensagensFornecimento.FICHA);
    titulo.getStyleClass().add(Enr6Styles.SHEET_DOC_TITLE);
    titulo.setMaxWidth(Double.MAX_VALUE);
    titulo.setAlignment(Pos.CENTER);
    Label termo = new Label(MensagensFornecimento.TERMO_TITULO);
    termo.getStyleClass().add(Enr6Styles.SHEET_DOC_TITLE);
    termo.setMaxWidth(Double.MAX_VALUE);
    termo.setAlignment(Pos.CENTER);
    Label aceito = new Label(MensagensFornecimento.TERMO_ACEITO);
    aceito.getStyleClass().add(Enr6Styles.SHEET_PROOF);
    aceito.setMaxWidth(Double.MAX_VALUE);
    aceito.setAlignment(Pos.CENTER);
    VBox blocoTermo = new VBox(6, termo, aceito);
    blocoTermo.setFillWidth(true);
    blocoTermo.setAlignment(Pos.CENTER);
    Label faixa = new Label(MensagensFornecimento.CONSEQUENCIA);
    faixa.getStyleClass().add(Enr6Styles.BANNER_INFO);
    faixa.setWrapText(true);
    faixa.setMaxWidth(Double.MAX_VALUE);
    coluna.getChildren().addAll(titulo, identificacaoRevisao(), blocoTermo, tabelaRevisao(), faixa);
    return folhaCentral(coluna, true);
  }

  private Node identificacaoRevisao() {
    GridPane grade = gradeFicha(50, 50);
    grade.add(campoFicha("Nome do trabalhador", trabalhador.nome()), 0, 0);
    grade.add(campoFicha("Matrícula", trabalhador.matricula()), 1, 0);
    grade.add(campoFicha("Local de trabalho", trabalhador.unidade()), 0, 1);
    grade.add(campoFicha("Setor", trabalhador.setor()), 1, 1);
    Node funcao = campoFicha("Função", trabalhador.funcao());
    grade.add(funcao, 0, 2);
    GridPane.setColumnSpan(funcao, 2);
    return grade;
  }

  private Node tabelaRevisao() {
    String[] titulos = {"EPI", "CA", "Lote", "Validade", "Qtd", "Motivo"};
    GridPane grade = gradeFicha(28, 12, 16, 14, 8, 22);
    for (int i = 0; i < titulos.length; i++) {
      grade.add(cabecalhoFicha(titulos[i]), i, 0);
    }
    int linha = 1;
    for (Rascunho rascunho : rascunhos) {
      String[] valores = {
        rascunho.epi,
        caDo(rascunho),
        codigoDo(rascunho),
        validadeDo(rascunho),
        rascunho.quantidade,
        motivoDa(rascunho)
      };
      for (int coluna = 0; coluna < valores.length; coluna++) {
        grade.add(valorFicha(valores[coluna], coluna == 4), coluna, linha);
      }
      linha++;
      Node nota = notaFicha(notaDoItem(rascunho));
      grade.add(nota, 0, linha);
      GridPane.setColumnSpan(nota, titulos.length);
      linha++;
    }
    return grade;
  }

  private static GridPane gradeFicha(double... larguras) {
    GridPane grade = new GridPane();
    grade.getStyleClass().add(Enr6Styles.SHEET_GRID);
    grade.setMaxWidth(Double.MAX_VALUE);
    for (double largura : larguras) {
      ColumnConstraints coluna = new ColumnConstraints();
      coluna.setPercentWidth(largura);
      coluna.setHgrow(Priority.ALWAYS);
      grade.getColumnConstraints().add(coluna);
    }
    return grade;
  }

  private static Node campoFicha(String rotulo, String valor) {
    Label nome = new Label(rotulo);
    nome.getStyleClass().add(Enr6Styles.SHEET_FIELD_LABEL);
    Label dado = new Label(valor == null ? "" : valor);
    dado.getStyleClass().add(Enr6Styles.SHEET_FIELD_VALUE);
    dado.setWrapText(true);
    dado.setMaxWidth(Double.MAX_VALUE);
    VBox caixa = new VBox(4, nome, dado);
    caixa.setFillWidth(true);
    return fecharCelula(caixa, false);
  }

  private static Node cabecalhoFicha(String texto) {
    Label label = new Label(texto);
    label.getStyleClass().add(Enr6Styles.SHEET_FIELD_LABEL);
    label.setWrapText(true);
    return fecharCelula(label, true);
  }

  private static Node valorFicha(String texto, boolean numero) {
    Label label = new Label(texto == null ? "" : texto);
    label.getStyleClass().add(Enr6Styles.SHEET_FIELD_VALUE);
    if (numero) {
      label.getStyleClass().add(Enr6Styles.SHEET_CELL_NUM);
      label.setAlignment(Pos.CENTER_RIGHT);
    }
    label.setWrapText(true);
    label.setMaxWidth(Double.MAX_VALUE);
    return fecharCelula(label, false);
  }

  private static Node notaFicha(String texto) {
    Label label = new Label(texto);
    label.getStyleClass().add(Enr6Styles.SHEET_NOTE);
    label.setWrapText(true);
    label.setMaxWidth(Double.MAX_VALUE);
    return fecharCelula(label, false);
  }

  private static Node fecharCelula(Region miolo, boolean cabecalho) {
    miolo.setMaxWidth(Double.MAX_VALUE);
    StackPane caixa = new StackPane(miolo);
    caixa.getStyleClass().add(Enr6Styles.SHEET_FIELD);
    if (cabecalho) {
      caixa.getStyleClass().add(Enr6Styles.SHEET_FIELD_HEAD);
    }
    caixa.setMaxWidth(Double.MAX_VALUE);
    StackPane.setAlignment(miolo, Pos.CENTER_LEFT);
    GridPane.setHgrow(caixa, Priority.ALWAYS);
    GridPane.setFillWidth(caixa, true);
    return caixa;
  }

  private static String notaDoItem(Rascunho rascunho) {
    String nota = linhaProva(rascunho);
    if (rascunho.excecao != null && !rascunho.excecao.isBlank()) {
      return nota + " " + MensagensFornecimento.FORA_DA_MATRIZ_LINHA + rascunho.excecao.trim();
    }
    return nota;
  }

  private String codigoDo(Rascunho rascunho) {
    LoteVisivel lote = lote(rascunho);
    return lote == null || lote.codigo() == null ? "" : lote.codigo();
  }

  private static String motivoDa(Rascunho rascunho) {
    if (rascunho.motivo == null) {
      return "";
    }
    if (rascunho.motivo == MotivoFornecimento.OUTRO
        && rascunho.motivoTexto != null
        && !rascunho.motivoTexto.isBlank()) {
      return rascunho.motivoTexto.trim();
    }
    return rascunho.motivo.rotulo();
  }

  private static String linhaProva(Rascunho rascunho) {
    String linha = MensagensFornecimento.ORIENTACAO_REGISTRADA + " — " + rascunho.epi + ".";
    if (rascunho.exigeTreinamento && rascunho.treinamento != null) {
      return linha + " Treinamento em " + DATA.format(rascunho.treinamento) + ".";
    }
    return linha;
  }

  private void abrirDialogo() {
    boolean confirmou = dialogo();
    ConfirmacaoFornecimento.seguir(confirmou, this::gravar);
  }

  private void gravar() {
    try {
      long fichaId = fornecimento.registrar(usuario.id(), pedido());
      avisarOk(MensagensFornecimento.sucesso(fichaId));
      trabalhador = null;
      rascunhos.clear();
      senhaDigitada = "";
      selecionadoId = 0;
      foraDaMatriz.setSelected(false);
      contexto = null;
      lotesPorEpi.clear();
      passo = 0;
      busca.clear();
      mostrar();
      avisarOk(MensagensFornecimento.sucesso(fichaId));
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("REGISTRAR_FORNECIMENTO", usuario.id(), "-", ex);
      avisar(MensagensFornecimento.erro(ex));
    }
  }

  private Pedido pedido() {
    List<ItemParaRegistrar> itens = new ArrayList<>();
    for (Rascunho rascunho : rascunhos) {
      Integer qtd = quantidade(rascunho.quantidade);
      itens.add(
          new ItemParaRegistrar(
              rascunho.epiId,
              rascunho.loteId == 0 ? null : rascunho.loteId,
              qtd == null ? 0 : qtd,
              rascunho.motivo,
              rascunho.motivoTexto,
              rascunho.ciencia,
              rascunho.treinamento,
              rascunho.excecao,
              List.of(),
              null,
              null));
    }
    String senha = senhaDigitada == null ? "" : senhaDigitada.trim();
    return new Pedido(trabalhador.id(), senha, itens);
  }

  private Node acoes() {
    HBox linha = new HBox(8);
    linha.setAlignment(Pos.CENTER_LEFT);
    linha.setMaxWidth(Double.MAX_VALUE);
    if (passo > 0) {
      Button anterior = new Button("Anterior");
      anterior.setOnAction(
          event -> {
            passo = passo - 1;
            limparFeedback();
            mostrar();
          });
      linha.getChildren().add(anterior);
    }
    Region espaco = new Region();
    HBox.setHgrow(espaco, Priority.ALWAYS);
    linha.getChildren().add(espaco);
    if (passo >= 4) {
      proximo = null;
      Button confirmar = new Button("Confirmar fornecimento");
      confirmar.getStyleClass().add(Styles.ACCENT);
      confirmar.setDefaultButton(true);
      confirmar.setDisable(!revisaoPronta());
      confirmar.setOnAction(event -> abrirDialogo());
      linha.getChildren().add(confirmar);
      Platform.runLater(confirmar::requestFocus);
      return linha;
    }
    proximo = new Button("Próximo");
    proximo.getStyleClass().add(Styles.ACCENT);
    proximo.setDisable(!passoPronto());
    proximo.setOnAction(
        event -> {
          passo = passo + 1;
          limparFeedback();
          mostrar();
        });
    linha.getChildren().add(proximo);
    return linha;
  }

  private boolean passoPronto() {
    return switch (passo) {
      case 0 -> trabalhador != null;
      case 1 -> !rascunhos.isEmpty() && rascunhos.stream().allMatch(this::excecaoPronta);
      case 2 -> rascunhos.stream().allMatch(this::lotePronto);
      case 3 -> rascunhos.stream().allMatch(this::cienciaPronta) && senhaPronta();
      default -> false;
    };
  }

  private boolean excecaoPronta(Rascunho rascunho) {
    if (rascunho.excecao == null) {
      return true;
    }
    return rascunho.excecao.trim().length() >= FornecimentoPolicy.EXCECAO_MINIMA;
  }

  private boolean revisaoPronta() {
    return trabalhador != null
        && !rascunhos.isEmpty()
        && rascunhos.stream().allMatch(item -> lotePronto(item) && cienciaPronta(item))
        && senhaPronta();
  }

  private boolean lotePronto(Rascunho rascunho) {
    Integer qtd = quantidade(rascunho.quantidade);
    if (rascunho.loteId == 0 || rascunho.motivo == null || qtd == null) {
      return false;
    }
    if (rascunho.motivo == MotivoFornecimento.OUTRO
        && (rascunho.motivoTexto == null || rascunho.motivoTexto.isBlank())) {
      return false;
    }
    if (!motivoPermitido(rascunho, rascunho.motivo)) {
      return false;
    }
    LoteVisivel lote = lote(rascunho);
    return loteUsavel(lote) && qtd <= lote.disponivel();
  }

  private boolean senhaPronta() {
    return senhaDigitada != null && !senhaDigitada.isBlank();
  }

  private boolean cienciaPronta(Rascunho rascunho) {
    if (!rascunho.ciencia) {
      return false;
    }
    if (!rascunho.exigeTreinamento) {
      return true;
    }
    return rascunho.treinamento != null && !rascunho.treinamento.isAfter(LocalDate.now());
  }

  private boolean treinamentoFuturo(Rascunho rascunho) {
    return rascunho.exigeTreinamento
        && rascunho.treinamento != null
        && rascunho.treinamento.isAfter(LocalDate.now());
  }

  private LoteVisivel lote(Rascunho rascunho) {
    List<LoteVisivel> lotes = lotesPorEpi.get(rascunho.epiId);
    if (lotes == null) {
      return null;
    }
    return lotes.stream().filter(item -> item.id() == rascunho.loteId).findFirst().orElse(null);
  }

  private boolean contem(long epiId) {
    return rascunhos.stream().anyMatch(item -> item.epiId == epiId);
  }

  private static String detalhe(Trabalhador trabalhador) {
    return trabalhador.matricula() + " · " + trabalhador.setor() + " · " + trabalhador.funcao();
  }

  private static boolean dialogo() {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle("Confirmar fornecimento");
    alerta.setHeaderText(MensagensFornecimento.CONFIRMAR);
    ButtonType seguir = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    ButtonType voltar = new ButtonType("Voltar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, voltar);
    Button confirmar = (Button) alerta.getDialogPane().lookupButton(seguir);
    confirmar.setDefaultButton(true);
    Button cancelar = (Button) alerta.getDialogPane().lookupButton(voltar);
    cancelar.setCancelButton(true);
    return alerta.showAndWait().filter(resposta -> resposta == seguir).isPresent();
  }

  private void avisar(String texto) {
    feedback.setText(texto);
    feedback.getStyleClass().setAll(Enr6Styles.FEEDBACK_DANGER);
  }

  private void avisarAviso(String texto) {
    feedback.setText(texto);
    feedback.getStyleClass().setAll(Enr6Styles.FEEDBACK_WARN);
  }

  private void avisarOk(String texto) {
    feedback.setText(texto);
    feedback.getStyleClass().setAll(Enr6Styles.FEEDBACK_OK);
  }

  private void limparFeedback() {
    feedback.setText("");
    feedback.getStyleClass().clear();
  }

  private static <T> javafx.util.StringConverter<T> nomes(Function<T, String> nome) {
    return new javafx.util.StringConverter<>() {
      @Override
      public String toString(T item) {
        return item == null ? "" : nome.apply(item);
      }

      @Override
      public T fromString(String value) {
        return null;
      }
    };
  }

  private static final class Rascunho {
    private final long epiId;
    private final String epi;
    private final boolean exigeTreinamento;
    private final boolean posto;
    private String excecao;
    private long loteId;
    private String quantidade = "1";
    private MotivoFornecimento motivo = MotivoFornecimento.PRIMEIRA_ENTREGA;
    private String motivoTexto = "";
    private boolean ciencia;
    private LocalDate treinamento;

    private Rascunho(
        long epiId, String epi, boolean exigeTreinamento, boolean posto, String excecao) {
      this.epiId = epiId;
      this.epi = epi;
      this.exigeTreinamento = exigeTreinamento;
      this.posto = posto;
      this.excecao = excecao;
    }

    private static Rascunho daFuncao(LinhaEpi linha) {
      return new Rascunho(
          linha.epiId(), linha.nome(), linha.exigeTreinamento(), linha.posto(), null);
    }

    private static Rascunho fora(LinhaEpi linha) {
      return new Rascunho(linha.epiId(), linha.nome(), false, false, "");
    }
  }

  private final class MarcaCell extends TableCell<LinhaEpi, LinhaEpi> {
    private final CheckBox box = new CheckBox();
    private boolean ajustando;

    private MarcaCell() {
      box.setOnAction(event -> aoMarcar());
    }

    private void aoMarcar() {
      if (ajustando) {
        return;
      }
      LinhaEpi linha = getItem();
      if (linha == null) {
        return;
      }
      if (!linha.naMatriz() && box.isSelected() && !podeExcecao()) {
        ajustando = true;
        box.setSelected(false);
        ajustando = false;
        avisarAviso(MensagensFornecimento.FORA_DA_MATRIZ);
        return;
      }
      alternarEpi(linha, box.isSelected());
    }

    @Override
    protected void updateItem(LinhaEpi linha, boolean vazio) {
      super.updateItem(linha, vazio);
      if (vazio || linha == null) {
        setGraphic(null);
        return;
      }
      ajustando = true;
      box.setSelected(contem(linha.epiId()));
      ajustando = false;
      setGraphic(box);
    }
  }

  private final class SituacaoCell extends TableCell<LinhaEpi, LinhaEpi> {
    private final TextField campo = new TextField();
    private boolean ajustando;

    private SituacaoCell() {
      campo.setPromptText(MensagensFornecimento.EXCECAO_CAMPO);
      campo
          .textProperty()
          .addListener(
              (obs, anterior, atual) -> {
                if (ajustando) {
                  return;
                }
                LinhaEpi linha = getItem();
                if (linha == null) {
                  return;
                }
                rascunhoDe(linha.epiId()).ifPresent(item -> item.excecao = atual);
                atualizarProximo();
              });
    }

    @Override
    protected void updateItem(LinhaEpi linha, boolean vazio) {
      super.updateItem(linha, vazio);
      if (vazio || linha == null) {
        setText(null);
        setGraphic(null);
        return;
      }
      if (!linha.naMatriz() && contem(linha.epiId())) {
        setText(null);
        String texto = rascunhoDe(linha.epiId()).map(item -> item.excecao).orElse("");
        if (!texto.equals(campo.getText())) {
          ajustando = true;
          campo.setText(texto);
          ajustando = false;
        }
        setGraphic(campo);
        return;
      }
      setGraphic(null);
      setText(linha.naMatriz() ? linha.situacao() : "");
    }
  }

  private final class LoteCell extends TableCell<Rascunho, Rascunho> {
    private final ComboBox<LoteVisivel> combo = new ComboBox<>();
    private boolean ajustando;

    private LoteCell() {
      combo.setMaxWidth(Double.MAX_VALUE);
      combo.setConverter(nomes(item -> item.codigo() + " · " + item.situacao()));
      dica(combo, MensagensFornecimento.DICA_LOTE);
      combo.setCellFactory(
          lista ->
              new ListCell<>() {
                @Override
                protected void updateItem(LoteVisivel item, boolean vazio) {
                  super.updateItem(item, vazio);
                  getStyleClass().remove(Enr6Styles.OPCAO_DESLIGADA);
                  if (vazio || item == null) {
                    setText(null);
                    return;
                  }
                  setText(item.codigo() + " · " + item.situacao());
                  if (!loteUsavel(item)) {
                    getStyleClass().add(Enr6Styles.OPCAO_DESLIGADA);
                  }
                }
              });
      combo.setOnAction(event -> aoEscolher());
    }

    private void aoEscolher() {
      if (ajustando) {
        return;
      }
      Rascunho rascunho = getItem();
      LoteVisivel escolhido = combo.getValue();
      if (rascunho == null) {
        return;
      }
      if (escolhido != null && !loteUsavel(escolhido)) {
        ajustando = true;
        combo.setValue(lote(rascunho));
        ajustando = false;
        avisarAviso(
            LotPolicy.SITUACAO_VENCIDO.equals(escolhido.situacao())
                ? MensagensFornecimento.LOTE_VENCIDO
                : MensagensFornecimento.SEM_DISPONIVEL);
        return;
      }
      rascunho.loteId = escolhido == null ? 0 : escolhido.id();
      limparFeedback();
      tabelaLotes.refresh();
      atualizarProximo();
    }

    @Override
    protected void updateItem(Rascunho rascunho, boolean vazio) {
      super.updateItem(rascunho, vazio);
      if (vazio || rascunho == null) {
        setGraphic(null);
        return;
      }
      List<LoteVisivel> lotes = lotesPorEpi.getOrDefault(rascunho.epiId, List.of());
      ajustando = true;
      combo.getItems().setAll(lotes);
      combo.setPromptText(
          lotes.isEmpty() ? MensagensFornecimento.LOTE_VAZIO : MensagensFornecimento.ESCOLHA_LOTE);
      combo.setValue(lote(rascunho));
      ajustando = false;
      setGraphic(combo);
    }
  }

  private final class DisponivelCell extends TableCell<Rascunho, Rascunho> {
    private final Tooltip dicaSaldo = new Tooltip();

    private DisponivelCell() {
      dicaSaldo.setWrapText(true);
      setTooltip(dicaSaldo);
    }

    @Override
    protected void updateItem(Rascunho rascunho, boolean vazio) {
      super.updateItem(rascunho, vazio);
      if (vazio || rascunho == null) {
        setText(null);
        setTooltip(null);
        return;
      }
      LoteVisivel escolhido = lote(rascunho);
      if (escolhido == null) {
        setText("");
        setTooltip(null);
        return;
      }
      setTooltip(dicaSaldo);
      setText(Integer.toString(escolhido.disponivel()));
      dicaSaldo.setText("Física " + escolhido.fisica() + " · Reservada " + escolhido.reservada());
    }
  }

  private final class QuantidadeCell extends TableCell<Rascunho, Rascunho> {
    private final TextField campo = new TextField();
    private boolean ajustando;

    private QuantidadeCell() {
      campo.setPromptText("Quantidade");
      campo
          .textProperty()
          .addListener(
              (obs, anterior, atual) -> {
                if (ajustando) {
                  return;
                }
                Rascunho rascunho = getItem();
                if (rascunho == null) {
                  return;
                }
                rascunho.quantidade = atual;
                marcar(rascunho);
                atualizarProximo();
              });
    }

    private void marcar(Rascunho rascunho) {
      Integer qtd = quantidade(rascunho.quantidade);
      LoteVisivel escolhido = lote(rascunho);
      boolean estourou = qtd != null && escolhido != null && qtd > escolhido.disponivel();
      Enr6Styles.markFieldInvalid(campo, estourou);
      if (estourou) {
        Tooltip dica = campo.getTooltip();
        if (dica == null) {
          dica = new Tooltip(MensagensFornecimento.SEM_DISPONIVEL);
          dica.setWrapText(true);
          campo.setTooltip(dica);
        }
        return;
      }
      campo.setTooltip(null);
    }

    @Override
    protected void updateItem(Rascunho rascunho, boolean vazio) {
      super.updateItem(rascunho, vazio);
      if (vazio || rascunho == null) {
        setGraphic(null);
        return;
      }
      if (!rascunho.quantidade.equals(campo.getText())) {
        ajustando = true;
        campo.setText(rascunho.quantidade);
        ajustando = false;
      }
      marcar(rascunho);
      setGraphic(campo);
    }
  }

  private final class MotivoCell extends TableCell<Rascunho, Rascunho> {
    private final ComboBox<MotivoFornecimento> combo = new ComboBox<>();
    private final TextField outro = new TextField();
    private final VBox caixa = new VBox(4, combo, outro);
    private boolean ajustando;

    private MotivoCell() {
      combo.setMaxWidth(Double.MAX_VALUE);
      combo.getItems().setAll(MotivoFornecimento.values());
      combo.setConverter(nomes(MotivoFornecimento::rotulo));
      dica(combo, MensagensFornecimento.DICA_MOTIVO);
      outro.setPromptText("Descreva o motivo");
      combo.setCellFactory(
          lista ->
              new ListCell<>() {
                @Override
                protected void updateItem(MotivoFornecimento item, boolean vazio) {
                  super.updateItem(item, vazio);
                  getStyleClass().remove(Enr6Styles.OPCAO_DESLIGADA);
                  if (vazio || item == null) {
                    setText(null);
                    return;
                  }
                  setText(item.rotulo());
                  Rascunho rascunho = MotivoCell.this.getItem();
                  if (rascunho != null && !motivoPermitido(rascunho, item)) {
                    getStyleClass().add(Enr6Styles.OPCAO_DESLIGADA);
                  }
                }
              });
      combo.setOnAction(event -> aoEscolher());
      outro
          .textProperty()
          .addListener(
              (obs, anterior, atual) -> {
                if (ajustando) {
                  return;
                }
                Rascunho rascunho = getItem();
                if (rascunho == null) {
                  return;
                }
                rascunho.motivoTexto = atual;
                atualizarProximo();
              });
    }

    private void aoEscolher() {
      if (ajustando) {
        return;
      }
      Rascunho rascunho = getItem();
      MotivoFornecimento escolhido = combo.getValue();
      if (rascunho == null || escolhido == null) {
        return;
      }
      if (!motivoPermitido(rascunho, escolhido)) {
        ajustando = true;
        combo.setValue(rascunho.motivo);
        ajustando = false;
        avisarAviso(MensagensFornecimento.motivoBloqueado(escolhido, tinhaFicha()));
        return;
      }
      rascunho.motivo = escolhido;
      mostrarOutro(escolhido == MotivoFornecimento.OUTRO);
      limparFeedback();
      atualizarProximo();
    }

    private void mostrarOutro(boolean visivel) {
      outro.setVisible(visivel);
      outro.setManaged(visivel);
    }

    @Override
    protected void updateItem(Rascunho rascunho, boolean vazio) {
      super.updateItem(rascunho, vazio);
      if (vazio || rascunho == null) {
        setGraphic(null);
        return;
      }
      ajustando = true;
      combo.setValue(rascunho.motivo);
      if (!rascunho.motivoTexto.equals(outro.getText())) {
        outro.setText(rascunho.motivoTexto);
      }
      mostrarOutro(rascunho.motivo == MotivoFornecimento.OUTRO);
      ajustando = false;
      setGraphic(caixa);
    }
  }

  private record LinhaEpi(
      long epiId,
      String nome,
      boolean naMatriz,
      String situacao,
      boolean exigeTreinamento,
      boolean posto) {}
}
