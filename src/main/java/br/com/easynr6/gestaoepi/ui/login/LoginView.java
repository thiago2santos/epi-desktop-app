package br.com.easynr6.gestaoepi.ui.login;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationProvider;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationResult;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationStatus;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LoginView extends BorderPane {

  private final AuthenticationProvider authenticationProvider;
  private final Consumer<UsuarioAutenticado> onLoginSuccess;
  private final Consumer<UsuarioAutenticado> onPasswordChangeRequired;
  private final TextField loginField;
  private final PasswordField senhaField;
  private final Label feedbackLabel;

  public LoginView(
      AuthenticationProvider authenticationProvider,
      Consumer<UsuarioAutenticado> onLoginSuccess,
      Consumer<UsuarioAutenticado> onPasswordChangeRequired) {
    this.authenticationProvider = authenticationProvider;
    this.onLoginSuccess = onLoginSuccess;
    this.onPasswordChangeRequired = onPasswordChangeRequired;

    setPadding(new Insets(32));
    getStyleClass().add(Enr6Styles.LOGIN_ROOT);

    VBox card = new VBox(14);
    card.setPadding(new Insets(28));
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPrefWidth(420);
    card.setMaxWidth(420);
    card.getStyleClass().add(Enr6Styles.LOGIN_CARD);

    Label titulo = new Label("Easy NR6 Gestao de EPI");
    titulo.getStyleClass().add(Enr6Styles.LOGIN_TITLE);

    Label subtitulo = new Label("Login de acesso");
    subtitulo.getStyleClass().add(Enr6Styles.LOGIN_SUBTITLE);

    loginField = new TextField();
    loginField.setPromptText("Login");
    loginField.setPrefWidth(360);
    loginField.setMaxWidth(Double.MAX_VALUE);

    senhaField = new PasswordField();
    senhaField.setPromptText("Senha");
    senhaField.setPrefWidth(360);
    senhaField.setMaxWidth(Double.MAX_VALUE);

    Button entrarButton = new Button("Entrar");
    entrarButton.getStyleClass().add(Styles.ACCENT);
    entrarButton.setDefaultButton(true);
    entrarButton.setOnAction(event -> autenticar());
    entrarButton.setPrefHeight(40);
    entrarButton.setMaxWidth(Double.MAX_VALUE);

    feedbackLabel = new Label();
    feedbackLabel.getStyleClass().add(Enr6Styles.FEEDBACK_DANGER);
    feedbackLabel.setWrapText(true);
    feedbackLabel.setMaxWidth(Double.MAX_VALUE);

    card.getChildren()
        .addAll(titulo, subtitulo, loginField, senhaField, entrarButton, feedbackLabel);
    setCenter(card);
    setAlignment(card, Pos.CENTER);
  }

  private void autenticar() {
    feedbackLabel.setText("");
    AuthenticationResult resultado =
        authenticationProvider.autenticar(loginField.getText(), senhaField.getText());
    senhaField.clear();
    AuthenticationStatus status = resultado.status();
    if (status == AuthenticationStatus.SUCCESS && resultado.usuario() != null) {
      onLoginSuccess.accept(resultado.usuario());
      return;
    }
    if (status == AuthenticationStatus.FORCE_PASSWORD_CHANGE && resultado.usuario() != null) {
      onPasswordChangeRequired.accept(resultado.usuario());
      return;
    }
    feedbackLabel.setText(mensagemErro(status));
  }

  private static String mensagemErro(AuthenticationStatus status) {
    return switch (status) {
      case BLOCKED -> "Conta temporariamente bloqueada. Tente novamente mais tarde.";
      case NO_ROLE -> "Usuário sem papel operacional. Contate o admin.";
      case INACTIVE_USER -> "Usuário inativo. Contate o admin.";
      default -> "Credenciais inválidas.";
    };
  }
}
