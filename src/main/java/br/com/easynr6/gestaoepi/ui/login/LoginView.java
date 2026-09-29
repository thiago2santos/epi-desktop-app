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
    setStyle("-fx-background-color: linear-gradient(to bottom, #eef2ff, #f8fafc);");

    VBox card = new VBox(14);
    card.setPadding(new Insets(30));
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPrefWidth(500);
    card.setMaxWidth(500);
    card.setStyle(
        "-fx-background-color: white;"
            + "-fx-background-radius: 12;"
            + "-fx-border-color: #cbd5e1;"
            + "-fx-border-radius: 12;");

    Label titulo = new Label("Easy NR6 Gestao de EPI");
    titulo.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

    Label subtitulo = new Label("Login de acesso");
    subtitulo.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 16px;");

    loginField = new TextField();
    loginField.setPromptText("Login");
    loginField.setPrefWidth(440);
    loginField.setMaxWidth(Double.MAX_VALUE);
    loginField.setStyle("-fx-font-size: 16px; -fx-padding: 10 12 10 12;");

    senhaField = new PasswordField();
    senhaField.setPromptText("Senha");
    senhaField.setPrefWidth(440);
    senhaField.setMaxWidth(Double.MAX_VALUE);
    senhaField.setStyle("-fx-font-size: 16px; -fx-padding: 10 12 10 12;");

    Button entrarButton = new Button("Entrar");
    entrarButton.setDefaultButton(true);
    entrarButton.setOnAction(event -> autenticar());
    entrarButton.setPrefHeight(42);
    entrarButton.setMaxWidth(Double.MAX_VALUE);
    entrarButton.setStyle(
        "-fx-font-size: 16px;"
            + "-fx-font-weight: bold;"
            + "-fx-background-color: #1d4ed8;"
            + "-fx-text-fill: white;");

    feedbackLabel = new Label();
    feedbackLabel.setStyle("-fx-text-fill: #991b1b; -fx-font-size: 14px; -fx-font-weight: bold;");
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
      case BLOCKED -> "AUTH-002 Conta temporariamente bloqueada. Tente novamente mais tarde.";
      case NO_ROLE -> "AUTH-012 Usuario sem papel operacional. Contate o admin.";
      case INACTIVE_USER -> "AUTH-013 Usuario inativo. Contate o admin.";
      default -> "AUTH-001 Credenciais invalidas.";
    };
  }
}
