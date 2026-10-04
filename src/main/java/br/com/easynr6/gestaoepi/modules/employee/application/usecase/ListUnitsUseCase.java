package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListUnitsUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;

  public ListUnitsUseCase(
      OrgStructureRepository orgStructureRepository, EmployeeAccessAuthorizer accessAuthorizer) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<UnitSummary> execute(Long actorId, String term) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    return orgStructureRepository.listUnitsByTerm(term);
  }
}
