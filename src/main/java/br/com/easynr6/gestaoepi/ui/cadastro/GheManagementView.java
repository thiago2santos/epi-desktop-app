package br.com.easynr6.gestaoepi.ui.cadastro;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.FuncaoDoGhe;
import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.GheSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitSummary;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.List;
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
import javafx.util.StringConverter;

/** UC-CAD-07. Mantém o GHE da unidade e as funções que compartilham a lista de EPI. */
public final class GheManagementView {

  static final String LISTA_VAZIA = "Nenhum GHE nesta unidade.";
  static final String CONFIRMAR_DESVINCULO =
      "Tirar esta função do GHE? A lista dela volta a ser a matriz da própria função.";
  static final String CONFIRMAR_INATIVAR =
      "Inativar este GHE? Enquanto estiver inativo, cada função volta a usar a matriz própria.";

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
  private final ComboBox<UnitSummary> unidade = new ComboBox<>();
  private final TextField filtro = new TextField();
  private final TextField nome = new TextField();
  private final ComboBox<StatusOpcao> status = new ComboBox<>();
  private final TableView<GheSummary> tabela = new TableView<>();
  private final TableView<FuncaoDoGhe> membros = new TableView<>();
  private final ComboBox<FuncaoDoGhe> candidata = new ComboBox<>();
  private final Button salvar = new Button("Salvar");
  private final Button inativar = new Button("Inativar");
  private final Button reativar = new Button("Reativar");
  private final Button vincular = new Button("Vincular");
  private final Button desvincular = new Button("Tirar do GHE");
  private final Label feedback = new Label();
  private final Label vazio = new Label(LISTA_VAZIA);
  private final Node root;
  private GheSummary editando;
  private boolean sincronizando;
  private boolean recarregando;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public GheManagementView(UsuarioAutenticado usuario, EmployeeManagementService estrutura) {
    this.usuario = usuario;
    this.estrutura = estrutura;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-07",
            "GHE",
            "Funções da mesma unidade que compartilham a lista de EPI. O trabalhador herda o grupo pela função.");
    page.section(montar());
    this.root = ReferenciaPage.scroll(page);
    carregarUnidades();
    prepararNovo();
  }

  public Node root() {
    return root;
  }

  static boolean gheProntoParaSalvar(boolean temUnidade, String nome) {
    return temUnidade && textoPreenchido(nome);
  }

  static boolean segueAposConfirmacao(boolean confirmou) {
    return confirmou;
  }

  private Node montar() {
    configurarTabelas();
    unidade.setMaxWidth(Double.MAX_VALUE);
    unidade.setConverter(nomes(UnitSummary::name));
    unidade.setOnAction(event -> trocarUnidade());
    filtro.setPromptText("Nome do GHE");
    filtro.textProperty().addListener((obs, anterior, atual) -> carregar());
    nome.setPromptText("Ex.: Ruído da caldeira");
    nome.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    prepararStatus(status);
    prepararAcao(salvar, inativar, reativar);
    salvar.setOnAction(event -> salvar());
    inativar.setOnAction(event -> inativar());
    reativar.setOnAction(event -> reativar());
    vincular.setOnAction(event -> vincular());
    desvincular.setOnAction(event -> desvincular());
    candidata.setMaxWidth(Double.MAX_VALUE);
    candidata.setConverter(nomes(FuncaoDoGhe::rotulo));
    candidata.setPromptText("Função ativa ainda sem GHE");
    candidata.valueProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    Button limpar = new Button("Limpar");
    limpar.setOnAction(event -> prepararNovo());
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);

    VBox lista = painel("Lista", campo("Unidade", unidade), filtro, tabela);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(280);
    VBox formulario =
        painel(
            "GHE",
            campo("Nome", nome),
            campo("Status inicial", status),
            feedback,
            new HBox(8, salvar, inativar, reativar, limpar));
    formulario.setPrefWidth(360);
    formulario.setMinWidth(300);
    VBox funcoes =
        painel("Funções do grupo", membros, new HBox(8, candidata, vincular, desvincular));
    HBox.setHgrow(candidata, Priority.ALWAYS);
    membros.setPrefHeight(180);
    VBox corpo = new VBox(12, new HBox(12, lista, formulario), funcoes);
    VBox.setVgrow(lista, Priority.ALWAYS);
    return corpo;
  }

  private void configurarTabelas() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getColumns()
        .setAll(
            coluna("Nome", GheSummary::name),
            coluna("Funções", item -> Integer.toString(item.funcoes())),
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
    membros.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    membros.setPlaceholder(new Label("Nenhuma função neste GHE."));
    membros
        .getColumns()
        .setAll(
            colunaFuncao("Função", FuncaoDoGhe::rotulo),
            colunaFuncao("Status", item -> item.active() ? "Ativo" : "Inativo"));
    membros
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, anterior, atual) -> atualizarAcoes());
  }

  private void carregarUnidades() {
    try {
      unidade.getItems().setAll(estrutura.listUnits(usuario.id(), ""));
      if (!unidade.getItems().isEmpty()) {
        unidade.getSelectionModel().selectFirst();
      }
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("CARREGAR_GHE", usuario.id(), "-", ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    }
  }

  private void trocarUnidade() {
    if (sincronizando) {
      return;
    }
    prepararNovo();
    carregar();
    carregarCandidatas();
  }

  private void prepararNovo() {
    tabela.getSelectionModel().clearSelection();
    editando = null;
    sincronizando = true;
    try {
      nome.clear();
      status.setValue(StatusOpcao.ATIVO);
      status.setDisable(false);
      unidade.setDisable(false);
      membros.getItems().clear();
      limparMarcacao();
      mostrar("", Tom.AVISO);
    } finally {
      sincronizando = false;
    }
    atualizarAcoes();
  }

  private void preencher(GheSummary ghe, boolean limparAviso) {
    editando = ghe;
    sincronizando = true;
    try {
      nome.setText(ghe.name());
      status.setValue(ghe.active() ? StatusOpcao.ATIVO : StatusOpcao.INATIVO);
      status.setDisable(true);
      unidade.setDisable(true);
      limparMarcacao();
      if (limparAviso) {
        mostrar("", Tom.AVISO);
      }
    } finally {
      sincronizando = false;
    }
    carregarMembros();
    atualizarAcoes();
  }

  private void salvar() {
    limparMarcacao();
    UnitSummary planta = unidade.getValue();
    boolean nova = editando == null;
    if (!gheProntoParaSalvar(planta != null, nome.getText())) {
      mostrar("Informe a unidade e o nome do GHE.", Tom.ERRO);
      Enr6Styles.markFieldInvalid(nome, !textoPreenchido(nome.getText()));
      return;
    }
    GheSummary atual = editando;
    if (!nova && nome.getText().trim().equals(atual.name())) {
      mostrar("Nenhuma alteração para salvar.", Tom.AVISO);
      return;
    }
    try {
      if (nova) {
        estrutura.createGhe(usuario.id(), planta.id(), nome.getText(), statusEscolhido(status));
      } else {
        estrutura.updateGhe(usuario.id(), atual.id(), nome.getText());
      }
      carregar();
      prepararNovo();
      carregarCandidatas();
      mostrar(nova ? "GHE cadastrado." : "Nome atualizado.", Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("SALVAR_GHE", usuario.id(), alvo(atual), ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    }
  }

  private void inativar() {
    GheSummary atual = editando;
    if (atual == null || !atual.active()) {
      return;
    }
    if (!segueAposConfirmacao(confirmar("Inativar GHE", CONFIRMAR_INATIVAR))) {
      return;
    }
    alterarStatus(atual, false, "GHE inativado. O registro permanece.");
  }

  private void reativar() {
    GheSummary atual = editando;
    if (atual == null || atual.active()) {
      return;
    }
    alterarStatus(atual, true, "GHE reativado.");
  }

  private void alterarStatus(GheSummary atual, boolean ativo, String sucesso) {
    try {
      estrutura.setGheStatus(usuario.id(), atual.id(), ativo);
      carregar();
      religar(atual.id());
      mostrar(sucesso, Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar(
          ativo ? "REATIVAR_GHE" : "INATIVAR_GHE", usuario.id(), alvo(atual), ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    }
  }

  private void vincular() {
    GheSummary atual = editando;
    FuncaoDoGhe escolhida = candidata.getValue();
    if (atual == null || escolhida == null) {
      return;
    }
    try {
      estrutura.linkJobRoleToGhe(usuario.id(), atual.id(), escolhida.id());
      carregar();
      religar(atual.id());
      carregarCandidatas();
      carregarMembros();
      mostrar("Função vinculada.", Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("VINCULAR_FUNCAO_GHE", usuario.id(), alvo(atual), ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    }
  }

  private void desvincular() {
    GheSummary atual = editando;
    FuncaoDoGhe escolhida = membros.getSelectionModel().getSelectedItem();
    if (atual == null || escolhida == null) {
      return;
    }
    if (!segueAposConfirmacao(confirmar("Tirar função do GHE", CONFIRMAR_DESVINCULO))) {
      return;
    }
    try {
      estrutura.unlinkJobRoleFromGhe(usuario.id(), atual.id(), escolhida.id());
      carregar();
      religar(atual.id());
      carregarCandidatas();
      carregarMembros();
      mostrar("Função retirada do GHE.", Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("DESVINCULAR_FUNCAO_GHE", usuario.id(), alvo(atual), ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    }
  }

  private void carregar() {
    UnitSummary planta = unidade.getValue();
    if (planta == null) {
      tabela.getItems().clear();
      vazio.setText(LISTA_VAZIA);
      return;
    }
    recarregando = true;
    try {
      Long manter = editando == null ? null : editando.id();
      tabela.getItems().setAll(estrutura.listGhes(usuario.id(), planta.id(), filtro.getText()));
      if (manter != null) {
        tabela.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabela.getSelectionModel().select(item));
      }
      vazio.setText(
          textoPreenchido(filtro.getText()) ? "Nenhum GHE com esse filtro." : LISTA_VAZIA);
    } catch (RuntimeException ex) {
      tabela.getItems().clear();
      LogTroubleshooting.registrar("CARREGAR_GHE", usuario.id(), String.valueOf(planta.id()), ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    } finally {
      recarregando = false;
    }
  }

  private void carregarMembros() {
    GheSummary atual = editando;
    if (atual == null) {
      membros.getItems().clear();
      return;
    }
    try {
      membros.getItems().setAll(estrutura.listGheMembers(usuario.id(), atual.id()));
    } catch (RuntimeException ex) {
      membros.getItems().clear();
      LogTroubleshooting.registrar("CARREGAR_GHE", usuario.id(), alvo(atual), ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    }
  }

  private void carregarCandidatas() {
    UnitSummary planta = unidade.getValue();
    FuncaoDoGhe anterior = candidata.getValue();
    try {
      candidata
          .getItems()
          .setAll(
              planta == null ? List.of() : estrutura.listGheCandidates(usuario.id(), planta.id()));
      if (anterior != null) {
        candidata.getItems().stream()
            .filter(item -> item.id().equals(anterior.id()))
            .findFirst()
            .ifPresent(candidata.getSelectionModel()::select);
      }
    } catch (RuntimeException ex) {
      candidata.getItems().clear();
      LogTroubleshooting.registrar(
          "CARREGAR_GHE", usuario.id(), planta == null ? "-" : String.valueOf(planta.id()), ex);
      mostrar(MensagensCadastroGhe.erro(ex), Tom.ERRO);
    }
  }

  private void religar(Long id) {
    tabela.getItems().stream()
        .filter(item -> item.id().equals(id))
        .findFirst()
        .ifPresent(
            item -> {
              tabela.getSelectionModel().select(item);
              editando = item;
            });
  }

  private void atualizarAcoes() {
    if (sincronizando) {
      return;
    }
    salvar.setDisable(!gheProntoParaSalvar(unidade.getValue() != null, nome.getText()));
    boolean ativo = editando != null && editando.active();
    inativar.setVisible(ativo);
    inativar.setManaged(ativo);
    reativar.setVisible(editando != null && !ativo);
    reativar.setManaged(editando != null && !ativo);
    vincular.setDisable(editando == null || candidata.getValue() == null);
    desvincular.setDisable(
        editando == null || membros.getSelectionModel().getSelectedItem() == null);
  }

  private void limparMarcacao() {
    Enr6Styles.markFieldInvalid(nome, false);
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

  private static String alvo(GheSummary ghe) {
    return ghe == null ? "-" : String.valueOf(ghe.id());
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

  private static boolean confirmar(String titulo, String conteudo) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle(titulo);
    alerta.setHeaderText(conteudo);
    ButtonType seguir = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, cancelar);
    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == seguir;
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

  private static boolean textoPreenchido(String valor) {
    return valor != null && !valor.isBlank();
  }

  private static TableColumn<GheSummary, String> coluna(
      String titulo, Function<GheSummary, String> valor) {
    TableColumn<GheSummary, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }

  private static TableColumn<FuncaoDoGhe, String> colunaFuncao(
      String titulo, Function<FuncaoDoGhe, String> valor) {
    TableColumn<FuncaoDoGhe, String> column = new TableColumn<>(titulo);
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
