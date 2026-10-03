package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeAssignment;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import br.com.easynr6.gestaoepi.modules.employee.domain.EmployeePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEmployeeUseCase {

  private final EmployeeRepository employeeRepository;
  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final EmployeePolicy employeePolicy = new EmployeePolicy();

  public UpdateEmployeeUseCase(
      EmployeeRepository employeeRepository,
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.employeeRepository = employeeRepository;
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public void execute(
      Long actorId,
      Long employeeId,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (employeeId == null || !employeeRepository.existsById(employeeId)) {
      throw new IllegalArgumentException("CAD-006 Trabalhador alvo nao encontrado.");
    }
    employeePolicy.validateUpdateFields(fullName, departmentId, jobRoleId);

    DepartmentOption department =
        orgStructureRepository.findDepartmentById(departmentId).orElse(null);
    JobRoleOption jobRole = orgStructureRepository.findJobRoleById(jobRoleId).orElse(null);
    employeePolicy.validateDepartmentAndRole(department, jobRole);
    EmployeeAssignment manager =
        managerId == null ? null : employeeRepository.findAssignmentById(managerId).orElse(null);
    employeePolicy.validateManager(
        employeeId, managerId, department == null ? null : department.unitId(), manager);

    employeeRepository.update(
        employeeId,
        employeePolicy.normalizeName(fullName),
        departmentId,
        jobRoleId,
        managerId,
        active);
    auditTrail.registrarEventoCritico(
        actorId, "EMPLOYEE_UPDATED", "EMPLOYEE", String.valueOf(employeeId), "Employee updated");
  }
}
