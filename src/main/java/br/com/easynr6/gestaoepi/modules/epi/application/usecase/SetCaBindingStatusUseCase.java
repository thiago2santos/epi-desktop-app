package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetCaBindingStatusUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final CaPolicy caPolicy = new CaPolicy();

  public SetCaBindingStatusUseCase(
      EpiRepository epiRepository,
      EpiCatalogAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public void execute(Long actorId, Long bindingId, boolean active) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    var details =
        epiRepository
            .findCaBindingById(bindingId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "CAD-039 Alvo de edicao/inativacao nao encontrado."));

    if (!active && epiRepository.hasHistoricalDependenciesForCaBinding(bindingId)) {
      throw new IllegalArgumentException(
          "CAD-038 Operacao nao permitida por dependencia historica.");
    }
    caPolicy.validateStatusForActivation(details.caStatus(), active);
    if (active
        && epiRepository.existsCaValidityConflict(
            details.epiId(),
            details.caNumber(),
            details.validFrom(),
            details.validUntil(),
            bindingId)) {
      throw new IllegalArgumentException("CAD-035 CA com conflito de vigencia para o mesmo EPI.");
    }

    epiRepository.setCaBindingActive(bindingId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        active ? "EPI_CA_REACTIVATED" : "EPI_CA_DEACTIVATED",
        "EPI_CA",
        String.valueOf(bindingId),
        active ? "CA binding reactivated." : "CA binding deactivated.");
  }
}
