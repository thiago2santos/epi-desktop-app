package br.com.easynr6.gestaoepi.ui.cadastros;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
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
import javafx.scene.control.CheckBox;
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

public class EmployeeManagementView extends VBox {

  private final UsuarioAutenticado actor;
  private final EmployeeManagementService employeeService;

  private final ObservableList<EmployeeSummary> employees = FXCollections.observableArrayList();
  private final ObservableList<DepartmentOption> departments = FXCollections.observableArrayList();
  private final ObservableList<JobRoleOption> jobRoles = FXCollections.observableArrayList();

  private final TextField searchField = new TextField();
  private final TableView<EmployeeSummary> employeeTable = new TableView<>(employees);
  private final TextField employeeCodeField = new TextField();
  private final TextField fullNameField = new TextField();
  private final ComboBox<DepartmentOption> departmentCombo = new ComboBox<>(departments);
  private final ComboBox<JobRoleOption> jobRoleCombo = new ComboBox<>(jobRoles);
  private final CheckBox activeCheck = new CheckBox("Ativo");
  private final Label feedbackLabel = new Label();

  public EmployeeManagementView(
      UsuarioAutenticado actor, EmployeeManagementService employeeService) {
    this.actor = actor;
    this.employeeService = employeeService;
    setSpacing(10);
    setPadding(new Insets(10));

    getChildren()
        .addAll(buildHeader(), buildSearchBar(), buildTable(), buildForm(), buildActions());
    setupFormBindings();
    reloadDepartments();
    refreshEmployees();
  }

  private VBox buildHeader() {
    Label title = new Label("Cadastro de empregado");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    Label description =
        new Label(
            "Gerencie matricula, funcao e setor do empregado com regras de consistencia e auditoria.");
    description.setStyle("-fx-text-fill: #4b5563;");
    return new VBox(4, title, description);
  }

  private HBox buildSearchBar() {
    searchField.setPromptText("Buscar por matricula ou nome");
    Button searchButton = new Button("Buscar");
    searchButton.setOnAction(event -> refreshEmployees());
    Button refreshButton = new Button("Atualizar");
    refreshButton.setOnAction(event -> refreshEmployees());
    HBox bar = new HBox(8, searchField, searchButton, refreshButton);
    HBox.setHgrow(searchField, Priority.ALWAYS);
    return bar;
  }

  private TableView<EmployeeSummary> buildTable() {
    employeeTable.setPrefHeight(250);
    employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    TableColumn<EmployeeSummary, String> codeCol = new TableColumn<>("Matricula");
    codeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().employeeCode()));
    TableColumn<EmployeeSummary, String> nameCol = new TableColumn<>("Nome");
    nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().fullName()));
    TableColumn<EmployeeSummary, String> deptCol = new TableColumn<>("Setor");
    deptCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().departmentName()));
    TableColumn<EmployeeSummary, String> roleCol = new TableColumn<>("Funcao");
    roleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().jobRoleName()));
    TableColumn<EmployeeSummary, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().active() ? "ATIVO" : "INATIVO"));

    employeeTable.getColumns().setAll(codeCol, nameCol, deptCol, roleCol, statusCol);
    return employeeTable;
  }

  private GridPane buildForm() {
    employeeCodeField.setPromptText("Matricula");
    fullNameField.setPromptText("Nome completo");
    activeCheck.setSelected(true);

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
    jobRoleCombo.setPromptText("Selecione a funcao");
    jobRoleCombo.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(JobRoleOption object) {
            return object == null ? "" : object.name();
          }

          @Override
          public JobRoleOption fromString(String string) {
            return null;
          }
        });

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Matricula"), employeeCodeField);
    form.addRow(1, new Label("Nome"), fullNameField);
    form.addRow(2, new Label("Setor"), departmentCombo);
    form.addRow(3, new Label("Funcao"), jobRoleCombo);
    form.add(activeCheck, 1, 4);
    return form;
  }

  private HBox buildActions() {
    Button createButton = new Button("Cadastrar");
    createButton.setOnAction(event -> createEmployee());
    Button updateButton = new Button("Salvar edicao");
    updateButton.setOnAction(event -> updateEmployee());
    Button deactivateButton = new Button("Inativar");
    deactivateButton.setOnAction(event -> setStatus(false));
    Button activateButton = new Button("Reativar");
    activateButton.setOnAction(event -> setStatus(true));
    Button clearButton = new Button("Limpar");
    clearButton.setOnAction(event -> clearForm());

    HBox actions =
        new HBox(
            8,
            createButton,
            updateButton,
            deactivateButton,
            activateButton,
            clearButton,
            feedbackLabel);
    actions.setAlignment(Pos.CENTER_LEFT);
    return actions;
  }

  private void setupFormBindings() {
    departmentCombo
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              loadJobRoles(newVal == null ? null : newVal.id());
              if (newVal == null) {
                jobRoleCombo.getSelectionModel().clearSelection();
              }
            });

    employeeTable
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, selected) -> {
              if (selected == null) {
                return;
              }
              employeeCodeField.setText(selected.employeeCode());
              employeeCodeField.setDisable(true);
              fullNameField.setText(selected.fullName());
              activeCheck.setSelected(selected.active());
              selectDepartmentAndRole(selected.departmentId(), selected.jobRoleId());
            });
  }

  private void createEmployee() {
    try {
      DepartmentOption department = departmentCombo.getValue();
      JobRoleOption jobRole = jobRoleCombo.getValue();
      employeeService.createEmployee(
          actor.id(),
          employeeCodeField.getText(),
          fullNameField.getText(),
          department == null ? null : department.id(),
          jobRole == null ? null : jobRole.id(),
          activeCheck.isSelected());
      showSuccess("EMP-100 Empregado cadastrado com sucesso.");
      clearForm();
      refreshEmployees();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void updateEmployee() {
    try {
      EmployeeSummary selected = employeeTable.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-006 Selecione um trabalhador para editar.");
      }
      DepartmentOption department = departmentCombo.getValue();
      JobRoleOption jobRole = jobRoleCombo.getValue();
      employeeService.updateEmployee(
          actor.id(),
          selected.id(),
          fullNameField.getText(),
          department == null ? null : department.id(),
          jobRole == null ? null : jobRole.id(),
          activeCheck.isSelected());
      showSuccess("EMP-101 Empregado atualizado com sucesso.");
      refreshEmployees();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void setStatus(boolean active) {
    try {
      EmployeeSummary selected = employeeTable.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-006 Selecione um trabalhador para alterar status.");
      }
      if (!active && !confirmDeactivate(selected)) {
        return;
      }
      employeeService.setEmployeeStatus(actor.id(), selected.id(), active);
      showSuccess(active ? "EMP-103 Empregado reativado." : "EMP-102 Empregado inativado.");
      refreshEmployees();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void refreshEmployees() {
    employees.setAll(employeeService.listEmployees(actor.id(), searchField.getText()));
  }

  public void refreshReferenceData() {
    reloadDepartments();
    refreshEmployees();
  }

  private void reloadDepartments() {
    departments.setAll(employeeService.listActiveDepartments());
    if (!departments.isEmpty()) {
      departmentCombo.getSelectionModel().selectFirst();
    }
  }

  private void loadJobRoles(Long departmentId) {
    List<JobRoleOption> available = employeeService.listActiveJobRolesByDepartment(departmentId);
    jobRoles.setAll(available);
    if (!jobRoles.isEmpty()) {
      jobRoleCombo.getSelectionModel().selectFirst();
    } else {
      jobRoleCombo.getSelectionModel().clearSelection();
    }
  }

  private void selectDepartmentAndRole(Long departmentId, Long jobRoleId) {
    for (DepartmentOption department : departments) {
      if (department.id().equals(departmentId)) {
        departmentCombo.getSelectionModel().select(department);
        break;
      }
    }
    loadJobRoles(departmentId);
    for (JobRoleOption role : jobRoles) {
      if (role.id().equals(jobRoleId)) {
        jobRoleCombo.getSelectionModel().select(role);
        break;
      }
    }
  }

  private void clearForm() {
    employeeCodeField.clear();
    employeeCodeField.setDisable(false);
    fullNameField.clear();
    activeCheck.setSelected(true);
    employeeTable.getSelectionModel().clearSelection();
    feedbackLabel.setText("");
    reloadDepartments();
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

  private boolean confirmDeactivate(EmployeeSummary employee) {
    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
    dialog.setTitle("Confirmar inativacao");
    dialog.setHeaderText("Inativar trabalhador");
    dialog.setContentText(
        "Deseja inativar o trabalhador "
            + employee.employeeCode()
            + " - "
            + employee.fullName()
            + "?");
    return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }
}
