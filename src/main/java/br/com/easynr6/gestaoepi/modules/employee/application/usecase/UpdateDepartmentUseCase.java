package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.domain.DepartmentPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
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

  @AcaoAuditada(acao = "DEPARTMENT_UPDATED", entidade = "DEPARTMENT", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long departmentId, String name, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    DepartmentOption current =
        departmentId == null
            ? null
            : orgStructureRepository.findDepartmentById(departmentId).orElse(null);
    if (current == null) {
      throw new IllegalArgumentException("CAD-023 Setor alvo nao encontrado.");
    }
    departmentPolicy.validateRequiredName(name);
    String normalizedName = departmentPolicy.normalizeName(name);
    if (orgStructureRepository.existsDepartmentByNameInUnitExcludingId(
        normalizedName, current.unitId(), departmentId)) {
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
