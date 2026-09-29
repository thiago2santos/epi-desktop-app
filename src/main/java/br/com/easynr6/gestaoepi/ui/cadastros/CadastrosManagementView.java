package br.com.easynr6.gestaoepi.ui.cadastros;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class CadastrosManagementView extends BorderPane {

  public CadastrosManagementView(
      UsuarioAutenticado actor, EmployeeManagementService employeeService) {
    EmployeeManagementView employeeView = new EmployeeManagementView(actor, employeeService);
    DepartmentManagementView departmentView = new DepartmentManagementView(actor, employeeService);
    JobRoleManagementView jobRoleView = new JobRoleManagementView(actor, employeeService);

    Tab employeesTab = new Tab("Empregados", employeeView);
    Tab departmentsTab = new Tab("Setores", departmentView);
    Tab jobRolesTab = new Tab("Funcoes", jobRoleView);

    TabPane tabs = new TabPane();
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabs.getTabs().addAll(employeesTab, departmentsTab, jobRolesTab);
    tabs.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldTab, selectedTab) -> {
              if (selectedTab == departmentsTab) {
                departmentView.refreshData();
              } else if (selectedTab == jobRolesTab) {
                jobRoleView.refreshReferenceData();
              } else if (selectedTab == employeesTab) {
                employeeView.refreshReferenceData();
              }
            });
    setCenter(tabs);
  }
}
