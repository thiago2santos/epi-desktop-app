package br.com.easynr6.gestaoepi.ui.cadastro;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiCatalogService;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.Linha;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.CaBindingSummary;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.EpiSummary;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.Destino;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

/** UC-CAD-05. Vincula CA ao EPI com a evidência que a política já exige. */
public final class CaBindingManagementView {

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter CONSULTA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private enum StatusOpcao {
    ATIVO("Ativo", true),
    INATIVO("Inativo", false);

    private final String rotulo;
    private final boolean ativo;

    StatusOpcao(String rotulo, boolean ativo) {
      this.rotulo = rotulo;
      this.ativo = ativo;
    }

    private boolean ativo() {
      return ativo;
    }

    @Override
    public String toString() {
      return rotulo;
    }
  }

  private enum Tom {
    OK,
    ERRO,
    AVISO
  }

  private final UsuarioAutenticado usuario;
  private final EpiCatalogManagementService catalogo;
  private final CaepiCatalogService caepi;
  private final ComboBox<EpiSummary> epi = new ComboBox<>();
  private final TextField numero = new TextField();
  private final ComboBox<CaStatus> situacao = new ComboBox<>();
  private final DatePicker vigenciaDe = new DatePicker();
  private final DatePicker vigenciaAte = new DatePicker();
  private final TextField consulta = new TextField();
  private final TextField evidencia = new TextField();
  private final ComboBox<StatusOpcao> status = new ComboBox<>();
  private final TableView<CaBindingSummary> tabela = new TableView<>();
  private final Button salvar = new Button("Salvar");
  private final Button inativar = new Button("Inativar");
  private final Button reativar = new Button("Reativar");
  private final Label feedback = new Label();
  private final Label vazio = new Label("Nenhum CA vinculado.");
  private final Label aviso = new Label();
  private final Label anexoRotulo = new Label("Nenhum print anexado.");
  private byte[] anexo;
  private String anexoNome;
  private final Node root;
  private CaBindingSummary editando;
  private boolean sincronizando;
  private boolean recarregando;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public CaBindingManagementView(
      UsuarioAutenticado usuario,
      EpiCatalogManagementService catalogo,
      CaepiCatalogService caepi,
      Consumer<Destino> navegar) {
    this.usuario = usuario;
    this.catalogo = catalogo;
    this.caepi = caepi;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-05",
            "Vínculo de CA por EPI",
            "Situação, vigência e evidência da consulta oficial. Vincular não apaga o histórico.");
    page.section(montar(navegar));
    this.root = ReferenciaPage.scroll(page);
    carregarEpis(null);
    prepararNovo();
  }

  public Node root() {
    return root;
  }

  static boolean vinculoProntoParaSalvar(
      boolean epiSelecionado,
      String numeroCa,
      CaStatus situacao,
      String consultaOficial,
      String evidencia) {
    return epiSelecionado
        && textoPreenchido(numeroCa)
        && situacao != null
        && textoPreenchido(consultaOficial)
        && textoPreenchido(evidencia);
  }

  static LocalDateTime lerConsulta(String texto) {
    if (texto == null || texto.isBlank()) {
      return null;
    }
    String valor = texto.trim();
    try {
      return LocalDateTime.parse(valor, CONSULTA);
    } catch (DateTimeParseException ex) {
      try {
        return LocalDateTime.parse(valor);
      } catch (DateTimeParseException deNovo) {
        return null;
      }
    }
  }

  static String formatarConsulta(String armazenada) {
    LocalDateTime instante = lerConsulta(armazenada);
    if (instante == null) {
      return armazenada == null ? "" : armazenada;
    }
    return instante.format(CONSULTA);
  }

  static LocalDate lerData(String texto) {
    if (texto == null || texto.isBlank()) {
      return null;
    }
    String valor = texto.trim();
    try {
      return LocalDate.parse(valor, DATA);
    } catch (DateTimeParseException ex) {
      try {
        return LocalDate.parse(valor);
      } catch (DateTimeParseException deNovo) {
        return null;
      }
    }
  }

  static String formatarData(String armazenada) {
    LocalDate data = lerData(armazenada);
    if (data == null) {
      return armazenada == null ? "" : armazenada;
    }
    return data.format(DATA);
  }

  private Node montar(Consumer<Destino> navegar) {
    configurarTabela();
    epi.setConverter(
        nomes(
            item ->
                item.epiCode() == null
                    ? item.description()
                    : item.epiCode() + " — " + item.description()));
    epi.setMaxWidth(Double.MAX_VALUE);
    epi.setPromptText("Selecione o EPI");
    epi.valueProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (sincronizando || anterior == atual) {
                return;
              }
              editando = null;
              numero.clear();
              situacao.getSelectionModel().clearSelection();
              vigenciaDe.setValue(null);
              vigenciaAte.setValue(null);
              consulta.clear();
              evidencia.clear();
              anexo = null;
              anexoNome = null;
              anexoRotulo.setText("Nenhum print anexado.");
              travarConsulta(false);
              status.setValue(StatusOpcao.ATIVO);
              status.setDisable(false);
              tabela.getSelectionModel().clearSelection();
              carregarVinculos();
              atualizarAcoes();
            });
    numero.setPromptText("Ex.: 28941");
    numero.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    situacao.getItems().setAll(CaStatus.values());
    situacao.setConverter(nomes(CaStatus::displayLabelPtBr));
    situacao.setMaxWidth(Double.MAX_VALUE);
    situacao.setPromptText("Situação");
    situacao.valueProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    consulta.setPromptText("dd/MM/yyyy HH:mm");
    consulta.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    evidencia.setPromptText("Carga ou observação da consulta CAEPI");
    evidencia.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    prepararStatus(status);
    prepararAcao(salvar, inativar, reativar);
    salvar.setOnAction(event -> salvar());
    inativar.setOnAction(event -> inativar());
    reativar.setOnAction(event -> reativar());
    Button limpar = new Button("Limpar");
    limpar.setOnAction(event -> prepararNovo());
    Button consultar = new Button("Consultar CAs");
    consultar.setOnAction(event -> consultar());
    Button anexar = new Button("Anexar print");
    anexar.setOnAction(event -> anexar());
    Button manual = new Button("Informar consulta online");
    manual.setOnAction(event -> liberarConsultaManual());
    Button catalogo = new Button("Ver catálogo de EPI");
    catalogo.setOnAction(event -> navegar.accept(Destino.EPI));
    aviso.setWrapText(true);
    aviso.getStyleClass().add(Enr6Styles.BANNER_INFO);
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);

    VBox lista = painel("Vínculos", tabela);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(420);
    VBox formulario =
        painel(
            "Vínculo",
            aviso,
            campo("EPI", epi),
            campo("Número do CA", numero),
            consultar,
            campo("Situação", situacao),
            campo("Vigência de", vigenciaDe),
            campo("Vigência até", vigenciaAte),
            campo("Consulta oficial", consulta),
            campo("Evidência", evidencia),
            anexoRotulo,
            new HBox(8, anexar, manual),
            campo("Status inicial", status),
            feedback,
            new HBox(8, salvar, inativar, reativar, limpar),
            catalogo);
    formulario.setPrefWidth(380);
    formulario.setMinWidth(320);
    return new HBox(12, lista, formulario);
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getColumns()
        .setAll(
            coluna("CA", CaBindingSummary::caNumber),
            coluna("Situação", item -> item.caStatus().displayLabelPtBr()),
            coluna("De", item -> formatarData(item.validFrom())),
            coluna("Até", item -> formatarData(item.validUntil())),
            coluna("Consulta", item -> formatarConsulta(item.officialCheckAt())),
            coluna("Status", item -> item.active() ? "Ativo" : "Inativo"));
    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (atual != null && !recarregando) {
                preencher(atual, true);
              }
            });
  }

  private void prepararNovo() {
    tabela.getSelectionModel().clearSelection();
    editando = null;
    sincronizando = true;
    try {
      numero.clear();
      situacao.getSelectionModel().clearSelection();
      vigenciaDe.setValue(null);
      vigenciaAte.setValue(null);
      consulta.clear();
      evidencia.clear();
      anexo = null;
      anexoNome = null;
      anexoRotulo.setText("Nenhum print anexado.");
      travarConsulta(false);
      status.setValue(StatusOpcao.ATIVO);
      status.setDisable(false);
      limparMarcacao();
      mostrar("", Tom.AVISO);
    } finally {
      sincronizando = false;
    }
    atualizarAviso();
    atualizarAcoes();
  }

  private void preencher(CaBindingSummary vinculo, boolean limparAviso) {
    editando = vinculo;
    sincronizando = true;
    try {
      numero.setText(vinculo.caNumber());
      situacao.setValue(vinculo.caStatus());
      vigenciaDe.setValue(lerData(vinculo.validFrom()));
      vigenciaAte.setValue(lerData(vinculo.validUntil()));
      consulta.setText(formatarConsulta(vinculo.officialCheckAt()));
      evidencia.setText(vinculo.officialCheckNote() == null ? "" : vinculo.officialCheckNote());
      status.setValue(vinculo.active() ? StatusOpcao.ATIVO : StatusOpcao.INATIVO);
      status.setDisable(true);
      limparMarcacao();
      if (limparAviso) {
        mostrar("", Tom.AVISO);
      }
    } finally {
      sincronizando = false;
    }
    atualizarAcoes();
  }

  private void salvar() {
    limparMarcacao();
    EpiSummary escolhido = epi.getValue();
    if (!vinculoProntoParaSalvar(
        escolhido != null,
        numero.getText(),
        situacao.getValue(),
        consulta.getText(),
        evidencia.getText())) {
      mostrar("Informe o EPI, o CA, a situação, a consulta e a evidência.", Tom.ERRO);
      marcar(epi, escolhido == null);
      Enr6Styles.markFieldInvalid(numero, !textoPreenchido(numero.getText()));
      marcar(situacao, situacao.getValue() == null);
      Enr6Styles.markFieldInvalid(consulta, !textoPreenchido(consulta.getText()));
      Enr6Styles.markFieldInvalid(evidencia, !textoPreenchido(evidencia.getText()));
      return;
    }
    LocalDateTime instante = lerConsulta(consulta.getText());
    if (instante == null) {
      mostrar("Use a consulta oficial no formato dd/MM/yyyy HH:mm.", Tom.ERRO);
      Enr6Styles.markFieldInvalid(consulta, true);
      return;
    }
    CaBindingSummary atual = editando;
    boolean novo = atual == null;
    try {
      if (novo) {
        catalogo.bindCaToEpi(
            usuario.id(),
            escolhido.id(),
            numero.getText(),
            situacao.getValue(),
            vigenciaDe.getValue(),
            vigenciaAte.getValue(),
            instante,
            evidencia.getText(),
            statusEscolhido(status),
            anexoNome,
            anexo);
      } else {
        catalogo.updateCaBinding(
            usuario.id(),
            atual.id(),
            numero.getText(),
            situacao.getValue(),
            vigenciaDe.getValue(),
            vigenciaAte.getValue(),
            instante,
            evidencia.getText(),
            atual.active(),
            anexoNome,
            anexo);
      }
      carregarVinculos();
      prepararNovo();
      mostrar(novo ? "CA vinculado." : "Alterações salvas.", Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensCadastroEpi.erro(ex), Tom.ERRO);
    }
  }

  private void inativar() {
    CaBindingSummary atual = editando;
    if (atual == null || !atual.active()) {
      return;
    }
    if (!confirmar(
        "Inativar vínculo",
        "Inativar o CA " + atual.caNumber() + "?",
        "O histórico do vínculo permanece.")) {
      return;
    }
    alterarStatus(atual, false, "Vínculo inativado. O histórico permanece.");
  }

  private void reativar() {
    CaBindingSummary atual = editando;
    if (atual == null || atual.active()) {
      return;
    }
    alterarStatus(atual, true, "Vínculo reativado.");
  }

  private void alterarStatus(CaBindingSummary atual, boolean ativo, String sucesso) {
    try {
      catalogo.setCaBindingStatus(usuario.id(), atual.id(), ativo);
      carregarVinculos();
      religar(atual.id());
      mostrar(sucesso, Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensCadastroEpi.erro(ex), Tom.ERRO);
    }
  }

  private void carregarEpis(Long manter) {
    try {
      epi.getItems().setAll(catalogo.listEpi(usuario.id(), ""));
      if (manter != null) {
        epi.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(epi::setValue);
      }
    } catch (RuntimeException ex) {
      epi.getItems().clear();
      mostrar(MensagensCadastroEpi.erro(ex), Tom.ERRO);
    }
    atualizarAviso();
  }

  private void carregarVinculos() {
    recarregando = true;
    try {
      EpiSummary escolhido = epi.getValue();
      Long manter = editando == null ? null : editando.id();
      if (escolhido == null) {
        tabela.getItems().clear();
      } else {
        tabela.getItems().setAll(catalogo.listCaByEpi(usuario.id(), escolhido.id()));
      }
      if (manter != null) {
        tabela.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabela.getSelectionModel().select(item));
      }
      vazio.setText(escolhido == null ? "Selecione um EPI." : "Nenhum CA vinculado.");
    } catch (RuntimeException ex) {
      tabela.getItems().clear();
      mostrar(MensagensCadastroEpi.erro(ex), Tom.ERRO);
    } finally {
      recarregando = false;
    }
  }

  private void religar(Long id) {
    tabela.getItems().stream()
        .filter(item -> item.id().equals(id))
        .findFirst()
        .ifPresent(
            item -> {
              tabela.getSelectionModel().select(item);
              preencher(item, false);
            });
  }

  private void atualizarAviso() {
    if (epi.getItems().isEmpty()) {
      aviso.setVisible(true);
      aviso.setManaged(true);
      aviso.setText("Cadastre um EPI antes de vincular o CA.");
      return;
    }
    boolean comCarga = caepi.ultimaSucesso().isPresent();
    aviso.setVisible(true);
    aviso.setManaged(true);
    aviso.setText(
        comCarga
            ? "Escolha o CA na base importada. O print só é obrigatório se o número não estiver nela."
            : "Sem carga CAEPI neste ciclo. Anexe o print da consulta online.");
  }

  private void consultar() {
    if (epi.getValue() == null) {
      mostrar("Selecione o EPI antes de consultar os CAs.", Tom.ERRO);
      return;
    }
    Optional<Linha> escolhida =
        ConsultaCaDialog.escolher(
            salvar.getScene() == null ? null : salvar.getScene().getWindow(),
            (termo, ativos, suspensos, cancelados, expirados) ->
                caepi.buscar(usuario.id(), termo, null, ativos, suspensos, cancelados, expirados));
    escolhida.ifPresent(this::aplicarLinha);
  }

  private void aplicarLinha(Linha linha) {
    CaStatus status;
    try {
      status = CaStatus.valueOf(linha.status());
    } catch (IllegalArgumentException ex) {
      mostrar("Este CA não tem situação utilizável na base.", Tom.ERRO);
      return;
    }
    numero.setText(linha.caNumber());
    situacao.setValue(status);
    vigenciaAte.setValue(linha.validUntil());
    caepi.ultimaSucesso().ifPresent(carga -> consulta.setText(carga.finishedAt().format(CONSULTA)));
    evidencia.setText("A evidência é preenchida ao salvar, com o registro da carga.");
    travarConsulta(true);
    atualizarAcoes();
  }

  private void anexar() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Print da consulta CAEPI");
    chooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("PNG ou JPG", "*.png", "*.jpg", "*.jpeg"));
    Window janela = salvar.getScene() == null ? null : salvar.getScene().getWindow();
    java.io.File arquivo = chooser.showOpenDialog(janela);
    if (arquivo == null) {
      return;
    }
    try {
      anexo = Files.readAllBytes(arquivo.toPath());
      anexoNome = arquivo.getName();
      anexoRotulo.setText(anexoNome);
    } catch (java.io.IOException ex) {
      mostrar("Não foi possível ler o print.", Tom.ERRO);
    }
  }

  private void liberarConsultaManual() {
    travarConsulta(false);
    consulta.clear();
    evidencia.clear();
    mostrar("Informe a data da consulta e anexe o print.", Tom.AVISO);
  }

  private void travarConsulta(boolean travado) {
    situacao.setDisable(travado);
    vigenciaAte.setDisable(travado);
    consulta.setDisable(travado);
    evidencia.setDisable(travado);
  }

  private void atualizarAcoes() {
    if (sincronizando) {
      return;
    }
    salvar.setDisable(
        !vinculoProntoParaSalvar(
            epi.getValue() != null,
            numero.getText(),
            situacao.getValue(),
            consulta.getText(),
            evidencia.getText()));
    boolean ativo = editando != null && editando.active();
    inativar.setVisible(ativo);
    inativar.setManaged(ativo);
    reativar.setVisible(editando != null && !ativo);
    reativar.setManaged(editando != null && !ativo);
  }

  private void limparMarcacao() {
    marcar(epi, false);
    Enr6Styles.markFieldInvalid(numero, false);
    marcar(situacao, false);
    Enr6Styles.markFieldInvalid(consulta, false);
    Enr6Styles.markFieldInvalid(evidencia, false);
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

  private static void prepararStatus(ComboBox<StatusOpcao> combo) {
    combo.getItems().setAll(StatusOpcao.values());
    combo.setValue(StatusOpcao.ATIVO);
    combo.setMaxWidth(Double.MAX_VALUE);
  }

  private static void prepararAcao(Button botaoSalvar, Button botaoInativar, Button botaoReativar) {
    botaoSalvar.getStyleClass().add(Styles.ACCENT);
    botaoSalvar.setDisable(true);
    botaoInativar.getStyleClass().add(Styles.DANGER);
    botaoInativar.setVisible(false);
    botaoInativar.setManaged(false);
    botaoReativar.setVisible(false);
    botaoReativar.setManaged(false);
  }

  private static boolean statusEscolhido(ComboBox<StatusOpcao> combo) {
    return combo.getValue() != null && combo.getValue().ativo();
  }

  private static boolean confirmar(String titulo, String cabecalho, String conteudo) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle(titulo);
    alerta.setHeaderText(cabecalho);
    alerta.setContentText(conteudo);
    ButtonType seguir = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, cancelar);
    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == seguir;
  }

  private static void marcar(Node node, boolean invalido) {
    if (invalido) {
      if (!node.getStyleClass().contains(Enr6Styles.FIELD_INVALID)) {
        node.getStyleClass().add(Enr6Styles.FIELD_INVALID);
      }
      return;
    }
    node.getStyleClass().remove(Enr6Styles.FIELD_INVALID);
  }

  private static boolean textoPreenchido(String valor) {
    return valor != null && !valor.isBlank();
  }

  private static TableColumn<CaBindingSummary, String> coluna(
      String titulo, Function<CaBindingSummary, String> valor) {
    TableColumn<CaBindingSummary, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }

  private static VBox campo(String rotulo, Node editor) {
    if (editor instanceof TextInputControl input) {
      input.setMaxWidth(Double.MAX_VALUE);
    }
    VBox box = new VBox(4, new Label(rotulo), editor);
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }

  private static VBox painel(String titulo, Node... filhos) {
    Label title = new Label(titulo);
    title.getStyleClass().add(Enr6Styles.EMPHASIS);
    VBox box = new VBox(8);
    box.getStyleClass().add(Enr6Styles.PANEL);
    box.getChildren().add(title);
    box.getChildren().addAll(filhos);
    return box;
  }

  private static <T> StringConverter<T> nomes(Function<T, String> nome) {
    return new StringConverter<>() {
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
}
