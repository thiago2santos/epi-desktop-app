package br.com.easynr6.gestaoepi.ui;

import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiCatalogService;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.DevolucaoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.EstornoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.FornecimentoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.HistoricoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.PendenciaManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.MatrizManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.PeriodicidadeManagementService;
import br.com.easynr6.gestaoepi.modules.stock.application.StockManagementService;
import br.com.easynr6.gestaoepi.shared.audit.AuditQueryService;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationProvider;
import br.com.easynr6.gestaoepi.shared.auth.CredentialManager;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.login.ChangePasswordView;
import br.com.easynr6.gestaoepi.ui.login.EscolhaUnidadeView;
import br.com.easynr6.gestaoepi.ui.login.LoginView;
import br.com.easynr6.gestaoepi.ui.shell.MainShellView;
import java.util.List;
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
        new LoginView(authenticationProvider, this::abrirEscopo, this::abrirTelaTrocaObrigatoria);
    stage.setMaximized(false);
    Scene scene = new Scene(loginView, 620, 760);
    Enr6Theme.apply(scene);
    stage.setScene(scene);
    stage.centerOnScreen();
  }

  private void abrirTelaTrocaObrigatoria(UsuarioAutenticado usuario) {
    stage.setMaximized(false);
    ChangePasswordView view = new ChangePasswordView(credentialManager, usuario, this::abrirEscopo);
    Scene scene = new Scene(view, 620, 420);
    Enr6Theme.apply(scene);
    stage.setScene(scene);
    stage.centerOnScreen();
  }

  private void abrirEscopo(UsuarioAutenticado usuario) {
    List<UnitOption> unidades;
    try {
      unidades = unidadesAcessiveis(usuario);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("LISTAR_UNIDADES_SESSAO", usuario.id(), usuario.login(), ex);
      unidades = List.of();
    }
    if (unidades.size() == 1) {
      UnitOption unica = unidades.getFirst();
      abrirShell(usuario.naUnidade(unica.id(), EscolhaUnidadeView.rotulo(unica)), true);
      return;
    }
    abrirEscolha(usuario, unidades);
  }

  private void abrirEscolha(UsuarioAutenticado usuario, List<UnitOption> unidades) {
    stage.setMaximized(false);
    EscolhaUnidadeView view =
        new EscolhaUnidadeView(
            unidades,
            escolhida ->
                abrirShell(
                    usuario.naUnidade(escolhida.id(), EscolhaUnidadeView.rotulo(escolhida)), true),
            this::abrirTelaLogin);
    Scene scene = new Scene(view, 620, 480);
    Enr6Theme.apply(scene);
    stage.setScene(scene);
    stage.centerOnScreen();
  }

  private List<UnitOption> unidadesAcessiveis(UsuarioAutenticado usuario) {
    List<UnitOption> ativas =
        applicationContext.getBean(EmployeeManagementService.class).listActiveUnits();
    if (usuario.veTodasAsUnidades()) {
      return ativas;
    }
    Long vinculo = usuario.unidadeVinculoId();
    if (vinculo == null) {
      return List.of();
    }
    return ativas.stream().filter(item -> vinculo.equals(item.id())).toList();
  }

  private void abrirShell(UsuarioAutenticado usuario, boolean registrarLogin) {
    if (registrarLogin) {
      auditTrail.registrarEventoCritico(
          usuario.id(),
          "LOGIN_SUCESSO",
          "USUARIO",
          String.valueOf(usuario.id()),
          "Login realizado no desktop na unidade " + usuario.unidadeNome());
    }

    MainShellView shell =
        new MainShellView(
            usuario,
            auditTrail,
            applicationContext.getBean(EmployeeManagementService.class),
            applicationContext.getBean(EpiCatalogManagementService.class),
            applicationContext.getBean(AuditQueryService.class),
            applicationContext.getBean(UserAdministrationService.class),
            applicationContext.getBean(StockManagementService.class),
            applicationContext.getBean(CaepiCatalogService.class),
            applicationContext.getBean(MatrizManagementService.class),
            applicationContext.getBean(PeriodicidadeManagementService.class),
            applicationContext.getBean(FornecimentoManagementService.class),
            applicationContext.getBean(DevolucaoManagementService.class),
            applicationContext.getBean(EstornoManagementService.class),
            applicationContext.getBean(HistoricoManagementService.class),
            applicationContext.getBean(PendenciaManagementService.class),
            opcao -> {
              auditTrail.registrarEventoCritico(
                  usuario.id(),
                  "UNIDADE_SESSAO",
                  "UNIDADE",
                  String.valueOf(opcao.id()),
                  "Sessão passou a gerenciar " + EscolhaUnidadeView.rotulo(opcao));
              abrirShell(usuario.naUnidade(opcao.id(), EscolhaUnidadeView.rotulo(opcao)), false);
            },
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
