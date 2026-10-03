package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.domain.DepartmentPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDepartmentUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final DepartmentPolicy departmentPolicy = new DepartmentPolicy();

  public CreateDepartmentUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public Long execute(Long actorId, String name, Long unitId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (unitId == null || !orgStructureRepository.isActiveUnit(unitId)) {
      throw new IllegalArgumentException("CAD-027 Unidade invalida ou inativa.");
    }
    departmentPolicy.validateRequiredName(name);
    String normalizedName = departmentPolicy.normalizeName(name);
    if (orgStructureRepository.existsDepartmentByNameInUnit(normalizedName, unitId)) {
      throw new IllegalArgumentException("CAD-021 Nome de setor ja existente.");
    }
    Long departmentId = orgStructureRepository.createDepartment(normalizedName, unitId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        "DEPARTMENT_CREATED",
        "DEPARTMENT",
        String.valueOf(departmentId),
        "Department created: " + normalizedName);
    return departmentId;
  }
}
