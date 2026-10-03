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
public class CreateEmployeeUseCase {

  private final EmployeeRepository employeeRepository;
  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final EmployeePolicy employeePolicy = new EmployeePolicy();

  public CreateEmployeeUseCase(
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
  public Long execute(
      Long actorId,
      String employeeCode,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    employeePolicy.validateRequiredFields(employeeCode, fullName, departmentId, jobRoleId);

    String normalizedCode = employeePolicy.normalizeCode(employeeCode);
    String normalizedName = employeePolicy.normalizeName(fullName);
    if (employeeRepository.existsByEmployeeCode(normalizedCode)) {
      throw new IllegalArgumentException("CAD-001 Matricula ja existente.");
    }

    DepartmentOption department =
        orgStructureRepository.findDepartmentById(departmentId).orElse(null);
    JobRoleOption jobRole = orgStructureRepository.findJobRoleById(jobRoleId).orElse(null);
    employeePolicy.validateDepartmentAndRole(department, jobRole);
    EmployeeAssignment manager =
        managerId == null ? null : employeeRepository.findAssignmentById(managerId).orElse(null);
    employeePolicy.validateManager(
        null, managerId, department == null ? null : department.unitId(), manager);

    Long employeeId =
        employeeRepository.create(
            normalizedCode, normalizedName, departmentId, jobRoleId, managerId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        "EMPLOYEE_CREATED",
        "EMPLOYEE",
        String.valueOf(employeeId),
        "Employee created with code " + normalizedCode);
    return employeeId;
  }
}
