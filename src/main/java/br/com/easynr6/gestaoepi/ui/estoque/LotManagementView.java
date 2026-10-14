package br.com.easynr6.gestaoepi.ui.estoque;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.stock.application.StockManagementService;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.CaOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.EpiOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LotBalance;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** UC-LOT-01 e UC-LOT-02. Recebe o lote e consulta física, reservada, disponível e situação. */
public final class LotManagementView {

  public static final String LISTA_VAZIA = "Nenhum lote recebido nesta unidade.";
  public static final String FILTRO_VAZIO = "Nenhum lote com esse filtro.";
  public static final String ACAO_REGISTRAR = "Registrar recebimento";
  public static final String ACAO_CORRIGIR = "Corrigir CA";
  public static final String SUCESSO = "Lote recebido.";
  public static final String SUCESSO_CA =
      "CA do lote corrigido. Entregas já assinadas continuam com o número daquele dia.";
  public static final String DIALOGO_CA =
      "Trocar o CA deste lote? As fichas já assinadas continuam com o número daquele dia.";
  public static final String DIALOGO_PECA_VENCIDA =
      "Esta peça já venceu. O lote entra sem quantidade disponível.";
  public static final String AVISO_CONSULTA =
      "A consulta deste CA não é de hoje. Confira o número impresso na peça.";
  public static final String SEM_CA =
      "Este EPI não tem CA ativo. Vincule um CA antes de receber o lote.";

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final LotPolicy LEITURA = new LotPolicy();

  private enum Tom {
    OK,
    ERRO,
    AVISO
  }

  private final UsuarioAutenticado usuario;
  private final StockManagementService estoque;
  private final boolean podeReceber;
  private final boolean podeCorrigir;
  private final ComboBox<UnitOption> unidade = new ComboBox<>();
  private final ComboBox<EpiOption> epi = new ComboBox<>();
  private final ComboBox<CaOption> ca = new ComboBox<>();
  private final TextField codigo = new TextField();
  private final TextField fabricante = new TextField();
  private final TextField tamanho = new TextField();
  private final DatePicker validade = new DatePicker();
  private final TextField quantidade = new TextField();
  private final TextField custo = new TextField();
  private final TextField filtro = new TextField();
  private final ComboBox<String> situacao = new ComboBox<>();
  private final TableView<LotBalance> tabela = new TableView<>();
  private final Button salvar = new Button(ACAO_REGISTRAR);
  private final Button corrigir = new Button(ACAO_CORRIGIR);
  private final ComboBox<CaOption> novoCa = new ComboBox<>();
  private final Label caAtual = new Label("Selecione um lote.");
  private final Label feedback = new Label();
  private final Label feedbackCa = new Label();
  private LotBalance loteSelecionado;
  private final Label avisoCa = new Label();
  private final Label vazio = new Label(LISTA_VAZIA);
  private final Node root;
  private boolean sincronizando;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public LotManagementView(UsuarioAutenticado usuario, StockManagementService estoque) {
    this.usuario = usuario;
    this.estoque = estoque;
    this.podeReceber = exibeRecebimento(usuario);
    this.podeCorrigir = exibeCorrecaoCa(usuario);
    ReferenciaPage page =
        ReferenciaPage.of(
            "Estoque",
            "Lotes e saldos",
            "Recebimento do lote físico: CA da peça, validade e as três quantidades.");
    page.section(montar());
    this.root = ReferenciaPage.scroll(page);
    carregarOpcoes();
  }

  public Node root() {
    return root;
  }

  static boolean loteProntoParaSalvar(
      boolean unidadeEscolhida,
      boolean epiEscolhido,
      boolean caEscolhido,
      String codigo,
      LocalDate validade,
      String quantidade) {
    return unidadeEscolhida
        && epiEscolhido
        && caEscolhido
        && textoPreenchido(codigo)
        && validade != null
        && textoPreenchido(quantidade);
  }

  static boolean pecaVencida(LocalDate validade, LocalDate hoje) {
    return validade != null && hoje != null && validade.isBefore(hoje);
  }

  static boolean consultaAntiga(LocalDate consulta, LocalDate hoje) {
    return consulta == null || hoje == null || consulta.isBefore(hoje);
  }

  /** Cancelar o diálogo da peça vencida devolve falso e a tela não chama o serviço. */
  static boolean segueAposConfirmacao(boolean confirmou) {
    return confirmou;
  }

  static String textoTamanho(String tamanho) {
    return LEITURA.tamanhoVisivel(tamanho);
  }

  static boolean exibeRecebimento(UsuarioAutenticado usuario) {
    return usuario.temPapel(Papel.ALMOXARIFE);
  }

  static boolean exibeCorrecaoCa(UsuarioAutenticado usuario) {
    return usuario.temPapel(Papel.SESMT) || usuario.temPapel(Papel.ADMIN);
  }

  static boolean filtroAtivo(String texto, String situacao) {
    if (texto != null && !texto.isBlank()) {
      return true;
    }
    return situacao != null
        && !situacao.isBlank()
        && !LotPolicy.SITUACAO_TODAS.equalsIgnoreCase(situacao.trim());
  }

  static String textoListaVazia(boolean filtrando) {
    return filtrando ? FILTRO_VAZIO : LISTA_VAZIA;
  }

  static String fabricanteDoCa(CaOption ca) {
    if (ca == null || ca.manufacturer() == null || ca.manufacturer().isBlank()) {
      return "";
    }
    return ca.manufacturer().trim();
  }

  private Node montar() {
    configurarTabela();
    prepararCombos();
    codigo.setPromptText("Código impresso na peça");
    fabricante.setPromptText("Fabricante ou importador");
    tamanho.setPromptText("Em branco vale como tamanho único");
    quantidade.setPromptText("Inteiro maior que zero");
    custo.setPromptText("Opcional. Ex.: 12,50");
    filtro.setPromptText("EPI, lote ou tamanho");
    situacao
        .getItems()
        .setAll(
            LotPolicy.SITUACAO_TODAS,
            LotPolicy.SITUACAO_VIGENTE,
            LotPolicy.SITUACAO_VENCIDO,
            LotPolicy.SITUACAO_ESGOTADO);
    situacao.getSelectionModel().selectFirst();
    situacao.setMinWidth(140);
    filtro.textProperty().addListener((obs, anterior, atual) -> carregarLotes());
    situacao.valueProperty().addListener((obs, anterior, atual) -> carregarLotes());
    salvar.getStyleClass().add(Styles.ACCENT);
    salvar.setDisable(true);
    salvar.setOnAction(event -> salvar());
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);
    avisoCa.setWrapText(true);
    avisoCa.setVisible(false);
    avisoCa.setManaged(false);
    ouvirCampos();

    HBox busca = new HBox(8, filtro, situacao);
    HBox.setHgrow(filtro, Priority.ALWAYS);
    filtro.setMaxWidth(Double.MAX_VALUE);
    VBox lista = painel("Lotes da unidade", busca, tabela);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(420);
    VBox formulario =
        painel(
            "Recebimento",
            campo("Unidade", unidade),
            campo("EPI", epi),
            campo("CA", ca),
            avisoCa,
            campo("Código do lote", codigo),
            campo("Fabricante", fabricante),
            campo("Tamanho", tamanho),
            campo("Validade da peça", validade),
            campo("Quantidade", quantidade),
            campo("Custo unitário", custo),
            feedback,
            salvar);
    formulario.setPrefWidth(360);
    formulario.setMinWidth(300);
    formulario.setVisible(podeReceber);
    formulario.setManaged(podeReceber);
    VBox lateral = new VBox(12);
    if (podeReceber) {
      lateral.getChildren().add(formulario);
    }
    if (podeCorrigir) {
      lateral.getChildren().add(painelCorrecao());
    }
    if (lateral.getChildren().isEmpty()) {
      return lista;
    }
    return new HBox(12, lista, lateral);
  }

  private VBox painelCorrecao() {
    novoCa.setPromptText("CA impresso na peça");
    novoCa.setMaxWidth(Double.MAX_VALUE);
    novoCa.valueProperty().addListener((obs, anterior, atual) -> atualizarCorrecao());
    corrigir.getStyleClass().add(Styles.ACCENT);
    corrigir.setDisable(true);
    corrigir.setOnAction(event -> corrigirCa());
    caAtual.setWrapText(true);
    feedbackCa.setWrapText(true);
    feedbackCa.setMaxWidth(Double.MAX_VALUE);
    VBox box = painel("Corrigir CA", caAtual, campo("Novo CA", novoCa), feedbackCa, corrigir);
    box.setPrefWidth(360);
    box.setMinWidth(300);
    return box;
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, anterior, atual) -> mostrarCorrecao(atual));
    tabela
        .getColumns()
        .setAll(
            coluna("Código", LotBalance::lotCode),
            coluna("CA", LotBalance::caNumber),
            coluna("EPI", LotBalance::epiDescription),
            coluna("Tamanho", item -> textoTamanho(item.sizeLabel())),
            coluna("Validade", item -> item.pieceValidUntil().format(DATA)),
            coluna("Física", item -> Integer.toString(item.fisica())),
            coluna("Reservada", item -> Integer.toString(item.reservada())),
            coluna("Disponível", item -> Integer.toString(item.disponivel())),
            coluna("Situação", LotBalance::situacao));
  }

  private void prepararCombos() {
    unidade.setMaxWidth(Double.MAX_VALUE);
    epi.setMaxWidth(Double.MAX_VALUE);
    ca.setMaxWidth(Double.MAX_VALUE);
    unidade.setOnAction(event -> carregarLotes());
    epi.setOnAction(
        event -> {
          carregarCas();
          atualizarAcoes();
        });
    ca.setOnAction(
        event -> {
          preencherFabricante();
          atualizarAvisoCa();
          atualizarAcoes();
        });
  }

  private void ouvirCampos() {
    codigo.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    quantidade.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    validade.valueProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
  }

  private void carregarOpcoes() {
    sincronizando = true;
    try {
      unidade.getItems().setAll(estoque.listUnits(usuario.id()));
      epi.getItems().setAll(estoque.listEpis(usuario.id()));
      if (!unidade.getItems().isEmpty()) {
        unidade.getSelectionModel().selectFirst();
      }
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("CARREGAR_OPCOES_LOTE", usuario.id(), "-", ex);
      mostrar(MensagensEstoque.erro(ex), Tom.ERRO);
    } finally {
      sincronizando = false;
    }
    carregarLotes();
  }

  private void carregarCas() {
    if (sincronizando) {
      return;
    }
    EpiOption escolhido = epi.getValue();
    ca.getItems().clear();
    ca.setValue(null);
    if (escolhido == null) {
      atualizarAvisoCa();
      return;
    }
    try {
      ca.getItems().setAll(estoque.listCas(usuario.id(), escolhido.id()));
      if (ca.getItems().isEmpty()) {
        ca.setPromptText("Nenhum CA ativo deste EPI");
        mostrar(SEM_CA, Tom.AVISO);
      } else {
        ca.setPromptText("CA ativo deste EPI");
      }
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("CARREGAR_CAS_LOTE", usuario.id(), alvo(escolhido.id()), ex);
      ca.setPromptText("Nenhum CA ativo deste EPI");
      mostrar(MensagensEstoque.erroConsulta(ex), Tom.ERRO);
    }
    atualizarAvisoCa();
  }

  private void carregarLotes() {
    if (sincronizando) {
      return;
    }
    UnitOption escolhida = unidade.getValue();
    try {
      if (escolhida == null) {
        tabela.getItems().clear();
      } else {
        tabela
            .getItems()
            .setAll(
                estoque.listLots(
                    usuario.id(), escolhida.id(), filtro.getText(), situacao.getValue()));
      }
      vazio.setText(textoListaVazia(filtroAtivo(filtro.getText(), situacao.getValue())));
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar(
          "CONSULTAR_LOTES", usuario.id(), alvo(escolhida == null ? null : escolhida.id()), ex);
      tabela.getItems().clear();
      vazio.setText("");
      mostrar(MensagensEstoque.erroConsulta(ex), Tom.ERRO);
    }
  }

  private void mostrarCorrecao(LotBalance lote) {
    loteSelecionado = lote;
    novoCa.getItems().clear();
    novoCa.setValue(null);
    if (lote == null) {
      caAtual.setText("Selecione um lote.");
      atualizarCorrecao();
      return;
    }
    caAtual.setText(lote.lotCode() + " · " + lote.epiDescription() + " · CA " + lote.caNumber());
    if (!podeCorrigir) {
      return;
    }
    try {
      novoCa.getItems().setAll(estoque.listCas(usuario.id(), lote.epiId()));
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("CARREGAR_CAS_CORRECAO", usuario.id(), alvo(lote.id()), ex);
      mostrarCa(MensagensEstoque.erro(ex), Tom.ERRO);
    }
    atualizarCorrecao();
  }

  private void atualizarCorrecao() {
    CaOption escolhido = novoCa.getValue();
    boolean outro =
        loteSelecionado != null
            && escolhido != null
            && !escolhido.id().equals(loteSelecionado.caBindingId());
    corrigir.setDisable(!outro);
  }

  private void corrigirCa() {
    if (loteSelecionado == null || novoCa.getValue() == null || corrigir.isDisable()) {
      return;
    }
    if (!segueAposConfirmacao(confirmar("Corrigir CA", DIALOGO_CA))) {
      return;
    }
    Long loteId = loteSelecionado.id();
    try {
      estoque.corrigirCa(usuario.id(), loteId, novoCa.getValue().id());
      carregarLotes();
      tabela.getItems().stream()
          .filter(item -> loteId.equals(item.id()))
          .findFirst()
          .ifPresent(item -> tabela.getSelectionModel().select(item));
      mostrarCa(SUCESSO_CA, Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("CORRIGIR_CA_LOTE", usuario.id(), alvo(loteId), ex);
      mostrarCa(MensagensEstoque.erro(ex), Tom.ERRO);
    }
  }

  private void salvar() {
    if (!podeReceber || !pronto()) {
      mostrar("Informe unidade, EPI, CA, lote, validade e quantidade.", Tom.ERRO);
      return;
    }
    LocalDate data = validade.getValue();
    boolean aceitaVencida = false;
    if (pecaVencida(data, LocalDate.now())) {
      if (!segueAposConfirmacao(confirmar("Peça vencida", DIALOGO_PECA_VENCIDA))) {
        return;
      }
      aceitaVencida = true;
    }
    try {
      estoque.receiveLot(
          usuario.id(),
          unidade.getValue().id(),
          epi.getValue().id(),
          ca.getValue().id(),
          codigo.getText(),
          fabricante.getText(),
          tamanho.getText(),
          data,
          quantidade.getText(),
          custo.getText(),
          aceitaVencida);
      limparRecebimento();
      carregarLotes();
      mostrar(SUCESSO, Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar(
          "RECEBER_LOTE",
          usuario.id(),
          alvo(unidade.getValue() == null ? null : unidade.getValue().id()),
          ex);
      mostrar(MensagensEstoque.erro(ex), Tom.ERRO);
    }
  }

  private static String alvo(Long id) {
    return id == null ? "-" : id.toString();
  }

  private void limparRecebimento() {
    codigo.clear();
    tamanho.clear();
    quantidade.clear();
    custo.clear();
    validade.setValue(null);
    preencherFabricante();
    atualizarAcoes();
  }

  private void preencherFabricante() {
    fabricante.setText(fabricanteDoCa(ca.getValue()));
  }

  private void atualizarAcoes() {
    if (sincronizando) {
      return;
    }
    salvar.setDisable(!pronto());
  }

  private boolean pronto() {
    return loteProntoParaSalvar(
        unidade.getValue() != null,
        epi.getValue() != null,
        ca.getValue() != null,
        codigo.getText(),
        validade.getValue(),
        quantidade.getText());
  }

  private void atualizarAvisoCa() {
    CaOption escolhido = ca.getValue();
    LocalDate consulta =
        escolhido == null || escolhido.officialCheckAt() == null
            ? null
            : escolhido.officialCheckAt().toLocalDate();
    boolean antiga = escolhido != null && consultaAntiga(consulta, LocalDate.now());
    avisoCa.setText(antiga ? AVISO_CONSULTA : "");
    avisoCa.setVisible(antiga);
    avisoCa.setManaged(antiga);
    if (antiga) {
      Enr6Styles.markWarn(avisoCa);
    }
  }

  private void mostrarCa(String texto, Tom tom) {
    aplicarFeedback(feedbackCa, texto, tom);
  }

  private void mostrar(String texto, Tom tom) {
    aplicarFeedback(feedback, texto, tom);
  }

  private void aplicarFeedback(Label destino, String texto, Tom tom) {
    String visivel = texto == null ? "" : texto;
    destino.setText(visivel);
    switch (tom) {
      case OK -> Enr6Styles.markOk(destino);
      case ERRO -> Enr6Styles.markDanger(destino);
      case AVISO -> Enr6Styles.markWarn(destino);
    }
    if (visivel.isBlank()) {
      destino
          .getStyleClass()
          .removeAll(Enr6Styles.FEEDBACK_OK, Enr6Styles.FEEDBACK_DANGER, Enr6Styles.FEEDBACK_WARN);
    }
    mensagens.agendar(destino, visivel);
  }

  private static boolean confirmar(String titulo, String conteudo) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle(titulo);
    alerta.setHeaderText(conteudo);
    alerta.setContentText(null);
    ButtonType seguir = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, cancelar);
    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == seguir;
  }

  private static boolean textoPreenchido(String valor) {
    return valor != null && !valor.isBlank();
  }

  private static TableColumn<LotBalance, String> coluna(
      String titulo, Function<LotBalance, String> valor) {
    TableColumn<LotBalance, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }

  private static VBox campo(String rotulo, Node editor) {
    if (editor instanceof TextInputControl input) {
      input.setMaxWidth(Double.MAX_VALUE);
    }
    if (editor instanceof ComboBox<?> combo) {
      combo.setMaxWidth(Double.MAX_VALUE);
    }
    if (editor instanceof DatePicker picker) {
      picker.setMaxWidth(Double.MAX_VALUE);
    }
    Label label = new Label(rotulo);
    VBox box = new VBox(4, label, editor);
    box.setFillWidth(true);
    return box;
  }

  private static VBox painel(String titulo, Node... conteudo) {
    Label label = new Label(titulo);
    label.getStyleClass().add(Styles.TITLE_4);
    VBox box = new VBox(8);
    box.setFillWidth(true);
    box.getChildren().add(label);
    box.getChildren().addAll(conteudo);
    return box;
  }
}
