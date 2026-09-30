package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.EpiSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListEpiUseCase {

  private final EpiRepository epiRepository;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;

  public ListEpiUseCase(EpiRepository epiRepository, EpiCatalogAccessAuthorizer accessAuthorizer) {
    this.epiRepository = epiRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<EpiSummary> execute(Long actorId, String term) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    return epiRepository.listByTerm(term);
  }
}
