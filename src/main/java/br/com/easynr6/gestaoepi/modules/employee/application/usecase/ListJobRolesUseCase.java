package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListJobRolesUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;

  public ListJobRolesUseCase(
      OrgStructureRepository orgStructureRepository, EmployeeAccessAuthorizer accessAuthorizer) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<JobRoleSummary> execute(Long actorId, String term) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    return orgStructureRepository.listJobRolesByTerm(term);
  }
}
