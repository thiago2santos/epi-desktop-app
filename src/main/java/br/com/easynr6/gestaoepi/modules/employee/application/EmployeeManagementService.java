package br.com.easynr6.gestaoepi.modules.employee.application;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.FuncaoDoGhe;
import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.GheSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateDepartmentUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateEmployeeUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateGheUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateJobRoleUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.CreateUnitUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.LinkJobRoleToGheUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListDepartmentsUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListEmployeesUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListGhesUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListJobRolesUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ListUnitsUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ResolvePerfilVigenteUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetDepartmentStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetEmployeeStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetGheStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetJobRoleStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.SetUnitStatusUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UnlinkJobRoleFromGheUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateDepartmentUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateEmployeeUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateGheUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateJobRoleUseCase;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.UpdateUnitUseCase;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
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
  private final CreateUnitUseCase createUnitUseCase;
  private final UpdateUnitUseCase updateUnitUseCase;
  private final SetUnitStatusUseCase setUnitStatusUseCase;
  private final ListUnitsUseCase listUnitsUseCase;
  private final CreateGheUseCase createGheUseCase;
  private final UpdateGheUseCase updateGheUseCase;
  private final SetGheStatusUseCase setGheStatusUseCase;
  private final LinkJobRoleToGheUseCase linkJobRoleToGheUseCase;
  private final UnlinkJobRoleFromGheUseCase unlinkJobRoleFromGheUseCase;
  private final ListGhesUseCase listGhesUseCase;
  private final ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase;
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
      CreateUnitUseCase createUnitUseCase,
      UpdateUnitUseCase updateUnitUseCase,
      SetUnitStatusUseCase setUnitStatusUseCase,
      ListUnitsUseCase listUnitsUseCase,
      CreateGheUseCase createGheUseCase,
      UpdateGheUseCase updateGheUseCase,
      SetGheStatusUseCase setGheStatusUseCase,
      LinkJobRoleToGheUseCase linkJobRoleToGheUseCase,
      UnlinkJobRoleFromGheUseCase unlinkJobRoleFromGheUseCase,
      ListGhesUseCase listGhesUseCase,
      ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase,
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
    this.createUnitUseCase = createUnitUseCase;
    this.updateUnitUseCase = updateUnitUseCase;
    this.setUnitStatusUseCase = setUnitStatusUseCase;
    this.listUnitsUseCase = listUnitsUseCase;
    this.createGheUseCase = createGheUseCase;
    this.updateGheUseCase = updateGheUseCase;
    this.setGheStatusUseCase = setGheStatusUseCase;
    this.linkJobRoleToGheUseCase = linkJobRoleToGheUseCase;
    this.unlinkJobRoleFromGheUseCase = unlinkJobRoleFromGheUseCase;
    this.listGhesUseCase = listGhesUseCase;
    this.resolvePerfilVigenteUseCase = resolvePerfilVigenteUseCase;
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

  public Long createUnit(Long actorId, String name, String cnpj, boolean active) {
    return createUnitUseCase.execute(actorId, name, cnpj, active);
  }

  public void updateUnit(Long actorId, Long unitId, String name) {
    updateUnitUseCase.execute(actorId, unitId, name);
  }

  public void setUnitStatus(Long actorId, Long unitId, boolean active) {
    setUnitStatusUseCase.execute(actorId, unitId, active);
  }

  public List<UnitSummary> listUnits(Long actorId, String term) {
    return listUnitsUseCase.execute(actorId, term);
  }

  public List<UnitOption> listActiveUnits() {
    return orgStructureRepository.listActiveUnits();
  }

  public List<DepartmentOption> listActiveDepartments() {
    return orgStructureRepository.listActiveDepartments();
  }

  public List<JobRoleOption> listActiveJobRolesByDepartment(Long departmentId) {
    return orgStructureRepository.listActiveJobRolesByDepartment(departmentId);
  }

  public Long createGhe(Long actorId, Long unitId, String name, boolean active) {
    return createGheUseCase.execute(actorId, unitId, name, active);
  }

  public void updateGhe(Long actorId, Long gheId, String name) {
    updateGheUseCase.execute(actorId, gheId, name);
  }

  public void setGheStatus(Long actorId, Long gheId, boolean active) {
    setGheStatusUseCase.execute(actorId, gheId, active);
  }

  public void linkJobRoleToGhe(Long actorId, Long gheId, Long jobRoleId) {
    linkJobRoleToGheUseCase.execute(actorId, gheId, jobRoleId);
  }

  public void unlinkJobRoleFromGhe(Long actorId, Long gheId, Long jobRoleId) {
    unlinkJobRoleFromGheUseCase.execute(actorId, gheId, jobRoleId);
  }

  public List<GheSummary> listGhes(Long actorId, Long unitId, String term) {
    return listGhesUseCase.list(actorId, unitId, term);
  }

  public List<FuncaoDoGhe> listGheMembers(Long actorId, Long gheId) {
    return listGhesUseCase.members(actorId, gheId);
  }

  public List<FuncaoDoGhe> listGheCandidates(Long actorId, Long unitId) {
    return listGhesUseCase.candidates(actorId, unitId);
  }

  public PerfilVigente perfilVigente(Long jobRoleId) {
    return resolvePerfilVigenteUseCase.execute(jobRoleId);
  }
}
