package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.VinculoCaResolver;
import br.com.easynr6.gestaoepi.modules.epi.application.VinculoCaResolver.Resolvido;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCaBindingUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final VinculoCaResolver vinculoCaResolver;

  public UpdateCaBindingUseCase(
      EpiRepository epiRepository,
      EpiCatalogAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail,
      VinculoCaResolver vinculoCaResolver) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
    this.vinculoCaResolver = vinculoCaResolver;
  }

  @AcaoAuditada(acao = "EPI_CA_UPDATED", entidade = "EPI_CA", alvo = 1)
  @Transactional
  public void execute(
      Long actorId,
      Long bindingId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active,
      String evidenceFileName,
      byte[] evidence) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    if (bindingId == null || !epiRepository.existsCaBindingById(bindingId)) {
      throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
    }

    var details =
        epiRepository
            .findCaBindingById(bindingId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "CAD-039 Alvo de edicao/inativacao nao encontrado."));
    Resolvido resolvido =
        vinculoCaResolver.resolver(
            caNumber,
            caStatus,
            validFrom,
            validUntil,
            officialCheckAt,
            officialCheckNote,
            active,
            evidenceFileName,
            evidence,
            epiRepository.hasCaEvidence(bindingId));
    if (epiRepository.existsCaValidityConflict(
        details.epiId(),
        resolvido.caNumber(),
        resolvido.validFrom(),
        resolvido.validUntil(),
        bindingId)) {
      throw new IllegalArgumentException("CAD-035 CA com conflito de vigencia para o mesmo EPI.");
    }

    epiRepository.updateCaBinding(
        bindingId,
        resolvido.caNumber(),
        resolvido.caStatus(),
        resolvido.validFrom(),
        resolvido.validUntil(),
        resolvido.officialCheckAt(),
        resolvido.officialCheckNote(),
        active);
    if (resolvido.temAnexoNovo()) {
      epiRepository.saveCaEvidence(bindingId, resolvido.evidenceFileName(), resolvido.evidence());
    }
    auditTrail.registrarEventoCritico(
        actorId, "EPI_CA_UPDATED", "EPI_CA", String.valueOf(bindingId), "CA binding updated.");
  }
}
