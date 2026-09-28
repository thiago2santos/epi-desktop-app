package br.com.easynr6.gestaoepi.ui;

import br.com.easynr6.gestaoepi.shared.audit.AuditService;
import br.com.easynr6.gestaoepi.shared.auth.AuthService;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.login.LoginView;
import br.com.easynr6.gestaoepi.ui.shell.MainShellView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class EasyNr6DesktopApp extends Application {

  private static ConfigurableApplicationContext applicationContext;

  private AuthService authService;
  private AuditService auditService;
  private Stage stage;

  public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
    EasyNr6DesktopApp.applicationContext = applicationContext;
  }

  @Override
  public void start(Stage primaryStage) {
    this.stage = primaryStage;
    this.authService = applicationContext.getBean(AuthService.class);
    this.auditService = applicationContext.getBean(AuditService.class);
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
    LoginView loginView = new LoginView(authService, this::abrirShellPrincipal);
    Scene scene = new Scene(loginView, 460, 320);
    stage.setScene(scene);
  }

  private void abrirShellPrincipal(UsuarioAutenticado usuario) {
    auditService.registrarEventoCritico(
        usuario.id(),
        "LOGIN_SUCESSO",
        "USUARIO",
        String.valueOf(usuario.id()),
        "Login realizado no desktop");

    MainShellView shell =
        new MainShellView(
            usuario,
            auditService,
            () -> {
              auditService.registrarEventoCritico(
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
