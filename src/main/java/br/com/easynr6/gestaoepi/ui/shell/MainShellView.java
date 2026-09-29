package br.com.easynr6.gestaoepi.ui.shell;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.admin.UserAdministrationView;
import br.com.easynr6.gestaoepi.ui.cadastros.CadastrosManagementView;
import br.com.easynr6.gestaoepi.ui.operacao.EntregaWizardView;
import java.util.EnumSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainShellView extends BorderPane {

  private final UsuarioAutenticado usuario;
  private final AuditTrail auditTrail;
  private final UserAdministrationService userAdministrationService;
  private final EmployeeManagementService employeeManagementService;
  private final Label conteudoLabel;

  public MainShellView(
      UsuarioAutenticado usuario,
      AuditTrail auditTrail,
      UserAdministrationService userAdministrationService,
      EmployeeManagementService employeeManagementService,
      Runnable onLogout) {
    this.usuario = usuario;
    this.auditTrail = auditTrail;
    this.userAdministrationService = userAdministrationService;
    this.employeeManagementService = employeeManagementService;
    setLeft(buildSidebar());
    setTop(buildHeader(onLogout));
    this.conteudoLabel = new Label("Selecione um modulo para comecar.");
    conteudoLabel.setStyle("-fx-font-size: 18px;");
    renderContent(conteudoLabel);
  }

  private VBox buildSidebar() {
    VBox sidebar = new VBox(8);
    sidebar.setPadding(new Insets(16));
    sidebar.setPrefWidth(260);
    sidebar.setStyle("-fx-background-color: #f3f4f6;");

    Label title = new Label("Modulos");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    sidebar.getChildren().add(title);

    for (Modulo modulo : Modulo.values()) {
      Button button = new Button(modulo.label);
      button.setMaxWidth(Double.MAX_VALUE);
      button.setAlignment(Pos.CENTER_LEFT);
      boolean habilitado = modulo.papeisPermitidos.stream().anyMatch(usuario::temPapel);
      button.setDisable(!habilitado);
      button.setOnAction(event -> acionarModulo(modulo));
      sidebar.getChildren().add(button);
    }
    return sidebar;
  }

  private VBox buildHeader(Runnable onLogout) {
    VBox header = new VBox();
    header.setPadding(new Insets(0, 16, 12, 16));
    header.setStyle("-fx-background-color: #1f2937;");

    MenuBar menuBar = new MenuBar();
    Menu arquivoMenu = new Menu("Arquivo");
    MenuItem sairItem = new MenuItem("Sair");
    sairItem.setOnAction(event -> onLogout.run());
    arquivoMenu.getItems().add(sairItem);

    Menu modulosMenu = new Menu("Modulos");
    for (Modulo modulo : Modulo.values()) {
      boolean habilitado = modulo.papeisPermitidos.stream().anyMatch(usuario::temPapel);
      MenuItem item = new MenuItem(modulo.label);
      item.setDisable(!habilitado);
      item.setOnAction(event -> acionarModulo(modulo));
      modulosMenu.getItems().add(item);
    }

    Menu sessaoMenu = new Menu("Sessao");
    MenuItem perfilItem = new MenuItem("Usuario: " + usuario.login());
    perfilItem.setDisable(true);
    MenuItem logoutItem = new MenuItem("Logout");
    logoutItem.setOnAction(event -> onLogout.run());
    sessaoMenu.getItems().addAll(perfilItem, new SeparatorMenuItem(), logoutItem);

    menuBar.getMenus().addAll(arquivoMenu, modulosMenu, sessaoMenu);

    BorderPane topLine = new BorderPane();
    topLine.setPadding(new Insets(8, 0, 0, 0));
    Label produto = new Label("Easy NR6 Gestao de EPI");
    produto.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
    topLine.setLeft(produto);

    Label usuarioInfo = new Label(usuario.nome() + " (" + usuario.login() + ")");
    usuarioInfo.setStyle("-fx-text-fill: #e5e7eb;");

    BorderPane right = new BorderPane();
    right.setLeft(usuarioInfo);
    Region spacer = new Region();
    spacer.setMinWidth(10);
    right.setCenter(spacer);

    topLine.setRight(right);
    setMargin(right, new Insets(0, 0, 0, 16));
    header.getChildren().addAll(menuBar, topLine);
    return header;
  }

  private void acionarModulo(Modulo modulo) {
    renderContent(contentFor(modulo));
    auditTrail.registrarEventoCritico(
        usuario.id(), "ACESSO_MODULO", "MODULO", modulo.name(), "Acesso ao modulo " + modulo.label);
  }

  private Node contentFor(Modulo modulo) {
    if (modulo == Modulo.OPERACAO) {
      return new EntregaWizardView(usuario, auditTrail);
    }
    if (modulo == Modulo.ADMINISTRACAO) {
      return new UserAdministrationView(usuario, userAdministrationService);
    }
    if (modulo == Modulo.CADASTROS) {
      return new CadastrosManagementView(usuario, employeeManagementService);
    }
    conteudoLabel.setText("Modulo selecionado: " + modulo.label);
    return conteudoLabel;
  }

  private void renderContent(Node node) {
    setCenter(node);
    setMargin(node, new Insets(16));
  }

  private enum Modulo {
    DASHBOARD("Dashboard", EnumSet.of(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE, Papel.CONSULTA)),
    OPERACAO("Operacao", EnumSet.of(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE)),
    CADASTROS("Cadastros", EnumSet.of(Papel.ADMIN, Papel.SESMT)),
    ESTOQUE("Estoque", EnumSet.of(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE, Papel.CONSULTA)),
    REGRAS("Regras", EnumSet.of(Papel.ADMIN, Papel.SESMT)),
    RELATORIOS(
        "Relatorios", EnumSet.of(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE, Papel.CONSULTA)),
    AUDITORIA("Auditoria", EnumSet.of(Papel.ADMIN, Papel.SESMT, Papel.CONSULTA)),
    ADMINISTRACAO("Administracao", EnumSet.of(Papel.ADMIN));

    private final String label;
    private final EnumSet<Papel> papeisPermitidos;

    Modulo(String label, EnumSet<Papel> papeisPermitidos) {
      this.label = label;
      this.papeisPermitidos = papeisPermitidos;
    }
  }
}
