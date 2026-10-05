package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetGheStatusUseCase {

  private final GheRepository gheRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;

  public SetGheStatusUseCase(
      GheRepository gheRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.gheRepository = gheRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(
      entidade = "GHE",
      acaoQuandoAtivo = "GHE_REATIVADO",
      acaoQuandoInativo = "GHE_INATIVADO",
      alvo = 1)
  @Transactional
  public void execute(Long actorId, Long gheId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (gheRepository.findById(gheId).isEmpty()) {
      throw new IllegalArgumentException("CAD-053 GHE alvo nao encontrado.");
    }
    gheRepository.setActive(gheId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        active ? "GHE_REATIVADO" : "GHE_INATIVADO",
        "GHE",
        String.valueOf(gheId),
        active ? "GHE reativado." : "GHE inativado.");
  }
}
