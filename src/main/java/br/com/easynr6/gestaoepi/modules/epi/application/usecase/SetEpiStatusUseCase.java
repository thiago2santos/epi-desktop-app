package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetEpiStatusUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;

  public SetEpiStatusUseCase(
      EpiRepository epiRepository,
      EpiCatalogAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(
      entidade = "EPI",
      acaoQuandoAtivo = "EPI_REACTIVATED",
      acaoQuandoInativo = "EPI_DEACTIVATED",
      alvo = 1)
  @Transactional
  public void execute(Long actorId, Long epiId, boolean active) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    if (epiId == null || !epiRepository.existsEpiById(epiId)) {
      throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
    }
    if (!active && epiRepository.hasHistoricalDependencies(epiId)) {
      throw new IllegalArgumentException(
          "CAD-038 Operacao nao permitida por dependencia historica.");
    }
    if (active && !epiRepository.hasActiveCoherentCaForEpi(epiId)) {
      throw new IllegalArgumentException(
          "CAD-038 Operacao nao permitida por dependencia historica.");
    }

    epiRepository.setEpiActive(epiId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        active ? "EPI_REACTIVATED" : "EPI_DEACTIVATED",
        "EPI",
        String.valueOf(epiId),
        active ? "EPI reactivated." : "EPI deactivated.");
  }
}
