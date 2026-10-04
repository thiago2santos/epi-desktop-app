package br.com.easynr6.gestaoepi.ui.cadastro;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitSummary;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.Optional;
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

/** UC-CAD-01. Mantém nome, CNPJ e status da unidade da empresa já semeada. */
public final class UnitManagementView {

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
  private final EmployeeManagementService estrutura;
  private final TextField filtro = new TextField();
  private final TextField nome = new TextField();
  private final TextField cnpj = new TextField();
  private final ComboBox<StatusOpcao> status = new ComboBox<>();
  private final TableView<UnitSummary> tabela = new TableView<>();
  private final Button salvar = new Button("Salvar");
  private final Button inativar = new Button("Inativar");
  private final Button reativar = new Button("Reativar");
  private final Label feedback = new Label();
  private final Label vazio = new Label("Nenhuma unidade cadastrada.");
  private final Node root;
  private UnitSummary editando;
  private boolean sincronizando;
  private boolean recarregando;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public UnitManagementView(UsuarioAutenticado usuario, EmployeeManagementService estrutura) {
    this.usuario = usuario;
    this.estrutura = estrutura;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-01",
            "Unidades",
            "Nome, CNPJ e status da empresa já cadastrada. O CNPJ não muda depois de gravado.");
    page.section(montar());
    this.root = ReferenciaPage.scroll(page);
    carregar();
    prepararNovo();
  }

  public Node root() {
    return root;
  }

  static boolean unidadeProntaParaSalvar(boolean nova, String nome, String cnpj) {
    if (!textoPreenchido(nome)) {
      return false;
    }
    return !nova || textoPreenchido(cnpj);
  }

  /** Cancelar o diálogo de inativação devolve falso e a tela não chama o serviço. */
  static boolean segueAposConfirmacao(boolean confirmou) {
    return confirmou;
  }

  private Node montar() {
    configurarTabela();
    filtro.setPromptText("Nome ou CNPJ");
    filtro.textProperty().addListener((obs, anterior, atual) -> carregar());
    nome.setPromptText("Ex.: Lagoa Santa");
    nome.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    cnpj.setPromptText("00.000.000/0000-00");
    cnpj.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    prepararStatus(status);
    prepararAcao(salvar, inativar, reativar);
    salvar.setOnAction(event -> salvar());
    inativar.setOnAction(event -> inativar());
    reativar.setOnAction(event -> reativar());
    Button novo = new Button("Nova unidade");
    novo.getStyleClass().add(Styles.ACCENT);
    novo.setOnAction(event -> prepararNovo());
    Button limpar = new Button("Limpar");
    limpar.setOnAction(event -> prepararNovo());
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);

    VBox lista = painel("Lista", filtro, novo, tabela);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(420);
    VBox formulario =
        painel(
            "Unidade",
            campo("Nome", nome),
            campo("CNPJ", cnpj),
            campo("Status inicial", status),
            feedback,
            new HBox(8, salvar, inativar, reativar, limpar));
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
            coluna("Nome", UnitSummary::name),
            coluna("CNPJ", item -> UnitPolicy.formatarCnpj(item.cnpj())),
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
      nome.clear();
      cnpj.clear();
      cnpj.setDisable(false);
      status.setValue(StatusOpcao.ATIVO);
      status.setDisable(false);
      limparMarcacao();
      mostrar("", Tom.AVISO);
    } finally {
      sincronizando = false;
    }
    atualizarAcoes();
  }

  private void preencher(UnitSummary unidade, boolean limparAviso) {
    editando = unidade;
    sincronizando = true;
    try {
      nome.setText(unidade.name());
      cnpj.setText(UnitPolicy.formatarCnpj(unidade.cnpj()));
      cnpj.setDisable(true);
      status.setValue(unidade.active() ? StatusOpcao.ATIVO : StatusOpcao.INATIVO);
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
    boolean nova = editando == null;
    if (!unidadeProntaParaSalvar(nova, nome.getText(), cnpj.getText())) {
      mostrar("Informe o nome e o CNPJ.", Tom.ERRO);
      Enr6Styles.markFieldInvalid(nome, !textoPreenchido(nome.getText()));
      Enr6Styles.markFieldInvalid(cnpj, nova && !textoPreenchido(cnpj.getText()));
      return;
    }
    UnitSummary atual = editando;
    if (!nova && semAlteracao(atual)) {
      mostrar("Nenhuma alteração para salvar.", Tom.AVISO);
      return;
    }
    try {
      if (nova) {
        estrutura.createUnit(usuario.id(), nome.getText(), cnpj.getText(), statusEscolhido(status));
      } else {
        estrutura.updateUnit(usuario.id(), atual.id(), nome.getText());
      }
      carregar();
      prepararNovo();
      mostrar(nova ? "Unidade cadastrada." : "Nome atualizado.", Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensCadastroUnidade.erro(ex), Tom.ERRO);
    }
  }

  private boolean semAlteracao(UnitSummary atual) {
    return nome.getText().trim().equals(atual.name());
  }

  private void inativar() {
    UnitSummary atual = editando;
    if (atual == null || !atual.active()) {
      return;
    }
    if (!segueAposConfirmacao(
        confirmar(
            "Inativar unidade",
            "Inativar " + atual.name() + "?",
            "O registro permanece. Unidade com setor ativo não pode ser inativada."))) {
      return;
    }
    alterarStatus(atual, false, "Unidade inativada. O registro permanece.");
  }

  private void reativar() {
    UnitSummary atual = editando;
    if (atual == null || atual.active()) {
      return;
    }
    alterarStatus(atual, true, "Unidade reativada.");
  }

  private void alterarStatus(UnitSummary atual, boolean ativo, String sucesso) {
    try {
      estrutura.setUnitStatus(usuario.id(), atual.id(), ativo);
      carregar();
      religar(atual.id());
      mostrar(sucesso, Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensCadastroUnidade.erro(ex), Tom.ERRO);
    }
  }

  private void carregar() {
    recarregando = true;
    try {
      Long manter = editando == null ? null : editando.id();
      tabela.getItems().setAll(estrutura.listUnits(usuario.id(), filtro.getText()));
      if (manter != null) {
        tabela.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabela.getSelectionModel().select(item));
      }
      vazio.setText(
          textoPreenchido(filtro.getText())
              ? "Nenhuma unidade com esse filtro."
              : "Nenhuma unidade cadastrada.");
    } catch (RuntimeException ex) {
      tabela.getItems().clear();
      mostrar(MensagensCadastroUnidade.erro(ex), Tom.ERRO);
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
    salvar.setDisable(!unidadeProntaParaSalvar(editando == null, nome.getText(), cnpj.getText()));
    boolean ativo = editando != null && editando.active();
    inativar.setVisible(ativo);
    inativar.setManaged(ativo);
    reativar.setVisible(editando != null && !ativo);
    reativar.setManaged(editando != null && !ativo);
  }

  private void limparMarcacao() {
    Enr6Styles.markFieldInvalid(nome, false);
    Enr6Styles.markFieldInvalid(cnpj, false);
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

  private static boolean textoPreenchido(String valor) {
    return valor != null && !valor.isBlank();
  }

  private static TableColumn<UnitSummary, String> coluna(
      String titulo, Function<UnitSummary, String> valor) {
    TableColumn<UnitSummary, String> column = new TableColumn<>(titulo);
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
}
