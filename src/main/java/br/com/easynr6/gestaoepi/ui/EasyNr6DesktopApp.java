package br.com.easynr6.gestaoepi.ui;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.shared.audit.AuditQueryService;
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
  private AuditTrail auditTrail;
  private Stage stage;

  public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
    EasyNr6DesktopApp.applicationContext = applicationContext;
  }

  @Override
  public void start(Stage primaryStage) {
    Enr6Theme.install();
    this.stage = primaryStage;
    this.authenticationProvider = applicationContext.getBean(AuthenticationProvider.class);
    this.credentialManager = applicationContext.getBean(CredentialManager.class);
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
    stage.setMaximized(false);
    Scene scene = new Scene(loginView, 620, 760);
    Enr6Theme.apply(scene);
    stage.setScene(scene);
    stage.centerOnScreen();
  }

  private void abrirTelaTrocaObrigatoria(UsuarioAutenticado usuario) {
    stage.setMaximized(false);
    ChangePasswordView view =
        new ChangePasswordView(credentialManager, usuario, this::abrirShellPrincipal);
    Scene scene = new Scene(view, 620, 420);
    Enr6Theme.apply(scene);
    stage.setScene(scene);
    stage.centerOnScreen();
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
            applicationContext.getBean(EmployeeManagementService.class),
            applicationContext.getBean(EpiCatalogManagementService.class),
            applicationContext.getBean(AuditQueryService.class),
            applicationContext.getBean(UserAdministrationService.class),
            () -> {
              auditTrail.registrarEventoCritico(
                  usuario.id(),
                  "LOGOUT",
                  "USUARIO",
                  String.valueOf(usuario.id()),
                  "Logout realizado no desktop");
              abrirTelaLogin();
            });

    Scene scene = new Scene(shell, 1280, 800);
    Enr6Theme.apply(scene);
    stage.setScene(scene);
    stage.setMaximized(true);
  }
}
