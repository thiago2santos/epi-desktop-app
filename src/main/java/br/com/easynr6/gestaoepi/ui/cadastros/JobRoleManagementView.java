package br.com.easynr6.gestaoepi.ui.cadastros;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleSummary;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class JobRoleManagementView extends VBox {

  private final UsuarioAutenticado actor;
  private final EmployeeManagementService employeeService;
  private final ObservableList<JobRoleSummary> jobRoles = FXCollections.observableArrayList();
  private final ObservableList<DepartmentOption> departments = FXCollections.observableArrayList();
  private final TextField searchField = new TextField();
  private final TableView<JobRoleSummary> table = new TableView<>(jobRoles);
  private final TextField nameField = new TextField();
  private final ComboBox<DepartmentOption> departmentCombo = new ComboBox<>(departments);
  private final Label statusPreview = new Label("ATIVO");
  private final Label feedbackLabel = new Label();
  private boolean activeSelection = true;

  public JobRoleManagementView(
      UsuarioAutenticado actor, EmployeeManagementService employeeService) {
    this.actor = actor;
    this.employeeService = employeeService;
    setSpacing(10);
    setPadding(new Insets(10));
    getChildren()
        .addAll(buildHeader(), buildSearchBar(), buildTable(), buildForm(), buildActions());
    reloadDepartments();
    bindTableSelection();
    refreshJobRoles();
  }

  private VBox buildHeader() {
    Label title = new Label("Cadastro de funcoes");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    Label description = new Label("Gerencie funcoes vinculadas aos setores ativos.");
    description.setStyle("-fx-text-fill: #4b5563;");
    return new VBox(4, title, description);
  }

  private HBox buildSearchBar() {
    searchField.setPromptText("Buscar funcao por nome");
    Button searchButton = new Button("Buscar");
    searchButton.setOnAction(event -> refreshJobRoles());
    Button refreshButton = new Button("Atualizar");
    refreshButton.setOnAction(event -> refreshJobRoles());
    HBox box = new HBox(8, searchField, searchButton, refreshButton);
    HBox.setHgrow(searchField, Priority.ALWAYS);
    return box;
  }

  private TableView<JobRoleSummary> buildTable() {
    table.setPrefHeight(220);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    TableColumn<JobRoleSummary, String> roleCol = new TableColumn<>("Funcao");
    roleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
    TableColumn<JobRoleSummary, String> deptCol = new TableColumn<>("Setor");
    deptCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().departmentName()));
    TableColumn<JobRoleSummary, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().active() ? "ATIVO" : "INATIVO"));

    table.getColumns().setAll(roleCol, deptCol, statusCol);
    return table;
  }

  private GridPane buildForm() {
    nameField.setPromptText("Nome da funcao");
    departmentCombo.setPromptText("Selecione o setor");
    departmentCombo.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(DepartmentOption object) {
            return object == null ? "" : object.name();
          }

          @Override
          public DepartmentOption fromString(String string) {
            return null;
          }
        });

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Funcao"), nameField);
    form.addRow(1, new Label("Setor"), departmentCombo);
    form.addRow(2, new Label("Status"), statusPreview);
    return form;
  }

  private HBox buildActions() {
    Button createButton = new Button("Cadastrar");
    createButton.setOnAction(event -> createJobRole());
    Button updateButton = new Button("Salvar edicao");
    updateButton.setOnAction(event -> updateJobRole());
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

  private void reloadDepartments() {
    List<DepartmentOption> activeDepartments = employeeService.listActiveDepartments();
    departments.setAll(activeDepartments);
    if (!departments.isEmpty()) {
      departmentCombo.getSelectionModel().selectFirst();
    }
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
              selectDepartment(selected.departmentId());
            });
  }

  private void createJobRole() {
    try {
      DepartmentOption department = departmentCombo.getValue();
      employeeService.createJobRole(
          actor.id(),
          nameField.getText(),
          department == null ? null : department.id(),
          activeSelection);
      showSuccess("CAD-130 Funcao cadastrada com sucesso.");
      clearForm();
      refreshJobRoles();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void updateJobRole() {
    try {
      JobRoleSummary selected = table.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-027 Selecione uma funcao para editar.");
      }
      DepartmentOption department = departmentCombo.getValue();
      employeeService.updateJobRole(
          actor.id(),
          selected.id(),
          nameField.getText(),
          department == null ? null : department.id(),
          activeSelection);
      showSuccess("CAD-131 Funcao atualizada com sucesso.");
      refreshJobRoles();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void setStatus(boolean active) {
    try {
      JobRoleSummary selected = table.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-027 Selecione uma funcao para alterar status.");
      }
      if (!active && !confirmDeactivate(selected.name())) {
        return;
      }
      employeeService.setJobRoleStatus(actor.id(), selected.id(), active);
      activeSelection = active;
      statusPreview.setText(active ? "ATIVO" : "INATIVO");
      showSuccess(active ? "CAD-133 Funcao reativada." : "CAD-132 Funcao inativada.");
      refreshJobRoles();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void refreshJobRoles() {
    jobRoles.setAll(employeeService.listJobRoles(actor.id(), searchField.getText()));
  }

  public void refreshReferenceData() {
    reloadDepartments();
    refreshJobRoles();
  }

  private void clearForm() {
    nameField.clear();
    activeSelection = true;
    statusPreview.setText("ATIVO");
    table.getSelectionModel().clearSelection();
    feedbackLabel.setText("");
    if (!departments.isEmpty()) {
      departmentCombo.getSelectionModel().selectFirst();
    }
  }

  private void selectDepartment(Long departmentId) {
    for (DepartmentOption option : departments) {
      if (option.id().equals(departmentId)) {
        departmentCombo.getSelectionModel().select(option);
        return;
      }
    }
    departmentCombo.getSelectionModel().clearSelection();
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
    dialog.setHeaderText("Inativar funcao");
    dialog.setContentText("Deseja inativar a funcao " + name + "?");
    return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }
}
