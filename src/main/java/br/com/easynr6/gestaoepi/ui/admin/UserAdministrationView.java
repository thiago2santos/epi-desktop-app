package br.com.easynr6.gestaoepi.ui.admin;

import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.DuplicateLoginException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.shared.auth.WeakPasswordException;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UserAdministrationView extends BorderPane {

  private final UsuarioAutenticado admin;
  private final UserAdministrationService userAdministrationService;
  private final Label tituloLabel = new Label("Administracao de usuarios");
  private final BorderPane contentPane = new BorderPane();

  public UserAdministrationView(
      UsuarioAutenticado admin, UserAdministrationService userAdministrationService) {
    this.admin = admin;
    this.userAdministrationService = userAdministrationService;

    setPadding(new Insets(16));

    tituloLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    VBox navegacao = buildNavigationMenu();
    VBox topo = new VBox(8, tituloLabel);
    topo.setPadding(new Insets(0, 0, 8, 0));

    setTop(topo);
    setLeft(navegacao);
    setCenter(contentPane);

    abrirTelaCadastro();
  }

  private VBox buildNavigationMenu() {
    VBox menu = new VBox(8);
    menu.setPadding(new Insets(8, 12, 8, 0));
    menu.setPrefWidth(230);

    Button cadastroButton = new Button("Cadastrar usuario");
    cadastroButton.setMaxWidth(Double.MAX_VALUE);
    cadastroButton.setOnAction(event -> abrirTelaCadastro());

    Button papelButton = new Button("Atribuir papel");
    papelButton.setMaxWidth(Double.MAX_VALUE);
    papelButton.setOnAction(event -> abrirTelaAtribuicaoPapel());

    Button resetButton = new Button("Resetar credencial");
    resetButton.setMaxWidth(Double.MAX_VALUE);
    resetButton.setOnAction(event -> abrirTelaResetCredencial());

    Button gestaoButton = new Button("Gerenciar usuarios");
    gestaoButton.setMaxWidth(Double.MAX_VALUE);
    gestaoButton.setOnAction(event -> abrirTelaGestaoUsuarios());

    menu.getChildren()
        .addAll(
            new Label("Acoes administrativas"),
            cadastroButton,
            papelButton,
            resetButton,
            gestaoButton);
    return menu;
  }

  private void abrirTelaCadastro() {
    tituloLabel.setText("Administracao de usuarios - cadastro");
    contentPane.setCenter(buildCadastroBox());
  }

  private VBox buildCadastroBox() {
    TextField nomeField = new TextField();
    nomeField.setPromptText("Nome completo");
    TextField loginField = new TextField();
    loginField.setPromptText("login");
    PasswordField senhaField = new PasswordField();
    senhaField.setPromptText("Senha inicial");
    CheckBox ativoCheck = new CheckBox("Ativo");
    ativoCheck.setSelected(true);
    ComboBox<Papel> papelCombo = new ComboBox<>(FXCollections.observableArrayList(Papel.values()));
    papelCombo.getSelectionModel().select(Papel.SESMT);
    Label feedback = new Label();

    GridPane grid = new GridPane();
    grid.setHgap(8);
    grid.setVgap(8);
    grid.addRow(0, new Label("Nome"), nomeField);
    grid.addRow(1, new Label("Login"), loginField);
    grid.addRow(2, new Label("Senha inicial"), senhaField);
    grid.addRow(3, new Label("Papel"), papelCombo);
    grid.add(ativoCheck, 1, 4);

    Button cadastrarButton = new Button("Cadastrar");
    cadastrarButton.setOnAction(
        event -> {
          try {
            userAdministrationService.cadastrarUsuario(
                admin.id(),
                nomeField.getText(),
                loginField.getText(),
                senhaField.getText(),
                ativoCheck.isSelected(),
                java.util.Set.of(papelCombo.getValue()));
            mostrarSucesso(feedback, "AUTH-100 Usuario cadastrado com sucesso.");
            limpar(nomeField, loginField, senhaField);
          } catch (WeakPasswordException
              | DuplicateLoginException
              | AuthorizationDeniedException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao cadastrar usuario.");
          }
        });

    HBox acoes = new HBox(10, cadastrarButton, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    return new VBox(10, new Label("Cadastro de usuario"), grid, acoes);
  }

  private void abrirTelaAtribuicaoPapel() {
    tituloLabel.setText("Administracao de usuarios - atribuicao de papel");
    contentPane.setCenter(buildAtribuicaoPapelBox());
  }

  private VBox buildAtribuicaoPapelBox() {
    TextField loginField = new TextField();
    loginField.setPromptText("Login do usuario");
    ComboBox<Papel> papelCombo = new ComboBox<>(FXCollections.observableArrayList(Papel.values()));
    papelCombo.getSelectionModel().select(Papel.CONSULTA);
    Label ajuda =
        new Label(
            "Um usuario pode ter mais de um papel. Em empresas pequenas, uma pessoa pode acumular papeis.");
    ajuda.setWrapText(true);
    Label feedback = new Label();

    Button atribuirButton = new Button("Atribuir");
    atribuirButton.setOnAction(
        event -> {
          try {
            userAdministrationService.atribuirPapelPorLogin(
                admin.id(), loginField.getText(), papelCombo.getValue());
            mostrarSucesso(feedback, "AUTH-101 Papel atribuido com sucesso.");
            loginField.clear();
          } catch (AuthorizationDeniedException | IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao atribuir papel.");
          }
        });
    HBox acoes = new HBox(10, atribuirButton, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    return new VBox(
        10,
        new Label("Atribuicao de papel"),
        ajuda,
        new Label("Login"),
        loginField,
        new Label("Papel"),
        papelCombo,
        acoes);
  }

  private void abrirTelaResetCredencial() {
    tituloLabel.setText("Administracao de usuarios - reset de credencial");
    contentPane.setCenter(buildResetCredencialBox());
  }

  private VBox buildResetCredencialBox() {
    TextField loginField = new TextField();
    loginField.setPromptText("Login do usuario");
    PasswordField novaSenhaField = new PasswordField();
    novaSenhaField.setPromptText("Nova credencial");
    Label feedback = new Label();
    Button resetButton = new Button("Resetar");
    resetButton.setOnAction(
        event -> {
          try {
            userAdministrationService.resetarCredencialPorLogin(
                admin.id(), loginField.getText(), novaSenhaField.getText());
            mostrarSucesso(feedback, "AUTH-102 Credencial resetada com sucesso.");
            limpar(loginField, novaSenhaField);
          } catch (WeakPasswordException
              | AuthorizationDeniedException
              | IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao resetar credencial.");
          }
        });
    HBox acoes = new HBox(10, resetButton, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    return new VBox(
        10,
        new Label("Reset de credencial"),
        new Label("Login"),
        loginField,
        new Label("Nova credencial"),
        novaSenhaField,
        acoes);
  }

  private void abrirTelaGestaoUsuarios() {
    tituloLabel.setText("Administracao de usuarios - gestao completa");
    contentPane.setCenter(buildGestaoUsuariosBox());
  }

  private VBox buildGestaoUsuariosBox() {
    ListView<UsuarioAdminResumo> lista = new ListView<>();
    lista.setPrefHeight(220);
    lista.setCellFactory(
        l ->
            new javafx.scene.control.ListCell<>() {
              @Override
              protected void updateItem(UsuarioAdminResumo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  return;
                }
                setText(
                    "#"
                        + item.id()
                        + " | "
                        + item.login()
                        + " | "
                        + (item.ativo() ? "ATIVO" : "BLOQUEADO")
                        + " | papeis="
                        + item.papeis());
              }
            });

    TextField loginAtualField = new TextField();
    loginAtualField.setPromptText("Login atual");
    TextField novoNomeField = new TextField();
    novoNomeField.setPromptText("Novo nome");
    TextField novoLoginField = new TextField();
    novoLoginField.setPromptText("Novo login");
    CheckBox ativoCheck = new CheckBox("Ativo");
    Label feedback = new Label();

    lista
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldValue, selected) -> {
              if (selected != null) {
                loginAtualField.setText(selected.login());
                novoNomeField.setText(selected.nome());
                novoLoginField.setText(selected.login());
                ativoCheck.setSelected(selected.ativo());
              }
            });

    Button atualizarButton = new Button("Salvar edicao");
    atualizarButton.setOnAction(
        event -> {
          try {
            userAdministrationService.editarUsuarioPorLogin(
                admin.id(),
                loginAtualField.getText(),
                novoNomeField.getText(),
                novoLoginField.getText(),
                ativoCheck.isSelected());
            mostrarSucesso(feedback, "AUTH-103 Usuario atualizado com sucesso.");
            preencherListaUsuarios(lista);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    Button bloquearButton = new Button("Bloquear");
    bloquearButton.setOnAction(
        event -> {
          try {
            userAdministrationService.bloquearUsuarioPorLogin(
                admin.id(), loginAtualField.getText());
            mostrarSucesso(feedback, "AUTH-104 Usuario bloqueado com sucesso.");
            preencherListaUsuarios(lista);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    Button reativarButton = new Button("Reativar");
    reativarButton.setOnAction(
        event -> {
          try {
            userAdministrationService.reativarUsuarioPorLogin(
                admin.id(), loginAtualField.getText());
            mostrarSucesso(feedback, "AUTH-105 Usuario reativado com sucesso.");
            preencherListaUsuarios(lista);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    Button excluirButton = new Button("Excluir");
    excluirButton.setOnAction(
        event -> {
          try {
            userAdministrationService.excluirUsuarioPorLogin(admin.id(), loginAtualField.getText());
            mostrarSucesso(feedback, "AUTH-106 Usuario excluido com sucesso.");
            limpar(loginAtualField, novoNomeField, novoLoginField);
            ativoCheck.setSelected(false);
            preencherListaUsuarios(lista);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    HBox acoes =
        new HBox(8, atualizarButton, bloquearButton, reativarButton, excluirButton, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    VBox box =
        new VBox(
            10,
            new Label("Selecione um usuario para editar, bloquear, reativar ou excluir."),
            lista,
            new Label("Login atual"),
            loginAtualField,
            new Label("Novo nome"),
            novoNomeField,
            new Label("Novo login"),
            novoLoginField,
            ativoCheck,
            acoes);
    preencherListaUsuarios(lista);
    return box;
  }

  private void preencherListaUsuarios(ListView<UsuarioAdminResumo> lista) {
    lista.getItems().setAll(userAdministrationService.listarUsuarios());
  }

  private void mostrarSucesso(Label feedback, String mensagem) {
    feedback.setText(mensagem);
    feedback.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
    PauseTransition pause = new PauseTransition(Duration.seconds(5));
    pause.setOnFinished(event -> feedback.setText(""));
    pause.play();
  }

  private void mostrarErro(Label feedback, String mensagem) {
    feedback.setText(mensagem);
    feedback.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
  }

  private static void limpar(TextField... fields) {
    for (TextField field : fields) {
      field.clear();
    }
  }
}
