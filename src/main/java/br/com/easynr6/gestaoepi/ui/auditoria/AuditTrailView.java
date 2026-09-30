package br.com.easynr6.gestaoepi.ui.auditoria;

import br.com.easynr6.gestaoepi.shared.audit.AuditQueryService;
import br.com.easynr6.gestaoepi.shared.audit.AuditQueryService.AuditEventSummary;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AuditTrailView extends VBox {

  private final UsuarioAutenticado actor;
  private final AuditQueryService auditQueryService;
  private final ObservableList<AuditEventSummary> events = FXCollections.observableArrayList();
  private final TextField searchField = new TextField();
  private final TableView<AuditEventSummary> table = new TableView<>(events);
  private final Label infoLabel = new Label();

  public AuditTrailView(UsuarioAutenticado actor, AuditQueryService auditQueryService) {
    this.actor = actor;
    this.auditQueryService = auditQueryService;
    setSpacing(10);
    setPadding(new Insets(10));
    getChildren().addAll(buildHeader(), buildSearchBar(), buildTable(), infoLabel);
    refreshData();
  }

  private VBox buildHeader() {
    Label title = new Label("Auditoria de eventos");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    Label description =
        new Label(
            "Consulte eventos criticos por acao, entidade, usuario, id da entidade ou texto livre.");
    description.setStyle("-fx-text-fill: #4b5563;");
    return new VBox(4, title, description);
  }

  private HBox buildSearchBar() {
    searchField.setPromptText("Buscar por acao, entidade, usuario, entidade_id ou detalhes");
    Button searchButton = new Button("Buscar");
    searchButton.setOnAction(event -> refreshData());
    Button refreshButton = new Button("Atualizar");
    refreshButton.setOnAction(event -> refreshData());

    HBox box = new HBox(8, searchField, searchButton, refreshButton);
    box.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(searchField, Priority.ALWAYS);
    return box;
  }

  private TableView<AuditEventSummary> buildTable() {
    table.setPrefHeight(420);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    TableColumn<AuditEventSummary, String> timestampCol = new TableColumn<>("Instante");
    timestampCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().timestamp()));

    TableColumn<AuditEventSummary, String> actorCol = new TableColumn<>("Usuario");
    actorCol.setCellValueFactory(
        cell -> {
          AuditEventSummary event = cell.getValue();
          String login = event.userLogin() == null ? "-" : event.userLogin();
          return new ReadOnlyStringWrapper(event.userId() + " (" + login + ")");
        });

    TableColumn<AuditEventSummary, String> actionCol = new TableColumn<>("Acao");
    actionCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().action()));

    TableColumn<AuditEventSummary, String> entityCol = new TableColumn<>("Entidade");
    entityCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().entity()));

    TableColumn<AuditEventSummary, String> entityIdCol = new TableColumn<>("Entidade ID");
    entityIdCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().entityId()));

    TableColumn<AuditEventSummary, String> detailsCol = new TableColumn<>("Detalhes");
    detailsCol.setCellValueFactory(
        cell ->
            new ReadOnlyStringWrapper(
                cell.getValue().details() == null ? "-" : cell.getValue().details()));

    table
        .getColumns()
        .setAll(timestampCol, actorCol, actionCol, entityCol, entityIdCol, detailsCol);
    return table;
  }

  public void refreshData() {
    events.setAll(auditQueryService.listEvents(searchField.getText(), 300));
    infoLabel.setText("Eventos encontrados: " + events.size() + " | Sessao: " + actor.login());
    infoLabel.setStyle("-fx-text-fill: #4b5563;");
  }
}
