package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.CaBindingSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListCaByEpiUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;

  public ListCaByEpiUseCase(
      EpiRepository epiRepository, EpiCatalogAccessAuthorizer accessAuthorizer) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<CaBindingSummary> execute(Long actorId, Long epiId) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    if (epiId == null || !epiRepository.existsEpiById(epiId)) {
      throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
    }
    return epiRepository.listCaByEpi(epiId);
  }
}
