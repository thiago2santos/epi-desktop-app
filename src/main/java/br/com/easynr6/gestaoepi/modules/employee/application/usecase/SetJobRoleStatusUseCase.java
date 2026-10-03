package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetJobRoleStatusUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;

  public SetJobRoleStatusUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(
      entidade = "JOB_ROLE",
      acaoQuandoAtivo = "JOB_ROLE_REACTIVATED",
      acaoQuandoInativo = "JOB_ROLE_DEACTIVATED",
      alvo = 1)
  @Transactional
  public void execute(Long actorId, Long jobRoleId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (jobRoleId == null || !orgStructureRepository.jobRoleExists(jobRoleId)) {
      throw new IllegalArgumentException("CAD-027 Funcao alvo nao encontrada.");
    }
    if (!active && orgStructureRepository.hasActiveEmployeesByJobRole(jobRoleId)) {
      throw new IllegalArgumentException(
          "CAD-028 Operacao nao permitida por dependencia historica.");
    }
    orgStructureRepository.setJobRoleActive(jobRoleId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        active ? "JOB_ROLE_REACTIVATED" : "JOB_ROLE_DEACTIVATED",
        "JOB_ROLE",
        String.valueOf(jobRoleId),
        active ? "Job role reactivated." : "Job role deactivated.");
  }
}
