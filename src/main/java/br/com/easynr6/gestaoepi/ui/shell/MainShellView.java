package br.com.easynr6.gestaoepi.ui.shell;

import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.operacao.EntregaWizardView;
import java.util.EnumSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainShellView extends BorderPane {

  private final UsuarioAutenticado usuario;
  private final AuditTrail auditTrail;
  private final Label conteudoLabel;

  public MainShellView(UsuarioAutenticado usuario, AuditTrail auditTrail, Runnable onLogout) {
    this.usuario = usuario;
    this.auditTrail = auditTrail;
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
      button.setOnAction(
          event -> {
            renderContent(contentFor(modulo));
            auditTrail.registrarEventoCritico(
                usuario.id(),
                "ACESSO_MODULO",
                "MODULO",
                modulo.name(),
                "Acesso ao modulo " + modulo.label);
          });
      sidebar.getChildren().add(button);
    }
    return sidebar;
  }

  private VBox buildHeader(Runnable onLogout) {
    VBox header = new VBox();
    header.setPadding(new Insets(12, 16, 12, 16));
    header.setStyle("-fx-background-color: #1f2937;");

    BorderPane topLine = new BorderPane();
    Label produto = new Label("Easy NR6 Gestao de EPI");
    produto.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
    topLine.setLeft(produto);

    Label usuarioInfo = new Label(usuario.nome() + " (" + usuario.login() + ")");
    usuarioInfo.setStyle("-fx-text-fill: #e5e7eb;");

    Button sairButton = new Button("Sair");
    sairButton.setOnAction(event -> onLogout.run());

    BorderPane right = new BorderPane();
    right.setLeft(usuarioInfo);
    Region spacer = new Region();
    spacer.setMinWidth(10);
    right.setCenter(spacer);
    right.setRight(sairButton);

    topLine.setRight(right);
    setMargin(right, new Insets(0, 0, 0, 16));
    header.getChildren().add(topLine);
    return header;
  }

  private Node contentFor(Modulo modulo) {
    if (modulo == Modulo.OPERACAO) {
      return new EntregaWizardView(usuario, auditTrail);
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
