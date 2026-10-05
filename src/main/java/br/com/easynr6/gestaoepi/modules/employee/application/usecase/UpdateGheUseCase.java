package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.GheStored;
import br.com.easynr6.gestaoepi.modules.employee.domain.GhePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateGheUseCase {

  private final GheRepository gheRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final GhePolicy policy = new GhePolicy();

  public UpdateGheUseCase(
      GheRepository gheRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.gheRepository = gheRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "GHE_EDITADO", entidade = "GHE", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long gheId, String name) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    GheStored atual =
        gheRepository
            .findById(gheId)
            .orElseThrow(() -> new IllegalArgumentException("CAD-053 GHE alvo nao encontrado."));
    String normalized = policy.requireName(name);
    if (gheRepository.existsNameInUnit(normalized, atual.unitId(), gheId)) {
      throw new IllegalArgumentException("CAD-052 Nome ja usado na unidade.");
    }
    gheRepository.updateName(gheId, normalized);
    auditTrail.registrarEventoCritico(
        actorId, "GHE_EDITADO", "GHE", String.valueOf(gheId), "Nome do GHE: " + normalized);
  }
}
