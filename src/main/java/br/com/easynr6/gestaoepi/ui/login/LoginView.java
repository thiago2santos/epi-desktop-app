package br.com.easynr6.gestaoepi.ui.login;

import br.com.easynr6.gestaoepi.shared.auth.AuthenticationProvider;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationResult;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationStatus;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {

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

    setSpacing(12);
    setPadding(new Insets(24));
    setAlignment(Pos.CENTER);

    Label titulo = new Label("Easy NR6 Gestao de EPI");
    titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

    Label subtitulo = new Label("Login de acesso");
    subtitulo.setStyle("-fx-text-fill: #4b5563;");

    loginField = new TextField();
    loginField.setPromptText("Login");
    loginField.setMaxWidth(280);

    senhaField = new PasswordField();
    senhaField.setPromptText("Senha");
    senhaField.setMaxWidth(280);

    Button entrarButton = new Button("Entrar");
    entrarButton.setDefaultButton(true);
    entrarButton.setOnAction(event -> autenticar());

    feedbackLabel = new Label();
    feedbackLabel.setStyle("-fx-text-fill: #b91c1c;");

    getChildren().addAll(titulo, subtitulo, loginField, senhaField, entrarButton, feedbackLabel);
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
      case BLOCKED -> "AUTH-002 Conta temporariamente bloqueada. Tente novamente mais tarde.";
      case NO_ROLE -> "AUTH-012 Usuario sem papel operacional. Contate o admin.";
      case INACTIVE_USER -> "AUTH-013 Usuario inativo. Contate o admin.";
      default -> "AUTH-001 Credenciais invalidas.";
    };
  }
}
