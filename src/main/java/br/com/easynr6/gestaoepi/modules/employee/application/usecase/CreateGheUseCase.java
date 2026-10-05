package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository;
import br.com.easynr6.gestaoepi.modules.employee.domain.GhePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateGheUseCase {

  private final GheRepository gheRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final GhePolicy policy = new GhePolicy();

  public CreateGheUseCase(
      GheRepository gheRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.gheRepository = gheRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "GHE_CRIADO", entidade = "GHE")
  @Transactional
  public Long execute(Long actorId, Long unitId, String name, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    policy.requireUnit(unitId);
    String normalized = policy.requireName(name);
    if (!gheRepository.unitExists(unitId)) {
      throw new IllegalArgumentException("CAD-051 Nome ou unidade ausente.");
    }
    if (gheRepository.existsNameInUnit(normalized, unitId, null)) {
      throw new IllegalArgumentException("CAD-052 Nome ja usado na unidade.");
    }
    Long gheId = gheRepository.create(unitId, normalized, active);
    auditTrail.registrarEventoCritico(
        actorId, "GHE_CRIADO", "GHE", String.valueOf(gheId), "GHE criado: " + normalized);
    return gheId;
  }
}
