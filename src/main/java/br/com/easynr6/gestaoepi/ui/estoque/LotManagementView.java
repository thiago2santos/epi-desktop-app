package br.com.easynr6.gestaoepi.ui.estoque;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.stock.application.StockManagementService;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.CaOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.EpiOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LotBalance;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.UnitOption;
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

/** UC-LOT-01. Recebe o lote e mostra física, reservada e disponível da unidade. */
public final class LotManagementView {

  public static final String LISTA_VAZIA = "Nenhum lote recebido nesta unidade.";
  public static final String ACAO_REGISTRAR = "Registrar recebimento";
  public static final String SUCESSO = "Lote recebido.";
  public static final String DIALOGO_PECA_VENCIDA =
      "Esta peça já venceu. O lote entra sem quantidade disponível.";
  public static final String AVISO_CONSULTA =
      "A consulta deste CA não é de hoje. Confira o número impresso na peça.";

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private enum Tom {
    OK,
    ERRO,
    AVISO
  }

  private final UsuarioAutenticado usuario;
  private final StockManagementService estoque;
  private final boolean podeReceber;
  private final ComboBox<UnitOption> unidade = new ComboBox<>();
  private final ComboBox<EpiOption> epi = new ComboBox<>();
  private final ComboBox<CaOption> ca = new ComboBox<>();
  private final TextField codigo = new TextField();
  private final TextField fabricante = new TextField();
  private final TextField tamanho = new TextField();
  private final DatePicker validade = new DatePicker();
  private final TextField quantidade = new TextField();
  private final TextField custo = new TextField();
  private final TableView<LotBalance> tabela = new TableView<>();
  private final Button salvar = new Button(ACAO_REGISTRAR);
  private final Label feedback = new Label();
  private final Label avisoCa = new Label();
  private final Label vazio = new Label(LISTA_VAZIA);
  private final Node root;
  private boolean sincronizando;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public LotManagementView(UsuarioAutenticado usuario, StockManagementService estoque) {
    this.usuario = usuario;
    this.estoque = estoque;
    this.podeReceber = usuario.temPapel(Papel.ALMOXARIFE);
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
    if (tamanho == null || tamanho.isBlank()) {
      return "Único";
    }
    return tamanho;
  }

  private Node montar() {
    configurarTabela();
    prepararCombos();
    codigo.setPromptText("Código impresso na peça");
    fabricante.setPromptText("Fabricante ou importador");
    tamanho.setPromptText("Em branco vale como tamanho único");
    quantidade.setPromptText("Inteiro maior que zero");
    custo.setPromptText("Opcional. Ex.: 12,50");
    salvar.getStyleClass().add(Styles.ACCENT);
    salvar.setDisable(true);
    salvar.setOnAction(event -> salvar());
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);
    avisoCa.setWrapText(true);
    avisoCa.setVisible(false);
    avisoCa.setManaged(false);
    ouvirCampos();

    VBox lista = painel("Lotes da unidade", tabela);
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
    return new HBox(12, lista, formulario);
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getColumns()
        .setAll(
            coluna("Código", LotBalance::lotCode),
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
    } catch (RuntimeException ex) {
      mostrar(MensagensEstoque.erro(ex), Tom.ERRO);
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
        tabela.getItems().setAll(estoque.listLots(usuario.id(), escolhida.id()));
      }
      vazio.setText(LISTA_VAZIA);
    } catch (RuntimeException ex) {
      tabela.getItems().clear();
      mostrar(MensagensEstoque.erro(ex), Tom.ERRO);
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
      mostrar(MensagensEstoque.erro(ex), Tom.ERRO);
    }
  }

  private void limparRecebimento() {
    codigo.clear();
    fabricante.clear();
    tamanho.clear();
    quantidade.clear();
    custo.clear();
    validade.setValue(null);
    atualizarAcoes();
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

  private void mostrar(String texto, Tom tom) {
    String visivel = texto == null ? "" : texto;
    feedback.setText(visivel);
    switch (tom) {
      case OK -> Enr6Styles.markOk(feedback);
      case ERRO -> Enr6Styles.markDanger(feedback);
      case AVISO -> Enr6Styles.markWarn(feedback);
    }
    if (visivel.isBlank()) {
      feedback
          .getStyleClass()
          .removeAll(Enr6Styles.FEEDBACK_OK, Enr6Styles.FEEDBACK_DANGER, Enr6Styles.FEEDBACK_WARN);
    }
    mensagens.agendar(feedback, visivel);
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
