package br.com.easynr6.gestaoepi.ui;

import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationProvider;
import br.com.easynr6.gestaoepi.shared.auth.CredentialManager;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.login.ChangePasswordView;
import br.com.easynr6.gestaoepi.ui.login.LoginView;
import br.com.easynr6.gestaoepi.ui.shell.MainShellView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class EasyNr6DesktopApp extends Application {

  private static ConfigurableApplicationContext applicationContext;

  private AuthenticationProvider authenticationProvider;
  private CredentialManager credentialManager;
  private UserAdministrationService userAdministrationService;
  private AuditTrail auditTrail;
  private Stage stage;

  public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
    EasyNr6DesktopApp.applicationContext = applicationContext;
  }

  @Override
  public void start(Stage primaryStage) {
    this.stage = primaryStage;
    this.authenticationProvider = applicationContext.getBean(AuthenticationProvider.class);
    this.credentialManager = applicationContext.getBean(CredentialManager.class);
    this.userAdministrationService = applicationContext.getBean(UserAdministrationService.class);
    this.auditTrail = applicationContext.getBean(AuditTrail.class);
    this.stage.setTitle("Easy NR6 Gestao de EPI");
    abrirTelaLogin();
    this.stage.show();
  }

  @Override
  public void stop() {
    if (applicationContext != null) {
      applicationContext.close();
    }
  }

  private void abrirTelaLogin() {
    LoginView loginView =
        new LoginView(
            authenticationProvider, this::abrirShellPrincipal, this::abrirTelaTrocaObrigatoria);
    Scene scene = new Scene(loginView, 460, 320);
    stage.setScene(scene);
  }

  private void abrirTelaTrocaObrigatoria(UsuarioAutenticado usuario) {
    ChangePasswordView view =
        new ChangePasswordView(credentialManager, usuario, this::abrirShellPrincipal);
    stage.setScene(new Scene(view, 520, 340));
  }

  private void abrirShellPrincipal(UsuarioAutenticado usuario) {
    auditTrail.registrarEventoCritico(
        usuario.id(),
        "LOGIN_SUCESSO",
        "USUARIO",
        String.valueOf(usuario.id()),
        "Login realizado no desktop");

    MainShellView shell =
        new MainShellView(
            usuario,
            auditTrail,
            userAdministrationService,
            () -> {
              auditTrail.registrarEventoCritico(
                  usuario.id(),
                  "LOGOUT",
                  "USUARIO",
                  String.valueOf(usuario.id()),
                  "Logout realizado no desktop");
              abrirTelaLogin();
            });

    stage.setScene(new Scene(shell, 1180, 760));
  }
}
