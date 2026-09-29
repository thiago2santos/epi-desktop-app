package br.com.easynr6.gestaoepi.modules.employee.application;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateEmployeeUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListEmployeesUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetEmployeeStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateEmployeeUseCase;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmployeeManagementService {

  private final CreateEmployeeUseCase createEmployeeUseCase;
  private final UpdateEmployeeUseCase updateEmployeeUseCase;
  private final SetEmployeeStatusUseCase setEmployeeStatusUseCase;
  private final ListEmployeesUseCase listEmployeesUseCase;
  private final OrgStructureRepository orgStructureRepository;

  public EmployeeManagementService(
      CreateEmployeeUseCase createEmployeeUseCase,
      UpdateEmployeeUseCase updateEmployeeUseCase,
      SetEmployeeStatusUseCase setEmployeeStatusUseCase,
      ListEmployeesUseCase listEmployeesUseCase,
      OrgStructureRepository orgStructureRepository) {
    this.createEmployeeUseCase = createEmployeeUseCase;
    this.updateEmployeeUseCase = updateEmployeeUseCase;
    this.setEmployeeStatusUseCase = setEmployeeStatusUseCase;
    this.listEmployeesUseCase = listEmployeesUseCase;
    this.orgStructureRepository = orgStructureRepository;
  }

  public Long createEmployee(
      Long actorId,
      String employeeCode,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      boolean active) {
    return createEmployeeUseCase.execute(
        actorId, employeeCode, fullName, departmentId, jobRoleId, active);
  }

  public void updateEmployee(
      Long actorId,
      Long employeeId,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      boolean active) {
    updateEmployeeUseCase.execute(actorId, employeeId, fullName, departmentId, jobRoleId, active);
  }

  public void setEmployeeStatus(Long actorId, Long employeeId, boolean active) {
    setEmployeeStatusUseCase.execute(actorId, employeeId, active);
  }

  public List<EmployeeSummary> listEmployees(Long actorId, String term) {
    return listEmployeesUseCase.execute(actorId, term);
  }

  public List<DepartmentOption> listActiveDepartments() {
    return orgStructureRepository.listActiveDepartments();
  }

  public List<JobRoleOption> listActiveJobRolesByDepartment(Long departmentId) {
    return orgStructureRepository.listActiveJobRolesByDepartment(departmentId);
  }
}
