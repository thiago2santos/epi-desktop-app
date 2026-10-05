package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnlinkJobRoleFromGheUseCase {

  private final GheRepository gheRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;

  public UnlinkJobRoleFromGheUseCase(
      GheRepository gheRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.gheRepository = gheRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "GHE_FUNCAO_DESVINCULADA", entidade = "GHE", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long gheId, Long jobRoleId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (gheRepository.findById(gheId).isEmpty()) {
      throw new IllegalArgumentException("CAD-053 GHE alvo nao encontrado.");
    }
    if (!gheRepository.unlink(gheId, jobRoleId)) {
      return;
    }
    auditTrail.registrarEventoCritico(
        actorId,
        "GHE_FUNCAO_DESVINCULADA",
        "GHE",
        String.valueOf(gheId),
        "Funcao desvinculada: " + jobRoleId);
  }
}
