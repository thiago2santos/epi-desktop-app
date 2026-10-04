package br.com.easynr6.gestaoepi.ui.admin;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** UC-ADM-01/02. Mantém usuário, papel e credencial no serviço de identidade. */
public final class UserAdministrationView {

  private enum Tom {
    OK,
    ERRO,
    AVISO
  }

  private final UsuarioAutenticado operador;
  private final UserAdministrationService usuarios;
  private final TextField filtro = new TextField();
  private final TextField nome = new TextField();
  private final TextField login = new TextField();
  private final PasswordField senha = new PasswordField();
  private final ComboBox<Papel> papel = new ComboBox<>();
  private final TableView<UsuarioAdminResumo> tabela = new TableView<>();
  private final Button salvar = new Button("Salvar");
  private final Button resetar = new Button("Resetar senha");
  private final Button bloquear = new Button("Bloquear");
  private final Button reativar = new Button("Reativar");
  private final Label feedback = new Label();
  private final Label vazio = new Label("Nenhum usuário cadastrado.");
  private final Node root;
  private UsuarioAdminResumo editando;
  private boolean sincronizando;
  private boolean recarregando;
  private List<UsuarioAdminResumo> todos = List.of();
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public UserAdministrationView(UsuarioAutenticado operador, UserAdministrationService usuarios) {
    this.operador = operador;
    this.usuarios = usuarios;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-ADM-01/02",
            "Usuários e papéis",
            "Cadastro, papel e reset de senha. Bloquear impede o login e mantém o histórico.");
    page.section(montar());
    this.root = ReferenciaPage.scroll(page);
    carregar();
    prepararNovo();
  }

  public Node root() {
    return root;
  }

  static boolean usuarioProntoParaSalvar(
      boolean novo, String nome, String login, String senha, boolean papelSelecionado) {
    if (!textoPreenchido(nome) || !textoPreenchido(login) || !papelSelecionado) {
      return false;
    }
    return !novo || textoPreenchido(senha);
  }

  static boolean usuarioCombina(String termo, String nome, String login, String papeis) {
    if (termo == null || termo.isBlank()) {
      return true;
    }
    String alvo = termo.trim().toLowerCase(Locale.ROOT);
    return contem(nome, alvo) || contem(login, alvo) || contem(papeis, alvo);
  }

  static List<Papel> papeisAtuais(String texto) {
    if (texto == null || texto.isBlank() || "-".equals(texto.trim())) {
      return List.of();
    }
    List<Papel> papeis = new ArrayList<>();
    for (String parte : texto.split(",")) {
      String codigo = parte.trim();
      if (codigo.isEmpty()) {
        continue;
      }
      try {
        papeis.add(Papel.valueOf(codigo));
      } catch (IllegalArgumentException ex) {
        continue;
      }
    }
    return List.copyOf(papeis);
  }

  private Node montar() {
    configurarTabela();
    filtro.setPromptText("Nome, login ou papel");
    filtro.textProperty().addListener((obs, anterior, atual) -> aplicarFiltro());
    nome.setPromptText("Nome exibido");
    nome.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    login.setPromptText("Login");
    login.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    senha.setPromptText("Senha inicial, 12 caracteres");
    senha.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    papel.getItems().setAll(Papel.values());
    papel.setConverter(nomes(UserAdministrationView::rotuloPapel));
    papel.setMaxWidth(Double.MAX_VALUE);
    papel.setPromptText("Papel");
    papel.valueProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    salvar.getStyleClass().add(Styles.ACCENT);
    salvar.setDisable(true);
    salvar.setOnAction(event -> salvar());
    resetar.setOnAction(event -> resetarSenha());
    bloquear.getStyleClass().add(Styles.DANGER);
    bloquear.setOnAction(event -> bloquear());
    reativar.setOnAction(event -> reativar());
    Button novo = new Button("Novo usuário");
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
            "Usuário",
            campo("Nome", nome),
            campo("Login", login),
            campo("Senha", senha),
            campo("Papel", papel),
            feedback,
            new HBox(8, salvar, resetar, bloquear, reativar, limpar));
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
            coluna("Login", UsuarioAdminResumo::login),
            coluna("Nome", UsuarioAdminResumo::nome),
            coluna("Papel", UsuarioAdminResumo::papeis),
            coluna("Status", item -> item.ativo() ? "Ativo" : "Bloqueado"));
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
      login.clear();
      senha.clear();
      papel.getSelectionModel().clearSelection();
      limparMarcacao();
      mostrar("", Tom.AVISO);
    } finally {
      sincronizando = false;
    }
    atualizarAcoes();
  }

  private void preencher(UsuarioAdminResumo usuario, boolean limparAviso) {
    editando = usuario;
    sincronizando = true;
    try {
      nome.setText(usuario.nome());
      login.setText(usuario.login());
      senha.clear();
      List<Papel> papeis = papeisAtuais(usuario.papeis());
      papel.setValue(papeis.isEmpty() ? null : papeis.getFirst());
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
    boolean novo = editando == null;
    if (!usuarioProntoParaSalvar(
        novo, nome.getText(), login.getText(), senha.getText(), papel.getValue() != null)) {
      mostrar("Informe nome, login, papel e, no cadastro novo, a senha inicial.", Tom.ERRO);
      return;
    }
    try {
      if (novo) {
        usuarios.cadastrarUsuario(
            operador.id(),
            nome.getText(),
            login.getText(),
            senha.getText(),
            true,
            Set.of(papel.getValue()));
      } else {
        usuarios.editarUsuario(
            operador.id(), editando.id(), nome.getText(), login.getText(), editando.ativo());
        sincronizarPapel(editando);
      }
      carregar();
      prepararNovo();
      mostrar(novo ? "Usuário cadastrado." : "Alterações salvas.", Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensAdministracaoUsuario.erro(ex), Tom.ERRO);
    }
  }

  private void sincronizarPapel(UsuarioAdminResumo atual) {
    Papel escolhido = papel.getValue();
    List<Papel> atuais = papeisAtuais(atual.papeis());
    if (!atuais.contains(escolhido)) {
      usuarios.atribuirPapel(operador.id(), atual.id(), escolhido);
    }
    for (Papel existente : atuais) {
      if (existente != escolhido) {
        usuarios.removerPapel(operador.id(), atual.id(), existente);
      }
    }
  }

  private void resetarSenha() {
    UsuarioAdminResumo atual = editando;
    if (atual == null || !textoPreenchido(senha.getText())) {
      mostrar("Selecione o usuário e informe a nova senha.", Tom.ERRO);
      return;
    }
    if (!confirmar(
        "Resetar senha",
        "Resetar a senha de " + atual.login() + "?",
        "No próximo acesso a troca da senha será obrigatória.")) {
      return;
    }
    try {
      usuarios.resetarCredencial(operador.id(), atual.id(), login.getText(), senha.getText());
      senha.clear();
      mostrar("Senha resetada. A troca será obrigatória no próximo acesso.", Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensAdministracaoUsuario.erro(ex), Tom.ERRO);
    }
  }

  private void bloquear() {
    UsuarioAdminResumo atual = editando;
    if (atual == null || !atual.ativo()) {
      return;
    }
    if (atual.id().equals(operador.id())) {
      mostrar("Não é possível bloquear o próprio usuário.", Tom.ERRO);
      return;
    }
    if (!confirmar(
        "Bloquear usuário",
        "Bloquear " + atual.login() + "?",
        "O login deixa de funcionar. O histórico permanece.")) {
      return;
    }
    try {
      usuarios.bloquearUsuario(operador.id(), atual.id());
      carregar();
      religar(atual.id());
      mostrar("Usuário bloqueado.", Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensAdministracaoUsuario.erro(ex), Tom.ERRO);
    }
  }

  private void reativar() {
    UsuarioAdminResumo atual = editando;
    if (atual == null || atual.ativo()) {
      return;
    }
    try {
      usuarios.reativarUsuario(operador.id(), atual.id());
      carregar();
      religar(atual.id());
      mostrar("Usuário reativado.", Tom.OK);
    } catch (RuntimeException ex) {
      mostrar(MensagensAdministracaoUsuario.erro(ex), Tom.ERRO);
    }
  }

  private void carregar() {
    recarregando = true;
    try {
      todos = usuarios.listarUsuarios();
      aplicarFiltro();
    } catch (RuntimeException ex) {
      todos = List.of();
      tabela.getItems().clear();
      mostrar(MensagensAdministracaoUsuario.erro(ex), Tom.ERRO);
    } finally {
      recarregando = false;
    }
  }

  private void aplicarFiltro() {
    recarregando = true;
    try {
      Long manter = editando == null ? null : editando.id();
      tabela
          .getItems()
          .setAll(
              todos.stream()
                  .filter(
                      item ->
                          usuarioCombina(
                              filtro.getText(), item.nome(), item.login(), item.papeis()))
                  .toList());
      if (manter != null) {
        tabela.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabela.getSelectionModel().select(item));
      }
      vazio.setText(
          textoPreenchido(filtro.getText())
              ? "Nenhum usuário com esse filtro."
              : "Nenhum usuário cadastrado.");
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
    boolean novo = editando == null;
    salvar.setDisable(
        !usuarioProntoParaSalvar(
            novo, nome.getText(), login.getText(), senha.getText(), papel.getValue() != null));
    boolean editandoAtivo = editando != null && editando.ativo();
    boolean proprio = editando != null && editando.id().equals(operador.id());
    bloquear.setVisible(editandoAtivo);
    bloquear.setManaged(editandoAtivo);
    bloquear.setDisable(proprio);
    reativar.setVisible(editando != null && !editandoAtivo);
    reativar.setManaged(editando != null && !editandoAtivo);
    resetar.setDisable(novo || !textoPreenchido(senha.getText()));
    senha.setPromptText(novo ? "Senha inicial, 12 caracteres" : "Nova senha para o reset");
  }

  private void limparMarcacao() {
    Enr6Styles.markFieldInvalid(nome, false);
    Enr6Styles.markFieldInvalid(login, false);
    Enr6Styles.markFieldInvalid(senha, false);
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

  static String rotuloPapel(Papel papel) {
    if (papel == null) {
      return "";
    }
    return switch (papel) {
      case ADMIN -> "Admin";
      case SESMT -> "SESMT";
      case ALMOXARIFE -> "Almoxarife";
      case CONSULTA -> "Consulta";
    };
  }

  private static boolean contem(String valor, String alvo) {
    return valor != null && valor.toLowerCase(Locale.ROOT).contains(alvo);
  }

  private static boolean textoPreenchido(String valor) {
    return valor != null && !valor.isBlank();
  }

  private static TableColumn<UsuarioAdminResumo, String> coluna(
      String titulo, Function<UsuarioAdminResumo, String> valor) {
    TableColumn<UsuarioAdminResumo, String> column = new TableColumn<>(titulo);
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
