package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListDepartmentsUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;

  public ListDepartmentsUseCase(
      OrgStructureRepository orgStructureRepository, EmployeeAccessAuthorizer accessAuthorizer) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<DepartmentSummary> execute(Long actorId, String term) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    return orgStructureRepository.listDepartmentsByTerm(term);
  }
}
