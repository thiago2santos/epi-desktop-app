package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.domain.DepartmentPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateDepartmentUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final DepartmentPolicy departmentPolicy = new DepartmentPolicy();

  public UpdateDepartmentUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public void execute(Long actorId, Long departmentId, String name, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (departmentId == null || !orgStructureRepository.departmentExists(departmentId)) {
      throw new IllegalArgumentException("CAD-023 Setor alvo nao encontrado.");
    }
    departmentPolicy.validateRequiredName(name);
    String normalizedName = departmentPolicy.normalizeName(name);
    if (orgStructureRepository.existsDepartmentByNameExcludingId(normalizedName, departmentId)) {
      throw new IllegalArgumentException("CAD-021 Nome de setor ja existente.");
    }
    if (!active && orgStructureRepository.hasActiveJobRoles(departmentId)) {
      throw new IllegalArgumentException(
          "CAD-024 Operacao nao permitida por dependencia historica.");
    }
    orgStructureRepository.updateDepartment(departmentId, normalizedName, active);
    auditTrail.registrarEventoCritico(
        actorId,
        "DEPARTMENT_UPDATED",
        "DEPARTMENT",
        String.valueOf(departmentId),
        "Department updated: " + normalizedName);
  }
}
