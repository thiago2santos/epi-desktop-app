package br.com.easynr6.gestaoepi.ui.login;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.shared.auth.CredentialManager;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.shared.auth.WeakPasswordException;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

public class ChangePasswordView extends VBox {

  private final CredentialManager credentialManager;
  private final UsuarioAutenticado usuario;
  private final Consumer<UsuarioAutenticado> onSuccess;
  private final PasswordField novaSenhaField;
  private final PasswordField confirmarSenhaField;
  private final Label feedbackLabel;

  public ChangePasswordView(
      CredentialManager credentialManager,
      UsuarioAutenticado usuario,
      Consumer<UsuarioAutenticado> onSuccess) {
    this.credentialManager = credentialManager;
    this.usuario = usuario;
    this.onSuccess = onSuccess;

    setSpacing(12);
    setPadding(new Insets(24));
    setAlignment(Pos.CENTER);

    Label titulo = new Label("Atualizacao obrigatoria de credencial");
    titulo.getStyleClass().add(Enr6Styles.PAGE_TITLE);

    Label subtitulo = new Label("Defina uma nova credencial para continuar.");
    subtitulo.getStyleClass().add(Enr6Styles.PAGE_DESCRIPTION);

    novaSenhaField = new PasswordField();
    novaSenhaField.setPromptText("Nova senha");
    novaSenhaField.setMaxWidth(320);

    confirmarSenhaField = new PasswordField();
    confirmarSenhaField.setPromptText("Confirmar nova senha");
    confirmarSenhaField.setMaxWidth(320);

    Button atualizarButton = new Button("Atualizar credencial");
    atualizarButton.getStyleClass().add(Styles.ACCENT);
    atualizarButton.setDefaultButton(true);
    atualizarButton.setOnAction(event -> atualizarCredencial());

    feedbackLabel = new Label();
    feedbackLabel.getStyleClass().add(Enr6Styles.FEEDBACK_DANGER);

    getChildren()
        .addAll(
            titulo, subtitulo, novaSenhaField, confirmarSenhaField, atualizarButton, feedbackLabel);
  }

  private void atualizarCredencial() {
    feedbackLabel.setText("");
    if (!novaSenhaField.getText().equals(confirmarSenhaField.getText())) {
      feedbackLabel.setText("As credenciais informadas não conferem.");
      return;
    }
    try {
      credentialManager.alterarCredencialObrigatoria(
          usuario.id(), usuario.login(), novaSenhaField.getText());
      novaSenhaField.clear();
      confirmarSenhaField.clear();
      onSuccess.accept(usuario);
    } catch (WeakPasswordException ex) {
      feedbackLabel.setText(ex.getMessage());
    }
  }
}
