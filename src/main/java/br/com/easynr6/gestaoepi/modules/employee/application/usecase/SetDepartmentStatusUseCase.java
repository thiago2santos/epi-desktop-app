package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetDepartmentStatusUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;

  public SetDepartmentStatusUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public void execute(Long actorId, Long departmentId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (departmentId == null || !orgStructureRepository.departmentExists(departmentId)) {
      throw new IllegalArgumentException("CAD-023 Setor alvo nao encontrado.");
    }
    if (!active && orgStructureRepository.hasActiveJobRoles(departmentId)) {
      throw new IllegalArgumentException(
          "CAD-024 Operacao nao permitida por dependencia historica.");
    }
    orgStructureRepository.setDepartmentActive(departmentId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        active ? "DEPARTMENT_REACTIVATED" : "DEPARTMENT_DEACTIVATED",
        "DEPARTMENT",
        String.valueOf(departmentId),
        active ? "Department reactivated." : "Department deactivated.");
  }
}
