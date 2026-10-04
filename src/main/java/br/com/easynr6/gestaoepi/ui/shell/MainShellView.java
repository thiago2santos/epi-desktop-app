package br.com.easynr6.gestaoepi.ui.shell;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.cadastro.CaBindingManagementView;
import br.com.easynr6.gestaoepi.ui.cadastro.EmployeeManagementView;
import br.com.easynr6.gestaoepi.ui.cadastro.EpiManagementView;
import br.com.easynr6.gestaoepi.ui.cadastro.OrgStructureManagementView;
import java.util.EnumMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainShellView extends BorderPane {

  private final UsuarioAutenticado usuario;
  private final AuditTrail auditTrail;
  private final EmployeeManagementService empregados;
  private final EpiCatalogManagementService catalogo;
  private final Map<Destino, Button> navButtons = new EnumMap<>(Destino.class);
  private final Map<Destino.Grupo, VBox> grupoItens = new EnumMap<>(Destino.Grupo.class);
  private final Map<Destino.Grupo, Label> grupoSetas = new EnumMap<>(Destino.Grupo.class);
  private Destino destinoAtual;

  public MainShellView(
      UsuarioAutenticado usuario,
      AuditTrail auditTrail,
      EmployeeManagementService empregados,
      EpiCatalogManagementService catalogo,
      Runnable onLogout) {
    this.usuario = usuario;
    this.auditTrail = auditTrail;
    this.empregados = empregados;
    this.catalogo = catalogo;
    setTop(buildHeader(onLogout));
    setLeft(buildSidebar());
    abrir(Destino.DASHBOARD, false);
  }

  private VBox buildHeader(Runnable onLogout) {
    VBox header = new VBox();
    header.getStyleClass().add(Enr6Styles.SHELL_HEADER);

    MenuBar menuBar = new MenuBar();
    Menu arquivoMenu = new Menu("Arquivo");
    MenuItem sairItem = new MenuItem("Sair");
    sairItem.setOnAction(event -> onLogout.run());
    arquivoMenu.getItems().add(sairItem);

    Menu modulosMenu = new Menu("Módulos");
    for (Destino.Grupo grupo : Destino.Grupo.values()) {
      if (!modulosMenu.getItems().isEmpty()) {
        modulosMenu.getItems().add(new SeparatorMenuItem());
      }
      for (Destino destino : grupo.destinos()) {
        MenuItem item = new MenuItem(grupo.titulo() + " · " + destino.label());
        boolean habilitado = destino.visivelPara(usuario::temPapel);
        item.setDisable(!habilitado);
        item.setOnAction(event -> abrir(destino, true));
        modulosMenu.getItems().add(item);
      }
    }

    Menu sessaoMenu = new Menu("Sessão");
    MenuItem perfilItem = new MenuItem("Usuário: " + usuario.login());
    perfilItem.setDisable(true);
    MenuItem logoutItem = new MenuItem("Sair");
    logoutItem.setOnAction(event -> onLogout.run());
    sessaoMenu.getItems().addAll(perfilItem, new SeparatorMenuItem(), logoutItem);
    menuBar.getMenus().addAll(arquivoMenu, modulosMenu, sessaoMenu);

    BorderPane topLine = new BorderPane();
    topLine.setPadding(new Insets(8, 16, 12, 16));
    Label produto = new Label("Easy NR6 · Gestão de EPI");
    produto.getStyleClass().add(Enr6Styles.SHELL_BRAND);
    Label usuarioInfo = new Label(usuario.nome() + " (" + usuario.login() + ")");
    usuarioInfo.getStyleClass().add(Enr6Styles.SHELL_META);
    topLine.setLeft(produto);
    topLine.setRight(usuarioInfo);
    header.getChildren().addAll(menuBar, topLine);

    Label status = new Label("CAEPI: carga válida 28/09/2026 · operação normal · Unidade: Itupeva");
    status.getStyleClass().add(Enr6Styles.STATUS_STRIP);
    status.setMaxWidth(Double.MAX_VALUE);
    VBox chrome = new VBox(header, status);
    return chrome;
  }

  private ScrollPane buildSidebar() {
    VBox sidebar = new VBox(4);
    sidebar.setPadding(new Insets(8, 8, 16, 8));
    sidebar.setPrefWidth(260);
    sidebar.getStyleClass().add(Enr6Styles.SHELL_SIDEBAR);

    for (Destino.Grupo grupo : Destino.Grupo.values()) {
      VBox itens = new VBox(2);
      Label seta = new Label("▸");
      seta.getStyleClass().add(Enr6Styles.NAV_CHEVRON);
      seta.setRotate(90);
      Label titulo = new Label(grupo.titulo().toUpperCase());
      titulo.getStyleClass().add(Enr6Styles.SHELL_SIDEBAR_TITLE);
      HBox cabecalho = new HBox(8, seta, titulo);
      cabecalho.setAlignment(Pos.CENTER_LEFT);
      Button head = new Button();
      head.setGraphic(cabecalho);
      head.getStyleClass().addAll(Styles.FLAT, Enr6Styles.NAV_GROUP_HEAD);
      head.setMaxWidth(Double.MAX_VALUE);
      head.setAlignment(Pos.CENTER_LEFT);
      head.setOnAction(event -> setGrupoAberto(grupo, !itens.isVisible()));
      for (Destino destino : grupo.destinos()) {
        Button button = new Button(destino.label());
        button.getStyleClass().addAll(Styles.FLAT, Enr6Styles.NAV_ITEM);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setDisable(!destino.visivelPara(usuario::temPapel));
        button.setOnAction(event -> abrir(destino, true));
        navButtons.put(destino, button);
        itens.getChildren().add(button);
      }
      grupoItens.put(grupo, itens);
      grupoSetas.put(grupo, seta);
      sidebar.getChildren().addAll(head, itens);
    }

    ScrollPane scroll = new ScrollPane(sidebar);
    scroll.setFitToWidth(true);
    scroll.setPrefWidth(260);
    scroll.setMinWidth(260);
    return scroll;
  }

  private void abrir(Destino destino, boolean auditar) {
    if (destinoAtual != null) {
      Button anterior = navButtons.get(destinoAtual);
      if (anterior != null) {
        anterior.getStyleClass().remove(Enr6Styles.NAV_ITEM_ACTIVE);
      }
    }
    destinoAtual = destino;
    setGrupoAberto(destino.grupo(), true);
    Button atual = navButtons.get(destino);
    if (atual != null && !atual.getStyleClass().contains(Enr6Styles.NAV_ITEM_ACTIVE)) {
      atual.getStyleClass().add(Enr6Styles.NAV_ITEM_ACTIVE);
    }
    Node conteudo = conteudo(destino);
    setCenter(conteudo);
    setMargin(conteudo, new Insets(16));
    if (auditar) {
      auditTrail.registrarEventoCritico(
          usuario.id(), "ACESSO_MODULO", "MODULO", destino.name(), "Acesso a " + destino.label());
    }
  }

  private void setGrupoAberto(Destino.Grupo grupo, boolean aberto) {
    VBox itens = grupoItens.get(grupo);
    if (itens != null) {
      itens.setVisible(aberto);
      itens.setManaged(aberto);
    }
    Label seta = grupoSetas.get(grupo);
    if (seta != null) {
      seta.setRotate(aberto ? 90 : 0);
    }
  }

  private Node conteudo(Destino destino) {
    if (destino == Destino.TRABALHADORES) {
      return new EmployeeManagementView(usuario, empregados, alvo -> abrir(alvo, true)).root();
    }
    if (destino == Destino.SETORES) {
      return new OrgStructureManagementView(usuario, empregados).root();
    }
    if (destino == Destino.EPI) {
      return new EpiManagementView(usuario, catalogo, alvo -> abrir(alvo, true)).root();
    }
    if (destino == Destino.CA) {
      return new CaBindingManagementView(usuario, catalogo, alvo -> abrir(alvo, true)).root();
    }
    return TelasReferencia.criar(destino, alvo -> abrir(alvo, true));
  }
}
