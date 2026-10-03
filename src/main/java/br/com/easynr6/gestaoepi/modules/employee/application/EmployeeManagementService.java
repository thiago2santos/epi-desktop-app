package br.com.easynr6.gestaoepi.modules.employee.application;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateDepartmentUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateEmployeeUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateJobRoleUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListDepartmentsUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListEmployeesUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListJobRolesUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetDepartmentStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetEmployeeStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetJobRoleStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateDepartmentUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateEmployeeUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateJobRoleUseCase;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmployeeManagementService {

  private final CreateEmployeeUseCase createEmployeeUseCase;
  private final UpdateEmployeeUseCase updateEmployeeUseCase;
  private final SetEmployeeStatusUseCase setEmployeeStatusUseCase;
  private final ListEmployeesUseCase listEmployeesUseCase;
  private final CreateDepartmentUseCase createDepartmentUseCase;
  private final UpdateDepartmentUseCase updateDepartmentUseCase;
  private final SetDepartmentStatusUseCase setDepartmentStatusUseCase;
  private final ListDepartmentsUseCase listDepartmentsUseCase;
  private final CreateJobRoleUseCase createJobRoleUseCase;
  private final UpdateJobRoleUseCase updateJobRoleUseCase;
  private final SetJobRoleStatusUseCase setJobRoleStatusUseCase;
  private final ListJobRolesUseCase listJobRolesUseCase;
  private final OrgStructureRepository orgStructureRepository;

  public EmployeeManagementService(
      CreateEmployeeUseCase createEmployeeUseCase,
      UpdateEmployeeUseCase updateEmployeeUseCase,
      SetEmployeeStatusUseCase setEmployeeStatusUseCase,
      ListEmployeesUseCase listEmployeesUseCase,
      CreateDepartmentUseCase createDepartmentUseCase,
      UpdateDepartmentUseCase updateDepartmentUseCase,
      SetDepartmentStatusUseCase setDepartmentStatusUseCase,
      ListDepartmentsUseCase listDepartmentsUseCase,
      CreateJobRoleUseCase createJobRoleUseCase,
      UpdateJobRoleUseCase updateJobRoleUseCase,
      SetJobRoleStatusUseCase setJobRoleStatusUseCase,
      ListJobRolesUseCase listJobRolesUseCase,
      OrgStructureRepository orgStructureRepository) {
    this.createEmployeeUseCase = createEmployeeUseCase;
    this.updateEmployeeUseCase = updateEmployeeUseCase;
    this.setEmployeeStatusUseCase = setEmployeeStatusUseCase;
    this.listEmployeesUseCase = listEmployeesUseCase;
    this.createDepartmentUseCase = createDepartmentUseCase;
    this.updateDepartmentUseCase = updateDepartmentUseCase;
    this.setDepartmentStatusUseCase = setDepartmentStatusUseCase;
    this.listDepartmentsUseCase = listDepartmentsUseCase;
    this.createJobRoleUseCase = createJobRoleUseCase;
    this.updateJobRoleUseCase = updateJobRoleUseCase;
    this.setJobRoleStatusUseCase = setJobRoleStatusUseCase;
    this.listJobRolesUseCase = listJobRolesUseCase;
    this.orgStructureRepository = orgStructureRepository;
  }

  public Long createEmployee(
      Long actorId,
      String employeeCode,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active) {
    return createEmployeeUseCase.execute(
        actorId, employeeCode, fullName, departmentId, jobRoleId, managerId, active);
  }

  public void updateEmployee(
      Long actorId,
      Long employeeId,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active) {
    updateEmployeeUseCase.execute(
        actorId, employeeId, fullName, departmentId, jobRoleId, managerId, active);
  }

  public void setEmployeeStatus(Long actorId, Long employeeId, boolean active) {
    setEmployeeStatusUseCase.execute(actorId, employeeId, active);
  }

  public List<EmployeeSummary> listEmployees(Long actorId, String term) {
    return listEmployeesUseCase.execute(actorId, term);
  }

  public List<EmployeeOption> listActiveEmployeesByUnit(Long unitId) {
    return listEmployeesUseCase.listActiveByUnit(unitId);
  }

  public Long createDepartment(Long actorId, String name, Long unitId, boolean active) {
    return createDepartmentUseCase.execute(actorId, name, unitId, active);
  }

  public void updateDepartment(Long actorId, Long departmentId, String name, boolean active) {
    updateDepartmentUseCase.execute(actorId, departmentId, name, active);
  }

  public void setDepartmentStatus(Long actorId, Long departmentId, boolean active) {
    setDepartmentStatusUseCase.execute(actorId, departmentId, active);
  }

  public List<DepartmentSummary> listDepartments(Long actorId, String term) {
    return listDepartmentsUseCase.execute(actorId, term);
  }

  public Long createJobRole(Long actorId, String name, Long departmentId, boolean active) {
    return createJobRoleUseCase.execute(actorId, name, departmentId, active);
  }

  public void updateJobRole(
      Long actorId, Long jobRoleId, String name, Long departmentId, boolean active) {
    updateJobRoleUseCase.execute(actorId, jobRoleId, name, departmentId, active);
  }

  public void setJobRoleStatus(Long actorId, Long jobRoleId, boolean active) {
    setJobRoleStatusUseCase.execute(actorId, jobRoleId, active);
  }

  public List<JobRoleSummary> listJobRoles(Long actorId, String term) {
    return listJobRolesUseCase.execute(actorId, term);
  }

  public List<DepartmentOption> listActiveDepartments() {
    return orgStructureRepository.listActiveDepartments();
  }

  public List<JobRoleOption> listActiveJobRolesByDepartment(Long departmentId) {
    return orgStructureRepository.listActiveJobRolesByDepartment(departmentId);
  }
}
