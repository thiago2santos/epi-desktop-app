package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.EpiPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEpiUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final EpiPolicy epiPolicy = new EpiPolicy();

  public CreateEpiUseCase(
      EpiRepository epiRepository,
      EpiCatalogAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "EPI_CREATED", entidade = "EPI")
  @Transactional
  public Long execute(
      Long actorId,
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    epiPolicy.validateRequiredFields(description, annexGroup, manufacturerName);

    String normalizedCode = epiPolicy.normalizeEpiCode(epiCode);
    String normalizedDescription = epiPolicy.normalizeDescription(description);
    String normalizedManufacturer = epiPolicy.normalizeManufacturerName(manufacturerName);
    if (epiRepository.existsEpiDuplicate(
        null, normalizedDescription, annexGroup, normalizedManufacturer)) {
      throw new IllegalArgumentException("CAD-033 EPI duplicado no escopo definido.");
    }

    Long epiId =
        epiRepository.createEpi(
            normalizedCode, normalizedDescription, annexGroup, normalizedManufacturer, active);
    auditTrail.registrarEventoCritico(
        actorId, "EPI_CREATED", "EPI", String.valueOf(epiId), "EPI catalog item created.");
    return epiId;
  }
}
