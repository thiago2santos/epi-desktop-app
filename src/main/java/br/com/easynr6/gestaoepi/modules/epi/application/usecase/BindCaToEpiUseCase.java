package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaPolicy;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BindCaToEpiUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final CaPolicy caPolicy = new CaPolicy();

  public BindCaToEpiUseCase(
      EpiRepository epiRepository,
      EpiCatalogAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "EPI_CA_BOUND", entidade = "EPI_CA", alvo = 1)
  @Transactional
  public Long execute(
      Long actorId,
      Long epiId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    if (epiId == null || !epiRepository.existsEpiById(epiId)) {
      throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
    }

    caPolicy.validateRequiredFields(caNumber, caStatus, officialCheckAt, officialCheckNote);
    caPolicy.validateStatusForActivation(caStatus, active);
    caPolicy.validateValidityWindow(validFrom, validUntil);

    String normalizedNumber = caPolicy.normalizeCaNumber(caNumber);
    String normalizedNote = caPolicy.normalizeOfficialNote(officialCheckNote);
    if (epiRepository.existsCaValidityConflict(
        epiId, normalizedNumber, validFrom, validUntil, null)) {
      throw new IllegalArgumentException("CAD-035 CA com conflito de vigencia para o mesmo EPI.");
    }

    Long bindingId =
        epiRepository.createCaBinding(
            epiId,
            normalizedNumber,
            caStatus,
            validFrom,
            validUntil,
            officialCheckAt,
            normalizedNote,
            active);
    auditTrail.registrarEventoCritico(
        actorId, "EPI_CA_BOUND", "EPI_CA", String.valueOf(bindingId), "CA bound to EPI.");
    return bindingId;
  }
}
