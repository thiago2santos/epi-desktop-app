package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.EpiPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEpiUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final EpiPolicy epiPolicy = new EpiPolicy();

  public UpdateEpiUseCase(
      EpiRepository epiRepository,
      EpiCatalogAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "EPI_UPDATED", entidade = "EPI", alvo = 1)
  @Transactional
  public void execute(
      Long actorId,
      Long epiId,
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      boolean active) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    if (epiId == null || !epiRepository.existsEpiById(epiId)) {
      throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
    }
    epiPolicy.validateRequiredFields(description, annexGroup);

    String normalizedCode = epiPolicy.normalizeEpiCode(epiCode);
    String normalizedDescription = epiPolicy.normalizeDescription(description);
    if (epiRepository.existsEpiDuplicate(epiId, normalizedDescription, annexGroup)) {
      throw new IllegalArgumentException("CAD-033 EPI duplicado no escopo definido.");
    }

    epiRepository.updateEpi(epiId, normalizedCode, normalizedDescription, annexGroup, active);
    auditTrail.registrarEventoCritico(
        actorId, "EPI_UPDATED", "EPI", String.valueOf(epiId), "EPI catalog item updated.");
  }
}
