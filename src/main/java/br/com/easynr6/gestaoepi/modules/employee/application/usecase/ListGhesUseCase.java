package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.FuncaoDoGhe;
import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.GheSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListGhesUseCase {

  private final GheRepository gheRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;

  public ListGhesUseCase(GheRepository gheRepository, EmployeeAccessAuthorizer accessAuthorizer) {
    this.gheRepository = gheRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<GheSummary> list(Long actorId, Long unitId, String term) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (unitId == null) {
      return List.of();
    }
    return gheRepository.listByUnit(unitId, term);
  }

  public List<FuncaoDoGhe> members(Long actorId, Long gheId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (gheId == null) {
      return List.of();
    }
    return gheRepository.listMembers(gheId);
  }

  public List<FuncaoDoGhe> candidates(Long actorId, Long unitId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (unitId == null) {
      return List.of();
    }
    return gheRepository.listCandidates(unitId);
  }
}
