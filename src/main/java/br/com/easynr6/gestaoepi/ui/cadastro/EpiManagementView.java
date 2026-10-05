package br.com.easynr6.gestaoepi.ui.cadastro;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.EpiSummary;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.Destino;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** UC-CAD-04. Mantém o catálogo de EPI no serviço que já aplica as regras CAD-03x. */
public final class EpiManagementView {

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
  private final TextField filtro = new TextField();
  private final TextField codigo = new TextField();
  private final TextField descricao = new TextField();
  private final ComboBox<AnnexGroup> grupo = new ComboBox<>();
  private final ComboBox<StatusOpcao> status = new ComboBox<>();
  private final TableView<EpiSummary> tabela = new TableView<>();
  private final Button salvar = new Button("Salvar");
  private final Button inativar = new Button("Inativar");
  private final Button reativar = new Button("Reativar");
  private final Label feedback = new Label();
  private final Label vazio = new Label("Nenhum EPI cadastrado.");
  private final Node root;
  private EpiSummary editando;
  private boolean sincronizando;
  private boolean recarregando;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public EpiManagementView(
      UsuarioAutenticado usuario, EpiCatalogManagementService catalogo, Consumer<Destino> navegar) {
    this.usuario = usuario;
    this.catalogo = catalogo;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-04 · Anexo I",
            "Catálogo de EPI",
            "Classificação do Anexo I e status. O fabricante entra no lote. Ativar exige CA ativo.");
    page.section(montar(navegar));
    this.root = ReferenciaPage.scroll(page);
    carregar();
    prepararNovo();
  }

  public Node root() {
    return root;
  }

  static boolean epiProntoParaSalvar(String descricao, AnnexGroup grupo) {
    return textoPreenchido(descricao) && grupo != null;
  }

  static String rotuloAnexo(AnnexGroup grupo) {
    if (grupo == null) {
      return "";
    }
    String nome =
        switch (grupo) {
          case A -> "Cabeça";
          case B -> "Olhos e face";
          case C -> "Audição";
          case D -> "Respiratória";
          case E -> "Tronco";
          case F -> "Membros superiores";
          case G -> "Membros inferiores";
          case H -> "Corpo inteiro";
          case I -> "Quedas";
        };
    return grupo.name() + " · " + nome;
  }

  private Node montar(Consumer<Destino> navegar) {
    configurarTabela();
    filtro.setPromptText("Descrição ou código");
    filtro.textProperty().addListener((obs, anterior, atual) -> carregar());
    codigo.setPromptText("Opcional. Ex.: LUV-VAQ");
    descricao.setPromptText("Ex.: Luva de vaqueta");
    descricao.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    grupo.getItems().setAll(AnnexGroup.values());
    grupo.setConverter(nomes(EpiManagementView::rotuloAnexo));
    grupo.setMaxWidth(Double.MAX_VALUE);
    grupo.setPromptText("Anexo I");
    grupo.valueProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    prepararStatus(status);
    prepararAcao(salvar, inativar, reativar);
    salvar.setOnAction(event -> salvar());
    inativar.setOnAction(event -> inativar());
    reativar.setOnAction(event -> reativar());
    Button novo = new Button("Novo EPI");
    novo.getStyleClass().add(Styles.ACCENT);
    novo.setOnAction(event -> prepararNovo());
    Button limpar = new Button("Limpar");
    limpar.setOnAction(event -> prepararNovo());
    Button ca = new Button("Gerenciar CA");
    ca.setOnAction(event -> navegar.accept(Destino.CA));
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);

    VBox lista = painel("Lista", filtro, novo, tabela);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(420);
    VBox formulario =
        painel(
            "EPI",
            campo("Código", codigo),
            campo("Descrição", descricao),
            campo("Grupo Anexo I", grupo),
            campo("Status inicial", status),
            feedback,
            new HBox(8, salvar, inativar, reativar, limpar),
            ca);
    formulario.setPrefWidth(360);
    formulario.setMinWidth(300);
    return new HBox(12, lista, formulario);
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getColumns()
        .setAll(
            coluna("Código", item -> item.epiCode() == null ? "" : item.epiCode()),
            coluna("Descrição", EpiSummary::description),
            coluna("Anexo", item -> rotuloAnexo(item.annexGroup())),
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
      codigo.clear();
      descricao.clear();
      grupo.getSelectionModel().clearSelection();
      status.setValue(StatusOpcao.ATIVO);
      status.setDisable(false);
      limparMarcacao();
      mostrar("", Tom.AVISO);
    } finally {
      sincronizando = false;
    }
    atualizarAcoes();
  }

  private void preencher(EpiSummary epi, boolean limparAviso) {
    editando = epi;
    sincronizando = true;
    try {
      codigo.setText(epi.epiCode() == null ? "" : epi.epiCode());
      descricao.setText(epi.description());
      grupo.setValue(epi.annexGroup());
      status.setValue(epi.active() ? StatusOpcao.ATIVO : StatusOpcao.INATIVO);
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
    if (!epiProntoParaSalvar(descricao.getText(), grupo.getValue())) {
      mostrar("Informe a descrição e o grupo do Anexo I.", Tom.ERRO);
      Enr6Styles.markFieldInvalid(descricao, !textoPreenchido(descricao.getText()));
      marcar(grupo, grupo.getValue() == null);
      return;
    }
    EpiSummary atual = editando;
    boolean novo = atual == null;
    if (!novo && semAlteracao(atual)) {
      mostrar("Nenhuma alteração para salvar.", Tom.AVISO);
      return;
    }
    try {
      if (novo) {
        catalogo.createEpi(
            usuario.id(),
            codigo.getText(),
            descricao.getText(),
            grupo.getValue(),
            statusEscolhido(status));
      } else {
        catalogo.updateEpi(
            usuario.id(),
            atual.id(),
            codigo.getText(),
            descricao.getText(),
            grupo.getValue(),
            atual.active());
      }
      carregar();
      prepararNovo();
      mostrar(novo ? "EPI cadastrado." : "Alterações salvas.", Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensCadastroEpi.erro(ex), Tom.ERRO);
    }
  }

  private boolean semAlteracao(EpiSummary atual) {
    String codigoAtual = atual.epiCode() == null ? "" : atual.epiCode();
    return codigo.getText().trim().equalsIgnoreCase(codigoAtual)
        && descricao.getText().trim().equals(atual.description())
        && grupo.getValue() == atual.annexGroup();
  }

  private void inativar() {
    EpiSummary atual = editando;
    if (atual == null || !atual.active()) {
      return;
    }
    if (!confirmar(
        "Inativar EPI",
        "Inativar " + atual.description() + "?",
        "Um CA ativo ou o histórico podem impedir a inativação. O registro permanece.")) {
      return;
    }
    alterarStatus(atual, false, "EPI inativado. O histórico permanece.");
  }

  private void reativar() {
    EpiSummary atual = editando;
    if (atual == null || atual.active()) {
      return;
    }
    alterarStatus(atual, true, "EPI reativado.");
  }

  private void alterarStatus(EpiSummary atual, boolean ativo, String sucesso) {
    try {
      catalogo.setEpiStatus(usuario.id(), atual.id(), ativo);
      carregar();
      religar(atual.id());
      mostrar(sucesso, Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensCadastroEpi.erro(ex), Tom.ERRO);
    }
  }

  private void carregar() {
    recarregando = true;
    try {
      Long manter = editando == null ? null : editando.id();
      tabela.getItems().setAll(catalogo.listEpi(usuario.id(), filtro.getText()));
      if (manter != null) {
        tabela.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabela.getSelectionModel().select(item));
      }
      vazio.setText(
          textoPreenchido(filtro.getText())
              ? "Nenhum EPI com esse filtro."
              : "Nenhum EPI cadastrado.");
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
        .ifPresent(item -> tabela.getSelectionModel().select(item));
  }

  private void atualizarAcoes() {
    if (sincronizando) {
      return;
    }
    salvar.setDisable(!epiProntoParaSalvar(descricao.getText(), grupo.getValue()));
    boolean ativo = editando != null && editando.active();
    inativar.setVisible(ativo);
    inativar.setManaged(ativo);
    reativar.setVisible(editando != null && !ativo);
    reativar.setManaged(editando != null && !ativo);
  }

  private void limparMarcacao() {
    Enr6Styles.markFieldInvalid(descricao, false);
    marcar(grupo, false);
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

  private static TableColumn<EpiSummary, String> coluna(
      String titulo, Function<EpiSummary, String> valor) {
    TableColumn<EpiSummary, String> column = new TableColumn<>(titulo);
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
