package br.com.easynr6.gestaoepi.ui.login;

import br.com.easynr6.gestaoepi.shared.auth.AuthenticationProvider;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.util.Optional;
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
  private final TextField loginField;
  private final PasswordField senhaField;
  private final Label feedbackLabel;

  public LoginView(
      AuthenticationProvider authenticationProvider, Consumer<UsuarioAutenticado> onLoginSuccess) {
    this.authenticationProvider = authenticationProvider;
    this.onLoginSuccess = onLoginSuccess;

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
    Optional<UsuarioAutenticado> usuario =
        authenticationProvider.autenticar(loginField.getText(), senhaField.getText());
    if (usuario.isEmpty()) {
      feedbackLabel.setText("Credenciais invalidas ou usuario inativo.");
      return;
    }
    senhaField.clear();
    onLoginSuccess.accept(usuario.get());
  }
}
