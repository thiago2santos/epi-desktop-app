package br.com.easynr6.gestaoepi.ui.cadastros;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentSummary;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DepartmentManagementView extends VBox {

  private final UsuarioAutenticado actor;
  private final EmployeeManagementService employeeService;
  private final ObservableList<DepartmentSummary> departments = FXCollections.observableArrayList();
  private final TextField searchField = new TextField();
  private final TableView<DepartmentSummary> table = new TableView<>(departments);
  private final TextField nameField = new TextField();
  private final Label statusPreview = new Label("ATIVO");
  private final Label feedbackLabel = new Label();
  private boolean activeSelection = true;

  public DepartmentManagementView(
      UsuarioAutenticado actor, EmployeeManagementService employeeService) {
    this.actor = actor;
    this.employeeService = employeeService;
    setSpacing(10);
    setPadding(new Insets(10));
    getChildren()
        .addAll(buildHeader(), buildSearchBar(), buildTable(), buildForm(), buildActions());
    bindTableSelection();
    refreshDepartments();
  }

  private VBox buildHeader() {
    Label title = new Label("Cadastro de setores");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    Label description = new Label("Gerencie setores para vinculo com funcoes e empregados.");
    description.setStyle("-fx-text-fill: #4b5563;");
    return new VBox(4, title, description);
  }

  private HBox buildSearchBar() {
    searchField.setPromptText("Buscar setor por nome");
    Button searchButton = new Button("Buscar");
    searchButton.setOnAction(event -> refreshDepartments());
    Button refreshButton = new Button("Atualizar");
    refreshButton.setOnAction(event -> refreshDepartments());
    HBox box = new HBox(8, searchField, searchButton, refreshButton);
    HBox.setHgrow(searchField, Priority.ALWAYS);
    return box;
  }

  private TableView<DepartmentSummary> buildTable() {
    table.setPrefHeight(220);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    TableColumn<DepartmentSummary, String> nameCol = new TableColumn<>("Setor");
    nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
    TableColumn<DepartmentSummary, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().active() ? "ATIVO" : "INATIVO"));

    table.getColumns().setAll(nameCol, statusCol);
    return table;
  }

  private GridPane buildForm() {
    nameField.setPromptText("Nome do setor");
    statusPreview.setStyle("-fx-font-weight: bold;");
    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Nome"), nameField);
    form.addRow(1, new Label("Status"), statusPreview);
    return form;
  }

  private HBox buildActions() {
    Button createButton = new Button("Cadastrar");
    createButton.setOnAction(event -> createDepartment());
    Button updateButton = new Button("Salvar edicao");
    updateButton.setOnAction(event -> updateDepartment());
    Button deactivateButton = new Button("Inativar");
    deactivateButton.setOnAction(event -> setStatus(false));
    Button reactivateButton = new Button("Reativar");
    reactivateButton.setOnAction(event -> setStatus(true));
    Button clearButton = new Button("Limpar");
    clearButton.setOnAction(event -> clearForm());
    HBox actions =
        new HBox(
            8,
            createButton,
            updateButton,
            deactivateButton,
            reactivateButton,
            clearButton,
            feedbackLabel);
    actions.setAlignment(Pos.CENTER_LEFT);
    return actions;
  }

  private void bindTableSelection() {
    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, selected) -> {
              if (selected == null) {
                return;
              }
              nameField.setText(selected.name());
              activeSelection = selected.active();
              statusPreview.setText(selected.active() ? "ATIVO" : "INATIVO");
            });
  }

  private void createDepartment() {
    try {
      employeeService.createDepartment(actor.id(), nameField.getText(), activeSelection);
      showSuccess("CAD-120 Setor cadastrado com sucesso.");
      clearForm();
      refreshDepartments();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void updateDepartment() {
    try {
      DepartmentSummary selected = table.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-023 Selecione um setor para editar.");
      }
      employeeService.updateDepartment(
          actor.id(), selected.id(), nameField.getText(), activeSelection);
      showSuccess("CAD-121 Setor atualizado com sucesso.");
      refreshDepartments();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void setStatus(boolean active) {
    try {
      DepartmentSummary selected = table.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-023 Selecione um setor para alterar status.");
      }
      if (!active && !confirmDeactivate(selected.name())) {
        return;
      }
      employeeService.setDepartmentStatus(actor.id(), selected.id(), active);
      activeSelection = active;
      statusPreview.setText(active ? "ATIVO" : "INATIVO");
      showSuccess(active ? "CAD-123 Setor reativado." : "CAD-122 Setor inativado.");
      refreshDepartments();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void refreshDepartments() {
    departments.setAll(employeeService.listDepartments(actor.id(), searchField.getText()));
  }

  public void refreshData() {
    refreshDepartments();
  }

  private void clearForm() {
    nameField.clear();
    activeSelection = true;
    statusPreview.setText("ATIVO");
    table.getSelectionModel().clearSelection();
    feedbackLabel.setText("");
  }

  private void showSuccess(String message) {
    feedbackLabel.setText(message);
    feedbackLabel.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
    PauseTransition pause = new PauseTransition(Duration.seconds(5));
    pause.setOnFinished(event -> feedbackLabel.setText(""));
    pause.play();
  }

  private void showError(String message) {
    feedbackLabel.setText(message == null ? "CAD-099 Falha ao processar operacao." : message);
    feedbackLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
  }

  private boolean confirmDeactivate(String name) {
    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
    dialog.setTitle("Confirmar inativacao");
    dialog.setHeaderText("Inativar setor");
    dialog.setContentText("Deseja inativar o setor " + name + "?");
    return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }
}
